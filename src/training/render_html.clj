(ns training.render-html
  "Build-time HTML renderer for `docs/samples/operator-console.html`.

  Closes flagship checklist item 2 (com-junkawasaki/root ADR-2607189300):
  this repo previously had NO demo page and no generator at all. This
  namespace drives the REAL actor stack (`training.operation` ->
  `training.governor` -> `training.store`, through the same
  `langgraph.graph/run*` supervised superstep loop the actor itself
  uses) and renders whatever that run actually produced.

  NOTHING on the emitted page is hand-written domain content. Every
  student id, name, jurisdiction, practice-hour figure, instructor-
  license flag, completion number, violation rule, violation detail and
  ledger row is read back out of the store the run just wrote, and the
  gate/rollout/jurisdiction tables are derived from the live
  `training.phase/phases`, `training.governor/high-stakes` and
  `training.facts/catalog` vars rather than described by hand -- so the
  page cannot drift away from the code the way a prose table can. (The
  sibling reference generator, `applianceshop.render-html` in
  cloud-itonami-isic-9522, hand-describes its action-gate table; this
  one derives it.)

  The scenario is adapted from this repo's own `training.sim` demo
  driver (`clojure -M:dev:run`, run BEFORE this file was written to
  confirm it produces a sensible ledger against the real seeded student
  ids `student-1`..`student-5`), and EXTENDED: `sim` exercises four of
  the Instruction Integrity Governor's five HARD rules, leaving
  `:evidence-incomplete` unproven. Finalizing `student-5` -- whose
  program requires no licensed instructor and whose practice hours are
  sufficient -- WITHOUT first filing a curriculum record isolates that
  fifth rule, so this console demonstrates all five.

  Deterministic: no timestamps, no wall-clock, no randomness, all map
  iteration explicitly sorted. Byte-identical across reruns against the
  same seed -- verify by diffing two consecutive runs into scratch
  directories.

  Usage: `clojure -M:dev:render-html [out-file]`
  (default `docs/samples/operator-console.html`)."
  (:require [jp-go-dds.skin]
            [clojure.string :as str]
            [training.store :as store]
            [training.facts :as facts]
            [training.phase :as phase]
            [training.governor :as governor]
            [training.operation :as op]
            [langgraph.graph :as g]))

(def ^:private operator
  "The same operator context `training.sim` uses -- a licensed educator
  at rollout phase 3 (supervised auto)."
  {:actor-id "op-1" :actor-role :licensed-educator :phase 3})

(def ^:private console-phase 3)

;; ----------------------------- the real run -----------------------------

(defn- exec! [actor tid request]
  (g/run* actor {:request request :context operator} {:thread-id tid}))

(defn- approve! [actor tid]
  (g/run* actor {:approval {:status :approved :by (:actor-id operator)}}
          {:thread-id tid :resume? true}))

(defn- approval-facts
  "The `:approval-granted` audit facts a resumed run actually emitted.

  These live ONLY in the graph state's `:audit` channel -- this actor's
  `:commit` node appends the `:committed` fact to the store ledger and
  the `:hold` node appends the hold fact, but nothing appends the
  approval fact, so it is not recoverable from `store/ledger` after the
  fact. Captured here at run time so the attribution section below can
  join against it honestly."
  [result]
  (filterv #(= :approval-granted (:t %)) (get-in result [:state :audit])))

(defn run-demo!
  "Runs a fresh seeded store through a scenario that reaches every
  disposition this actor can produce, and every HARD rule its governor
  implements.

  Clean path -- `student-1` (JPN, driving-school program, 40/30 practice
  hours, instructor licensed) walks a full lifecycle: intake (the ONE
  op phase 3 may auto-commit), curriculum verification (phase-gated,
  approved), instructor-license screening (approved), and completion
  finalization (`:actuation/finalize-completion`, which NEVER auto-
  commits at any phase -- both `training.phase` and
  `training.governor/high-stakes` enforce that independently --
  approved, producing the run's one real completion record).

  Five HARD holds, none of which reaches a human:
    1. `:no-spec-basis`               -- `student-2`'s jurisdiction
                                         (\"ATL\") is deliberately absent
                                         from `training.facts/catalog`;
                                         the advisor must not invent its
                                         requirements.
    2. `:practice-hours-insufficient` -- `student-3` has 15 of 30
                                         required practice hours, re-
                                         computed independently by the
                                         governor.
    3. `:instructor-license-unconfirmed`
                                      -- `student-4`'s program requires
                                         a licensed instructor whose
                                         license is unconfirmed. The
                                         screening op HARD-holds on its
                                         own finding.
    4. `:evidence-incomplete`         -- `student-5` is finalized with
                                         no curriculum record on file.
    5. `:already-finalized`           -- `student-1` is finalized a
                                         second time.

  One deliberate NON-hold contrast: `student-5`'s instructor-license
  screening is a no-op that escalates for sign-off rather than holding,
  because that student's own record declares `:instructor-license-
  required? false` -- proving check 3 is CONDITIONAL on the student's
  ground truth, not a blanket rule.

  Returns `{:db store :approvals [approval-granted facts]}`."
  []
  (let [db (store/seed-db)
        actor (op/build db)
        approvals (volatile! [])
        approve-and-record! (fn [tid]
                              (let [r (approve! actor tid)]
                                (vswap! approvals into (approval-facts r))
                                r))]
    ;; -- student-1: full clean lifecycle --
    (exec! actor "t1-intake" {:op :student/intake :subject "student-1"
                              :patch {:id "student-1" :student-name "Sato Kenji"}})

    (exec! actor "t1-curriculum" {:op :curriculum/verify :subject "student-1"})
    (approve-and-record! "t1-curriculum")

    (exec! actor "t1-instructor" {:op :instructor-license/screen :subject "student-1"})
    (approve-and-record! "t1-instructor")

    (exec! actor "t1-finalize" {:op :actuation/finalize-completion :subject "student-1"})
    (approve-and-record! "t1-finalize")

    ;; -- student-2: unregistered jurisdiction -> HARD :no-spec-basis --
    (exec! actor "t2-curriculum" {:op :curriculum/verify :subject "student-2" :no-spec? true})

    ;; -- student-3: clean curriculum, then HARD :practice-hours-insufficient --
    (exec! actor "t3-curriculum" {:op :curriculum/verify :subject "student-3"})
    (approve-and-record! "t3-curriculum")
    (exec! actor "t3-finalize" {:op :actuation/finalize-completion :subject "student-3"})

    ;; -- student-4: HARD :instructor-license-unconfirmed --
    (exec! actor "t4-instructor" {:op :instructor-license/screen :subject "student-4"})

    ;; -- student-5: screening is a conditional no-op (escalates, NOT a hold) --
    (exec! actor "t5-instructor" {:op :instructor-license/screen :subject "student-5"})
    (approve-and-record! "t5-instructor")
    ;; ...but finalizing with no curriculum record on file -> HARD :evidence-incomplete
    (exec! actor "t5-finalize" {:op :actuation/finalize-completion :subject "student-5"})

    ;; -- student-1 again: HARD :already-finalized --
    (exec! actor "t1-finalize-again" {:op :actuation/finalize-completion :subject "student-1"})

    {:db db :approvals @approvals}))

;; ----------------------------- html helpers -----------------------------

(defn- esc [v]
  (-> (str v)
      (str/replace "&" "&amp;")
      (str/replace "<" "&lt;")
      (str/replace ">" "&gt;")))

(defn- kw-name [k] (if (keyword? k) (subs (str k) 1) (str k)))

(defn- code [v] (str "<code>" (esc v) "</code>"))

(defn- tag [class label] (str "<span class=\"" class "\">" label "</span>"))

(defn- ok [s] (tag "ok" (esc s)))
(defn- warn [s] (tag "warn" (esc s)))
(defn- crit [s] (tag "critical" (esc s)))
(defn- muted [s] (tag "muted" (esc s)))

(defn- row [& cells]
  (str "        <tr>" (str/join (map #(str "<td>" % "</td>") cells)) "</tr>"))

(defn- table [headers rows]
  (str "    <table>\n"
       "      <thead><tr>"
       (str/join (map #(str "<th>" (esc %) "</th>") headers))
       "</tr></thead>\n"
       "      <tbody>\n"
       (str/join "\n" rows) "\n"
       "      </tbody>\n"
       "    </table>\n"))

(defn- section [title note body]
  (str "  <section class=\"card\">\n"
       "    <h2>" (esc title) "</h2>\n"
       (when note (str "    <p class=\"muted\">" note "</p>\n"))
       body
       "  </section>\n"))

;; ----------------------------- derived views -----------------------------

(defn- holds
  "Every HARD governor hold this run actually wrote to the ledger."
  [ledger]
  (filterv #(= :governor-hold (:t %)) ledger))

(defn- last-fact-for [ledger student-id]
  (last (filter #(= (:subject %) student-id) ledger)))

(defn- status-cell [ledger student-id]
  (let [f (last-fact-for ledger student-id)]
    (cond
      (nil? f) (muted "no activity")
      (= :committed (:t f)) (ok (str "committed · " (kw-name (:op f))))
      (= :governor-hold (:t f))
      (crit (str "HARD hold · " (kw-name (-> f :violations first :rule))))
      :else (muted (kw-name (:t f))))))

(defn- student-row [ledger {:keys [id student-name jurisdiction
                                   practice-hours-completed practice-hours-required
                                   instructor-license-required? instructor-license-confirmed?
                                   completion-finalized? completion-number]}]
  (row (code id)
       (esc student-name)
       (esc jurisdiction)
       (let [txt (str practice-hours-completed " / " practice-hours-required)]
         (if (< practice-hours-completed practice-hours-required) (crit txt) (ok txt)))
       (cond
         (not instructor-license-required?) (muted "not required")
         instructor-license-confirmed? (ok "confirmed")
         :else (crit "UNCONFIRMED"))
       (if completion-finalized?
         (ok (str "finalized · " completion-number))
         (muted "not finalized"))
       (status-cell ledger id)))

(defn- hold-row [{:keys [op subject violations confidence]}]
  (let [v (first violations)]
    (row (crit (kw-name (:rule v)))
         (code (kw-name op))
         (code subject)
         (esc (:detail v))
         (esc confidence))))

(defn- ledger-row [{:keys [t op subject basis summary violations]}]
  (row (if (= :governor-hold t) (crit "governor-hold") (ok (kw-name t)))
       (code (kw-name op))
       (code subject)
       (esc (if (= :governor-hold t)
              (str/join ", " (map (comp kw-name :rule) violations))
              (str/join ", " (map kw-name basis))))
       (esc (or summary
                (:detail (first violations))))))

(defn- phase-rows
  "Derived from the live `training.phase/phases` var -- not described by
  hand, so the page tracks the code."
  []
  (for [[n {:keys [label writes auto]}] (sort-by key phase/phases)]
    (row (esc n)
         (code label)
         (if (seq writes)
           (str/join " " (map (comp code kw-name) (sort-by str writes)))
           (muted "none"))
         (if (seq auto)
           (str/join " " (map (comp code kw-name) (sort-by str auto)))
           (muted "none"))
         (if (= n console-phase) (ok "this console") (muted "")))))

(defn- op-gate-rows
  "Derived from `training.phase/phases` at the console's phase plus
  `training.governor/high-stakes`."
  []
  (let [{:keys [writes auto]} (get phase/phases console-phase)]
    (for [o (sort-by str phase/write-ops)]
      (row (code (kw-name o))
           (cond
             (not (contains? writes o)) (crit "disabled at this phase")
             (contains? auto o) (ok "auto-commit when governor-clean")
             (contains? governor/high-stakes o)
             (crit "ALWAYS human approval · never auto at any phase")
             :else (warn "human approval · not auto-eligible at this phase"))
           (if (contains? governor/high-stakes o)
             (crit "high-stakes actuation")
             (muted "—"))))))

(defn- jurisdiction-rows []
  (for [[iso3 m] (sort-by key facts/catalog)]
    (row (code iso3)
         (esc (:name m))
         (esc (:owner-authority m))
         (esc (:legal-basis m))
         (esc (:instructor-owner-authority m))
         (esc (count (:required-evidence m))))))

;; ---- approver attribution: DERIVED, never asserted ----

(def ^:private approver-keys
  "Every spelling of 'who approved this' a record could plausibly carry.
  Checked against real committed records at render time."
  [:approved-by "approved_by" :approver "approver"])

(defn- retained-approver
  "The approver a committed record ACTUALLY retains, or nil. Read off
  the record rather than assumed, so this page self-corrects if the
  store's `commit-record!` later starts (or stops) persisting the
  approval payload -- see the note rendered alongside these rows."
  [record]
  (when (map? record)
    (some #(get record %) approver-keys)))

(defn- audit-approver
  "The approver the RUN observed, joined from the `:approval-granted`
  audit facts captured during `run-demo!`."
  [approvals op subject]
  (:by (first (filter #(and (= op (:op %)) (= subject (:subject %))) approvals))))

(defn- attribution-row [approvals op subject register-label record]
  (let [retained (retained-approver record)
        observed (audit-approver approvals op subject)]
    (row (code subject)
         (esc register-label)
         (code (kw-name op))
         (if retained
           (ok (str retained))
           (if observed
             (crit "not retained in record")
             (muted "—")))
         (cond
           retained (muted "record is self-describing")
           observed (warn (str observed " (audit only — not retained in record)"))
           :else (muted "no human approval in this run")))))

(defn- attribution-rows [db approvals]
  (let [students (store/all-students db)
        register-rows
        (for [s students
              [o label lookup] [[:curriculum/verify "curriculum record" store/curriculum-of]
                                [:instructor-license/screen "instructor-license screening"
                                 store/instructor-license-screen-of]]
              :let [rec (lookup db (:id s))]
              :when rec]
          (attribution-row approvals o (:id s) label rec))
        completion-rows
        (for [r (store/completion-history db)]
          (attribution-row approvals :actuation/finalize-completion
                           (get r "student_id") "completion-finalization record" r))]
    (vec (concat register-rows completion-rows))))

(defn- completion-rows [db]
  (for [r (store/completion-history db)]
    (row (code (get r "record_id"))
         (code (get r "student_id"))
         (esc (get r "jurisdiction"))
         (esc (get r "kind"))
         (if (get r "immutable") (ok "immutable") (warn "mutable")))))

;; ----------------------------- render -----------------------------

(defn render
  "Renders the whole console from a `run-demo!` result. Pure: same
  input, same bytes."
  [{:keys [db approvals]}]
  (let [ledger (vec (store/ledger db))
        students (store/all-students db)
        hs (holds ledger)
        cov (facts/coverage)]
    (str
     "<!DOCTYPE html>\n<html lang=\"en\"><head><meta charset=\"utf-8\">"
     "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
     "<title>cloud-itonami-isic-8549 &middot; other education n.e.c. &middot; Operator Console</title><style>"
     (jp-go-dds.skin/dds+skin)
     "</style></head><body>\n"
     "<header class=\"bar\">\n"
     "  <h1>Other education n.e.c. (ISIC 8549) — Operator Console</h1>\n"
     "  <span class=\"badge\">read-only sample · governor-gated · completion finalization always human-approved</span>\n"
     "</header>\n"
     "<main>\n"

     (section
      "Students"
      (str "Demo snapshot — build-time-generated from " (code "training.store")
           " via " (code "training.render-html") " (" (code "clojure -M:dev:render-html")
           "). Every row is the seeded student record as the actor left it; "
           "practice hours and instructor-license state are the governor's own ground truth inputs.")
      (table ["Student" "Name" "Jurisdiction" "Practice hours" "Instructor license"
              "Completion" "Last op status"]
             (map (partial student-row ledger) students)))

     (section
      "HARD governor holds (this run)"
      (str "A HARD violation cannot be overridden — these never reached a human at all. "
           "Rules and details below are read back out of the ledger the run wrote, "
           "not transcribed. " (code "training.governor")
           " implements five HARD checks; this scenario fires all five.")
      (table ["Rule" "Op" "Student" "Detail (from the governor)" "Advisor confidence"]
             (map hold-row hs)))

     (section
      "Rollout phase gate"
      (str "Derived from the live " (code "training.phase/phases")
           " var. " (code ":actuation/finalize-completion")
           " is deliberately absent from every phase's auto set, including phase 3 — "
           "a permanent structural fact, not a milestone still to come.")
      (table ["Phase" "Label" "May write" "May auto-commit" ""]
             (phase-rows)))

     (section
      "Action gate at phase 3"
      (str "Derived from " (code "training.phase/phases") " and "
           (code "training.governor/high-stakes")
           ". Two independent layers agree that actuation is always a human call.")
      (table ["Op" "Gate at this phase" "Stake"]
             (op-gate-rows)))

     (section
      "Jurisdiction spec-basis catalog"
      (str "Derived from " (code "training.facts/catalog")
           ". A jurisdiction absent from this table has NO spec-basis and the governor "
           "holds any proposal that cites one — that is exactly what happens to "
           (code "student-2") " above. " (esc (:note cov)))
      (table ["ISO3" "Jurisdiction" "Training-provider authority" "Legal basis"
              "Driving-instructor authority" "Required evidence"]
             (jurisdiction-rows)))

     (section
      "Approver attribution (derived)"
      (str "Who approved each committed record, and whether the record itself still says so. "
           "The left column is read off the committed record at render time; the right column "
           "joins the " (code ":approval-granted")
           " audit fact the run emitted. Where they disagree, the approval happened but "
           (code "training.store/commit-record!")
           " did not persist it — stated explicitly rather than silently omitted, "
           "because a blank cell cannot distinguish &quot;nobody approved&quot; from "
           "&quot;the store did not keep it&quot;. This section is computed, not hardcoded, "
           "so it self-corrects if the store changes.")
      (table ["Student" "Register" "Op" "Approver retained in record" "Audit join"]
             (attribution-rows db approvals)))

     (section
      "Completion-finalization records"
      (str "The append-only book-of-record drafts this run produced, from "
           (code "training.registry") ". Unsigned by construction — signature is the "
           "training provider's own act, not this actor's.")
      (table ["Completion number" "Student" "Jurisdiction" "Kind" "Record"]
             (completion-rows db)))

     (section
      "Audit ledger (this run)"
      "Append-only decision-fact log — every commit and every hold this scenario produced, in order."
      (table ["Fact" "Op" "Student" "Basis / rules" "Summary or detail"]
             (map ledger-row ledger)))

     "</main>\n</body></html>\n")))

;; ----------------------------- entry point -----------------------------

(defn -main [& args]
  (let [out (or (first args) "docs/samples/operator-console.html")
        {:keys [db] :as result} (run-demo!)
        ledger (vec (store/ledger db))
        hs (holds ledger)
        rules (sort (distinct (map (comp :rule first :violations) hs)))]

    ;; Build-time invariant, not a convention: a console that shows no
    ;; HARD hold has not demonstrated that the governor can refuse
    ;; anything, which is the entire claim this page exists to make.
    ;; Fail the build rather than emit a page that quietly proves
    ;; nothing (precedent: cloud-itonami-isic-2513).
    (when (zero? (count hs))
      (throw (ex-info "render-html: the scenario produced 0 :governor-hold records; refusing to emit a console that demonstrates no enforcement"
                      {:ledger-facts (count ledger)
                       :governor-holds 0})))

    ;; Second invariant: the happy path must actually have committed
    ;; something, or the page proves only that the actor refuses.
    (when (zero? (count (store/completion-history db)))
      (throw (ex-info "render-html: the scenario produced 0 completion records; the clean lifecycle did not commit"
                      {:ledger-facts (count ledger)})))

    (let [html (render result)]
      (spit out html)
      (println "wrote" out
               (str "(" (count ledger) " ledger facts, "
                    (count hs) " HARD holds across " (count rules) " distinct rules: "
                    (str/join ", " (map kw-name rules)) ", "
                    (count (store/completion-history db)) " completion records, "
                    (count html) " chars)")))))
