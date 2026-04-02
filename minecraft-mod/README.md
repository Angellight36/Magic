# Magic Fabric Prototype

This directory contains the first playable Minecraft mod prototype for the Magic project.

## Why Fabric

Fabric is the better fit for this phase because it keeps the bootstrap light, works cleanly on dedicated servers, and makes it easier for a solo developer to iterate on a custom game system without carrying extra platform boilerplate.

## Why one integrated mod first

The current design docs describe a single, tightly coupled magic language: glyphs, mana flow, resolution domains, persistent world effects, and multiplayer testing all depend on the same core rules. Starting as one integrated mod keeps those rules in one codebase and avoids the overhead of maintaining a separate core library plus a modpack before the gameplay loop exists.

A curated modpack can still come later once the core systems are stable and we know which companion mods genuinely improve the experience. The current plan is to keep the magic system itself in this mod, then eventually build a modpack around it for broader content such as enemies, worldgen, and progression.

## Current slice

- Fabric 1.21.11 bootstrap
- Server-persistent player mana with automatic regeneration
- A held `glyph_focus` item plus keybound glyph composer for building and casting chains without chat commands
- A quick-cast loop for reusing the last composed spell during multiplayer dev-release playtests
- An always-visible mana HUD with optional debug-detail text
- Dedicated-server-safe `/magic` and `/magicstatus` prototype commands
- Global and per-feature debug toggles for temporary testing visuals
- Initial glyph registry based on the existing design notes
- Prototype spell registry plus first-pass interpretation and resolution
- Persistent anchored effects with a working `alert_ward` prototype
- Unit tests around mana, spell registry, prototype spell structure, and construction placement rules

## Playtest controls

From this folder you can use:

```powershell
.\dev-build.bat
.\dev-run-client.bat
.\dev-run-server.bat
```

The helper scripts pin the workspace to the local JDK 21 install at `C:\Program Files\Java\jdk-21`, which matches this machine. If Java 21 lives somewhere else later, update the scripts and `gradle.properties`.

The first server launch may create the run directory and EULA file. Set `run/eula.txt` to `eula=true` before leaving the server up for normal playtests.

Current dev-build casting loop:

```text
1. Craft or grab a Glyph Focus.
2. Hold it in either hand.
3. Press G to open the Glyph Composer.
4. Click glyphs to build a chain.
5. Press Enter or click Cast to send the spell.
6. Press R later to quick-cast the last composed chain.
```

Prototype commands still exist for debugging and admin-style testing:

```text
/magic status
/magic glyphs
/magic analyze fireball
/magic analyze chain gather fire shape separate forward direct stabilize on_impact release
/magic debug true
/magic debug feature ward_boundary_particles true
/magic debug feature mana_hud_text true
/magic spells
/magic cast fireball
/magic cast chain gather fire shape separate forward direct stabilize on_impact release
/magic anchor alert_ward
/magic anchors
/magic mana refill
```

## Local references

- Local Fabric docs mirror: `C:\Users\antho\Desktop\Magic\FabricDocs`
- Current implementation snapshot: `C:\Users\antho\Desktop\Magic\Notes\currentImpl.md`
- Casting focus and mana direction: `C:\Users\antho\Desktop\Magic\Notes\CastingFocusAndMana.md`
- Effect runtime audit: `C:\Users\antho\Desktop\Magic\Notes\EffectRuntimeAudit.md`
- Release versioning note: `C:\Users\antho\Desktop\Magic\Notes\ReleaseVersioning.md`
