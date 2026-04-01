# Magic Overview

> This document is the high-level design overview for the magic system.
> It focuses on the structure of the system, how spellcasting is meant to feel,
> and the major rules that define how magic behaves in practice.
>
> This file is intended to serve as the main concept reference.
> Deeper world-facing details and lore-specific framing have been moved into `MagicLore.md`.

---

# 1. Core Vision

The system is being designed around a few major goals:

- Magic should feel like a **language of construction**, not a menu of predefined spells.
- Spells should be **composable**, with broad reusable parts.
- Specific spell outcomes such as `Fireball`, `Unlock`, `Ward`, or `Dispel` should often be **emergent results**, not always primitive glyphs.
- Early-game casting should be **long, manual, and error-prone**.
- Advanced casters should gain mastery through **understanding, compression, precision, and safe shorthand**, not just raw mana.
- The system should support both:
  - practical spellcasting in the moment
  - enormous ritual-scale magic such as city shaping, environmental transformation, and magical infrastructure
- Failed or abandoned magic should be able to leave **persistent marks on the world**, creating magical scars, haunted places, unstable wards, cursed ruins, and strange biomes.

This means the system is not trying to emulate a simple “spell list.”
It is trying to define a **magical grammar**.

---

# 2. Foundational Design Principle

**Magic is a structured language of instruction rather than a library of predefined spells; stable forms such as projectiles, wards, barriers, rituals, messages, and scrying effects emerge from the successful resolution of sufficiently complete glyph chains.**

Another way to say this:

**Glyphs are units of magical instruction. A spell is a chain of instructions that reality attempts to interpret.**

This is the heart of the design.

---

# 3. Spells as Magical Sentences

A spell is not best thought of as a named effect.
A spell is better understood as a **magical sentence** or **instruction chain**.

Instead of saying:

- “cast Unlock”
- “cast Fireball”
- “cast Ward”

the system asks:

- what principles are involved?
- what operations are being performed?
- what constraints define where, when, and how the magic behaves?
- what target or pattern is being acted upon?
- what qualifiers fine-tune the interaction?
- how does the world resolve the chain?

This means:
- `Unlock` is usually not a primitive component
- `Ward` is usually not a primitive cast mode
- `Projectile` is not necessarily a primitive form
- `Explosion` is often a resolved behavior, not a base element

---

# 4. Early, Mid, and Late Game Spellcasting

## 4.1 Early Game

Magic is manual, verbose, and dangerous.

A novice should often need to manually construct even a simple spell from many components.
For example, a primitive fireball may require a full sequence to:

- gather fire-aspected mana
- compress it
- shape it
- separate it from the caster
- direct it outward
- stabilize it in motion
- release it on contact

If any of these steps are missing or poorly done, the result may misfire.

This is intended.

## 4.2 Mid Game

Casters begin to recognize repeated structures.

They may:
- learn more efficient spell chains
- specialize in particular domains
- bind repeated logic into runes or prepared foci
- reduce error through better understanding

## 4.3 Late Game

Mastery is expressed through compression and precision.

Masters do not merely know “stronger” spells.
They know how to:

- shorten chains safely
- create stable shorthand
- construct runic systems
- chain constructs together
- create large-scale rituals
- coordinate multi-caster architecture
- build lasting magical infrastructure

**Mastery is compression of understanding.**

---

# 5. Why Many Spells Should Look Similar

Many spells solve similar structural problems.

For example, most reliable traveling attack effects require some version of:

- principle selection
- gathering
- shaping
- positional separation from the caster
- directional logic
- stability in motion
- release condition

So a fireball, lightning bolt, ice shard, shadow dart, and stone lance may all share a similar skeleton.

This is good.
It means the magical grammar is coherent.

The differences arise from:
- the principles used
- the target patterns involved
- the qualifiers and constraints
- the release behavior
- the way the world resolves the interaction

---

# 6. Why Specific Spell Results Should Usually Be Emergent

A major design conclusion was that many named spell effects should not be base glyphs.

For example, `Unlock` should usually not be a raw glyph.
Instead, it should emerge from broad operations interacting with an object that has relevant properties.

A locked door is not merely “a door.” It may have:

- a `Locking Pattern`
- a `Binding Pattern`
- a `Mechanical Structure`
- a `Runic Seal`
- a `Boundary Condition`

Then a spell can interact with those patterns.

This allows a single broad system to handle:

- locks
- seals
- magical prisons
- cursed bindings
- anchored constructs
- living restraints
- warded thresholds

This is much more scalable than creating one-off glyphs for each use case.

---

# 7. Spell Categories as Resolution Outcomes

The following are usually better understood as **resolved outcomes** rather than always primitive glyphs:

- projectile
- beam
- burst
- explosion
- barrier
- field
- ward
- seal
- unlock
- dispel
- bridge
- healing effect
- scrying surface
- message relay
- ritual construct

A spell chain becomes one of these through interpretation and successful structure.

---

# 8. Glyph Taxonomy v1

The current taxonomy organizes glyphs into several major categories:

- Principles
- Operations
- Constraints
- References
- Qualifiers

These categories do not all represent the same kind of thing.
Some are primal magical forces, while others are structural or informational helpers. That is acceptable so long as they remain broad, reusable, and not mere one-off spell shortcuts.

---

# 9. Principles

Principles are the fundamental magical concepts, forces, or metaphysical substances being manipulated.

## 9.1 Elemental / Material Principles

- `Fire`
- `Water`
- `Earth`
- `Air`
- `Lightning`
- `Sound`

## 9.2 Conceptual / Arcane Principles

- `Light`
- `Shadow`
- `Life`
- `Death`
- `Force`
- `Motion`
- `Binding`
- `Change`
- `Order`
- `Chaos`
- `Space`
- `Perception`

---

# 10. Operations

Operations are verbs. They define what the caster is trying to do with principles, references, and structure.

## 10.1 Core Operations

- `Gather`
- `Shape`
- `Bind`
- `Release`
- `Direct`
- `Anchor`
- `Sustain`
- `Stabilize`
- `Disrupt`
- `Separate`
- `Combine`
- `Transform`
- `Compress`
- `Expand`
- `Refine`
- `Weaken`
- `Strengthen`
- `Attune`
- `Repeat`
- `Delay`
- `Terminate`

## 10.2 New or Likely Needed Operations

- `Trigger`
- `Transmit`
- `Trace`
- `Absorb`

## 10.3 Tentative Future Operations

- `Raise`
- `Carve`
- `Restore`
- `Unravel`
- `Transfer`
- `Imprint`
- `Display`

---

# 11. Constraints

Constraints define how a spell behaves in practice.

## 11.1 Spatial Constraints

- `Self`
- `Near`
- `Far`
- `Forward`
- `Above`
- `Below`
- `Centered`
- `Boundary`
- `Surface`
- `Hollow`
- `Solid`
- `Line`
- `Sphere`
- `Cone`
- `Ring`
- `Field`
- `Path`

## 11.2 Temporal Constraints

- `Instant`
- `Persist`
- `Pulse`
- `Delayed`
- `Until Broken`
- `Gradual`

## 11.3 Event / Behavior Constraints

- `On Contact`
- `On Impact`
- `On Entry`
- `On Exit`
- `While Seen`
- `While Fed`
- `Conditional`

---

# 12. References

References tell the spell what it is working on, where it starts, or what pattern it should interpret.

## 12.1 Basic References

- `Caster`
- `Touched Target`
- `Seen Target`
- `Held Object`
- `Marked Target`
- `Chosen Point`
- `Current Space`
- `Path Ahead`
- `Nearest`
- `All Within`

## 12.2 Structural References

- `Locking Pattern`
- `Binding Pattern`
- `Life Pattern`
- `Runic Pattern`
- `Material Pattern`
- `Boundary Pattern`

## 12.3 Additional Reference Candidates

- `Between`
- `Interior`
- `Exterior`
- `True Core`
- `Hidden Pattern`

---

# 13. Qualifiers

Qualifiers modify or intensify previous instructions without needing entirely new glyphs.

- `Greater`
- `Lesser`
- `Precise`
- `Broad`
- `Gentle`
- `Violent`
- `Pure`
- `Turbulent`
- `Hidden`
- `Revealed`
- `Fixed`
- `Adaptive`

## Helper Qualifiers / Structural Helpers

- `Filter`
- `Conditional`

---

# 14. Forms Are Usually Emergent, Not Primitive

An important conclusion was that `Form` should not usually be a primitive category like “projectile,” “ward,” or “beam” that the player automatically knows.

Instead, forms should often emerge from instruction chains.

For example, a projectile-like effect may require:
- gathering
- shaping
- separating from self
- directing
- stabilizing
- release condition

If the caster fails to build that structure properly, the spell may:
- detonate in hand
- drift
- sputter
- launch badly
- collapse early

This is intentional and supports the learning curve.

Similarly, wards are usually not “ward glyphs.”
A ward-like effect emerges from things such as:

- anchor
- boundary
- field
- persist
- trigger logic
- reactive principle

That allows environmental magical anomalies to happen naturally.

---

# 15. Resolution Categories

These are not necessarily glyphs. They are categories of result the world may conclude a spell has become.

- projectile-like effect
- beam or ray
- burst or explosion
- lingering field
- ward or seal
- unlocking or opening
- healing or restoration
- corruption or decay
- construction or shaping
- information or communication effect

---

# 16. Domains

As the system expanded, especially into information magic, it became clear that practical implementation should likely use **resolution domains**.

## 16.1 Damage / Force Domain
Handles impact, heat, kinetic force, pushing / pulling, direct destructive release, explosions, and projectiles.

## 16.2 Structure Domain
Handles shaping matter, walls, bridges, buildings, hollow vs solid construction, support and reinforcement, and anchored forms.

## 16.3 Pattern Domain
Handles locks, seals, runes, bindings, wards, enchantments, dispel logic, and pattern reading / unbinding.

## 16.4 Information Domain
Handles sensing, attunement, tracing, triggering, transmitting, displaying, recording / imprinting, message relays, mirror links, and magical diagnostics.

## 16.5 Spatial Domain
Handles relation between points, placement, distance, directional logic, centered and boundary behavior, and movement through or across space.

## 16.6 Life Domain
Handles healing, life pattern interpretation, vitality, restoration, growth, and biological corruption or change.

---

# 17. Common Failure Modes

A major part of the system is that incomplete or crude spell chains do not simply “fail.”
They often resolve badly.

- structural failure
- positional failure
- directional failure
- persistence failure
- interpretive failure
- overload failure
- information failure

---

# 18. Minimal Structural Requirements

These are not hard rules yet, but they are useful guidance.

## 18.1 To affect the world at all
A spell usually needs at least:
- one `Principle`
- one `Operation`

## 18.2 To aim reliably
A spell usually needs:
- a `Reference`
- or a very clear spatial `Constraint`

## 18.3 To avoid self-harm
A spell usually needs:
- some positional distinction from `Self`
- often `Separate`, `Direct`, or equivalent logic

## 18.4 To persist
A spell usually needs:
- `Anchor` or `Bind`
- plus `Sustain` or `Persist`

## 18.5 To create a stable traveling effect
A spell usually needs:
- `Gather`
- `Shape`
- positional separation
- direction/path logic
- `Stabilize`
- a release or trigger condition

## 18.6 To create large structures
A spell usually needs:
- material or conceptual principles
- shape logic
- spatial references
- solid / hollow / boundary decisions
- anchoring
- stabilization
- sufficient mana
- likely iteration, segmentation, or multiple casters

---

# 19. Stress-Test Spell Examples

These are example constructions, not final canonical recipes.

## 19.1 Fireball
`Gather -> Fire -> Compress -> Shape -> Separate -> Forward -> Direct -> Stabilize -> On Impact -> Release`

## 19.2 Healing Spell
`Perception -> Life -> Life Pattern -> Refine -> Strengthen -> Stabilize`

## 19.3 Levitation
`Force -> Motion -> Target -> Above -> Sustain -> Stabilize`

## 19.4 Unlock
`Perception -> Order -> Binding -> Locking Pattern -> Separate -> Gentle`

## 19.5 Warded Area
`Anchor -> Boundary -> Field -> Persist -> Binding -> On Entry -> Disrupt`

## 19.6 Bridge
`Earth -> Shape -> Between -> Path -> Surface -> Above -> Anchor -> Strengthen -> Stabilize`

## 19.7 City Block / Constructive Ritual
`Earth -> Gather -> Shape -> Boundary -> Hollow -> Anchor -> Strengthen -> Order -> Precise -> Repeat`

## 19.8 Accidental Cursed Forest / Magical Scar
`Life -> Field -> Persist -> Binding -> Chaos`

## 19.9 Dispel Magic
`Perception -> Runic Pattern -> Binding -> Order -> Separate -> Terminate`

## 19.10 Alert Ward
`Anchor -> Boundary -> Field -> Persist -> Perception -> On Entry -> Attune -> Caster -> Bind -> Transmit`

---

# 20. Information Magic v1

Information magic includes:
- alert wards
- magical diagnostics
- message relays
- communication constructs
- linked mirrors
- remote sensing
- scrying
- magical surveillance
- runic networks

Core information-relevant principles:
- `Perception`
- `Light`
- `Sound`
- `Order`
- `Space`
- `Binding`

Core information-relevant operations:
- `Attune`
- `Trigger`
- `Trace`
- `Transmit`
- `Absorb`

Likely future additions:
- `Imprint`
- `Display`
- maybe `Receive`
- maybe `Transfer`

---

# 21. Message Magic

A major conclusion was that `Message` should probably **not** be a primitive glyph.

Instead, “message” should be an emergent outcome produced by information magic.

Possible forms include:
- pulse message
- spoken message relay
- conceptual message

---

# 22. Scrying and Mirrors

A better design is to treat scrying as **pattern capture and reconstruction** rather than literal camera simulation.

Possible chain ideas:
- `Perception -> Light -> Trace -> Attune -> Transmit -> Display`
- `Attune -> Space -> Perception -> Chosen Point -> Bind -> Transmit -> Display`

---

# 23. Helper vs Raw Glyphs

Some glyphs are:
- deep principles (`Fire`, `Life`, `Binding`, `Order`)
- direct operations (`Gather`, `Shape`, `Disrupt`)
- structural helpers (`Attune`, `Trigger`, `Transmit`, `Trace`)
- practical filters (`Filter`)
- contextual logic (`Conditional`)

This is okay so long as they remain:
- broad
- reusable
- composable
- not one-off spell buttons

---

# 24. Current Working Glyph List

## 24.1 Principles
- Fire
- Water
- Earth
- Air
- Lightning
- Sound
- Light
- Shadow
- Life
- Death
- Force
- Motion
- Binding
- Change
- Order
- Chaos
- Space
- Perception

## 24.2 Core Operations
- Gather
- Shape
- Bind
- Release
- Direct
- Anchor
- Sustain
- Stabilize
- Disrupt
- Separate
- Combine
- Transform
- Compress
- Expand
- Refine
- Weaken
- Strengthen
- Attune
- Repeat
- Delay
- Terminate

## 24.3 Newly Identified / Strong Candidates
- Trigger
- Transmit
- Trace
- Absorb

## 24.4 Tentative Future Operations
- Raise
- Carve
- Restore
- Unravel
- Transfer
- Imprint
- Display

## 24.5 Constraints
- Self
- Near
- Far
- Forward
- Above
- Below
- Centered
- Boundary
- Surface
- Hollow
- Solid
- Line
- Sphere
- Cone
- Ring
- Field
- Path
- Instant
- Persist
- Pulse
- Delayed
- Until Broken
- Gradual
- On Contact
- On Impact
- On Entry
- On Exit
- While Seen
- While Fed
- Conditional

## 24.6 References
- Caster
- Touched Target
- Seen Target
- Held Object
- Marked Target
- Chosen Point
- Current Space
- Path Ahead
- Nearest
- All Within
- Locking Pattern
- Binding Pattern
- Life Pattern
- Runic Pattern
- Material Pattern
- Boundary Pattern

## 24.7 Additional Reference Candidates
- Between
- Interior
- Exterior
- True Core
- Hidden Pattern

## 24.8 Qualifiers
- Greater
- Lesser
- Precise
- Broad
- Gentle
- Violent
- Pure
- Turbulent
- Hidden
- Revealed
- Fixed
- Adaptive

## 24.9 Helper-Style Logic
- Filter
- Conditional

---

# 25. Key Design Laws

## Law 1: Principles provide possibility
Without a principle, the spell lacks a conceptual or energetic basis.

## Law 2: Operations provide intent
Without operations, the spell has power but no instruction.

## Law 3: Constraints provide practical form
What players think of as forms are usually repeated combinations of constraints and operations.

## Law 4: References provide targeting
Without them, the world may resolve to defaults or ambiguity.

## Law 5: Stability must be earned
Shorter chains are not always weaker; they are often cruder.

## Law 6: Persistent magic is dangerous by default
Anything not explicitly ended, dissipated, or cleanly structured may linger.

## Law 7: Similar spells should share cores
This is good. It means the magical grammar is working.

## Law 8: Old magic must often be understood before it can be safely undone
This especially applies to dispel and ancient persistent effects.

## Law 9: Large-scale magic requires structure, not just mana
Massive rituals need precision, references, and coherence.

## Law 10: Information magic is its own serious complexity layer
It must be handled deliberately.

## Law 11: Spells care about provided mana, not just caster reserves
A spell should be evaluated based on how much mana is actually supplied into it, not merely how much mana the caster personally owns.

## Law 12: Persistent magic must continue to be fed or decay
Long-lived magic should usually weaken over time unless it stores, receives, or absorbs continuing mana.

---

# 26. Suggested Next Steps

1. refine the glyph list into “confirmed” vs “tentative”
2. define exact semantics for troublesome spatial concepts:
   - Between
   - Above
   - Surface
   - Boundary
   - Hollow
   - Solid
3. build a small test catalog of canonical spells
4. prototype the system in a Minecraft mod to pressure-test:
   - player readability
   - spell length tolerance
   - failure fun
   - persistent world effects
   - information magic complexity
5. later continue translating the design into implementation-facing docs

---

# 27. Closing Summary

The current design direction is strong.

It supports:
- deep composability
- broad magical grammar
- early-game danger and experimentation
- advanced compression and mastery
- environmental magical scars
- ritual-scale construction
- dispel as understanding, not just destruction
- information magic as a real subsystem
- practical implementation through domains rather than impossible raw simulation

The immediate plan to prototype this in a Minecraft mod is a good one.

It allows focus on:
- magic language
- player experience
- spell construction
- resolution rules
- failure states
- world interaction

without first needing to solve every problem that comes with a full standalone engine.

That makes the mod a testbed for the actual heart of the project: the magic system itself.
