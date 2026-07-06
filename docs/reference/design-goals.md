# Design Goals &amp; Roadmap

## Design goals

Configory is intentionally small. It is not trying to be a full settings UI, command framework, or
complete config ecosystem — it is the config foundation those things can build on.

The goals are:

- a simple public API,
- a predictable file layout,
- low boilerplate,
- typed keys where Java code needs safety,
- string paths where tools need flexibility,
- explicit save and reload behavior,
- validation near the definition,
- mod-level isolation by default.

When a feature would pull Configory toward being a framework rather than a foundation, that's a
signal it belongs in a layer built *on* Configory, not inside it.

## Roadmap ideas

Possible future additions, roughly in the spirit of the goals above:

- `trySet(...)` for command and GUI validation
- generated Markdown docs
- generated JSON schema
- config command helpers
- config screen metadata
- change listeners with `.onChange(...)`
- client/server sync metadata
- restart-required flags
- JSON5 / TOML storage backends
- list and object config values
- migration helpers for renamed keys

> [!NOTE]
> These are directions, not commitments. Some are already reflected in gaps noted while writing
> these docs — see the project's issue tracker for what's actively tracked.

## Next steps

- [Introduction](getting-started/introduction.md) — start from the top.
- [API Summary](reference/api-summary.md) — the method surface at a glance.
