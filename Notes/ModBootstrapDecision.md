# Mod Bootstrap Decision

## Repository review

The existing project files all point in the same direction:

- `MagicOverview.md` defines a composable magic language rather than a static spell list.
- `MagicDesign.md` translates that idea into a data-driven architecture with spell parsing, resolution domains, and persistent effects.
- `MagicLore.md` reinforces persistent mana systems and world scars that need world saves and multiplayer testing.
- `MagicOverviewAndLore.md` keeps the combined design history as a large reference document.

## Decision: Fabric over Forge

Fabric is the better fit for the first playable mod because:

- the official Fabric project flow is lighter and faster to bootstrap for a solo developer;
- the mod will live or die on rapid iteration around custom gameplay systems, not on heavyweight platform abstractions;
- dedicated-server support is straightforward as long as gameplay logic stays out of client-only hooks;
- the current scope is a focused prototype with room to grow, not a large dependency-heavy ecosystem modpack.

Forge is still a viable future port if the project later needs a Forge-only ecosystem, but it adds more upfront metadata and lifecycle boilerplate than this phase needs.

## Decision: one integrated mod first

A modpack with a separate core system would make sense after the magic loop is stable. Right now the core gameplay, UI, progression, mana, persistence, and multiplayer playtesting all depend on one another. Keeping them in one integrated mod is the fastest path to a coherent playable slice.

## Long-term packaging note

The long-term target is now:

- one core `Magic` mod that owns the magic language, mana systems, casting flow, persistence, and integration hooks;
- one eventual modpack built around that core mod so enemy, biome, structure, loot, and progression content can adopt the system without forcing every experiment into the core codebase.

That means current implementation work should keep the system modular enough to expose clean hooks later, even while gameplay is still shipping as a single mod right now.

## Immediate next gameplay milestones

1. Expand the command-driven casting flow into richer spell authoring and debugging tools.
2. Add stronger world interaction on top of the anchored effect framework.
3. Synchronize mana and spell state to client-facing HUDs once assets/UI begin.
4. Add GameTests once world-facing spell execution is in place.
