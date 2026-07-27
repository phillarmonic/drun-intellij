<!-- drun:skill:drun-basics:start -->
Before editing drun files or suggesting task commands:
- read `.drun/ai/drun-basics.md`
- inspect the existing spec before changing it
- use `xdrun --list` to discover tasks
- keep task parameters in `key=value` form
- use `@platform(...)` to separate platform-specific tasks clearly
- remember that an unannotated task in the same family acts as the fallback after platform-specific variants are checked
<!-- drun:skill:drun-basics:end -->

<!-- repertoire:graphify:copilot:copilot-guidance:start -->
## graphify

This repository uses Graphify. Load and follow the installed `graphify` skill for codebase questions when `graphify-out/graph.json` exists and whenever the user invokes `/graphify`.
<!-- repertoire:graphify:copilot:copilot-guidance:end -->
