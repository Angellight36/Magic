# Magic System Design Notes

> This document summarizes the current theory and design direction of the magic system.  
> It is intended as a large-form reference for worldbuilding, prototyping, and future implementation work.
>
> The immediate goal is to use this as a design foundation while prototyping the system in a Minecraft mod, so the magic language and player experience can be stress-tested without needing to first build an entire standalone game engine.
>
> A later implementation-focused document such as `MagicDesign.md` can translate these ideas into code architecture, data structures, resolution systems, and content pipelines.

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

# 3. General Theory of Magic

Magic, in essence, is the act of using the energy within the world—known as `Mana`—to manipulate reality through structured intent.

This manipulation can manifest in many forms, including but not limited to:

- `Spells` — direct, immediate magical expressions
- `Enchantments` — persistent changes bound to objects or beings
- `Runic Arrays` — structured magical systems that automate or stabilize effects
- `Wards` — usually persistent reactive fields or boundaries
- `Potions` — magic condensed into physical mixtures
- `Rituals` — long-form multi-step magical constructions
- `Constructs` — shaped magical or material outcomes bound into stable function

Magic is not necessarily creation from true nothingness. More often, it is:

- gathering
- transforming
- shaping
- binding
- refining
- moving
- attuning
- disrupting
- sustaining

In most cases, magic works by altering what already exists or by reorganizing mana and matter into a new form.

---

# 4. Mana

`Mana` is the fundamental magical energy that permeates all things.

It exists in multiple useful states:

## 4.1 Ambient Mana
The raw, untamed mana that exists naturally in the world. It flows through the air, the earth, and the unseen layers between.

- exists naturally in environments
- may vary by biome, structure, or magical history
- can be drawn upon by skilled casters
- is often difficult to control directly
- may be unstable or difficult to control in high concentrations
- should usually be treated as a powerful chaotic force

Though ambient mana is often difficult to control directly, some practitioners—especially `Druids` and others with natural affinity—can harmonize with it more easily than most. Others may harness it through `Runes`, `Wards`, or larger magical constructs that tap into the mana already present in the world.

Ambient mana is also important for persistent magic. A long-lived spell that does not have access to continuing mana input should usually weaken over time. Truly ancient or durable magical effects generally require either:
- a large bound reserve of mana
- an attached mana source
- or a component that allows the spell to draw from ambient mana over time

## 4.2 Internal Mana
Mana stored within a living being.

Most creatures in the world possess some internal store of magical potential. Some have larger stores than others and are more properly considered `Magical Beings`, while those capable of actively shaping and manipulating these stores are often called `Magic Users`.

Internal mana varies heavily between individuals:
- many magical beings regenerate mana over time
- some can cast repeatedly with only temporary limits
- some have stores of mana but regenerate so slowly that their lifetime spell use is effectively finite
- some can replenish themselves by siphoning mana from other beings or by drawing in ambient mana
- some can have their internal mana blocked, bound, or redirected by outside forces

This means that “having mana” and “being able to keep casting” are not always the same thing.

## 4.3 Bound Mana
Mana held inside an object, structure, system, construct, or living vessel.

Bound magical potential is often found in:
- enchanted items
- runes
- potions
- wards
- batteries, vessels, or prepared reagents
- curses and blessings placed upon living beings

Bound mana is often more predictable than other forms because it usually has some degree of `intent` already impressed upon it.

It can:
- power magical effects over time
- make complex spells more stable
- be reused or depleted depending on design
- remain available after the original caster is gone
- be used to isolate, suppress, or redirect mana inside living beings

It is possible to bind mana within a living being, either as a curse or a blessing. It is also possible to temporarily block the flow of mana through a body by binding that mana and preventing it from circulating properly.

### 4.3.1 Binding Mana
The act of binding mana generally requires two things:

1. Mana that will be bound  
   Once the binding process is complete, it is usually difficult to add more mana into the bound structure.

2. Mana to act as the binding agent  
   This second mana is used to isolate, contain, suppress, or shape the subject mana.

A common model is to wrap the subject mana in a coating or shell of mana carrying a strong isolating intent. This shell prevents that mana from freely mixing with other nearby sources.

Wrapping the subject mana in a blanket of mana is probably the most common method, but it is not the only one. Other methods may include:
- overwhelming the target mana with suppressive binding mana
- layering multiple shells
- forcing mana into runic channels
- smothering unstable mana into submission through superior structure

## 4.4 Mana Provision Matters More Than Mana Ownership
A very important design principle is that spells should care less about whether the **caster** has enough mana and more about whether **enough mana has been provided to the spell**.

This supports several important outcomes:
- a weak caster can contribute to a large ritual
- multiple casters can pool power
- prepared objects can provide stored mana
- environmental constructs can draw in mana over time
- a spell can be underpowered or overpowered depending on what is fed into it

This also means spell resolution should care about:
- how much mana was required
- how much mana was actually supplied
- where that mana came from
- whether the supplied mana matches the spell structure
- whether excess mana destabilizes the spell

### 4.4.1 Underpowered Spells
A spell that receives too little mana may:
- fail to resolve
- resolve only partially
- collapse early
- misfire
- produce a weaker or incomplete form

### 4.4.2 Overpowered Spells
A spell that receives too much mana may:
- become unstable
- widen beyond intended limits
- rupture into an explosion
- overbuild or distort structures
- burn out its own pattern and collapse violently

Overpowering a spell should therefore be a meaningful phenomenon rather than automatically a benefit.

## 4.5 Persistent Magic Still Needs Fuel
Persistent magic should still require mana to continue existing.

As a result:
- most magical scars left in the environment should be relatively weak unless they have a way to feed themselves
- abandoned spells without incoming mana should slowly decay
- ancient or powerful persistent effects should usually have a visible or hidden reason they are still active

One important possibility is a component that allows a spell to draw in ambient mana and store or consume it over time. Such a component would let a persistent spell sustain itself beyond the original casting.

This also creates another way to dispel or weaken magic:
- identify the sustaining structure
- perceive the ambient-drain or absorb mechanism
- disrupt or remove that part of the spell
- allow the rest of the magic to starve and die over time

A conceptual example might look like:
`Perception -> Absorb Component -> Dispel`

The exact final glyph naming is still flexible, but the design idea is important: some spells survive because they continue feeding themselves.


---

# 5. Magic Beings and Magic Users

While all beings contain mana, only some can actively manipulate it.

Those with meaningful internal stores, unusual affinity, or naturally significant magical presence may broadly be considered `Magical Beings`. Those who can consciously shape mana are more specifically referred to as `Magic Users`, though their methods differ greatly.

Possible archetypes include:

- `Mages` — scholars of structured spellcraft who rely on study, precision, and deliberate casting
- `Warlocks` — users of pact, borrowing, or external contracts with greater beings or powers
- `Druids` — practitioners who work with natural magical flows and living systems rather than dominating them
- `Artificers` — crafters of magical constructs, runes, and devices
- `Ritualists` — specialists in long-form and collaborative magical architecture
- `Seers` — specialists in perception, omen, and information magic

These classifications are cultural and functional, not absolute. A practitioner may blend disciplines or develop entirely new approaches.

Historical and legendary examples could include:
- an arch-mage who held a contract with a god while still being strong enough to perform city-leveling magic without invoking that contract
- a figure such as Grudhill the Unknown, who allegedly tricked a deity into allowing invocation without a formal contract
- an ancient being such as Quinlog the Immortal, who siphons power directly from external realms without the resident powers of those realms being able to stop it

These examples reinforce an important truth of the setting: categories describe tendencies, not limits.


---

# 6. Spells as Magical Sentences

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

# 7. Early, Mid, and Late Game Spellcasting

## 7.1 Early Game
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

## 7.2 Mid Game
Casters begin to recognize repeated structures.

They may:
- learn more efficient spell chains
- specialize in particular domains
- bind repeated logic into runes or prepared foci
- reduce error through better understanding

## 7.3 Late Game
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

# 8. Why Many Spells Should Look Similar

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

# 9. Why Specific Spell Results Should Usually Be Emergent

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

# 10. Persistent Magic and World Scars

One of the strongest outcomes of the design is the idea that magic can leave lasting marks on the world.

A failed or unfinished spell does not always vanish.  
It may:

- persist as a field
- root itself into a place
- bind into the terrain
- continue partially executing
- distort living and nonliving things nearby
- become an environmental anomaly

This creates excellent worldbuilding possibilities:

- haunted forests created by failed experiments
- ruins that are really unfinished spells
- cursed battlefields containing lingering field logic
- ancient communication wards still whispering broken messages
- strange biomes formed by collapsed rituals
- dungeons that are partially stabilized magical architecture

This idea makes the world feel alive with magical history.

---

# 11. Spell Categories as Resolution Outcomes

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

# 12. Glyph Taxonomy v1

The current taxonomy organizes glyphs into several major categories:

- Principles
- Operations
- Constraints
- References
- Qualifiers

These categories do not all represent the same kind of thing.  
Some are primal magical forces, while others are structural or informational helpers. That is acceptable so long as they remain broad, reusable, and not mere one-off spell shortcuts.

---

# 13. Principles

Principles are the fundamental magical concepts, forces, or metaphysical substances being manipulated.

## 13.1 Elemental / Material Principles

### `Fire`
Heat, combustion, passion, rapid energy release.

Common tendencies:
- ignition
- heating
- expansion
- destruction
- purification by burning

### `Water`
Fluidity, flow, dissolution, adaptability, pressure.

Common tendencies:
- cooling
- movement
- erosion
- carrying
- softening
- infusion

### `Earth`
Mass, stone, solidity, structure, resistance.

Common tendencies:
- shaping matter
- reinforcement
- barriers
- compression
- stability
- weight

### `Air`
Breath, wind, pressure gradients, motion through space.

Common tendencies:
- pushing
- carrying sound
- lift
- dispersal
- speed
- cutting currents

### `Lightning`
Discharge, impulse, transmission, sudden release.

Common tendencies:
- shock
- arc behavior
- signaling
- energizing
- overload
- rapid transfer

### `Sound` *(added during information-magic discussion)*
Vibration, resonance, tone, pressure pattern, voice, acoustic structure.

Common tendencies:
- carrying spoken pattern
- resonance
- signaling
- auditory impression
- vibration-based sensing

## 13.2 Conceptual / Arcane Principles

### `Light`
Revelation, clarity, purity, illumination, visible pattern.

Common tendencies:
- exposure
- visibility
- guidance
- cleansing
- visual projection
- representational display

### `Shadow`
Obscurity, concealment, dampening, unseen movement, inversion.

Common tendencies:
- hiding
- suppression
- confusion
- quiet influence
- distortion
- stealth

### `Life`
Growth, healing, vitality, adaptation, living complexity.

Common tendencies:
- restoration
- growth
- regeneration
- adaptive response
- organic pattern reinforcement

### `Death`
Ending, decay, stillness, severance, entropy of the living.

Common tendencies:
- withering
- rot
- silence
- severing life-patterns
- weakening vitality

### `Force`
Pressure, impact, acceleration, blunt transfer of power.

Common tendencies:
- striking
- pushing
- pulling
- crushing
- launching
- concussive effect

### `Motion`
Movement, direction, travel, momentum, flow through space.

Common tendencies:
- shifting
- carrying
- redirection
- pursuit
- evasion
- rotation

### `Binding`
Connection, restraint, attachment, sealing, tethering, held relationships.

Common tendencies:
- locking
- sealing
- warding
- tethering
- linking
- fastening

### `Change`
Transformation, transition, reshaping, conversion of state.

Common tendencies:
- transmutation
- adaptation
- mutation
- refinement
- material conversion
- state shifting

### `Order`
Pattern, structure, logic, alignment, stability.

Common tendencies:
- sorting
- organizing
- preserving
- reinforcing systems
- reducing ambiguity
- strengthening pattern

### `Chaos`
Instability, unpredictability, rupture, divergence, wild spread.

Common tendencies:
- mutation
- volatility
- random behavior
- unstable amplification
- disordered spread

### `Space`
Distance, position, arrangement, relation in place.

Common tendencies:
- relocation
- placement
- area shaping
- distance manipulation
- relation between points

### `Perception`
The principle of magical sensing, interpretation, and awareness of patterns, states, changes, structures, presences, and signatures.

Common tendencies:
- detect presence
- identify structure
- notice change
- read magical signatures
- observe motion
- reveal hidden forms
- sense intrusion
- interpret target patterns

This became especially important for:
- unlocking
- healing
- dispelling
- ward detection
- tracking
- scrying
- magical diagnostics

---

# 14. Operations

Operations are verbs. They define what the caster is trying to do with principles, references, and structure.

## 14.1 Core Operations

### `Gather`
Pull mana or a principle into concentration.

### `Shape`
Give structure or geometry to gathered magic, matter, or pattern.

### `Bind`
Attach, hold, fasten, link, or maintain connection.

### `Release`
Let accumulated magical structure act or discharge.

### `Direct`
Give heading, orientation, or intended path.

### `Anchor`
Tie an effect to a place, pattern, surface, or structure.

### `Sustain`
Keep the effect active over time.

### `Stabilize`
Reduce drift, mutation, backlash, collapse, or unwanted variance.

### `Disrupt`
Break pattern, interrupt continuity, or destabilize a target.

### `Separate`
Split apart held things, divide states, or break union cleanly.

### `Combine`
Merge principles, structures, or effects.

### `Transform`
Convert one state, material, pattern, or expression into another.

### `Compress`
Condense energy, matter, or magical structure.

### `Expand`
Spread outward, enlarge area, or disperse structure.

### `Refine`
Make more precise, exact, filtered, or clean.

### `Weaken`
Reduce integrity, resistance, or intensity.

### `Strengthen`
Increase integrity, resistance, or intensity.

### `Attune`
Align a spell, construct, signal, pattern, object, place, person, or receiver to a specific magical signature or resonance.

This became one of the most important helper operations for:
- communication wards
- linked constructs
- keyed runes
- security magic
- remote viewing
- paired receivers
- ally-safe systems

### `Repeat`
Cause cyclical, pulsed, or iterative behavior.

### `Delay`
Postpone activation or stage transition.

### `Terminate`
Explicitly end an effect or ensure clean dissipation.

## 14.2 New or Likely Needed Operations Identified by Stress Tests

These emerged naturally during testing and likely belong in the system.

### `Trigger`
Define that a spell or construct activates only when a specified event or pattern occurs.

This helps generalize things like:
- on entry
- on impact
- on contact
- on sound
- on break
- on sight

### `Transmit`
Move a signal, pattern, impression, or magical message from one place or construct to another.

This is broader and better than a one-off “message” glyph.

Potential uses:
- alert wards
- linked mirrors
- message stones
- ritual synchronization
- magical networks

### `Trace`
Follow an existing pattern, path, signal, signature, or movement.

Potential uses:
- advanced alert wards
- tracking
- targeted perception
- remote observation
- signal following

### `Absorb`
Pull mana, energy, or magical potential into a spell, construct, reservoir, or sustaining pattern.

Potential uses:
- ambient-fed persistent effects
- mana siphoning
- recharging structures
- long-lived wards
- gradual self-sustaining scars

## 14.3 Proposed or Tentative Operations for Later Consideration

These were identified as likely useful, but not yet fully confirmed.

### `Raise`
Vertical constructive movement, especially useful in terrain and building logic.

### `Carve`
Subtractive shaping rather than additive shaping.

### `Restore`
May be a more explicit healing / repair operation than `Strengthen` in some contexts.

### `Unravel`
May eventually be useful as a safer or more precise dispel operation for old persistent magic.

### `Transfer`
Move mana, state, or held structure from one place to another.

### `Imprint`
Store a pattern in an object, rune, medium, mirror, stone, or surface.

Likely useful for:
- recorded messages
- magical memory
- prepared spell logic

### `Display`
Render stored or transmitted information into visible, audible, or interpretable output.

Likely useful for:
- mirror displays
- visible warnings
- projected symbols
- magical screens

---

# 15. Constraints

Constraints define how a spell behaves in practice.  
This is where much of what players think of as “form” truly comes from.

## 15.1 Spatial Constraints

### `Self`
Centered on the caster.

### `Near`
Close to the caster or point of origin.

### `Far`
At range.

### `Forward`
In a chosen direction, usually away from the caster.

### `Above`
Offset upward.

### `Below`
Offset downward or beneath a surface.

### `Centered`
Organized around a chosen point.

### `Boundary`
At the edge of a region, volume, room, or pattern.

### `Surface`
Limited to a surface or exposed layer.

### `Hollow`
Creates shell-like geometry rather than full fill.

### `Solid`
Creates full volume or occupancy.

### `Line`
Long narrow pattern.

### `Sphere`
Round volume or round shell depending on other constraints.

### `Cone`
Expanding directional spread.

### `Ring`
Circular perimeter.

### `Field`
Distributed through a region.

### `Path`
Along a route or trajectory.

## 15.2 Temporal Constraints

### `Instant`
Resolves immediately.

### `Persist`
Continues after casting.

### `Pulse`
Repeats in bursts.

### `Delayed`
Waits for a duration or condition.

### `Until Broken`
Persists until actively disrupted.

### `Gradual`
Resolves over time.

## 15.3 Event / Behavior Constraints

These may eventually be better represented partly through `Trigger`, but they remain useful shorthand concepts.

### `On Contact`
Triggers when touching a target.

### `On Impact`
Triggers when movement ends by collision.

### `On Entry`
Triggers when something enters a region or crosses a threshold.

### `On Exit`
Triggers when something leaves a region.

### `While Seen`
Valid only while perceived.

### `While Fed`
Valid only while mana continues being supplied.

### `Conditional`
Dependent on a chosen rule or sensed property.

---

# 16. References

References tell the spell what it is working on, where it starts, or what pattern it should interpret.

## 16.1 Basic References

### `Caster`
The one casting.

### `Touched Target`
Whatever the caster is touching.

### `Seen Target`
A chosen visible target.

### `Held Object`
An item currently held.

### `Marked Target`
A previously marked entity or object.

### `Chosen Point`
A selected location.

### `Current Space`
The location presently occupied.

### `Path Ahead`
The route extending in front of the caster.

### `Nearest`
The closest valid target.

### `All Within`
All valid targets in the affected region.

## 16.2 Structural References

### `Locking Pattern`
The part of a structure responsible for closure or restraint.

### `Binding Pattern`
The magical or physical relationship holding something in place.

### `Life Pattern`
The living structure or vitality signature of a creature.

### `Runic Pattern`
An existing magical inscription or encoded pattern.

### `Material Pattern`
The physical composition of an object.

### `Boundary Pattern`
The edge definition of a room, field, ward, region, or shape.

## 16.3 Additional Reference Ideas Identified Later

These emerged naturally during testing and likely need support in some form.

### `Between`
Useful for bridges, linked constructs, and spanning logic.

### `Interior`
Useful for construction, wards, and hollowed structures.

### `Exterior`
Useful for shell logic, barriers, and outer-surface interaction.

### `True Core`
Useful for advanced dispels and hidden structure targeting.

### `Hidden Pattern`
Useful for perception, dispel, and secret magical logic.

---

# 17. Qualifiers

Qualifiers modify or intensify previous instructions without needing entirely new glyphs.

### `Greater`
More intensity.

### `Lesser`
Less intensity.

### `Precise`
Narrower, cleaner, more exact.

### `Broad`
Wider, more diffuse.

### `Gentle`
Less destructive, smoother interaction.

### `Violent`
More forceful, abrupt, unstable.

### `Pure`
Less contamination or side effect.

### `Turbulent`
Less orderly, more chaotic behavior.

### `Hidden`
Less visible or detectable.

### `Revealed`
More visible or explicit.

### `Fixed`
More rigid, less adaptive.

### `Adaptive`
More dynamically responsive.

## Helper Qualifiers / Structural Helpers
These are not necessarily primal but may be useful:

### `Filter`
A helper-style structural glyph or effect that narrows valid targets, events, or sensed outputs.

This is likely not a deep raw glyph, but it is very useful for:
- wards
- information systems
- ally/enemy selection
- noise reduction
- event gating

---

# 18. Forms Are Usually Emergent, Not Primitive

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

# 19. Resolution Categories

These are not necessarily glyphs. They are categories of result the world may conclude a spell has become.

## 19.1 Projectile-like effect
Usually emerges from:
- gathered principle
- containment or shaping
- positional separation
- direction
- stability
- release on impact or command

## 19.2 Beam or ray
Usually emerges from:
- line/path logic
- sustained direction
- focused release
- continuous flow

## 19.3 Burst or explosion
Usually emerges from:
- compression
- sudden release
- expansion
- sometimes instability
- often fire, force, lightning, or chaos

## 19.4 Lingering field
Usually emerges from:
- anchor
- field
- persist or sustain
- distributed effect logic

## 19.5 Ward or seal
Usually emerges from:
- anchor
- boundary
- binding
- persistence
- condition or trigger logic

## 19.6 Unlocking or opening
Usually emerges from:
- perception
- order
- binding
- pattern identification
- separation or disruption

## 19.7 Healing or restoration
Usually emerges from:
- life
- perception
- pattern reading
- refine
- strengthen
- stabilization

## 19.8 Corruption or decay
Usually emerges from:
- death
- chaos
- weaken
- disrupt
- persistence or spread

## 19.9 Construction or shaping
Usually emerges from:
- earth / force / order / change
- shape
- solid / hollow / boundary logic
- anchors
- strengthening
- precision

## 19.10 Information or communication effect
Usually emerges from:
- perception
- attunement
- trace
- trigger logic
- transmit
- optional imprint or display

---

# 20. Domains

As the system expanded, especially into information magic, it became clear that practical implementation should likely use **resolution domains**.

This means the glyph grammar can remain rich and expressive, while the engine resolves outcomes through a smaller number of organized systems.

## 20.1 Damage / Force Domain
Handles:
- impact
- heat
- kinetic force
- pushing / pulling
- direct destructive release
- explosions and projectiles

## 20.2 Structure Domain
Handles:
- shaping matter
- walls, bridges, buildings
- hollow vs solid construction
- support and reinforcement
- anchored forms
- load-bearing logic

## 20.3 Pattern Domain
Handles:
- locks
- seals
- runes
- bindings
- wards
- enchantments
- dispel logic
- pattern reading and unbinding

## 20.4 Information Domain
Handles:
- sensing
- attunement
- tracing
- triggering
- transmitting
- displaying
- recording / imprinting
- message relays
- mirror links
- magical diagnostics

## 20.5 Spatial Domain
Handles:
- relation between points
- placement
- distance
- directional logic
- centered and boundary behavior
- movement through or across space

## 20.6 Life Domain *(possibly merged with others depending on implementation)*
Handles:
- healing
- life pattern interpretation
- vitality
- restoration
- growth
- biological corruption or change

Using domains can protect implementation complexity while preserving design richness.

---

# 21. Common Failure Modes

A major part of the system is that incomplete or crude spell chains do not simply “fail.”  
They often resolve badly.

## 21.1 Structural Failure
The spell lacks enough shaping or stabilization to hold form.

Examples:
- fire collapses in hand
- stone shaping slumps into rubble
- lightning disperses before action

## 21.2 Positional Failure
The spell resolves in the wrong place.

Examples:
- detonation at self
- field anchored a step too early
- structure spawned partially underground

## 21.3 Directional Failure
The spell moves incorrectly.

Examples:
- projectile veers
- beam splays
- push rebounds toward caster

## 21.4 Persistence Failure
The spell remains when it should end, or ends too early.

Examples:
- accidental ward
- floating flame remains in a corridor
- summoned structure collapses prematurely

## 21.5 Interpretive Failure
The world resolves ambiguity in a dangerous or unintended way.

Examples:
- “open” becomes “break”
- “purify” burns both impurity and host
- “bind” restrains ally and enemy together

## 21.6 Overload Failure
Mana is sufficient but coherence is not.

Examples:
- explosion
- environmental mutation
- ritual collapse
- impossible architecture from high-powered drift

## 21.7 Information Failure
A growing category revealed by communication stress tests.

Examples:
- detection without notification
- notification without filtering
- signal sent to wrong attunement
- delayed or distorted transmission
- old magical networks causing signal echo

---

# 22. Minimal Structural Requirements

These are not hard rules yet, but they are useful guidance.

## 22.1 To affect the world at all
A spell usually needs at least:
- one `Principle`
- one `Operation`

## 22.2 To aim reliably
A spell usually needs:
- a `Reference`
- or a very clear spatial `Constraint`

## 22.3 To avoid self-harm
A spell usually needs:
- some positional distinction from `Self`
- often `Separate`, `Direct`, or equivalent logic

## 22.4 To persist
A spell usually needs:
- `Anchor` or `Bind`
- plus `Sustain` or `Persist`

## 22.5 To create a stable traveling effect
A spell usually needs:
- `Gather`
- `Shape`
- positional separation
- direction/path logic
- `Stabilize`
- a release or trigger condition

## 22.6 To create large structures
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

# 23. Stress-Test Spell Examples

These are example constructions, not final canonical recipes.

---

## 23.1 Fireball

### Goal
Create a contained mass of fire, move it away from the caster, keep it coherent in transit, then release it on impact.

### Example chain
`Gather -> Fire -> Compress -> Shape -> Separate -> Forward -> Direct -> Stabilize -> On Impact -> Release`

---

## 23.2 Healing Spell

### Goal
Identify damage in a living target and restore correct pattern.

### Example chain
`Perception -> Life -> Life Pattern -> Refine -> Strengthen -> Stabilize`

---

## 23.3 Levitation

### Goal
Counter downward pull or supply sustained support force to a target.

### Example chain
`Force -> Motion -> Target -> Above -> Sustain -> Stabilize`

A more precise form may involve:
`Perception -> Force -> Target -> Oppose Downward Motion -> Sustain -> Stabilize`

---

## 23.4 Unlock

### Goal
Open a lock or binding without destroying the object.

### Example chain
`Perception -> Order -> Binding -> Locking Pattern -> Separate -> Gentle`

---

## 23.5 Warded Area

### Goal
Create a persistent anchored field with reactive behavior.

### Example chain
`Anchor -> Boundary -> Field -> Persist -> Binding -> On Entry -> Disrupt`

---

## 23.6 Bridge

This example revealed that the system still needs careful spatial semantics.

A bridge-like construction likely needs better vertical / spanning references such as:
- `Between`
- `Surface`
- `Above`
- maybe `Raise`

Better conceptual bridge chain:
`Earth -> Shape -> Between -> Path -> Surface -> Above -> Anchor -> Strengthen -> Stabilize`

or

`Earth -> Gather -> Raise -> Between -> Path -> Surface -> Strengthen -> Anchor`

---

## 23.7 City Block / Constructive Ritual

### Goal
Shape multiple structures, placements, hollows, supports, and civic patterns in one massive coordinated spell.

A city-building ritual may include repeated and nested structures such as:
- road placement
- foundation raising
- wall shaping
- room hollowing
- support reinforcement
- water routing
- light placement
- district boundaries
- ward anchors

A recurring chain theme might look like:
`Earth -> Gather -> Shape -> Boundary -> Hollow -> Anchor -> Strengthen -> Order -> Precise -> Repeat`

---

## 23.8 Accidental Cursed Forest / Magical Scar

### Example accidental logic
`Life -> Field -> Persist -> Binding -> Chaos`

or

`Anchor -> Field -> Life -> Shadow -> Persist -> Adaptive`

This is not a special “haunted forest” spell.  
It is a world scar caused by bad magic.

---

## 23.9 Dispel Magic

A very important conclusion:

**Fresh magic may be disrupted directly. Old persistent magic usually must be perceived and understood before it can be safely unraveled.**

### Crude active counter-disruption
Example:
`Disrupt -> Binding`

### True persistent dispel / unravel
Example:
`Perception -> Runic Pattern -> Binding -> Order -> Separate -> Terminate`

---

## 23.10 Alert Ward

### Goal
Create a persistent field that detects intrusion and notifies a linked receiver.

### Example chain
`Anchor -> Boundary -> Field -> Persist -> Perception -> On Entry -> Attune -> Caster -> Bind -> Transmit`

A longer-lived version may also include an ambient-feeding structure such as `Absorb` so the ward can continue functioning after its initial bound reserve is depleted.

This stress test strongly justified:
- `Attune`
- `Transmit`
- likely `Trigger`
- maybe `Filter`
- possibly `Trace`

---

# 24. Information Magic v1

As the system expanded, it became clear that practical information magic needs its own organized conceptual space.

## 24.1 Core Information-Relevant Principles
- `Perception`
- `Light`
- `Sound`
- `Order`
- `Space`
- `Binding`

## 24.2 Core Information-Relevant Operations
- `Attune`
- `Trigger`
- `Trace`
- `Transmit`
- `Absorb` *(when information structures sustain themselves via external mana)*

## 24.3 Likely Future Information Operations
- `Imprint`
- `Display`
- maybe `Receive`
- maybe `Transfer`

## 24.4 Why This Matters
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

---

# 25. Message Magic

A major conclusion was that `Message` should probably **not** be a primitive glyph.

Instead, “message” should be an emergent outcome produced by information magic.

Possible forms of message include:

## 25.1 Pulse Message
A simple coded signal.

## 25.2 Spoken Message Relay
Captures a voice or sound pattern, transmits it, and replays it.

## 25.3 Conceptual Message
Sends a symbolic pattern, warning, impression, or coded magical signature.

---

# 26. Scrying and Mirrors

A better design is to treat scrying as **pattern capture and reconstruction** rather than literal camera simulation.

## 26.1 Pattern Reconstruction Model
Possible chain idea:
`Perception -> Light -> Trace -> Attune -> Transmit -> Display`

## 26.2 Linked Viewpoint Model
Alternative:
`Attune -> Space -> Perception -> Chosen Point -> Bind -> Transmit -> Display`

---

# 27. Practical Limits and Code Architecture Concerns

The proposed answer to complexity is:

- keep the glyph grammar rich
- keep the world logic coherent
- use **resolution domains** so implementation stays manageable

The engine should not ask:
- “How do I literally stream photons through mana?”

It should ask:
- “This spell is in the information domain. What pattern is being sensed, encoded, transmitted, and reconstructed?”

---

# 28. Helper vs Raw Glyphs

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

# 29. Current Working Glyph List

## 29.1 Principles
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

## 29.2 Core Operations
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

## 29.3 Newly Identified / Strong Candidates
- Trigger
- Transmit
- Trace
- Absorb

## 29.4 Tentative Future Operations
- Raise
- Carve
- Restore
- Unravel
- Transfer
- Imprint
- Display

## 29.5 Constraints
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

## 29.6 References
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

## 29.7 Additional Reference Candidates
- Between
- Interior
- Exterior
- True Core
- Hidden Pattern

## 29.8 Qualifiers
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

## 29.9 Helper-Style Logic
- Filter
- Conditional

---

# 30. Key Design Laws

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

# 31. Suggested Next Steps

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
5. later produce `MagicDesign.md` for implementation architecture

---

# 32. Closing Summary

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
