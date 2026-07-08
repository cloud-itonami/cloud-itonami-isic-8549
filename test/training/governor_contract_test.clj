(ns training.governor-contract-test
  "The governor contract as executable tests -- the other-education-
  training-provider analog of `cloud-itonami-isic-8542`'s `cultural.
  governor-contract-test`. The single invariant under test:

    EdOps-LLM never finalizes a completion record the Instruction
    Integrity Governor would reject, `:actuation/finalize-completion`
    NEVER auto-commits at any phase, `:student/intake` (no direct
    capital risk) MAY auto-commit when clean, and every decision
    (commit OR hold) leaves exactly one ledger fact."
  (:require [clojure.test :refer [deftest is testing]]
            [langgraph.graph :as g]
            [training.store :as store]
            [training.operation :as op]))

(defn- fresh []
  (let [db (store/seed-db)]
    [db (op/build db)]))

(def operator {:actor-id "op-1" :actor-role :licensed-educator :phase 3})

(defn- exec-op [actor tid request context]
  (g/run* actor {:request request :context context} {:thread-id tid}))

(defn- approve! [actor tid]
  (g/run* actor {:approval {:status :approved :by "op-1"}} {:thread-id tid :resume? true}))

(defn- verify!
  "Walks `subject` through verify -> approve, leaving a curriculum
  assessment on file. Uses distinct thread-ids per call site by
  suffixing `tid-prefix`."
  [actor tid-prefix subject]
  (exec-op actor (str tid-prefix "-verify") {:op :curriculum/verify :subject subject} operator)
  (approve! actor (str tid-prefix "-verify")))

(deftest clean-intake-auto-commits
  (let [[db actor] (fresh)
        res (exec-op actor "t1"
                  {:op :student/intake :subject "student-1"
                   :patch {:id "student-1" :student-name "Sato Kenji"}} operator)]
    (is (= :commit (get-in res [:state :disposition])))
    (is (= "Sato Kenji" (:student-name (store/student db "student-1"))) "SSoT actually updated")
    (is (= 1 (count (store/ledger db))))))

(deftest curriculum-verify-always-needs-approval
  (testing "verify is never in any phase's :auto set -- always human approval, even when clean"
    (let [[db actor] (fresh)
          res (exec-op actor "t2" {:op :curriculum/verify :subject "student-1"} operator)]
      (is (= :interrupted (:status res)))
      (let [r2 (approve! actor "t2")]
        (is (= :commit (get-in r2 [:state :disposition])))
        (is (some? (store/curriculum-of db "student-1")))))))

(deftest fabricated-jurisdiction-is-held
  (testing "a curriculum/verify proposal with no official spec-basis -> HOLD, never reaches a human"
    (let [[db actor] (fresh)
          res (exec-op actor "t3"
                    {:op :curriculum/verify :subject "student-1" :no-spec? true} operator)]
      (is (= :hold (get-in res [:state :disposition])))
      (is (some #{:no-spec-basis} (-> (store/ledger db) first :basis)))
      (is (nil? (store/curriculum-of db "student-1")) "no curriculum written"))))

(deftest finalize-completion-without-curriculum-is-held
  (testing "actuation/finalize-completion before any curriculum verification -> HOLD (evidence incomplete)"
    (let [[db actor] (fresh)
          res (exec-op actor "t4" {:op :actuation/finalize-completion :subject "student-1"} operator)]
      (is (= :hold (get-in res [:state :disposition])))
      (is (some #{:evidence-incomplete} (-> (store/ledger db) first :basis))))))

(deftest practice-hours-insufficient-is-held
  (testing "a student whose own recorded practice hours fall short of their own recorded requirement -> HOLD (honest reuse of cultural/8542's own check)"
    (let [[db actor] (fresh)
          _ (verify! actor "t5pre" "student-3")
          res (exec-op actor "t5" {:op :actuation/finalize-completion :subject "student-3"} operator)]
      (is (= :hold (get-in res [:state :disposition])))
      (is (some #{:practice-hours-insufficient} (-> (store/ledger db) last :basis)))
      (is (empty? (store/completion-history db))))))

(deftest instructor-license-unconfirmed-is-held-and-unoverridable
  (testing "a student whose program requires a licensed instructor but whose instructor's license is unconfirmed -> HOLD, and never reaches request-approval -- exercised via :instructor-license/screen DIRECTLY, not via the actuation op against an unscreened student -- the genuinely NEW check this vertical adds, the 66th unconditional-evaluation-discipline grounding overall and the THIRD conditional variant, STRUCTURALLY DIFFERENT from every prior instance (verifies a fact about the ASSESSOR, not the subject) (see this actor's governor ns docstring / parksafety's ADR-2607071922 Decision 5 / eldercare's, museum's, conservation's, salon's, entertainment's, casework's, hospital's, facility's, school's, association's, leasing's, behavioral's, secondary's, card's, water's, telecom's, aerospace's, recovery's, consulting's, union's, congregation's, fab's, energy's, care's, navigator's, learning's, banking's, advertising's, polling's, research's, design's, nursing's, sports's, alliedhealth's, laundry's, holdco's, photo's, personalservice's, edsupport's, headoffice's, residential's, cultural's, reserve's, proserv's, sportsevent's, recreation's, sportsclub's, partyops's, memberorg's, commrepair's, applianceshop's, socialresearch's, bizassoc's and vocational's ADR-0001s)"
    (let [[db actor] (fresh)
          res (exec-op actor "t6" {:op :instructor-license/screen :subject "student-4"} operator)]
      (is (= :hold (get-in res [:state :disposition])) "settles immediately, no interrupt")
      (is (not= :interrupted (:status res)))
      (is (some #{:instructor-license-unconfirmed} (-> (store/ledger db) first :basis)))
      (is (nil? (store/instructor-license-screen-of db "student-4")) "no clearance written"))))

(deftest instructor-license-screen-is-a-noop-when-not-required
  (testing "the instructor-license check is CONDITIONAL: a student in a program that does not require a licensed instructor (e.g. exam-prep or corporate training) has no such requirement at all"
    (let [[_db actor] (fresh)
          res (exec-op actor "t6b" {:op :instructor-license/screen :subject "student-5"} operator)]
      (is (= :interrupted (:status res)) "clean screening still escalates for human sign-off, but is NOT a HARD hold"))))

(deftest finalize-completion-always-escalates-then-human-decides
  (testing "a clean, fully-assessed student still ALWAYS interrupts for human approval -- actuation/finalize-completion is never auto"
    (let [[db actor] (fresh)
          _ (verify! actor "t7pre" "student-1")
          r1 (exec-op actor "t7" {:op :actuation/finalize-completion :subject "student-1"} operator)]
      (is (= :interrupted (:status r1)) "pauses for human approval even when governor-clean")
      (testing "approve -> commit, completion-finalization record drafted"
        (let [r2 (approve! actor "t7")]
          (is (= :commit (get-in r2 [:state :disposition])))
          (is (true? (:completion-finalized? (store/student db "student-1"))))
          (is (= 1 (count (store/completion-history db))) "one draft finalization record"))))))

(deftest double-finalization-is-held
  (testing "finalizing the same student's completion twice -> HOLD on the second attempt"
    (let [[db actor] (fresh)
          _ (verify! actor "t8pre" "student-1")
          _ (exec-op actor "t8a" {:op :actuation/finalize-completion :subject "student-1"} operator)
          _ (approve! actor "t8a")
          res (exec-op actor "t8" {:op :actuation/finalize-completion :subject "student-1"} operator)]
      (is (= :hold (get-in res [:state :disposition])))
      (is (some #{:already-finalized} (-> (store/ledger db) last :basis)))
      (is (= 1 (count (store/completion-history db))) "still only the one earlier finalization"))))

(deftest every-decision-leaves-one-ledger-fact
  (testing "write-only-through-ledger: N operations -> N ledger facts"
    (let [[db actor] (fresh)]
      (exec-op actor "a" {:op :student/intake :subject "student-1"
                          :patch {:id "student-1" :student-name "Sato Kenji"}} operator)
      (exec-op actor "b" {:op :curriculum/verify :subject "student-1" :no-spec? true} operator)
      (is (= 2 (count (store/ledger db)))
          "one commit + one hold, both recorded"))))
