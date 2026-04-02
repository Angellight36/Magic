# Release Versioning

This note captures the current release-numbering decision for the Magic prototype.

## Current rule

Friend-facing playtest builds should now use a `dev-release` line instead of an `alpha` line.

When a dev-release is already published and we need to ship a crash fix or similar hotfix, we should version forward instead of silently replacing the same release asset.

Preferred style:

- feature dev-release: `0.1.0-dev.1`
- hotfix to that dev-release: `0.1.0-dev.1.1`
- another hotfix to that same dev-release line: `0.1.0-dev.1.2`

## Why

- it keeps downloaded jars traceable
- it makes friend playtesting support easier
- it avoids confusion about whether two people are actually running the same build
- it preserves a cleaner release history when we fix crashes after a prerelease is already out

## Practical guidance

- the withdrawn alpha line should not be revived for new friend-facing playtests
- use `0.1.0-dev.2` for the next planned dev-release with new feature work
- use `0.1.0-dev.1.1` when fixing a published `0.1.0-dev.1`
- do not silently overwrite an already-published release asset when the fix materially changes runtime stability
