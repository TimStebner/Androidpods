# Third-Party Notices

Androidpods is licensed under GPL-3.0-or-later (see `LICENSE`). This file lists upstream
projects whose code, documentation, or protocol research has been adapted into Androidpods,
per `PROJECT.md` §25.

Files containing adapted code carry their own SPDX header and an attribution comment naming
the upstream project, file, and commit (see "SPDX and attribution convention" below). This
file is the project-wide index; it does not replace those per-file notices.

## Upstream projects

| Project | License | Scope of use |
|---|---|---|
| [LibrePods](https://github.com/kavishdevar/librepods) (commit `790e3963451002a3aabf8dcd71d40c635724176a`) | GPL-3.0-or-later | AAP packet field layouts and session-start handshake sequence, adapted into `core.airpods.AapPacketDecoder` and `core.airpods.AapSession`. Verified against this project's own real-hardware capture, not trusted blindly. |

## SPDX and attribution convention

- Every source file carries an SPDX header: `// SPDX-License-Identifier: GPL-3.0-or-later`
- Every file with adapted third-party code additionally carries an attribution comment:
  `// Adapted from <project>, <file>, commit <sha>. Original license: <license>.`
- Protocol facts (PSM numbers, opcodes, packet field layouts, advertisement byte offsets) are
  treated as factual interface information per §25 and are not subject to attribution, but
  each protocol constant still carries a comment naming the origin of the observation (own
  capture vs. upstream documentation).
