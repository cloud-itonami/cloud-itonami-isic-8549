(ns training.store
  "SSoT for the other-education-training-provider actor, behind a
  `Store` protocol so the backend is a swap, not a rewrite -- the same
  seam every prior `cloud-itonami-isic-*` actor in this fleet uses,
  closely modeled on `cloud-itonami-isic-8542`'s `cultural.store`:

    - `MemStore`     -- atom of EDN. The deterministic default for
                        dev/tests/demo (no deps).
    - `DatomicStore` -- backed by `langchain.db`, a Datomic-API-compatible
                        EAV store (datalog q / pull / upsert). Pure `.cljc`,
                        so it runs offline AND can be pointed at a real
                        Datomic Local or a kotoba-server pod by swapping
                        `langchain.db`'s `:db-api` (see langchain.kotoba-db).

  Both implement the same protocol and pass the same contract
  (test/training/store_contract_test.clj), which is the whole point:
  the actor, the Instruction Integrity Governor and the audit ledger
  never know which SSoT they run on.

  Like `cultural.store`'s simpler entity, a STUDENT is acted on
  directly by the ONE actuation op -- no dynamically-filed sub-record,
  and the double-finalization guard checks a dedicated `:completion-
  finalized?` boolean rather than a `:status` value, the same
  discipline `cultural.governor`'s guards establish.

  Beyond `cultural.store`'s own `permit-screen-of`, this store ALSO
  carries `instructor-license-screen-of` (driving-instructor-license
  confirmation status) -- the genuinely new concern this vertical
  adds, since driving-instruction (one of this blueprint's own named
  example activities) depends on the credential of the ASSESSOR
  (the instructor), not merely a fact about the student.

  The ledger stays append-only on every backend: 'which student was
  screened for an unconfirmed instructor license, which completion
  record was finalized, on what jurisdictional basis, approved by
  whom' is always a query over an immutable log -- the audit trail a
  student/family trusting a training provider needs, and the evidence
  an operator needs if a completion decision is later disputed."
  (:require #?(:clj  [clojure.edn :as edn]
               :cljs [cljs.reader :as edn])
            [training.registry :as registry]
            [langchain.db :as d]))

(defprotocol Store
  (student [s id])
  (all-students [s])
  (instructor-license-screen-of [s student-id] "committed instructor-license screening verdict for a student, or nil")
  (curriculum-of [s student-id] "committed curriculum evidence assessment, or nil")
  (ledger [s])
  (completion-history [s] "the append-only completion-finalization history (training.registry drafts)")
  (next-sequence [s jurisdiction] "next completion-number sequence for a jurisdiction")
  (student-already-finalized? [s student-id] "has this student's completion already been finalized?")
  (commit-record! [s record] "apply a committed op's record to the SSoT")
  (append-ledger! [s fact]   "append one immutable decision fact")
  (with-students [s students] "replace/seed the student directory (map id->student)"))

;; ----------------------------- demo data -----------------------------

(defn demo-data
  "A small, self-contained student set so the actor + tests run
  offline."
  []
  {:students
   {"student-1" {:id "student-1" :student-name "Sato Kenji"
                :practice-hours-completed 40 :practice-hours-required 30
                :instructor-license-required? true
                :instructor-license-confirmed? true
                :completion-finalized? false :jurisdiction "JPN" :status :intake}
    "student-2" {:id "student-2" :student-name "Atlantis Doe"
                :practice-hours-completed 40 :practice-hours-required 30
                :instructor-license-required? true
                :instructor-license-confirmed? true
                :completion-finalized? false :jurisdiction "ATL" :status :intake}
    "student-3" {:id "student-3" :student-name "鈴木花子"
                :practice-hours-completed 15 :practice-hours-required 30
                :instructor-license-required? true
                :instructor-license-confirmed? true
                :completion-finalized? false :jurisdiction "JPN" :status :intake}
    "student-4" {:id "student-4" :student-name "田中一郎"
                :practice-hours-completed 40 :practice-hours-required 30
                :instructor-license-required? true
                :instructor-license-confirmed? false
                :completion-finalized? false :jurisdiction "JPN" :status :intake}
    "student-5" {:id "student-5" :student-name "高橋美咲"
                :practice-hours-completed 20 :practice-hours-required 20
                :instructor-license-required? false
                :instructor-license-confirmed? false
                :completion-finalized? false :jurisdiction "JPN" :status :intake}}})

;; ----------------------------- shared commit logic -----------------------------

(defn- finalize-completion!
  "Backend-agnostic `:student/mark-finalized` -- looks up the student
  via the protocol and drafts the completion-finalization record, and
  returns {:result .. :student-patch ..} for the caller to persist."
  [s student-id]
  (let [st (student s student-id)
        seq-n (next-sequence s (:jurisdiction st))
        result (registry/register-completion-finalization student-id (:jurisdiction st) seq-n)]
    {:result result
     :student-patch {:completion-finalized? true
                     :completion-number (get result "completion_number")}}))

;; ----------------------------- MemStore (default) -----------------------------

(defrecord MemStore [a]
  Store
  (student [_ id] (get-in @a [:students id]))
  (all-students [_] (sort-by :id (vals (:students @a))))
  (instructor-license-screen-of [_ id] (get-in @a [:instructor-license-screens id]))
  (curriculum-of [_ student-id] (get-in @a [:curricula student-id]))
  (ledger [_] (:ledger @a))
  (completion-history [_] (:completions @a))
  (next-sequence [_ jurisdiction] (get-in @a [:sequences jurisdiction] 0))
  (student-already-finalized? [_ student-id] (boolean (get-in @a [:students student-id :completion-finalized?])))
  (commit-record! [s {:keys [effect path value payload]}]
    (case effect
      :student/upsert
      (swap! a update-in [:students (:id value)] merge value)

      :curriculum/set
      (swap! a assoc-in [:curricula (first path)] payload)

      :instructor-license-screen/set
      (swap! a assoc-in [:instructor-license-screens (first path)] payload)

      :student/mark-finalized
      (let [student-id (first path)
            {:keys [result student-patch]} (finalize-completion! s student-id)
            jurisdiction (:jurisdiction (student s student-id))]
        (swap! a (fn [state]
                   (-> state
                       (update-in [:sequences jurisdiction] (fnil inc 0))
                       (update-in [:students student-id] merge student-patch)
                       (update :completions registry/append result))))
        result)
      nil)
    s)
  (append-ledger! [_ fact] (swap! a update :ledger conj fact) fact)
  (with-students [s students] (when (seq students) (swap! a assoc :students students)) s))

(defn seed-db
  "A MemStore seeded with the demo student set. The deterministic
  default."
  []
  (->MemStore (atom (assoc (demo-data)
                           :curricula {} :instructor-license-screens {} :ledger [] :sequences {}
                           :completions []))))

;; ----------------------------- DatomicStore (langchain.db) -----------------------------

(def ^:private schema
  "DataScript/Datomic-style schema: only constraint attrs are declared.
  Compound values (curriculum/instructor-license-screen payloads,
  ledger facts, completion records) are stored as EDN strings so
  `langchain.db` doesn't expand them into sub-entities -- the same
  convention every sibling actor's store uses."
  {:student/id                              {:db/unique :db.unique/identity}
   :curriculum/student-id                   {:db/unique :db.unique/identity}
   :instructor-license-screen/student-id    {:db/unique :db.unique/identity}
   :ledger/seq                              {:db/unique :db.unique/identity}
   :completion/seq                          {:db/unique :db.unique/identity}
   :sequence/jurisdiction                   {:db/unique :db.unique/identity}})

(defn- enc [v] (pr-str v))
(defn- dec* [s] (when s (edn/read-string s)))

(defn- student->tx [{:keys [id student-name practice-hours-completed practice-hours-required
                           instructor-license-required? instructor-license-confirmed?
                           completion-finalized? jurisdiction status completion-number]}]
  (cond-> {:student/id id}
    student-name                                  (assoc :student/student-name student-name)
    practice-hours-completed                       (assoc :student/practice-hours-completed practice-hours-completed)
    practice-hours-required                         (assoc :student/practice-hours-required practice-hours-required)
    (some? instructor-license-required?)             (assoc :student/instructor-license-required? instructor-license-required?)
    (some? instructor-license-confirmed?)             (assoc :student/instructor-license-confirmed? instructor-license-confirmed?)
    (some? completion-finalized?)                      (assoc :student/completion-finalized? completion-finalized?)
    jurisdiction                                          (assoc :student/jurisdiction jurisdiction)
    status                                                  (assoc :student/status status)
    completion-number                                         (assoc :student/completion-number completion-number)))

(def ^:private student-pull
  [:student/id :student/student-name :student/practice-hours-completed :student/practice-hours-required
   :student/instructor-license-required? :student/instructor-license-confirmed?
   :student/completion-finalized?
   :student/jurisdiction :student/status :student/completion-number])

(defn- pull->student [m]
  (when (:student/id m)
    {:id (:student/id m) :student-name (:student/student-name m)
     :practice-hours-completed (:student/practice-hours-completed m)
     :practice-hours-required (:student/practice-hours-required m)
     :instructor-license-required? (boolean (:student/instructor-license-required? m))
     :instructor-license-confirmed? (boolean (:student/instructor-license-confirmed? m))
     :completion-finalized? (boolean (:student/completion-finalized? m))
     :jurisdiction (:student/jurisdiction m) :status (:student/status m)
     :completion-number (:student/completion-number m)}))

(defrecord DatomicStore [conn]
  Store
  (student [_ id]
    (pull->student (d/pull (d/db conn) student-pull [:student/id id])))
  (all-students [_]
    (->> (d/q '[:find [?id ...] :where [?e :student/id ?id]] (d/db conn))
         (map #(pull->student (d/pull (d/db conn) student-pull [:student/id %])))
         (sort-by :id)))
  (instructor-license-screen-of [_ id]
    (dec* (d/q '[:find ?p . :in $ ?sid
                :where [?k :instructor-license-screen/student-id ?sid] [?k :instructor-license-screen/payload ?p]]
              (d/db conn) id)))
  (curriculum-of [_ student-id]
    (dec* (d/q '[:find ?p . :in $ ?sid
                :where [?a :curriculum/student-id ?sid] [?a :curriculum/payload ?p]]
              (d/db conn) student-id)))
  (ledger [_]
    (->> (d/q '[:find ?s ?f :where [?e :ledger/seq ?s] [?e :ledger/fact ?f]] (d/db conn))
         (sort-by first)
         (mapv (comp dec* second))))
  (completion-history [_]
    (->> (d/q '[:find ?s ?r :where [?e :completion/seq ?s] [?e :completion/record ?r]] (d/db conn))
         (sort-by first)
         (mapv (comp dec* second))))
  (next-sequence [_ jurisdiction]
    (or (d/q '[:find ?n . :in $ ?j
              :where [?e :sequence/jurisdiction ?j] [?e :sequence/next ?n]]
            (d/db conn) jurisdiction)
        0))
  (student-already-finalized? [s student-id]
    (boolean (:completion-finalized? (student s student-id))))
  (commit-record! [s {:keys [effect path value payload]}]
    (case effect
      :student/upsert
      (d/transact! conn [(student->tx value)])

      :curriculum/set
      (d/transact! conn [{:curriculum/student-id (first path) :curriculum/payload (enc payload)}])

      :instructor-license-screen/set
      (d/transact! conn [{:instructor-license-screen/student-id (first path) :instructor-license-screen/payload (enc payload)}])

      :student/mark-finalized
      (let [student-id (first path)
            {:keys [result student-patch]} (finalize-completion! s student-id)
            jurisdiction (:jurisdiction (student s student-id))
            next-n (inc (next-sequence s jurisdiction))]
        (d/transact! conn
                     [(student->tx (assoc student-patch :id student-id))
                      {:sequence/jurisdiction jurisdiction :sequence/next next-n}
                      {:completion/seq (count (completion-history s)) :completion/record (enc (get result "record"))}])
        result)
      nil)
    s)
  (append-ledger! [s fact]
    (d/transact! conn [{:ledger/seq (count (ledger s)) :ledger/fact (enc fact)}])
    fact)
  (with-students [s students]
    (when (seq students) (d/transact! conn (mapv student->tx (vals students)))) s))

(defn datomic-store
  "A DatomicStore (langchain.db backend) seeded from `data`
  ({:students ..}); empty when omitted."
  ([] (datomic-store {}))
  ([{:keys [students]}]
   (let [s (->DatomicStore (d/create-conn schema))]
     (with-students s students))))

(defn datomic-seed-db
  "A DatomicStore seeded with the demo student set -- the Datomic-
  backed analog of `seed-db`, used to prove protocol parity."
  []
  (datomic-store (demo-data)))
