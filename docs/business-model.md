# Business Model: Other education n.e.c.

## Classification

- Repository: `cloud-itonami-isic-8549`
- ISIC Rev.5: `8549`
- Activity: other education not elsewhere classified (e.g. driving schools, exam-preparation courses, corporate training)
- Social impact: education access, data sovereignty, transparent audit

## Customer

- independent training providers
- cooperative instructor collectives
- community continuing-education programs

## Offer

- student enrollment intake
- curriculum/program proposal
- certification/completion proposal
- immutable audit ledger

## Revenue

- self-host setup: one-time implementation fee
- managed hosting: monthly subscription per provider
- support: monthly retainer with SLA
- migration: import from an incumbent training-management system
- per-enrollment fee

## Trust Controls

- no certification or completion record is finalized without human sign-off
- a fabricated assessment forces a hold, not an override
- for programs requiring a licensed instructor's sign-off (e.g.
  driving instruction), a completion record cannot be finalized
  without a confirmed instructor license on file -- unconfirmed, this
  is a hold, never an override
- every record path is auditable
- student data stays outside Git
- emergency manual override paths remain outside LLM control

## Instruction Integrity Governor: decision rule

This vertical's governor shares its name (`:instruction-integrity-
governor`) with `cloud-itonami-isic-8542`'s (cultural education). This
is a deliberate reuse, not a naming error: both actors perform
instruction-integrity oversight of a training provider finalizing a
certification/completion record for a student. The genuinely
distinguishing concern this vertical adds is instructor-license
confirmation: unlike `cloud-itonami-isic-8542`'s own child-performer-
work-permit check (a fact ABOUT THE STUDENT), driving instruction (one
of this blueprint's own named example activities) depends on whether
the INSTRUCTOR who signs off on a student's competency is themselves
a licensed driving instructor -- a fact about the assessor, not the
subject being assessed. This requirement is CONDITIONAL: a student in
a program that does not require a licensed instructor's sign-off
(e.g. exam-preparation or corporate training) carries no such
requirement at all.
