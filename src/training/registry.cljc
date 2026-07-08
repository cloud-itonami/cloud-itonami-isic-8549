(ns training.registry
  "Pure-function completion-finalization record construction -- an
  append-only training-provider book-of-record draft, closely modeled
  on `cloud-itonami-isic-8542`'s `cultural.registry`.

  Like every sibling actor's registry, there is no single
  international check-digit standard for a completion-finalization
  reference number -- every training provider/jurisdiction assigns its
  own reference format. This namespace does NOT invent one; it builds
  a jurisdiction-scoped sequence number and validates the record's
  required fields, the same honest, non-fabricating discipline
  `training.facts` uses.

  `practice-hours-insufficient?` is an HONEST, LITERAL reuse of
  `cultural.registry`'s own NINTH-instance MINIMUM-threshold
  sufficiency check -- NOT claimed as new. A training student's own
  recorded completed practice/training hours against their own
  recorded required hours is the SAME real-world concern whether the
  training is a cultural-arts practice regimen or a driving-lesson/
  exam-prep/corporate-training hour requirement.

  This namespace is pure data + pure functions -- no I/O, no network
  call to any real training-management system. It builds the RECORD a
  training provider would keep, not the act of finalizing the
  completion record itself (that is `training.operation`'s
  `:actuation/finalize-completion`, always human-gated -- see README
  `Actuation`)."
  (:require [clojure.string :as str]))

(defn- unsigned-certificate
  "Every certificate this actor produces is UNSIGNED -- signature is the
  training provider's own act, not this actor's. See README
  `Actuation`."
  [kind subject record-id]
  {"@context" ["https://www.w3.org/ns/credentials/v2"]
   "type" ["VerifiableCredential" kind]
   "credentialSubject" {"id" subject "record" record-id}
   "proof" nil
   "issued_by_registry" false
   "status" "draft-unsigned"})

(defn- zero-pad [n w]
  (let [s (str n)]
    (str (apply str (repeat (max 0 (- w (count s))) "0")) s)))

(defn practice-hours-insufficient?
  "Does `student`'s own `:practice-hours-completed` fall short of the
  jurisdiction's own recorded `:practice-hours-required` minimum? An
  honest, literal reuse of `cultural.registry`'s own shape -- see ns
  docstring."
  [{:keys [practice-hours-completed practice-hours-required]}]
  (and (number? practice-hours-completed) (number? practice-hours-required)
       (< practice-hours-completed practice-hours-required)))

(defn register-completion-finalization
  "Validate + construct the COMPLETION-FINALIZATION registration
  DRAFT -- the training provider's own act of finalizing a real
  certification or completion record. Pure function -- does not touch
  any real training-management system; it builds the RECORD a
  provider would keep. `training.governor` independently re-verifies
  the student's own practice-hours ground truth and instructor-
  license confirmation (when required), and blocks a double-
  finalization for the same student, before this is ever allowed to
  commit."
  [student-id jurisdiction sequence]
  (when-not (and student-id (not= student-id ""))
    (throw (ex-info "completion-finalization: student_id required" {})))
  (when-not (and jurisdiction (not= jurisdiction ""))
    (throw (ex-info "completion-finalization: jurisdiction required" {})))
  (when (< sequence 0)
    (throw (ex-info "completion-finalization: sequence must be >= 0" {})))
  (let [completion-number (str (str/upper-case jurisdiction) "-CMP-" (zero-pad sequence 6))
        record {"record_id" completion-number
                "kind" "completion-finalization-draft"
                "student_id" student-id
                "jurisdiction" jurisdiction
                "immutable" true}]
    {"record" record "completion_number" completion-number
     "certificate" (unsigned-certificate "CompletionFinalization" completion-number completion-number)}))

(defn append [history result]
  (conj (vec history) (get result "record")))
