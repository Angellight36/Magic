# Current Implementation

This file tracks the current state of the Magic prototype so we can see what already exists, what is only planned, and where future systems should plug in.

## Project structure

- Core mod project: `C:\Users\antho\Desktop\Magic\minecraft-mod`
- Local Fabric docs mirror: `C:\Users\antho\Desktop\Magic\FabricDocs`
- Design references:
  - `C:\Users\antho\Desktop\Magic\MagicOverview.md`
  - `C:\Users\antho\Desktop\Magic\MagicDesign.md`
  - `C:\Users\antho\Desktop\Magic\MagicLore.md`
  - `C:\Users\antho\Desktop\Magic\MagicOverviewAndLore.md`
- Focused system notes:
  - `C:\Users\antho\Desktop\Magic\Notes\CastingFocusAndMana.md`
  - `C:\Users\antho\Desktop\Magic\Notes\SpellResolutionReference.md`
  - `C:\Users\antho\Desktop\Magic\Notes\LocksAndKeys.md`
  - `C:\Users\antho\Desktop\Magic\Notes\EffectRuntimeAudit.md`
  - `C:\Users\antho\Desktop\Magic\Notes\ReleaseVersioning.md`

## Implemented systems

### 1. Mod bootstrap and dev environment

- Fabric mod project scaffolded for Minecraft `1.21.11`
- Java 21 pinned for this machine in Gradle and helper scripts
- Dev helper scripts exist:
  - `C:\Users\antho\Desktop\Magic\minecraft-mod\dev-build.bat`
  - `C:\Users\antho\Desktop\Magic\minecraft-mod\dev-run-client.bat`
  - `C:\Users\antho\Desktop\Magic\minecraft-mod\dev-run-server.bat`
- Main mod entrypoint exists in `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\MagicGameMod.java`
- Client bootstrap exists in `C:\Users\antho\Desktop\Magic\minecraft-mod\src\client\java\com\anthony\magicgame\client\MagicGameClient.java`

### 2. Command-driven prototype interface

- `/magic status`
- `/magic glyphs`
- `/magic analyze <spell>`
- `/magic analyze chain <glyph ids...>`
- `/magic spells`
- `/magic cast <spell>`
- `/magic cast chain <glyph ids...>`
- `/magic debug`
- `/magic debug <true/false>`
- `/magic debug feature <feature> <true/false>`
- `/magic anchor <spell> [radius] [duration_seconds]`
- `/magic anchors`
- `/magic anchors clear`
- `/magic mana refill`
- `/magic mana set <amount>`
- Legacy alias: `/magicstatus`

Primary implementation:

- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\command\MagicCommand.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\PrototypeSpellCastingService.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\item\MagicItems.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\item\LinkedKeyItem.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\item\PhysicalLockItem.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\pattern\BlockPatternTag.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\pattern\TaggedBlockPatternState.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\pattern\BlockPatternTagManager.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\pattern\PatternTaggedBlocks.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\pattern\BlockPatternTagTicker.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\pattern\LockedBlockManager.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\pattern\LockingPatternBlocks.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\pattern\LockingPatternInteractionGuard.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\command\MagicStatusCommand.java`

### 3. Player mana system

- Persistent server-side mana storage per player
- Default max mana
- Mana spending
- Mana refill helpers
- Automatic regeneration on server ticks
- Server-to-client mana HUD sync once per second and on direct mana changes
- Multiplayer-safe because it is stored in server saved data

Primary implementation:

- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\mana\ManaProfile.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\mana\PlayerManaManager.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\mana\ManaRegenerationService.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\network\MagicNetworking.java`

### 4. Glyph system foundation

- `MagicDomain` enum exists
- `GlyphCategory` enum exists
- `GlyphDefinition` record exists
- `SpellChain` raw ordered glyph container exists
- `SpellChainParser` supports temporary text-form custom glyph sequences
- Initial core glyph registry exists with the first prototype slice from the design docs

Primary implementation:

- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\MagicDomain.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\GlyphCategory.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\GlyphDefinition.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\SpellChain.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\SpellChainParser.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\registry\CoreGlyphRegistry.java`

### 5. Prototype spell interpretation and resolution

- Data-driven prototype spell definitions exist
- Seeded prototype spell registry exists
- Weighted spell interpretation exists
- First-pass resolution plan exists
- Prototype mana cost and stability scoring exist
- `/magic analyze` now surfaces:
  - intent scores
  - domain scores
  - recipient scores
  - source scores
  - likely failure profile
- Prototype intent classification exists:
  - `TRAVELING_EFFECT`
  - `BOUNDARY_WARD`
  - `PATTERN_INTERACTION`
  - `RESTORATION_EFFECT`
  - `VITALITY_TRANSFER`
  - `CONSTRUCTION_EFFECT`
  - `UNKNOWN_UNSTABLE`
- Interpreted spells now expose:
  - weighted domain scores
  - competing intent scores
  - semantic traits
  - confidence margin
- Current semantic traits include:
  - anchored / bounded / field-shaped
  - traveling delivery
  - restorative
  - vitality transfer
  - pattern-sensitive / disruptive
  - structural shaping
  - self / targeted reference
  - persistent / attuned owner

Primary implementation:

- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\PrototypeSpellDefinition.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\SpellFailureType.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\SpellFailureProfile.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\SpellIntent.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\SpellRecipient.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\SpellSource.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\SpellFlowRules.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\InterpretedSpell.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\SpellTrait.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\SpellResolutionPlan.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\SpellInterpreter.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\SpellResolver.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\registry\PrototypeSpellRegistry.java`

Quick reference note:

- `C:\Users\antho\Desktop\Magic\Notes\SpellResolutionReference.md`

Current failure support:

- First-pass likely failure profiles now exist
- Current failure types in use:
  - `STRUCTURAL_FAILURE`
  - `INTERPRETIVE_FAILURE`
  - `INFORMATION_FAILURE`
  - `PERSISTENCE_FAILURE`

### 6. Anchored effect system

- Server-persistent anchored effects manager exists
- Anchored effects tick once per second
- Anchored effects decay over time
- `alert_ward` can now be anchored at a player's position
- Alert wards currently detect living non-owner entrants and can emit debug activation messages
- Alert wards can render temporary vanilla particle boundaries for testing
- Alert wards can render temporary vanilla activation burst particles
- Alert wards can play temporary vanilla activation sounds
- Player-owned anchors can be listed and cleared with commands

Primary implementation:

- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\effect\AnchoredEffectKind.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\effect\AnchoredEffectInstance.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\effect\AnchoredEffectManager.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\effect\AnchoredEffectTicker.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\effect\WardTrackingRules.java`

### 7. Debug toggle system

- World-persistent global debug toggle exists
- Per-feature debug toggles exist
- Current debug features:
  - `ward_messages`
  - `ward_boundary_particles`
  - `ward_activation_particles`
  - `ward_activation_sound`
  - `fireball_visuals`
  - `fireball_trail_particles`
  - `fireball_launch_sound`
  - `lock_state_particles`
  - `mana_hud_text`
  - `spell_feedback_text`

Primary implementation:

- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\debug\MagicDebugFeature.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\debug\MagicDebugSettings.java`

### 8. Client HUD and spell feedback sync

- An always-visible mana HUD now exists as a current dev-build placeholder UI layer
- The mana HUD now uses segmented mana pips, with optional debug-detail text for exact values and regeneration
- Short-lived spell feedback can appear on the client HUD after analysis/casts without relying on chat alone
- Mana and spell feedback use Fabric payloads so richer final HUD art can replace the current placeholder layer without changing the server authority model

Primary implementation:

- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\network\ManaHudPayload.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\network\SpellFeedbackPayload.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\client\java\com\anthony\magicgame\client\MagicClientNetworking.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\client\java\com\anthony\magicgame\client\MagicHudOverlay.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\network\MagicNetworking.java`

### 8.5. Client spell composer and quick-cast loop

- A held `glyph_focus` item now acts as the current player-facing casting tool for current dev builds
- `glyph_focus` is currently only one casting aid, not a universal lore rule for who can perform magic
- Pressing `G` while holding the focus opens a client-side glyph composer
- The composer can append glyphs by category, analyze the current chain, cast it, remove the last glyph, or clear the chain
- The composer now shows the current chain as visible glyph chips while you build it and uses a neutral dark overlay instead of the earlier heavy blue tint
- Pressing `R` while holding the focus quick-casts the last composed chain
- The composer currently reuses the existing `/magic analyze chain ...` and `/magic cast chain ...` server commands under the hood so the early gameplay loop does not fork the spell runtime yet

Primary implementation:

- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\item\GlyphFocusItem.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\client\java\com\anthony\magicgame\client\MagicCastingClientController.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\client\java\com\anthony\magicgame\client\GlyphComposerScreen.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\client\java\com\anthony\magicgame\client\GlyphComposerState.java`

### 9. Seeded prototype spells currently available

- `fireball`
- `force_bolt`
- `healing_touch`
- `vitality_exchange`
- `unlock`
- `alert_ward`
- `stone_path`
- `stone_wall`

Source:

- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\registry\PrototypeSpellRegistry.java`

### 10. Prototype cast effects currently implemented

- Fire traveling chains now use an owned traveling-spell runtime for range, impact selection, direct damage, splash damage, and burn duration.
- Fire traveling chains still use vanilla particles and sounds as temporary presentation placeholders.
- Force traveling chains can now strike and knock back a looked-at living target.
- Force traveling chains now use the same owned traveling-spell runtime rather than a borrowed vanilla projectile mechanic.
- Restoration chains can heal the caster or a looked-at living target depending on the chain references.
- Restoration targeting now uses weighted recipient scoring and can resolve ambiguously instead of following one hard rule.
- Vitality transfer chains can now convert the caster's health into stronger healing for a looked-at target.
- Pattern interaction chains can now bind, unlock, or disrupt a persistent magical lock state on openable blocks.
- Persistent block pattern tags now exist as a generalized system, with `MAGIC_LOCKED` as the first concrete tag.
- Magical locks now work across more than doors:
  - doors, trapdoors, and fence gates
  - levers and other powered toggles
  - pistons and other extendable blocks
  - container-backed blocks such as chests
- Stateful blocks with exposed booleans can have those states re-enforced while a magical tag is active.
- Container-backed blocks can still be tagged and blocked from manual interaction even when there is no useful open/closed property to freeze.
- Locked blocks now block manual interaction until their magical lock state is removed.
- Prototype keying now exists on top of the same lock state:
  - `linked_key` carries a persistent lock signature
  - `physical_lock` can apply a keyed lock and mint the first linked key when needed
  - another linked key plus one iron ingot can craft a copied linked key with the same signature
  - holding a matching linked key can unlock keyed locks during interaction
- Physical locks intentionally have no special resistance advantage over spell locks yet; they are currently an alternate application method.
- Physical locks are currently limited to entryways and chest-like containers.
- Levers, pistons, and similar mechanisms remain magic-lock-only right now.
- A targeted lock debug view can now show red particles for locked physical-lock targets and green particles for unlocked ones.
- Construction chains can now place short prototype stone paths and raised stone walls.
- `stone_path` now replaces targeted ground surfaces instead of always floating one block above them.

Primary implementation:

- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\command\MagicCommand.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\ConstructionPlacementRules.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\effect\PrototypeSpellEffectService.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\effect\TravelingSpellRuntime.java`

### 11. Prototype item and recipe foundation

- First custom items now exist:
  - `glyph_focus`
  - `linked_key`
  - `physical_lock`
- First manual data JSONs now exist for:
  - item models
  - language entries
  - crafting recipes
- Item visuals currently use placeholder vanilla item textures through generated item models.
- `glyph_focus` now uses the vanilla `writable_book` texture plus permanent glint as a temporary "magical book and quill" placeholder.

Primary implementation:

- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\item\MagicItems.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\item\GlyphFocusItem.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\item\LinkedKeyItem.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\item\PhysicalLockItem.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\item\MagicRecipeSerializers.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\item\LinkedKeyCopyRecipe.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\resources\assets\magicgame\lang\en_us.json`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\resources\assets\magicgame\models\item\glyph_focus.json`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\resources\assets\magicgame\models\item\linked_key.json`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\resources\assets\magicgame\models\item\physical_lock.json`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\resources\data\magicgame\recipe\glyph_focus.json`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\resources\data\magicgame\recipe\linked_key_copying.json`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\resources\data\magicgame\recipe\physical_lock.json`

### 12. Tests currently in place

- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\test\java\com\anthony\magicgame\debug\MagicDebugSettingsTest.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\test\java\com\anthony\magicgame\item\LinkedKeyItemTest.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\test\java\com\anthony\magicgame\spell\SpellChainTest.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\test\java\com\anthony\magicgame\spell\SpellChainParserTest.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\test\java\com\anthony\magicgame\spell\ConstructionPlacementRulesTest.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\test\java\com\anthony\magicgame\spell\SpellFlowRulesTest.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\test\java\com\anthony\magicgame\spell\SpellInterpreterTest.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\test\java\com\anthony\magicgame\spell\SpellResolverTest.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\test\java\com\anthony\magicgame\spell\PatternInteractionRulesTest.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\test\java\com\anthony\magicgame\spell\pattern\LockKeyingTest.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\test\java\com\anthony\magicgame\spell\pattern\PatternTaggedBlocksTest.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\test\java\com\anthony\magicgame\spell\effect\AnchoredEffectInstanceTest.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\test\java\com\anthony\magicgame\spell\effect\TravelingSpellRuntimeTest.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\test\java\com\anthony\magicgame\spell\effect\WardTrackingRulesTest.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\test\java\com\anthony\magicgame\spell\PrototypeSpellCastingServiceTest.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\test\java\com\anthony\magicgame\spell\registry\CoreGlyphRegistryTest.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\test\java\com\anthony\magicgame\spell\registry\PrototypeSpellRegistryTest.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\test\java\com\anthony\magicgame\mana\ManaProfileTest.java`

## JSON components currently present

### Source JSON

1. `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\resources\fabric.mod.json`
   - Declares the mod id, version, entrypoints, dependency requirements, icon path, and metadata.

2. `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\resources\assets\magicgame\lang\en_us.json`
   - Current English names for prototype custom items.

3. `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\resources\assets\magicgame\models\item\linked_key.json`
   - Prototype generated item model using a placeholder vanilla texture.

4. `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\resources\assets\magicgame\models\item\physical_lock.json`
   - Prototype generated item model using a placeholder vanilla texture.

5. `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\resources\data\magicgame\recipe\linked_key_copying.json`
   - Special crafting recipe for copying a linked key from another linked key plus iron.

6. `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\resources\data\magicgame\recipe\physical_lock.json`
   - Crafting recipe for the physical lock.

### JSON-like/data-driven systems not created yet

- No spell JSON definitions yet
- No loot tables yet
- No blockstate JSON yet
- No data pack JSON yet

## Assets currently present

- Current assets:
  - `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\resources\assets\magicgame\icon.png`
  - `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\resources\assets\magicgame\models\item\linked_key.json`
  - `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\resources\assets\magicgame\models\item\physical_lock.json`

## Temporary vanilla placeholders that need replacement later

- `alert_ward` boundary visualization currently uses vanilla `END_ROD` and `ENCHANT` particles
- `alert_ward` activation currently uses vanilla `CRIT` and `END_ROD` particles
- `alert_ward` activation currently uses vanilla `AMETHYST_BLOCK_CHIME`
- `fireball` launch feedback currently uses vanilla `FLAME` and `SMOKE` particles
- `fireball` launch feedback currently uses vanilla `BLAZE_SHOOT`
- `fireball` impact feedback currently uses vanilla `FLAME`, `SMOKE`, `LAVA`, and `GENERIC_EXPLODE`
- `glyph_focus` currently uses the vanilla `writable_book` texture through a generated item model and permanent glint
- `linked_key` currently uses the vanilla `tripwire_hook` texture through a generated item model
- `physical_lock` currently uses the vanilla `iron_door_top` texture through a generated item model
- mana and spell state currently use a temporary placeholder HUD instead of final art
- Ward activation currently uses debug system chat messages instead of custom UI/audio

## Asset management status

We are not in the real asset-production phase yet.

We should pull in generated assets once we start one or more of these:

- HUD elements for mana or spell construction
- custom items, blocks, or spell tools
- spell icons
- GUI textures
- particles, sounds, or world-facing visual identity

## What does not exist yet

- Rich world interaction beyond placeholder particles, sounds, and anchor state
- magical scars or other persistent environmental consequences
- timing-based or shorthand-heavy spell input beyond the current button-driven composer
- enemies or AI using the magic system
- modpack integration hooks beyond keeping the architecture clean
- asset pipeline beyond placeholder icon and generated item JSON

## Next steps

1. Add more real spell outcomes beyond the current ward feedback placeholder path.
2. Evolve the current `glyph_focus` composer beyond explicit buttons into richer hotkeys, shorthand, and timing-based spell input.
3. Add magical scar persistence and decay behavior on top of the anchored effect framework.
4. Replace temporary vanilla placeholders with project-owned visuals, audio, and feedback once asset work starts.
5. Add more multiplayer content hooks so future enemies and content mods can adopt the core system cleanly.
