# Current Playtest Findings

This note captures the current round of playtest-facing bugs and design pressure points before more implementation work continues.

## Immediate bug observations

- All `Magic` items are currently showing the missing/error texture instead of their intended placeholder visuals.
- The glyph composer still has a UI bug where the current-chain area can render as blue placeholder squares instead of readable glyph text/chips.
- Locked doors still visually flicker open for a brief moment when interaction is denied.

## Lock and key behavior notes

- A physical/keyed lock currently unlocks successfully, but once a matching key opens it, the door behaves as permanently unlocked rather than remaining part of a keyed lock lifecycle.
- We want the same key to be able to:
  - unlock the target
  - re-lock the target
  - keep ownership over that keyed state
- We should also consider allowing multiple locks on the same entryway so a threshold can require more than one successful unlock action to fully pass.

## Targeting pressure points

- `healing_touch` currently behaves too much like a cheap "lay on hands" effect.
- `fireball` and other offensive spells need stronger spatial targeting semantics.
- A cast aimed at the caster's feet should be able to hit the caster if the spell structure allows it.
- More generally, target resolution still needs deeper treatment than "look target vs self" heuristics.

## Stone path notes

- `stone_path` currently only resolves in orthogonal directions.
- It currently only places four blocks.
- The current "4 block" behavior should be treated as a temporary prototype constant, not an intended final rule.
- Replacement-only behavior is acceptable as one mode, but the system should also support constructive placement where new path blocks can be created.

## Bridge building direction

Bridge construction should eventually be treated as a composed multi-step instruction rather than one single monolithic spell meaning.

Desired high-level steps:

1. get two points to connect
2. draw a line between them
3. use that line to define an arc, for example by sampling a circle and keeping the portion above the line
4. thicken the result so it is not one block wide
5. execute the result as actual blocks

Each of those steps should eventually be expressible through glyphs or combinations of glyphs.

## Glyph semantics reminder

- Glyphs are partial pieces of larger interpreted commands.
- They should not be treated as one-to-one spell buttons.
- Complex results like targeting, bridge shape, path length, or ward feeding should emerge from combining these partial pieces into a coherent instruction chain.

## What may need to change to preserve that principle

- The composer UI should keep presenting chains as assembled instructions, not as a thin skin over a spellbook of fixed outcomes.
- Seeded prototype spells should stay examples and debugging shortcuts, not the source of truth for how magic is structured.
- Targeting should come from reference, perception, delivery, and constraint glyph combinations rather than spell-specific fallback rules.
- Mana sourcing should be made explicit in chains so supply decisions are not silently inferred from a spell label.
- Construction behavior such as span, arc, thickness, elevation, and replacement-vs-creation should resolve from glyph structure instead of hidden constants.
- Sustained effects like wards should depend on anchor, trigger, concentration, and feed semantics rather than one-off bespoke spell classes.
- Similar effects like `fireball` and a future `flare` should diverge through differentiating glyph vocabulary and runtime parameters, not because they were manually assigned to unrelated canned spell slots.
