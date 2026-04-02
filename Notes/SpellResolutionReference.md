# Spell Resolution Reference

This note is meant to be the quick-read version of the current prototype resolution model.

It does not try to replace the larger design docs. It just answers:

- what resolution types currently exist?
- what glyph combinations usually push a spell into them?
- what source / recipient patterns are currently recognized?

## Current resolution types

### `TRAVELING_EFFECT`

Usually means the spell is being formed, sent outward, and released.

Common pushes:

- `shape + direct + release`
- `fire + gather + forward + on_impact`
- `force + direct + forward + release`

Common feel:

- projectile
- bolt
- launched impact effect

### `BOUNDARY_WARD`

Usually means the spell is being fixed to a place and made to watch or react there.

Common pushes:

- `anchor + boundary + field`
- `anchor + boundary + field + persist`
- `anchor + boundary + field + on_entry + bind`

Common feel:

- warded area
- anchored trigger zone
- sustained local field

### `PATTERN_INTERACTION`

Usually means the spell is reading and modifying an existing structure or magical pattern.

Common pushes:

- `locking_pattern + separate`
- `locking_pattern + disrupt`
- `locking_pattern + gentle + separate`
- `order + binding + perception + locking_pattern`

Common feel:

- unlock
- lock
- careful unbinding
- pattern disruption

Execution note:

- `binding + locking_pattern` now tends to create or reinforce a magical lock state
- `separate + gentle + locking_pattern` now tends to remove a magical lock state
- `disrupt` / `terminate` / `unravel` now tend to break a magical lock state more forcefully
- the current prototype no longer treats vanilla open/closed as the meaning of lock
- instead it keeps a separate magical locked-state over the target block
- that lock now sits on top of the generalized block pattern tag layer, so the same infrastructure can later carry tags besides `MAGIC_LOCKED`
- current `MAGIC_LOCKED` support covers:
  - openable blocks like doors, trapdoors, and fence gates
  - powered toggles like levers
  - extendable blocks like pistons
  - container-backed blocks like chests

### `RESTORATION_EFFECT`

Usually means the spell is correcting or reinforcing life structure rather than moving life from one being to another.

Common pushes:

- `life + life_pattern + refine`
- `life + life_pattern + strengthen`
- `life + life_pattern + refine + strengthen + stabilize`
- `restore` can reinforce this, but is no longer required

Common feel:

- ordinary healing
- restoration
- reinforcement of a living pattern

### `VITALITY_TRANSFER`

Usually means the spell is moving life/vitality from one place or being into another.

Common pushes:

- `transfer + life`
- `self + life_pattern + transfer + life`
- `self + transfer + life + seen_target`
- `self + life_pattern + transfer + life + refine + seen_target`

Common feel:

- sacrificial heal
- vitality exchange
- directed life transfer

### `CONSTRUCTION_EFFECT`

Usually means the spell is shaping matter or local structure in the world.

Common pushes:

- `earth + shape + path`
- `earth + shape + surface`
- `earth + shape + raise`
- `earth + shape + chosen_point + strengthen`

Common feel:

- path
- wall
- lifted or shaped terrain/stone

### `UNKNOWN_UNSTABLE`

Usually means the chain is missing enough structure that the interpreter cannot strongly prefer one outcome.

Common pushes:

- not enough principles / operations
- too little targeting / spatial structure
- multiple outcomes scoring too closely together

## Current recipient patterns

These are structural recipient candidates, not guaranteed final answers.

### `SELF`

Common pushes:

- `self`
- `caster`
- no strong outward recipient evidence

### `LOOK_TARGET`

Common pushes:

- `seen_target`
- `direct`
- `forward`
- `perception`
- `life_pattern`
- `transfer` when paired with outward reference logic

### `CHOSEN_POINT`

Common pushes:

- `chosen_point`

### `ANCHORED_POINT`

Common pushes:

- `anchor`
- `boundary`
- `field`
- strongest when all three appear together

## Current source patterns

These are structural source candidates, not full runtime resource accounting yet.

### `CASTER_MANA`

Common pushes:

- baseline default for most ordinary spells
- `gather`
- `restore`
- `refine`
- `strengthen`
- `stabilize`

### `SELF_HEALTH`

Common pushes:

- `transfer`
- `life`
- `life_pattern`
- `self`
- `caster`
- especially strong in self-to-target vitality transfer chains

### `AMBIENT_MANA`

Common pushes:

- mild default possibility
- `gather`

### `TARGET_VITALITY`

Common pushes:

- `transfer`
- `life`
- `life_pattern`
- `seen_target`

This is currently recognized structurally, but the runtime does not fully exploit it yet.

## First-pass likely failures

The current prototype now emits a likely failure profile during analysis.

### `STRUCTURAL_FAILURE`

Common pushes:

- missing required structural categories
- barely held-together chains
- mis-shaped but still partially resolving spell forms

### `INTERPRETIVE_FAILURE`

Common pushes:

- multiple intents scoring too closely together
- unresolved meaning about what the chain is trying to do

### `INFORMATION_FAILURE`

Common pushes:

- weak references
- vague targeting
- recipient/destination ambiguity

### `PERSISTENCE_FAILURE`

Common pushes:

- under-defined anchored/persistent structures
- weak field/boundary support for persistent spells

## Current prototype examples

### Normal healing

`perception -> life -> life_pattern -> refine -> strengthen -> stabilize`

Typical reading:

- intent: `RESTORATION_EFFECT`
- likely source: `CASTER_MANA`
- recipients: somewhat ambiguous unless more target data is supplied

### Sacrificial / vitality heal

`perception -> self -> life_pattern -> transfer -> life -> refine -> seen_target -> stabilize`

Typical reading:

- intent: `VITALITY_TRANSFER`
- likely source: `SELF_HEALTH`
- likely recipient: `LOOK_TARGET`

### Fire projectile

`gather -> fire -> shape -> separate -> forward -> direct -> on_impact -> release`

Typical reading:

- intent: `TRAVELING_EFFECT`
- likely source: `CASTER_MANA` with some gather/ambient flavor
- likely recipient: outward target or impact point

### Unlock

`perception -> order -> binding -> locking_pattern -> separate -> gentle`

Typical reading:

- intent: `PATTERN_INTERACTION`
- likely source: `CASTER_MANA`
- likely recipient: looked-at pattern / target structure
- likely result: remove a magical lock state rather than directly toggling the block open/closed

### Lock

`perception -> order -> binding -> locking_pattern`

Typical reading:

- intent: `PATTERN_INTERACTION`
- likely source: `CASTER_MANA`
- likely recipient: looked-at pattern / target structure
- likely result: impose a crude magical lock state

### Alert ward

`anchor -> boundary -> field -> persist -> perception -> on_entry -> attune -> caster -> bind`

Typical reading:

- intent: `BOUNDARY_WARD`
- likely source: `CASTER_MANA`
- likely recipient: anchored point / field location

## Current tuning philosophy

- Missing structure should create ambiguity instead of pretending the spell was explicit.
- Missing structure should more often degrade or redirect the outcome than cleanly cancel the cast.
- `restore` is now support for restoration, not the sole reason healing works.
- `transfer` should increasingly mean actual movement of life, mana, or state rather than just a stronger heal tag.
- The analysis output should stay explainable enough that we can tune these scores by hand.
