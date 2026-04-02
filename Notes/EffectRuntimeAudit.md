# Prototype Effect Runtime Audit

This note separates real gameplay mechanics from temporary presentation so we do not accidentally ship borrowed vanilla behavior again.

## Real gameplay mechanics right now

- Mana spending, regeneration, and server-authoritative mana sync are real project mechanics.
- Spell interpretation, resolution scoring, ambiguity, source scoring, and recipient scoring are real project mechanics.
- Fire traveling spells now use an owned impact runtime:
  - range, direct damage, splash damage, splash radius, and burn duration come from our code
  - entity/block impact choice comes from our code
  - no vanilla projectile entity is used for the actual effect anymore
- Force traveling spells now use the same owned impact runtime:
  - damage, knockback, lift, and impact choice come from our code
- Restoration spells use our own target scoring and healing logic.
- Vitality transfer uses our own self-damage and target-healing logic.
- Magical lock state, linked keys, physical locks, and generalized block pattern tags are real project mechanics.
- Construction spells place blocks through our own runtime rules.
- Anchored wards are real project mechanics in the sense that the ward state, persistence, owner tracking, and entrant detection are ours.

## Presentation placeholders that still remain

- Fire and force casts still use vanilla particles and sounds as placeholder presentation.
- Wards still use vanilla particles and sounds as placeholder presentation.
- Lock and unlock feedback still uses vanilla particles and sounds as placeholder presentation.
- Mana HUD art and spell feedback art are still placeholder UI.
- Item models still use vanilla textures as placeholder art.

## Explicitly removed borrowed mechanics

- Fireball casts no longer spawn a vanilla `LargeFireball` and no longer inherit its explosion behavior.
- Traveling damage is no longer coupled to a debug visual toggle.

## Current release stance

- The withdrawn `alpha` line should be treated as invalid for balance or mechanics playtesting.
- The next friend-facing playtest should be a `dev-release`, not an `alpha`.
- Future release notes should call out whether a system is:
  - real gameplay logic
  - placeholder presentation
  - intentionally incomplete
