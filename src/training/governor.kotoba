(ns training.governor
  "Instruction Integrity Governor -- the independent compliance layer
  that earns the EdOps-LLM the right to commit. The LLM has no notion
  of jurisdictional training-provider law, whether a student's own
  recorded practice hours actually satisfy their own recorded
  requirement, whether the INSTRUCTOR responsible for a driving
  student's competency sign-off actually holds a confirmed driving-
  instructor license, or when an act stops being a draft and becomes
  a real-world completion/certification finalization, so this MUST be
  a separate system able to *reject* a proposal and fall back to HOLD
  -- the other-education-training-provider analog of `cloud-itonami-
  isic-8620`'s ClinicGovernor.

  This is the SIXTH confirmation of the fleet-wide governor-name-reuse
  precedent `commrepair`/9512's own ADR-0001 established (1st:
  commrepair/9512; 2nd: applianceshop/9522; 3rd: socialresearch/7220;
  4th: bizassoc/9411; 5th: vocational/8522, first within an existing
  two-member family), and the THIRD distinct governor-name family
  after `:repair-shop-governor` and `:research-integrity-governor`/
  `:association-governance-governor`. This blueprint's own
  `:itonami.blueprint/governor` keyword, `:instruction-integrity-
  governor`, is IDENTICAL to `cultural`/8542's (cultural education) --
  both actors perform instruction-integrity oversight of a training
  provider finalizing a certification/completion record for a
  student, differing only in the domain-specific concern each adds:
  `cultural`/8542 checks an unresolved child-performer work permit (a
  fact ABOUT THE STUDENT); this build checks an unconfirmed driving-
  instructor license (a fact about the ASSESSOR, not the student) --
  see this repo's own `docs/adr/0001-architecture.md` Decision 1.

  Five checks, in priority order, ALL HARD violations: a human
  approver CANNOT override them (you don't get to approve your way
  past a fabricated jurisdiction spec-basis, incomplete evidence,
  insufficient practice hours, an unconfirmed driving-instructor
  license where required, or a double finalization). The confidence/
  actuation gate is SOFT: it asks a human to look (low confidence /
  actuation), and the human may approve -- but see `training.phase`:
  for `:stake :actuation/finalize-completion` (a real certification/
  completion record) NO phase ever allows auto-commit either. Two
  independent layers agree that actuation is always a human call.

    1. Spec-basis                  -- did the curriculum proposal cite
                                       an OFFICIAL source (`training.
                                       facts`), or invent one?
    2. Evidence incomplete         -- for `:actuation/finalize-
                                       completion`, has the student
                                       actually been assessed with a
                                       full student-enrollment-
                                       consent-record/curriculum-
                                       record/instructor-license-
                                       verification-record/
                                       completion-record evidence
                                       checklist on file?
    3. Practice hours insufficient  -- for `:actuation/finalize-
                                       completion`, INDEPENDENTLY
                                       recompute whether the student's
                                       own recorded completed practice
                                       hours fall short of their own
                                       recorded required practice
                                       hours (`training.registry/
                                       practice-hours-insufficient?`)
                                       -- an HONEST, LITERAL reuse of
                                       `cultural.registry`'s own
                                       NINTH-instance MINIMUM-threshold
                                       sufficiency check, NOT claimed
                                       as new.
    4. Instructor license
       unconfirmed                    -- for a student whose own
                                       record declares `:instructor-
                                       license-required? true` (i.e.
                                       this student's program requires
                                       a licensed instructor's sign-
                                       off -- driving instruction in
                                       this R0 seed, NOT exam-prep or
                                       corporate training),
                                       INDEPENDENTLY check whether
                                       `:instructor-license-
                                       confirmed?` is true. A GENUINELY
                                       NEW concept (grep-verified
                                       absent fleet-wide -- zero hits
                                       for any governor check function
                                       named 'instructor-
                                       certification'/'instructor-
                                       licens'/'driving-instructor'/
                                       'assessor-credential'), the
                                       66th distinct application of
                                       the unconditional-evaluation
                                       discipline overall (most
                                       recently `vocational.governor/
                                       workplace-safety-training-
                                       unconfirmed-violations` at 65th,
                                       unconditional), the THIRD
                                       CONDITIONAL variant (after
                                       `socialresearch`/7220's and
                                       `bizassoc`/9411's own, at 63rd
                                       and 64th). STRUCTURALLY
                                       DIFFERENT from every prior
                                       instance of this discipline:
                                       every prior check verifies a
                                       ground-truth fact ABOUT THE
                                       SUBJECT being assessed (a
                                       study, a position, a ticket);
                                       this one verifies a ground-
                                       truth fact about the ASSESSOR
                                       (the instructor) who signs off
                                       on the subject. Grounded in
                                       real driving-instructor-
                                       licensing law: Japan's 道路交通法
                                       (指定自動車教習所指導員技能検定),
                                       US state DMV driving-instructor
                                       licensing requirements, UK's
                                       DVSA Approved Driving Instructor
                                       (ADI) register (Road Traffic Act
                                       1988 Part V), Germany's
                                       Fahrlehrergesetz.
    5. Confidence floor / actuation
       gate                          -- LLM confidence below threshold,
                                       OR the op is `:actuation/
                                       finalize-completion` (a REAL
                                       certification/completion record
                                       act) -> escalate.

  One more guard, double-finalization prevention, is enforced but NOT
  listed as a numbered HARD check above because it needs no upstream
  comparison at all -- `already-finalized-violations` refuses to
  finalize a completion for the SAME student twice, off a dedicated
  `:completion-finalized?` fact (never a `:status` value) -- an
  honest, literal reuse of `cultural.governor`'s own guard, informed
  by `cloud-itonami-isic-6492`'s status-lifecycle bug
  (ADR-2607071320)."
  (:require [training.facts :as facts]
            [training.registry :as registry]
            [training.store :as store]))

(def confidence-floor 0.6)

(def high-stakes
  "Stakes grave enough to always require a human, even when clean.
  Finalizing a real certification/completion record is the ONE
  real-world actuation event this actor performs -- a single-member
  set, matching `cultural`'s (and every other single-actuation
  sibling's) shape. This blueprint's own text names ONE real-world act
  ('finalizing a certification OR completion record' -- a single
  decision with two possible outcome kinds, not two separate
  sequential milestones the way `secondary`/8521's and `vocational`/
  8522's own grading-THEN-graduation dual-actuation shape works),
  grounded directly in this blueprint's own README ('No automated
  proposal, by itself, can complete the following without governor
  approval and audit evidence: finalizing a certification or
  completion record')."
  #{:actuation/finalize-completion})

;; ----------------------------- checks -----------------------------

(defn- spec-basis-violations
  "A `:curriculum/verify` (or `:actuation/finalize-completion`)
  proposal with no spec-basis citation is a HARD violation -- never
  invent a jurisdiction's training-provider requirements."
  [{:keys [op]} proposal]
  (when (contains? #{:curriculum/verify :actuation/finalize-completion} op)
    (let [value (:value proposal)]
      (when (or (empty? (:cites proposal))
                (and (contains? value :spec-basis) (nil? (:spec-basis value))))
        [{:rule :no-spec-basis
          :detail "公式spec-basisの引用が無い提案は認定基準として扱えない"}]))))

(defn- evidence-incomplete-violations
  "For `:actuation/finalize-completion`, the jurisdiction's required
  student-enrollment-consent-record/curriculum-record/instructor-
  license-verification-record/completion-record evidence must
  actually be satisfied -- do not trust the advisor's self-reported
  confidence alone."
  [{:keys [op subject]} st]
  (when (= op :actuation/finalize-completion)
    (let [s (store/student st subject)
          curriculum (store/curriculum-of st subject)]
      (when-not (and curriculum
                     (facts/required-evidence-satisfied?
                      (:jurisdiction s) (:checklist curriculum)))
        [{:rule :evidence-incomplete
          :detail "法域の必要書類(受講同意記録/カリキュラム記録/指導員資格確認記録/修了認定記録等)が充足していない状態での提案"}]))))

(defn- practice-hours-insufficient-violations
  "For `:actuation/finalize-completion`, INDEPENDENTLY recompute
  whether the student's own recorded completed practice hours fall
  short of their own recorded required practice hours via `training.
  registry/practice-hours-insufficient?` -- an HONEST, LITERAL reuse
  of `cultural.registry`'s own check, NOT claimed as new."
  [{:keys [op subject]} st]
  (when (= op :actuation/finalize-completion)
    (let [s (store/student st subject)]
      (when (registry/practice-hours-insufficient? s)
        [{:rule :practice-hours-insufficient
          :detail (str subject " の練習時間(" (:practice-hours-completed s)
                      ")が必要時間(" (:practice-hours-required s) ")に満たない")}]))))

(defn- instructor-license-unconfirmed-violations
  "For a student whose own record declares `:instructor-license-
  required? true`, INDEPENDENTLY check whether `:instructor-license-
  confirmed?` is true -- a genuinely new concept (see ns docstring),
  CONDITIONAL on the student's own `:instructor-license-required?`
  ground truth (a student in a program that does not require a
  licensed instructor's sign-off, e.g. exam-prep or corporate
  training, has no such requirement at all). Scoped to `:instructor-
  license/screen` and `:actuation/finalize-completion`, so the
  screening op itself can HARD-hold on its own finding, matching every
  prior unconditional-evaluation check's scoping shape."
  [{:keys [op subject]} st]
  (when (contains? #{:instructor-license/screen :actuation/finalize-completion} op)
    (let [s (store/student st subject)]
      (when (and (true? (:instructor-license-required? s))
                 (not (true? (:instructor-license-confirmed? s))))
        [{:rule :instructor-license-unconfirmed
          :detail (str subject " の指導員資格が未確認 -- 修了認定提案は進められない")}]))))

(defn- already-finalized-violations
  "For `:actuation/finalize-completion`, refuses to finalize a
  completion for the SAME student twice, off a dedicated `:completion-
  finalized?` fact (never a `:status` value). An honest, literal reuse
  of `cultural.governor`'s own guard."
  [{:keys [op subject]} st]
  (when (= op :actuation/finalize-completion)
    (when (store/student-already-finalized? st subject)
      [{:rule :already-finalized
        :detail (str subject " は既に修了認定済み")}])))

(defn check
  "Censors an EdOps-LLM proposal against the governor rules. Returns
  {:ok? bool :violations [..] :confidence c :escalate? bool
  :high-stakes? bool :hard? bool}."
  [request _context proposal st]
  (let [hard (into []
                   (concat (spec-basis-violations request proposal)
                           (evidence-incomplete-violations request st)
                           (practice-hours-insufficient-violations request st)
                           (instructor-license-unconfirmed-violations request st)
                           (already-finalized-violations request st)))
        conf (:confidence proposal 0.0)
        low? (< conf confidence-floor)
        stakes? (boolean (high-stakes (:stake proposal)))
        hard? (boolean (seq hard))]
    {:ok?          (and (not hard?) (not low?) (not stakes?))
     :violations   hard
     :confidence   conf
     :hard?        hard?
     :escalate?    (and (not hard?) (or low? stakes?))
     :high-stakes? stakes?}))

(defn hold-fact
  "The audit fact written when a proposal is rejected (HOLD)."
  [request context verdict]
  {:t          :governor-hold
   :op         (:op request)
   :actor      (:actor-id context)
   :subject    (:subject request)
   :disposition :hold
   :basis      (mapv :rule (:violations verdict))
   :violations (:violations verdict)
   :confidence (:confidence verdict)})
