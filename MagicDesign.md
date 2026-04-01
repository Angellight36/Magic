# MagicDesign

> This document translates the theory in `MagicSystem_Summary.md` into an implementation-oriented design reference.
>
> It is not meant to lock every system permanently, but to provide a practical architecture for prototyping the magic system in code.
>
> The immediate intended use is as a guide for building a prototype inside a Minecraft mod, where the focus is on validating:
>
> - glyph composition
> - spell resolution
> - mana cost and stability
- mana provision and mana sourcing
> - failure behavior
> - persistent magical effects
> - player-facing usability
>
> Later, these ideas can be adapted into a standalone game engine.

---

# 1. Implementation Goals

The code architecture should aim for the following:

- spells are built from **glyph chains**
- glyphs are **broad, reusable, and data-driven**
- specific effects like `Fireball`, `Unlock`, `Ward`, and `Dispel` usually emerge from **resolution logic**, not from one dedicated hardcoded spell class each
- the system supports:
  - short crude spells
  - long structured spells
  - persistent world effects
  - multi-stage constructs
  - information-based magic
- the system allows:
  - incomplete spells
  - underpowered spells
  - overpowered spells
  - unstable spells
  - ambiguity-driven spell resolution
  - environmental magical scars
- code complexity remains manageable through **resolution domains**
- mod-prototype architecture should remain portable enough to later influence a standalone implementation

---

# 2. Recommended High-Level Architecture

The system should be split into several major layers:

## 2.1 Content Layer
Defines:
- glyphs
- glyph metadata
- spell recipes / saved shorthand
- domain tags
- mana cost factors
- stability modifiers
- localization / display names
- progression unlocks

This layer should ideally be mostly data-driven.

## 2.2 Spell Construction Layer
Responsible for:
- building a glyph chain
- validating sequence structure
- storing player-made chains
- handling shorthand compression later
- tracking explicit and inferred spell semantics

This is the “authoring” side of magic.

## 2.3 Spell Interpretation Layer
Responsible for:
- reading the glyph chain
- grouping glyphs into semantic roles
- identifying principles, operations, constraints, references, qualifiers
- inferring missing or defaulted behavior
- detecting ambiguity
- preparing a resolved spell plan
- recognizing mana-feed structures such as absorb or sustain logic

This is where a spell becomes a structured internal model rather than just a raw list.

## 2.4 Resolution Layer
Responsible for:
- resolving the interpreted spell through one or more domains
- deciding the outcome category
- calculating mana cost, stability, and failure risk
- producing actual gameplay results

This is where “what the spell does” is decided.

## 2.5 Execution Layer
Responsible for:
- spawning projectiles
- creating persistent fields
- applying healing
- shaping terrain or blocks
- attaching ward logic to locations
- transmitting information signals
- creating or ending persistent effects
- draining, storing, or feeding mana into ongoing constructs

This is the gameplay-action side.

## 2.6 Persistence Layer
Responsible for:
- saving spell constructs in the world
- storing anchored fields
- loading old wards or scars
- serializing attunements, triggers, timers, receivers, and runic structures

This is especially important because the design explicitly allows magic to remain in the world.

---

# 3. Core Data Model

The following types are recommended as the foundation.

## 3.1 GlyphCategory
An enum or equivalent identifying the category of a glyph.

Suggested values:
- PRINCIPLE
- OPERATION
- CONSTRAINT
- REFERENCE
- QUALIFIER
- HELPER

`HELPER` can be used for things like `Filter`, `Trigger`, or later structural glyphs if needed.

## 3.2 GlyphDefinition
Represents a single glyph type.

Suggested fields:
- `id`
- `displayName`
- `category`
- `description`
- `domainHints`
- `manaWeight`
- `stabilityWeight`
- `tags`
- `requiresUnlock`
- `isHelper`
- `isPrimitive`
- `notes`

Example conceptual fields:
- `id = "fire"`
- `category = PRINCIPLE`
- `domainHints = [DAMAGE, STRUCTURE]`
- `manaWeight = 4`
- `stabilityWeight = 2`

## 3.3 GlyphInstance
Represents a glyph used in a specific spell chain.

Suggested fields:
- `definitionId`
- `positionInChain`
- `optionalParameters`
- `source` (player cast, item, rune, ritual anchor, etc.)

This allows future parameterization.

## 3.4 SpellChain
The raw ordered list of glyphs used in a cast.

Suggested fields:
- `glyphs`
- `casterId`
- `manaCommitted`
- `manaSources`
- `manaProvided`
- `castContext`
- `spellLength`
- `shorthandSourceId` (nullable)
- `creationMethod` (manual, shorthand, ritual, construct, scroll, etc.)

## 3.5 InterpretedSpell
The semantic version of the chain after parsing.

Suggested fields:
- `principles`
- `operations`
- `constraints`
- `references`
- `qualifiers`
- `helperLogic`
- `domainCandidates`
- `resolvedIntentHints`
- `ambiguities`
- `missingStructureWarnings`

This is a key type.

## 3.6 SpellResolutionPlan
The final structured result produced before execution.

Suggested fields:
- `primaryDomain`
- `secondaryDomains`
- `resolvedOutcomeType`
- `manaCost`
- `manaProvided`
- `manaDelta`
- `stabilityScore`
- `failureRisk`
- `overpowerRisk`
- `underpowerRisk`
- `executionSteps`
- `persistentComponents`
- `triggerDefinitions`
- `attunementLinks`
- `transmissionModel`
- `cleanupRules`

## 3.7 CastResult
Returned when a spell is resolved and executed.

Suggested fields:
- `successLevel`
- `actualOutcomeType`
- `manaSpent`
- `stabilityResult`
- `failuresTriggered`
- `spawnedEffects`
- `persistentEffectIds`
- `messagesToPlayer`

---

# 4. Resolution Domains

One of the most important conclusions was that the engine should not try to resolve every spell as raw one-off logic.

Instead, spells should be interpreted, then handed off to one or more **resolution domains**.

## 4.1 Damage / Force Domain
Handles:
- direct force
- impact
- explosions
- projectiles
- heat
- kinetic motion
- destructive release

Useful for:
- fireball
- lightning burst
- push wave
- force bolt

## 4.2 Structure Domain
Handles:
- shaping terrain
- block construction
- bridges
- barriers
- walls
- buildings
- hollow / solid logic
- support and reinforcement

Useful for:
- bridge
- wall
- room creation
- city-shaping rituals

## 4.3 Pattern Domain
Handles:
- locks
- bindings
- runes
- wards
- enchantments
- dispel logic
- anchored magical patterns
- old magical scars

Useful for:
- unlock
- dispel
- seal creation
- ward breakdown

## 4.4 Information Domain
Handles:
- sensing
- attunement
- tracing
- transmitting
- trigger logic
- pattern recording
- display logic
- magical diagnostics

Useful for:
- alert wards
- message relays
- linked mirrors
- remote sensing
- scrying

## 4.5 Spatial Domain
Handles:
- point selection
- relative positioning
- directional resolution
- boundary placement
- spanning logic
- area relation
- centered vs offset construction

Useful for:
- projectile direction
- bridge span
- area spell placement
- field anchoring

## 4.6 Life Domain
Handles:
- healing
- vitality
- regeneration
- biological pattern interpretation
- corruption of living things
- growth

Useful for:
- healing
- regeneration
- living curses
- plant manipulation

---

# 5. Recommended Resolution Flow

The following pipeline should work well as a first implementation pass.

## Step 1: Build Raw SpellChain
Collect glyphs in order.

## Step 2: Categorize Glyphs
Map each glyph to its category and metadata.

## Step 3: Interpret Chain
Extract:
- principles
- operations
- constraints
- references
- qualifiers
- helper logic

Identify:
- repeated patterns
- missing expected pieces
- conflicting instructions
- likely domain candidates

## Step 4: Infer Outcome Shape
Determine whether the spell most resembles:
- traveling effect
- field
- structure
- dispel
- information link
- healing effect
- etc.

This should not be fully hardcoded per spell name.  
It should be pattern-driven.

## Step 5: Compute Mana Cost
Calculate based on:
- glyph count
- glyph weights
- domain complexity
- persistence
- attunement links
- target count
- structure size
- unresolved ambiguity
- distance or scale

## Step 5.5: Evaluate Mana Provision
Compare required mana against actually provided mana.

Determine:
- whether the spell is fully fed
- whether it is underpowered
- whether it is overpowered
- whether supplied mana sources are compatible with the spell
- whether ambient-fed sustain logic is present

## Step 6: Compute Stability
Calculate based on:
- completeness
- complexity
- caster skill
- shorthand familiarity
- mana sufficiency
- domain difficulty
- contradictory logic
- unstable glyph combinations

## Step 7: Select Failure Risk
If stability is too low or mana insufficient:
- assign one or more likely failure modes
- determine whether the spell partially resolves, misfires, drifts, or persists incorrectly

## Step 8: Produce SpellResolutionPlan
Create a concrete plan the game can execute.

## Step 9: Execute
Spawn entities, modify blocks, create effects, register persistent constructs, or send magical signals.

## Step 10: Persist if Needed
Store anchored spells, scars, wards, runes, or network links in world data.

---

# 6. Mana Model

The mana model should support both gameplay and system logic.

## 6.1 Mana Sources
Suggested support for:
- internal mana
- ambient mana
- bound mana
- item-provided mana
- ritual pooled mana
- siphoned mana
- construct-fed mana

The important implementation rule is that a spell should care primarily about how much mana is **provided to the spell**, not merely how much mana the caster personally owns.

## 6.2 Mana Cost Factors
Mana should scale with:
- number of glyphs
- glyph difficulty
- number of principles
- persistence
- area or size
- target count
- attunement complexity
- transmission distance
- information density
- structural precision
- unresolved ambiguity
- world resistance (optional future feature)

## 6.3 Mana Provision Outcomes

### Underpowered Spell Outcomes
If less mana is supplied than the spell expects:
- spell fails cleanly
- spell resolves partially
- spell weakens early
- spell collapses before completing
- persistent effect forms but decays rapidly

### Overpowered Spell Outcomes
If substantially more mana is supplied than the structure safely handles:
- spell becomes unstable
- effect widens beyond intended limits
- structure overbuilds or distorts
- projectile ruptures early
- ritual collapses violently

### Mana Source Mismatch
Even if enough raw mana exists, a spell may still behave poorly if:
- the mana source is incompatible
- the supplied mana is too chaotic
- the spell expected a sustaining feed but only received a burst
- excess mana is not properly absorbed or stored

## 6.4 Persistent Spell Fuel
Persistent magic should continue consuming, storing, or drawing mana over time.

This means persistent effects should track:
- initial stored mana
- passive decay
- optional ambient absorption
- optional linked external source
- starvation threshold

A persistent spell without an ongoing feed should eventually weaken unless it was massively overcharged or built with a very large reserve.

---

# 7. Stability Model

Stability should be its own tracked concept and not merely another word for mana.

Mana answers:
- can enough energy be supplied?

Stability answers:
- can the spell hold together coherently?

## 7.1 Stability Inputs
Suggested factors:
- glyph count
- chain completeness
- structural coherence
- contradictory constraints
- caster familiarity
- domain difficulty
- old shorthand mastery
- attunement accuracy
- environmental interference
- multi-caster coordination quality
- underpower or overpower ratio
- quality of mana-source matching

## 7.2 Stability Outcomes
Possible result bands:
- stable
- rough but functional
- unstable but successful
- partial miscast
- dangerous misfire
- catastrophic collapse
- persistent scar

---

# 8. Failure System

The design depends heavily on failure being interesting rather than binary.

## 8.1 Recommended Failure Types
- structural failure
- positional failure
- directional failure
- persistence failure
- interpretive failure
- overload failure
- information failure
- starvation failure
- overload by overfeeding

## 8.2 Failure Representation
Consider a type like `SpellFailureProfile` with:
- `failureType`
- `severity`
- `domain`
- `description`
- `gameplayOutcome`

## 8.3 Failure Selection
Failure should be influenced by:
- missing glyph categories
- low stability
- mana deficit
- ambiguous references
- domain mismatch
- badly handled persistence
- poor attunement
- unresolved trigger logic

---

# 9. Persistent Effects Model

A major system requirement is that spells may remain in the world.

## 9.1 PersistentEffect
Suggested fields:
- `id`
- `originSpellId`
- `location`
- `radiusOrBounds`
- `domain`
- `effectType`
- `boundPrinciples`
- `triggerLogic`
- `attunedTargets`
- `ownerId`
- `startTime`
- `durationMode`
- `storedMana`
- `decayRate`
- `absorbProfile`
- `feedSources`
- `terminationConditions`
- `stabilityState`
- `serializedState`

## 9.2 Persistent Effect Categories
Examples:
- anchored field
- ward boundary
- environmental hazard
- magic scar
- communication node
- scrying anchor
- ritual construct
- linked receiver
- attuned transmitter
- block-shaping construct

## 9.3 World Save Integration
For a Minecraft mod prototype, persistent effects should be serializable in a way that survives:
- world save/load
- chunk unload/load
- player logout
- long-duration persistence

This matters a lot if ancient spell remains are part of the design.

---

# 10. Suggested Glyph Registry Structure

The glyph system should be content-driven.

A registry entry can look conceptually like:

```json
{
  "id": "fire",
  "displayName": "Fire",
  "category": "PRINCIPLE",
  "domainHints": ["DAMAGE", "STRUCTURE"],
  "manaWeight": 4,
  "stabilityWeight": 2,
  "tags": ["heat", "combustion", "energy"],
  "requiresUnlock": true,
  "isHelper": false,
  "isPrimitive": true
}
```

That does not require JSON specifically, but the structure should be similar.

This makes future expansion easier and avoids hardcoding everything directly in classes.

---

# 11. Confirmed vs Tentative Glyph Strategy

To prevent uncontrolled system growth, glyphs should be tracked as:

## 11.1 Confirmed
Already justified and expected in first implementation.

Examples:
- Fire
- Water
- Earth
- Air
- Lightning
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

## 11.2 Strong Candidates
Justified by stress tests and probably needed.

Examples:
- Trigger
- Transmit
- Trace
- Absorb
- Sound
- Between

## 11.3 Tentative
Useful, but should be added only when pressure-tested further.

Examples:
- Raise
- Carve
- Restore
- Unravel
- Transfer
- Imprint
- Display
- Interior
- Exterior
- True Core
- Hidden Pattern
- Filter

This split will help keep the mod prototype focused.

---

# 12. Spell Interpretation Strategy

A purely sequential left-to-right interpretation may be too naive later, but it is a fine starting point.

## 12.1 Recommended First Version
Interpret the chain as:
- collect principles
- collect operations
- collect constraints
- collect references
- collect qualifiers
- identify trigger / helper logic
- then run pattern rules over the chain

This gives a practical first parser.

## 12.2 Later Possible Upgrade
Move to a richer internal structure such as:
- grouped segments
- clause-like subsequences
- nested constructs
- linked subchains for rituals

This would support city-scale or multi-function spells better later.

For the prototype, do not overcomplicate too soon.

---

# 13. Outcome Classification

The engine should classify interpreted spells into broad outcome families.

Suggested early categories:
- DIRECT_RELEASE
- TRAVELING_EFFECT
- AREA_FIELD
- BOUNDARY_WARD
- PATTERN_INTERACTION
- HEALING_EFFECT
- CONSTRUCTION_EFFECT
- DISPel_EFFECT
- INFORMATION_LINK
- RITUAL_CONSTRUCT
- UNKNOWN_UNSTABLE

These are not player-facing spell names.  
They are internal resolution classes.

---

# 14. Example Mapping: Fireball

## Raw Chain
`Gather -> Fire -> Compress -> Shape -> Separate -> Forward -> Direct -> Stabilize -> On Impact -> Release`

## Interpreted Structure
- principles: Fire
- operations: Gather, Compress, Shape, Separate, Direct, Stabilize, Release
- constraints: Forward, On Impact
- domain candidates: Damage, Spatial
- inferred outcome: Traveling effect with impact release

## Resolution Result
- outcome type: TRAVELING_EFFECT
- primary domain: DAMAGE
- secondary domain: SPATIAL
- execution:
  - spawn projectile entity
  - apply fire payload
  - detonate on impact

---

# 15. Example Mapping: Unlock

## Raw Chain
`Perception -> Order -> Binding -> Locking Pattern -> Separate -> Gentle`

## Interpreted Structure
- principles: Perception, Order, Binding
- operations: Separate
- references: Locking Pattern
- qualifiers: Gentle
- domain candidates: Pattern
- inferred outcome: Pattern interaction focused on opening without damage

## Resolution Result
- outcome type: PATTERN_INTERACTION
- primary domain: PATTERN
- execution:
  - inspect target lock state
  - identify openable binding
  - reduce or remove locked state
  - avoid destructive block damage if possible

---

# 16. Example Mapping: Alert Ward

## Raw Chain
`Anchor -> Boundary -> Field -> Persist -> Perception -> On Entry -> Attune -> Caster -> Bind -> Transmit`

## Interpreted Structure
- principles: Perception
- operations: Anchor, Persist, Attune, Bind, Transmit
- constraints: Boundary, Field, On Entry
- references: Caster
- domain candidates: Information, Pattern, Spatial
- inferred outcome: Persistent perceptive field that signals a linked receiver when crossed

## Resolution Result
- outcome type: INFORMATION_LINK
- primary domain: INFORMATION
- secondary domains: SPATIAL, PATTERN
- execution:
  - create persistent region
  - register trigger
  - bind receiver
  - send signal on entry event

---

# 17. Prototype Feature Recommendation for Minecraft Mod

To avoid overbuilding too early, the first prototype should support a constrained but meaningful subset.

## 17.1 Recommended Initial Feature Set
- raw glyph chains
- mana cost
- mana provision
- stability
- miscasts
- 3 to 5 core domains
- simple projectile spells
- simple pattern spells
- simple persistent fields
- basic dispel
- basic alert ward
- basic construction spell
- world persistence for anchored effects

## 17.2 Recommended Initial Glyph Set
Principles:
- Fire
- Earth
- Force
- Binding
- Order
- Life
- Space
- Perception

Operations:
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
- Refine
- Strengthen
- Attune
- Terminate

Constraints / refs:
- Self
- Forward
- Boundary
- Field
- Path
- Surface
- On Impact
- On Entry
- Chosen Point
- Caster
- Seen Target
- Locking Pattern
- Life Pattern

Strong candidate additions:
- Trigger
- Transmit
- Absorb
- Between

That is enough to test the real system without drowning in content.

---

# 18. Minecraft-Specific Implementation Notes

Because this is being prototyped as a mod, some practical adjustments help.

## 18.1 Favor Data-Driven Glyph Definitions
Avoid burying glyph meaning only in hardcoded switch statements.

## 18.2 Use Internal Outcome Classes
Even if player spell construction stays flexible, actual execution should map to controlled internal behaviors.

## 18.3 Separate World Effects from Cast Parsing
Do not mix:
- parsing glyph logic
- block manipulation
- entity spawning
- persistence storage

Keep these distinct.

## 18.4 Start with Limited Persistent Effect Types
Examples:
- anchored field
- alert ward
- simple flame scar
- block-shaped structure

You can expand later.

## 18.5 Make Debugging Visible
For prototype quality, it may help to expose:
- interpreted spell summary
- mana estimate
- stability estimate
- likely failure warnings
- resolved outcome type

This will massively help iteration.

---

# 19. Suggested Class / Interface Sketch

This is only conceptual, not final code.

## Core Types
- `GlyphDefinition`
- `GlyphRegistry`
- `GlyphInstance`
- `SpellChain`
- `SpellInterpreter`
- `InterpretedSpell`
- `SpellResolver`
- `SpellResolutionPlan`
- `SpellExecutor`
- `PersistentEffect`
- `PersistentEffectManager`
- `ManaProfile`
- `ManaContribution`
- `ManaFeedProfile`
- `StabilityCalculator`
- `FailureResolver`

## Domain Types
- `ResolutionDomain`
- `DamageDomainResolver`
- `StructureDomainResolver`
- `PatternDomainResolver`
- `InformationDomainResolver`
- `SpatialDomainResolver`
- `LifeDomainResolver`

## Helper Types
- `AttunementLink`
- `TriggerDefinition`
- `SpellFailureProfile`
- `SpellCastContext`
- `ResolvedOutcomeType`

The exact names can change later.

---

# 20. Suggested Data Flow Example

Player casts spell:

1. build `SpellChain`
2. pass chain into `SpellInterpreter`
3. produce `InterpretedSpell`
4. pass interpreted spell into `SpellResolver`
5. resolver picks outcome type and domain(s)
6. calculate mana and stability
7. assign failure behavior if needed
8. produce `SpellResolutionPlan`
9. `SpellExecutor` applies effects to world
10. persistent components saved through `PersistentEffectManager`

This gives a clean separation of responsibilities.

---

# 21. Important Design Warnings

## 21.1 Do Not Hardcode Every Named Spell
Doing so would defeat the purpose of the system.

## 21.2 Do Not Let Every New Need Create a Hyper-Specific Glyph
Always ask:
- is this broad?
- is this reusable?
- is this structural rather than a shortcut?

## 21.3 Do Not Overbuild Grammar Too Early
The mod prototype should prove:
- whether players enjoy long-form casting
- whether miscasts are fun
- whether persistent effects are understandable
- whether information magic is worth the added complexity

## 21.4 Do Not Simulate Everything Literally
Especially for information magic, prefer pattern-domain logic over full physical realism.

---

# 22. What This Document Should Guide

This file should help future implementation work answer questions like:

- how do we represent a glyph?
- how do we interpret a chain?
- how do we calculate mana cost?
- how do we decide when a spell becomes a projectile vs field vs dispel?
- how do persistent wards live in the world?
- how do we keep the system flexible without making the code a mess?
- how do we prototype in a mod while preserving long-term design value?

---

# 23. Final Summary

A few new implementation truths should remain explicit:

- spell logic should track mana **provided**, not just mana owned by the caster
- persistent effects should usually decay unless they store or absorb continued mana
- ambient-fed spells should have a real sustaining structure that can be targeted and broken
- overpowering a spell should be as mechanically meaningful as underpowering it


The implementation direction should preserve the same core truth as the design document:

**Magic is not a list of finished spells. It is a composable language that must be interpreted, resolved, and executed through structured systems.**

To make this practical in code:

- glyphs should be data-driven
- spells should be parsed into meaning
- meaning should be resolved through domains
- execution should be separate from interpretation
- persistence should be a first-class system
- failure should be expressive, not binary
- information magic should be treated as a serious subsystem, not a hand-waved afterthought

For the Minecraft mod prototype, the best path is to build a focused slice of this architecture, prove that the magic language is fun and understandable, and let those lessons shape the later standalone game.
