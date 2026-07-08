# cloud-itonami-isic-8549

Open Business Blueprint for **ISIC Rev.5 8549**: Other education
n.e.c..

This repository publishes an other-education-training-provider actor
-- student intake, per-jurisdiction training-provider regulatory
assessment, instructor-license screening and certification/completion
finalization -- as an OSS business that any qualified, licensed
operator can fork, deploy, run, improve and sell, so a community or
independent educator never surrenders student data and ledgers to a
closed SaaS.

Built on this workspace's
[`langgraph`](https://github.com/kotoba-lang/langgraph)
StateGraph runtime (portable `.cljc`, supervised superstep loop,
interrupts, Datomic/in-mem checkpoints) -- the same actor pattern as
every prior actor in this fleet
([`cloud-itonami-isic-6511`](https://github.com/cloud-itonami/cloud-itonami-isic-6511),
[`6512`](https://github.com/cloud-itonami/cloud-itonami-isic-6512),
[`6621`](https://github.com/cloud-itonami/cloud-itonami-isic-6621),
[`6622`](https://github.com/cloud-itonami/cloud-itonami-isic-6622),
[`6629`](https://github.com/cloud-itonami/cloud-itonami-isic-6629),
[`6520`](https://github.com/cloud-itonami/cloud-itonami-isic-6520),
[`6530`](https://github.com/cloud-itonami/cloud-itonami-isic-6530),
[`6820`](https://github.com/cloud-itonami/cloud-itonami-isic-6820),
[`6612`](https://github.com/cloud-itonami/cloud-itonami-isic-6612),
[`6492`](https://github.com/cloud-itonami/cloud-itonami-isic-6492),
[`6920`](https://github.com/cloud-itonami/cloud-itonami-isic-6920),
[`6611`](https://github.com/cloud-itonami/cloud-itonami-isic-6611),
[`7120`](https://github.com/cloud-itonami/cloud-itonami-isic-7120),
[`8620`](https://github.com/cloud-itonami/cloud-itonami-isic-8620),
[`8530`](https://github.com/cloud-itonami/cloud-itonami-isic-8530),
[`9200`](https://github.com/cloud-itonami/cloud-itonami-isic-9200),
[`7500`](https://github.com/cloud-itonami/cloud-itonami-isic-7500),
[`9603`](https://github.com/cloud-itonami/cloud-itonami-isic-9603),
[`9521`](https://github.com/cloud-itonami/cloud-itonami-isic-9521),
[`9321`](https://github.com/cloud-itonami/cloud-itonami-isic-9321),
[`8730`](https://github.com/cloud-itonami/cloud-itonami-isic-8730),
[`9102`](https://github.com/cloud-itonami/cloud-itonami-isic-9102),
[`9103`](https://github.com/cloud-itonami/cloud-itonami-isic-9103),
[`9602`](https://github.com/cloud-itonami/cloud-itonami-isic-9602),
[`9000`](https://github.com/cloud-itonami/cloud-itonami-isic-9000),
[`8890`](https://github.com/cloud-itonami/cloud-itonami-isic-8890),
[`8610`](https://github.com/cloud-itonami/cloud-itonami-isic-8610),
[`9311`](https://github.com/cloud-itonami/cloud-itonami-isic-9311),
[`8510`](https://github.com/cloud-itonami/cloud-itonami-isic-8510),
[`9412`](https://github.com/cloud-itonami/cloud-itonami-isic-9412),
[`6491`](https://github.com/cloud-itonami/cloud-itonami-isic-6491),
[`8720`](https://github.com/cloud-itonami/cloud-itonami-isic-8720),
[`8521`](https://github.com/cloud-itonami/cloud-itonami-isic-8521),
[`6619`](https://github.com/cloud-itonami/cloud-itonami-isic-6619),
[`3600`](https://github.com/cloud-itonami/cloud-itonami-isic-3600),
[`6190`](https://github.com/cloud-itonami/cloud-itonami-isic-6190),
[`3030`](https://github.com/cloud-itonami/cloud-itonami-isic-3030),
[`3830`](https://github.com/cloud-itonami/cloud-itonami-isic-3830),
[`7020`](https://github.com/cloud-itonami/cloud-itonami-isic-7020),
[`9420`](https://github.com/cloud-itonami/cloud-itonami-isic-9420),
[`9491`](https://github.com/cloud-itonami/cloud-itonami-isic-9491),
[`2610`](https://github.com/cloud-itonami/cloud-itonami-isic-2610),
[`3512`](https://github.com/cloud-itonami/cloud-itonami-isic-3512),
[`8810`](https://github.com/cloud-itonami/cloud-itonami-isic-8810),
[`8691`](https://github.com/cloud-itonami/cloud-itonami-isic-8691),
[`8569`](https://github.com/cloud-itonami/cloud-itonami-isic-8569),
[`6419`](https://github.com/cloud-itonami/cloud-itonami-isic-6419),
[`7310`](https://github.com/cloud-itonami/cloud-itonami-isic-7310),
[`7320`](https://github.com/cloud-itonami/cloud-itonami-isic-7320),
[`7210`](https://github.com/cloud-itonami/cloud-itonami-isic-7210),
[`7410`](https://github.com/cloud-itonami/cloud-itonami-isic-7410),
[`8710`](https://github.com/cloud-itonami/cloud-itonami-isic-8710),
[`8541`](https://github.com/cloud-itonami/cloud-itonami-isic-8541),
[`8690`](https://github.com/cloud-itonami/cloud-itonami-isic-8690),
[`9601`](https://github.com/cloud-itonami/cloud-itonami-isic-9601),
[`6420`](https://github.com/cloud-itonami/cloud-itonami-isic-6420),
[`7420`](https://github.com/cloud-itonami/cloud-itonami-isic-7420),
[`9609`](https://github.com/cloud-itonami/cloud-itonami-isic-9609),
[`8550`](https://github.com/cloud-itonami/cloud-itonami-isic-8550),
[`7010`](https://github.com/cloud-itonami/cloud-itonami-isic-7010),
[`8790`](https://github.com/cloud-itonami/cloud-itonami-isic-8790),
[`8542`](https://github.com/cloud-itonami/cloud-itonami-isic-8542),
[`6411`](https://github.com/cloud-itonami/cloud-itonami-isic-6411),
[`7490`](https://github.com/cloud-itonami/cloud-itonami-isic-7490),
[`9319`](https://github.com/cloud-itonami/cloud-itonami-isic-9319),
[`9329`](https://github.com/cloud-itonami/cloud-itonami-isic-9329),
[`9312`](https://github.com/cloud-itonami/cloud-itonami-isic-9312),
[`9492`](https://github.com/cloud-itonami/cloud-itonami-isic-9492),
[`9499`](https://github.com/cloud-itonami/cloud-itonami-isic-9499),
[`9512`](https://github.com/cloud-itonami/cloud-itonami-isic-9512),
[`9522`](https://github.com/cloud-itonami/cloud-itonami-isic-9522),
[`7220`](https://github.com/cloud-itonami/cloud-itonami-isic-7220),
[`9411`](https://github.com/cloud-itonami/cloud-itonami-isic-9411),
[`8522`](https://github.com/cloud-itonami/cloud-itonami-isic-8522)) --
here it is **EdOps-LLM ⊣ Instruction Integrity Governor** -- the SAME
governor keyword `cultural`/8542 (cultural education) already uses, a
deliberate, honest reuse of the same instruction-integrity-oversight
business archetype for a different domain-specific concern (see
`docs/adr/0001-architecture.md` Decision 1 for why this is not a
naming error, and for why this is the SIXTH confirmation of the fleet-
wide governor-name-reuse precedent, and the THIRD distinct governor-
name family).

> **Why an actor layer at all?** An LLM is great at drafting a
> student-intake summary, normalizing records, and checking whether a
> student's own recorded practice hours actually satisfy their own
> recorded requirement -- but it has **no notion of which
> jurisdiction's training-provider law is official, no license to
> finalize a real certification or completion record, and no way to
> know on its own whether the INSTRUCTOR responsible for a driving
> student's competency sign-off actually holds a confirmed driving-
> instructor license**. Letting it finalize a completion record
> directly invites fabricated regulatory citations, a completion
> record finalized on top of insufficient practice hours, and a
> student certified as road-ready by an unlicensed instructor --
> real physical-safety risk, and liability, for whoever runs it. This
> project seals the EdOps-LLM into a single node and wraps it with an
> independent **Instruction Integrity Governor**, a human **approval
> workflow**, and an immutable **audit ledger**.

## Scope: what this actor does and does not do

This actor covers student intake through training-provider regulatory
assessment, instructor-license screening and certification/completion
finalization. It does **not**, by itself, hold any license required to
operate a training provider in a given jurisdiction, and it does not
claim to. It also does not perform the actual driving/exam-prep/
corporate-training instruction itself, or judge its pedagogical
quality -- `training.registry/practice-hours-insufficient?` is a pure
ground-truth recompute against the student's own recorded fields, not
a pedagogical assessment. Whoever deploys and operates a live instance
(a licensed training-provider operator) supplies any jurisdiction-
specific license, the real instructional delivery and the real
training-management-system integrations, and bears that jurisdiction's
liability -- the software supplies the governed, spec-cited, audited
execution scaffold so that operator does not have to build the
compliance layer from scratch.

### Actuation

**Finalizing a real certification or completion record is never
autonomous, at any phase, by construction.** Two independent layers
enforce this (`training.governor`'s `:actuation/finalize-completion`
high-stakes gate and `training.phase`'s phase table, which never puts
`:actuation/finalize-completion` in any phase's `:auto` set) -- see
`training.phase`'s docstring and `test/training/phase_test.clj`'s
`finalize-completion-never-auto-at-any-phase`. The actor may draft,
check and recommend; a human licensed educator is always the one who
actually finalizes a completion record. Following `sports`/8541's and
`cultural`/8542's own either/or-naming precedent, this build treats
"certification or completion record" as ONE conceptual act -- a
single decision with two possible outcome kinds, not two separate
sequential milestones the way `secondary`/8521's and `vocational`/
8522's own grading-THEN-graduation dual-actuation shape works.
Grounded directly in this blueprint's own README text ("No automated
proposal, by itself, can complete the following without governor
approval and audit evidence: finalizing a certification or completion
record").

## The core contract

```
student intake + jurisdiction facts (training.facts, spec-cited)
        |
        v
   ┌───────────────────────┐   proposal      ┌───────────────────────┐
   │ EdOps-LLM             │ ─────────────▶ │ Instruction                    │  (independent system)
   │ (sealed)              │  + citations    │ Integrity Governor:          │
   └───────────────────────┘                 │ spec-basis · evidence-       │
          │                 commit ◀┼ incomplete · practice-           │
          │                         │ hours-insufficient (honest             │
    record + ledger        escalate ┼ reuse) · instructor-license-             │
          │              (ALWAYS for│ unconfirmed (conditional, NEW,             │
          │               :actuation│ verifies the ASSESSOR not the               │
          │               /finalize-│ subject) · already-finalized                  │
          ▼               completion)└───────────────────────┘
      human approval
```

**The EdOps-LLM never finalizes a completion record the Instruction
Integrity Governor would reject, and never does so without a human
sign-off.** Hard violations (fabricated regulatory requirements;
unsupported evidence; insufficient practice hours; an unconfirmed
instructor license where required; a double finalization) force
**hold** and *cannot* be approved past; a clean finalization proposal
still always routes to a human.

## Run

```bash
clojure -M:dev:run     # walk one clean single-actuation lifecycle + three HARD-hold cases + one conditional-noop case through the actor
clojure -M:dev:test    # governor contract · phase invariants · store parity · registry conformance · facts coverage
clojure -M:lint        # clj-kondo (errors fail; CI mirrors this)
```

## Robotics premise

All cloud-itonami verticals are designed on the premise that a **robot
performs the physical domain work**. Here a facility-safety monitoring
robot supports physical supervision during practical instruction,
under the actor, gated by the independent **Instruction Integrity
Governor**. The governor never dispatches hardware itself;
`:high`/`:safety-critical` actions require human sign-off.

## Open business

This repository is not only source code. It is a public, forkable
business model:

| Layer | What is open |
|---|---|
| OSS core | Actor runtime, Instruction Integrity Governor, completion-finalization draft records, audit ledger |
| Business blueprint | Customer, offer, pricing, unit economics, sales motion |
| Operator playbook | How to fork, license, deploy and support the service in a jurisdiction |
| Trust controls | Governance, security reporting, actuation invariant, audit requirements |

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md) to start this as an
open business on itonami.cloud, and
[`docs/adr/0001-architecture.md`](docs/adr/0001-architecture.md) for the
full architecture and decision record.

## Capability layer

This blueprint resolves its technology stack via
[`kotoba-lang/industry`](https://github.com/kotoba-lang/industry) (ISIC
`8549`). This vertical's academic/case records are practice-specific
rather than a shared cross-operator data contract, so `training.*`
runs on the generic robotics/identity/forms/dmn/bpmn/audit-ledger
stack only -- no bespoke domain capability lib to reference at all.

## Layout

| File | Role |
|---|---|
| `src/training/store.cljc` | **Store** protocol -- `MemStore` ‖ `DatomicStore` (`langchain.db`) + append-only audit ledger + completion-finalization history. No dynamically-filed sub-record -- the actuation op acts directly on a pre-seeded student, and the double-finalization guard checks a dedicated `:completion-finalized?` boolean rather than a `:status` value |
| `src/training/registry.cljc` | Completion-finalization draft records, plus `practice-hours-insufficient?` -- an HONEST, literal reuse of `cultural.registry`'s own NINTH-instance MINIMUM-threshold sufficiency check, not claimed as new |
| `src/training/facts.cljc` | Per-jurisdiction training-provider catalog AND a SEPARATE driving-instructor-licensing citation per jurisdiction (a genuine extension beyond `cultural.facts`'s own child-performer-permit-only catalog) with an official spec-basis citation per entry, honest coverage reporting |
| `src/training/edopsllm.cljc` | **EdOps-LLM** -- `mock-advisor` ‖ `llm-advisor`; intake/curriculum-verification/instructor-license-screening/completion-finalization proposals |
| `src/training/governor.cljc` | **Instruction Integrity Governor** -- 4 checks: spec-basis · evidence-incomplete · practice-hours-insufficient (honest reuse) · instructor-license-unconfirmed (CONDITIONAL unconditional-evaluation, GENUINELY NEW, the 66th grounding of this discipline and the THIRD conditional variant -- structurally different from every prior instance, verifying a fact about the ASSESSOR rather than the subject), + already-finalized guard + 1 soft (confidence/actuation gate) |
| `src/training/phase.cljc` | **Phase 0→3** -- read-only → assisted intake → assisted verify → supervised (completion finalization always human; student intake is the ONLY auto-eligible op, no direct capital risk) |
| `src/training/operation.cljc` | **OperationActor** -- langgraph StateGraph |
| `src/training/sim.cljc` | demo driver |
| `test/training/*_test.clj` | governor contract · phase invariants · store parity · registry conformance · facts coverage |

## Business-process coverage (honest)

This actor covers student intake through training-provider regulatory
assessment, instructor-license screening and certification/completion
finalization -- the core governed lifecycle this blueprint's own
`docs/business-model.md` names as its Offer:

| Covered | Not covered (out of scope for this R0) |
|---|---|
| Student intake + per-jurisdiction evidence checklisting, HARD-gated on an official spec-basis citation (`:student/intake`/`:curriculum/verify`) | Real training-management-system integration, real driving/exam-prep/corporate-training instruction itself (see `training.facts`'s docstring) |
| Instructor-license screening, CONDITIONAL on the student's own program actually requiring a licensed instructor, evaluated so the screening op itself can HARD-hold on its own finding (`:instructor-license/screen`) | Pedagogical-quality judgment itself -- deliberately outside this actor's competence |
| Certification/completion finalization, HARD-gated on full evidence, sufficient practice hours and a confirmed instructor license (when required), plus a double-finalization guard (`:actuation/finalize-completion`) | |
| Immutable audit ledger for every intake/verification/screening/finalization decision | |

Extending coverage is additive: add the next gate (e.g. a vehicle-
insurance-coverage-verification check for driving schools) as its own
governed op with its own HARD checks and tests, following the SAME
"an independent governor re-verifies against the actor's own records
before any real-world act" pattern this repo's flagship op already
establishes.

## Jurisdiction coverage (honest)

`training.facts/coverage` reports how many requested jurisdictions
actually have an official spec-basis in `training.facts/catalog` --
currently 4 seeded (JPN, USA, GBR, DEU) out of ~194 jurisdictions
worldwide. This is a starting catalog to prove the governor contract
end-to-end, not a claim of global coverage. Adding a jurisdiction is
additive: one map entry in `training.facts/catalog`, citing a real
official source -- never fabricate a jurisdiction's requirements to
make coverage look bigger.

## Maturity

`:implemented` -- `EdOps-LLM` + `Instruction Integrity Governor` run
as real, tested code (see `Run` above), promoted from the originally-
published `:blueprint`-tier scaffold, modeled closely on `cultural`/
8542's own architecture and the eighty other prior actors'
architecture across this fleet. See `docs/adr/0001-architecture.md`
for the history and design.

## License

Code and implementation templates are AGPL-3.0-or-later.
