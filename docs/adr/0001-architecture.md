# ADR-0001: EdOps-LLM ⊣ Instruction Integrity Governor architecture

## Status

Accepted. `cloud-itonami-isic-8549` promoted from `:blueprint` to
`:implemented` in the `kotoba-lang/industry` registry.

## Context

`cloud-itonami-isic-8549` publishes an OSS business blueprint for
other education not elsewhere classified: driving schools, exam-
preparation courses, corporate training. Like every prior actor in
this fleet, the blueprint alone is not an implementation: this ADR
records the governed-actor architecture that promotes it to real,
tested code, following the same langgraph StateGraph + independent
Governor + Phase 0→3 rollout pattern established by `cloud-itonami-
isic-6511` (life insurance) and applied across eighty prior siblings,
most recently `cloud-itonami-isic-8522` (technical and vocational
secondary education).

This blueprint's own `:itonami.blueprint/governor` keyword,
`:instruction-integrity-governor`, is IDENTICAL to `cultural`/8542's
(cultural education). Per the fleet-wide governor-name-reuse
precedent `commrepair`/9512's own ADR-0001 established -- confirmed
five times since across three distinct governor-name families
(`:repair-shop-governor`, `:research-integrity-governor`/
`:association-governance-governor`, and `:curriculum-safeguarding-
governor`) -- sharing a governor name is acceptable when the
underlying business archetype is genuinely the same, provided the
reuse is documented and the new build brings its own genuinely
differentiated, well-grounded check. This build is the SIXTH
confirmation overall, and the THIRD distinct governor-name family.

## Decision

### Decision 1: governor-name reuse -- sixth confirmation, third distinct family

`cultural`/8542 and `training`/8549 both perform instruction-integrity
oversight of a training provider finalizing a certification/
completion record for a student. Reusing `:instruction-integrity-
governor` is an honest reflection of that shared archetype. The
genuinely distinguishing concern this build adds -- an unconfirmed
driving-instructor license -- is a fact about the ASSESSOR (the
instructor who signs off on the student), not the STUDENT being
assessed, unlike `cultural`/8542's own child-performer-work-permit
check (a fact about the student). This is a structurally new KIND of
check for this fleet, not merely a new instance of an existing shape.

### Decision 2: single-actuation shape, following `sports`/8541's and `cultural`/8542's own either/or-naming precedent

This blueprint's own README, business-model.md and operator-guide.md
consistently phrase the one real-world act as "finalizing a
certification OR completion record" -- an "or," not an "and,"
signaling ONE decision with two possible outcome kinds, not two
separate sequential milestones (unlike `secondary`/8521's and
`vocational`/8522's own genuinely dual grading-THEN-graduation shape,
where the blueprint text names two acts joined by "and"). Following
`sports`/8541's and `cultural`/8542's own precedent of treating
either/or-phrased text as ONE conceptual act, `high-stakes` here is a
one-member set, `#{:actuation/finalize-completion}`.

### Decision 3: `practice-hours-insufficient?` -- an honest, literal reuse

`training.registry/practice-hours-insufficient?` is an HONEST, LITERAL
reuse of `cultural.registry`'s own NINTH-instance MINIMUM-threshold
sufficiency check, NOT claimed as new. A training student's own
recorded completed practice/training hours against their own recorded
required hours is the same real-world concern whether the training is
a cultural-arts practice regimen or a driving-lesson/exam-prep/
corporate-training hour requirement.

### Decision 4: `instructor-license-unconfirmed?` -- the 66th unconditional-evaluation grounding, structurally novel, the THIRD conditional variant

Before writing this check, every prior sibling's governor namespace
across the entire fleet was grepped for any check function named
`instructor-certification`, `instructor-licens`, `driving-instructor`
or `assessor-credential` -- zero hits, confirming this is a genuinely
new concept. `instructor-license-unconfirmed-violations` reuses the
unconditional-evaluation-screening DISCIPLINE (`casualty.governor/
sanctions-violations`'s original fix) for the 66th distinct
application overall (most recently `vocational.governor/workplace-
safety-training-unconfirmed-violations` at 65th, unconditional). This
is the THIRD conditional variant (after `socialresearch`/7220's and
`bizassoc`/9411's own, at 63rd and 64th) -- CONDITIONAL on the
student's own `:instructor-license-required? true` ground truth, since
only some of this blueprint's own named program types (driving
instruction) require a licensed instructor's sign-off; exam-
preparation and corporate training do not.

Unlike every prior instance of this discipline, this check does NOT
verify a fact about the SUBJECT being assessed (a study's human-
subjects status, a position's lobbying-registration status, a
student's own workplace-safety-training completion). It verifies a
fact about the ASSESSOR -- whether the instructor who signs off on
the student's driving competency is themselves a licensed driving
instructor. This is a genuinely novel kind of check for this fleet.
Grounded in real driving-instructor-licensing law: Japan's 道路交通法
(指定自動車教習所指導員技能検定, National Police Agency/Prefectural Public
Safety Commissions), US state DMV driving-instructor licensing
requirements, UK's DVSA Approved Driving Instructor (ADI) register
(Road Traffic Act 1988 Part V), Germany's Fahrlehrergesetz.

### Decision 5: dedicated double-finalization-guard boolean

`:completion-finalized?` is a dedicated boolean on the `student`
record, never a single `:status` value -- an honest, literal reuse of
`cultural.governor`'s own guard, informed by `cloud-itonami-isic-
6492`'s real status-lifecycle bug (ADR-2607071320).

### Decision 6: Store protocol, MemStore + DatomicStore parity

`training.store/Store` is implemented by both `MemStore` (atom-
backed, default for dev/tests/demo) and `DatomicStore` (`langchain.
db`-backed), proven to satisfy the same contract in
`test/training/store_contract_test.clj` -- the same seam every
sibling actor uses so swapping the SSoT backend is a configuration
change, not a rewrite.

### Decision 7: Phase 0→3 rollout

Phase 3's `:auto` set has exactly one member, `:student/intake` (no
capital risk). `:curriculum/verify` and `:instructor-license/screen`
are never auto-eligible at any phase (matching every sibling's
screening-op posture), and `:actuation/finalize-completion` is
permanently excluded from every phase's `:auto` set -- a structural
fact, not a rollout milestone, enforced by BOTH `training.phase` and
`training.governor`'s `high-stakes` set independently.

### Decision 8: no bespoke domain capability lib, and no `blueprint.edn` field-sync fixes needed

This blueprint's own `:itonami.blueprint/required-technologies` names
no domain-specific capability beyond the generic robotics/identity/
forms/dmn/bpmn/audit-ledger stack -- there was no capability-lib
decision to make at all. This repo's `blueprint.edn` already had the
correct `isic-` prefixed `:id` and correctly populated `:required-
technologies`/`:optional-technologies` matching the `kotoba-lang/
industry` registry's own entry for `"8549"` exactly -- only the
`:maturity` field itself needed adding.

### Decision 9: mock + LLM advisor pair

`training.edopsllm` provides `mock-advisor` (deterministic, default
everywhere -- the actor graph and governor contract run offline) and
`llm-advisor` (backed by `langchain.model/ChatModel`, with a defensive
EDN-proposal parser so a malformed LLM response degrades to a safe
low-confidence noop rather than ever auto-finalizing a completion
record).

### Decision 10: general spec-basis catalog framed around training-provider oversight, not a single unifying regulator

Unlike most sibling `facts` catalogs, this blueprint's own named
example activities (driving schools, exam-prep, corporate training)
have no single unifying regulator. Following `secondary.facts`'s own
"cite the most domain-specific real regulator available" discipline,
`training.facts`'s general citation covers training-provider licensing
broadly (state/national training-provider registration authorities),
while the SEPARATE instructor-license citation covers the genuinely
new, more specific driving-instructor-licensing concern.

## Alternatives considered

- **A dual-actuation shape** (treating "certification" and
  "completion" as two separate sequential acts, mirroring `secondary`/
  8521's and `vocational`/8522's own grading-THEN-graduation shape).
  Rejected: this blueprint's own text phrases the act as "certification
  OR completion record" (an "or," signaling one decision with two
  possible outcome kinds), not "AND" (two genuinely separate
  milestones) -- following `sports`/8541's and `cultural`/8542's own
  precedent for interpreting this exact phrasing pattern.
- **An unconditional instructor-license check** (applying to every
  student regardless of program type). Rejected: exam-preparation and
  corporate-training programs do not require a licensed instructor's
  sign-off -- forcing the check onto every student would fabricate a
  requirement that does not exist for those program types.
- **Reusing `cultural.governor/child-performer-work-permit-
  unresolved-violations` directly** (since both concern a domain-
  specific compliance gap). Rejected: a child-performer work permit
  and a driving-instructor license are legally and conceptually
  unrelated concerns for genuinely different program types -- a
  mechanical copy would not be grounded in this vertical's actual
  regulatory reality.

## Consequences

- Eighty-second actor in this fleet (81 implemented before this
  build).
- Confirms the fleet-wide governor-name-reuse precedent a sixth time,
  and establishes the THIRD distinct governor-name family this
  precedent generalizes across.
- Establishes a genuinely NEW unconditional-evaluation-screening
  concept (instructor-license-unconfirmed?, the THIRD conditional
  variant) that is structurally novel for this fleet: the FIRST check
  in this discipline to verify a fact about the ASSESSOR rather than
  the subject being assessed.
- `MemStore` ‖ `DatomicStore` parity is proven by
  `test/training/store_contract_test.clj`, the same `:db-api`-driven
  swap pattern every sibling actor uses.
- 32 tests / 141 assertions pass; lint is clean; the demo
  (`clojure -M:dev:run`) walks one clean single-actuation lifecycle
  plus three HARD-hold scenarios and one conditional-noop scenario
  end-to-end.
- `blueprint.edn` required no field-sync fixes this time (already
  correct) -- only the `:maturity` flip itself.
