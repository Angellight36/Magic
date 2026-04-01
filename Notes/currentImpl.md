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
- `/magic spells`
- `/magic cast <spell>`
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
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\command\MagicStatusCommand.java`

### 3. Player mana system

- Persistent server-side mana storage per player
- Default max mana
- Mana spending
- Mana refill helpers
- Automatic regeneration on server ticks
- Multiplayer-safe because it is stored in server saved data

Primary implementation:

- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\mana\ManaProfile.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\mana\PlayerManaManager.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\mana\ManaRegenerationService.java`

### 4. Glyph system foundation

- `MagicDomain` enum exists
- `GlyphCategory` enum exists
- `GlyphDefinition` record exists
- `SpellChain` raw ordered glyph container exists
- Initial core glyph registry exists with the first prototype slice from the design docs

Primary implementation:

- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\MagicDomain.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\GlyphCategory.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\GlyphDefinition.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\SpellChain.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\registry\CoreGlyphRegistry.java`

### 5. Prototype spell interpretation and resolution

- Data-driven prototype spell definitions exist
- Seeded prototype spell registry exists
- First-pass spell interpretation exists
- First-pass resolution plan exists
- Prototype mana cost and stability scoring exist
- Prototype intent classification exists:
  - `TRAVELING_EFFECT`
  - `BOUNDARY_WARD`
  - `PATTERN_INTERACTION`
  - `HEALING_EFFECT`
  - `CONSTRUCTION_EFFECT`
  - `UNKNOWN_UNSTABLE`

Primary implementation:

- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\PrototypeSpellDefinition.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\SpellIntent.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\InterpretedSpell.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\SpellResolutionPlan.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\SpellInterpreter.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\SpellResolver.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\registry\PrototypeSpellRegistry.java`

### 6. Anchored effect system

- Server-persistent anchored effects manager exists
- Anchored effects tick once per second
- Anchored effects decay over time
- `alert_ward` can now be anchored at a player's position
- Alert wards detect non-owner player entry and can emit debug activation messages
- Alert wards can render temporary vanilla particle boundaries for testing
- Player-owned anchors can be listed and cleared with commands

Primary implementation:

- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\effect\AnchoredEffectKind.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\effect\AnchoredEffectInstance.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\effect\AnchoredEffectManager.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\effect\AnchoredEffectTicker.java`

### 7. Debug toggle system

- World-persistent global debug toggle exists
- Per-feature debug toggles exist
- Current debug features:
  - `ward_messages`
  - `ward_boundary_particles`
  - `fireball_visuals`

Primary implementation:

- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\debug\MagicDebugFeature.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\debug\MagicDebugSettings.java`

### 8. Seeded prototype spells currently available

- `fireball`
- `healing_touch`
- `unlock`
- `alert_ward`
- `stone_path`

Source:

- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\java\com\anthony\magicgame\spell\registry\PrototypeSpellRegistry.java`

### 9. Tests currently in place

- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\test\java\com\anthony\magicgame\debug\MagicDebugSettingsTest.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\test\java\com\anthony\magicgame\spell\SpellChainTest.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\test\java\com\anthony\magicgame\spell\SpellInterpreterTest.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\test\java\com\anthony\magicgame\spell\effect\AnchoredEffectInstanceTest.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\test\java\com\anthony\magicgame\spell\registry\CoreGlyphRegistryTest.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\test\java\com\anthony\magicgame\spell\registry\PrototypeSpellRegistryTest.java`
- `C:\Users\antho\Desktop\Magic\minecraft-mod\src\test\java\com\anthony\magicgame\mana\ManaProfileTest.java`

## JSON components currently present

### Source JSON

1. `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\resources\fabric.mod.json`
   - Declares the mod id, version, entrypoints, dependency requirements, icon path, and metadata.

### JSON-like/data-driven systems not created yet

- No spell JSON definitions yet
- No loot tables yet
- No recipes yet
- No blockstate/model JSON yet
- No language JSON yet
- No data pack JSON yet

## Assets currently present

- Existing icon only:
  - `C:\Users\antho\Desktop\Magic\minecraft-mod\src\main\resources\assets\magicgame\icon.png`

## Temporary vanilla placeholders that need replacement later

- `alert_ward` boundary visualization currently uses vanilla `END_ROD` and `ENCHANT` particles
- `fireball` debug visualization currently uses vanilla `LargeFireball`
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

- Rich world interaction beyond server messages and anchor state
- magical scars or other persistent environmental consequences
- networking/client sync for mana HUDs
- spell authoring UI
- custom items or blocks
- enemies or AI using the magic system
- modpack integration hooks beyond keeping the architecture clean
- asset pipeline beyond the placeholder icon

## Next steps

1. Expand the command-driven prototype from seeded spells into flexible spell authoring and debugging.
2. Add server-to-client sync for mana and spell state so a HUD can exist later.
3. Add a stronger real world effect, likely a safe debug `fireball`, construction effect, or visible ward feedback.
4. Add magical scar persistence and decay behavior on top of the anchored effect framework.
5. Replace temporary vanilla placeholders with project-owned visuals, audio, and feedback once asset work starts.
