# TODO Mirror

## Code TODOs mirrored from the codebase

- None currently.

## Planning backlog

- Keep `Notes/currentImpl.md` current as systems, JSON assets, and data-driven content are added.
- Keep `Notes/SpellResolutionReference.md` current as we retune intent, source, and recipient scoring.
- Keep `Notes/LocksAndKeys.md` current as keyed locks, physical locks, and future seal types evolve.
- Keep `Notes/EffectRuntimeAudit.md` current as we replace placeholder visuals and tighten owned gameplay mechanics.
- Keep `Notes/ReleaseVersioning.md` current if we change prerelease or hotfix numbering policy.
- Use `dev-release` naming for the next friend-facing playtest build instead of reviving the withdrawn alpha line.
- Evolve the current `glyph_focus` composer into the longer-term input model, including timed glyph registration and more direct hotkey casting.
- Build visible world interactions on top of the anchored effect framework, including magical scars and richer ward behavior.
- Keep tuning the weighted interpreter as more glyphs, rituals, and mixed-purpose spell chains are added.
- Expand the generalized block pattern tag system beyond `MAGIC_LOCKED`, including seals, keyed runes, cursed bindings, and future entity-facing tags.
- Decide which non-plant block families should become first-class tag targets beyond the current open/powered/extended/container heuristics.
- Add lock strength / unlock resistance so physical and magical locks can diverge meaningfully later.
- Decide how broad automation denial should be for keyed or sealed containers beyond simple player interaction blocking.
- Move more execution preconditions out of `MagicCommand` and into testable spell/runtime rule helpers.
- Tighten temporary ward trigger filtering once solo testing no longer needs "any non-owner entity" detection.
- Replace temporary vanilla placeholders when asset work begins:
  - `END_ROD` / `ENCHANT` ward boundary particles
  - `CRIT` / `END_ROD` ward activation burst particles
  - `AMETHYST_BLOCK_CHIME` ward activation sound
  - `FLAME` / `SMOKE` fireball trail particles
  - `BLAZE_SHOOT` fireball launch sound
  - fire impact particles and sound
  - temporary mana bar / spell feedback HUD art
  - debug chat-only ward activation feedback
- Add automated gameplay tests once the first real spell execution path exists.
- Keep the core magic systems packaged as one mod, but design APIs/data boundaries so a future modpack can add enemies, structures, and content that implement the system cleanly.
