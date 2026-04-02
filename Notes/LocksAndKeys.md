# Locks And Keys

This note is the quick-read guide for the current prototype lock ecosystem.

It exists because lock behavior now spans:

- spell pattern interaction
- persistent block pattern tags
- keyed rune items
- physical lock items

## Current lock model

All current lock sources resolve into the same world state:

- a persistent `MAGIC_LOCKED` block pattern tag

That means:

- spell locks
- physical locks
- keyed locks

all currently share one runtime enforcement path.

## What is actually stored

Each tagged lock can currently carry:

- the target block position and dimension
- an optional key signature
- snapshots of relevant block state booleans:
  - `open`
  - `powered`
  - `extended`

These snapshots let the tag system keep some stateful blocks from being changed by hand, redstone, or other state updates for very long.

## Current lock sources

### Spell lock

Typical chain:

`perception -> order -> binding -> locking_pattern`

Current result:

- applies `MAGIC_LOCKED`
- no key required by default

### Physical lock item

Item:

- `physical_lock`

Current result:

- applies the same `MAGIC_LOCKED` tag as a spell lock
- if the player is carrying an attuned rune key in the other hand, the lock becomes keyed to that signature
- if no attuned key is present, the lock is unkeyed

There is intentionally no special durability or resistance layer yet. For now it is mainly a different delivery method for the same locked state.

## Current key model

Item:

- `rune_key`

Current behavior:

- first use in air attunes the key to a generated signature
- the key keeps that signature in item custom data
- the key also gets a visible shortened label so players can tell keys apart

## How keyed unlocking works

If a locked block has a stored key signature:

- interacting while holding a rune key with the exact same signature removes the lock
- after that, normal block interaction is allowed to continue

If a lock has no stored key signature:

- it is currently treated as an ordinary unkeyed magical lock
- it still needs spell-based unlocking or direct removal by other systems

## Current supported lock targets

The `MAGIC_LOCKED` tag currently supports:

- doors
- trapdoors
- fence gates
- levers and other powered toggles
- pistons and other extendable blocks
- container-backed blocks like chests

## Automation and redstone

Current behavior:

- blocks with stored `open`, `powered`, or `extended` state are re-enforced by the tag ticker
- this gives the current lock system a first-pass resistance to redstone or automation-driven state changes

Current limitation:

- container-backed blocks mainly rely on the interaction guard right now
- we do not yet have a broad automation denial layer for every possible non-player container access path

## Immediate future ideas

- lock strength and unlock resistance
- copied or forged keys
- keyed runes applied directly by spells
- separate `SEALED` or `WARDED` tags
- stronger automation denial for container-style blocks
