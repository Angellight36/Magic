# Casting Focus and Mana Notes

This note captures the current direction for foci, concentration, spell differentiation, and explicit mana supply.

## Glyph focus is not the same thing as "permission to cast"

- `glyph_focus` should be treated as one current player-facing casting aid, not the universal prerequisite for magic.
- `glyph_focus` currently also acts as a written spell carrier, with one stored chain per focus in the current dev build.
- Different magical beings may not need a focus at all.
- Some beings may cast innately.
- Some beings may use body parts, runes, voices, or environmental attunement instead of a handheld focus.
- A focus should eventually provide advantages beyond merely enabling the UI:
  - stabilization
  - written spell storage
  - storage
  - shorthand support
  - safer targeting
  - mana shaping or buffering
  - improved recall of known chains

## Written spells versus remembered shorthand

- The current focus direction is that players are writing spells down because directly remembering every chain should be hard.
- Each `glyph_focus` should carry its own stored spell rather than sharing one global draft across all foci.
- This written-storage model should stay distinct from a future memorized shorthand model.
- A future shorthand system can let players bind frequently used glyphs or fragments to a small fixed input set, such as roughly ten quick-access keys.
- That future memory system should help assemble spells on the fly, not replace the role of written foci entirely.
- A later scroll system can derive from this written-spell model by turning a composed focus spell into a more permanent reusable carrier with reduced risk of accidental edits.

## Concentration should become a second limiting axis

Mana should not be the only limiter.

Concentration is a good candidate for:

- maximum active glyph complexity
- simultaneous maintained spells
- how many anchored effects a caster can actively feed
- whether a spell can continue accepting mana after the initial cast
- whether a caster can safely hold an under-specified or unstable chain together

Good high-level split:

- mana limits how much power can be supplied
- concentration limits how much structure can be held at once

## How far we are from meaningfully different similar spells

We are not extremely far away on the interpreter side, but we are still missing important vocabulary on the execution side.

Right now, interpretation already distinguishes more than just domain:

- intent
- traits
- recipients
- sources
- confidence / ambiguity

That means `fireball` and `flare` do not need different domains in order to become different spells.

What is still missing is clearer vocabulary and runtime handling for:

- luminosity / signaling / revelation
- intensity and payload size
- sustained ascent vs direct impact
- harmless light vs damaging fire
- burst vs lingering emission

Example direction:

- `fireball` should read as condensed damaging fire with impact and splash.
- `flare` should read as bright elevated fire/light with low damage, high visibility, and signaling/revelation utility.

So the real gap is less "domain collision" and more "missing differentiating glyphs and effect parameters."

## Mana input should be explicit, not merely inferred

Current source scoring is useful for interpretation, but long-term mana supply should be explicitly declared.

We should separate:

- what the spell does
- where the mana comes from
- how the mana is fed
- whether the feed is one-time or maintained

### Suggested model

Treat mana supply as having four parts:

1. Source
- self
- ambient environment
- stored object / focus
- linked casters
- target / stolen source

2. Resource
- mana
- life
- heat
- other future magical substrates if needed

3. Flow operation
- gather
- transfer
- attune
- sustain

4. Feed mode
- one-time burst
- draw-as-needed
- fixed quota
- maintained channel
- conditional refill

## What `gather mana` and `mana transfer` should probably mean

### Pull mana from self as needed

Best meaning:

- self is the authorized source
- the spell may continue drawing while active
- draw should stop when the spell ends, concentration breaks, or the caster cuts it off

Good conceptual shape:

- `self -> mana -> attune -> sustain`
- `self -> mana -> gather -> sustain`

### Pull a specific amount of mana from self

Best meaning:

- take a fixed quantity up front
- do not keep draining unless the chain also includes sustain/channel logic

Good conceptual shape:

- `self -> mana -> transfer -> measured_portion`
- `self -> mana -> gather -> fixed_portion`

We do not yet have good quantity glyphs, so "specific amount" is still a missing vocabulary area.

### Gather mana from the environment

Best meaning:

- pull from ambient magical availability instead of the caster's own reserve
- rate and efficiency should depend on environment and stability
- weak environments should reduce throughput or duration

Good conceptual shape:

- `ambient -> mana -> gather`
- `environment -> mana -> gather -> stabilize`

This points to a likely future need for an explicit `ambient` / `environment` reference glyph.

## Wards and sustained mana feed

Wards are one of the clearest reasons to make mana supply explicit.

We should be able to support all of these eventually:

- ward with only upfront mana
- ward with a local reserve that depletes
- ward continuously fed by one caster
- ward continuously fed by multiple casters
- ward that collapses when concentration or supply breaks

The Harry Potter "teachers feeding a giant ward together" scene is a good target reference for the maintained-channel model.

That implies future ward logic should track:

- initial reserve
- current inflow
- upkeep rate
- concentration commitments
- supporting caster count
- what happens when feed drops below upkeep

## Recommended future vocabulary gaps

These are the biggest missing pieces revealed by this pass:

- explicit `mana` principle
- explicit `ambient` / `environment` reference
- quantity / measure glyphs
- channel / maintain / cut-off semantics distinct from one-time casting
- light / signal / reveal semantics for spells like `flare`
