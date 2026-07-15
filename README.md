# Drun Automation Language Support for JetBrains IDEs

Write and run readable project automation in JetBrains IDEs. This plugin adds syntax highlighting and editor support for [Drun](https://phillarmonic.github.io/drun/), the automation language executed by the [`xdrun` CLI](https://github.com/phillarmonic/drun).

## What you get

- Native syntax highlighting for `.drun` files
- Comments, bracket matching, auto-closing pairs, indentation-aware folding, and color-scheme settings
- Parser-backed diagnostics and completions when `xdrun` is installed
- Global language-server settings with per-project overrides
- Support for tasks, conditions, loops, Docker, Git, HTTP, secrets, orchestration, and more

The plugin targets IntelliJ Platform 2025.3 and newer. Native syntax highlighting and editor support work in Android Studio and other IntelliJ Platform products. The `xdrun` language server activates automatically only in products that contain JetBrains' LSP module; Android Studio currently receives syntax/editor support without diagnostics or completion.

## Get started

1. Install the plugin from the JetBrains Marketplace.
2. Install [`xdrun`](https://phillarmonic.github.io/drun/getting-started/install/) to run Drunfiles and enable language-server features.
3. Open or create a file ending in `.drun`.

Try a small task:

```drun
version: 2.0

task "hello":
  step "Hello from Drun"
  run "echo Hello from the shell"
```

Run it from your project directory with `xdrun`.

## Teach your AI agents how to use drun

Install the basics skill in your current project with:

```bash
xdrun cmd:skill install drun-basics
```

## Language server

Install `xdrun` and ensure it is available on the IDE's `PATH`, or configure its absolute path under **Settings | Languages & Frameworks | Drun**. The server starts lazily when a `.drun` file opens and runs as:

```text
xdrun cmd:lsp
```

Global settings default to an enabled server and the command `xdrun`. Project settings can inherit, enable, or disable the server. A blank project path inherits the global path; a nonblank project path takes precedence. If the executable is unavailable, native highlighting continues to work.

## Learn Drun

- [Getting started](https://phillarmonic.github.io/drun/getting-started/)
- [Language reference](https://phillarmonic.github.io/drun/reference/language/overview/)
- [Built-in actions](https://phillarmonic.github.io/drun/reference/language/built-in-actions/)
- [Examples](https://phillarmonic.github.io/drun/examples/)

Found a problem with the plugin? [Open an issue](https://github.com/phillarmonic/drun-intellij/issues).

## Development

Gradle runs on Java 26, while the plugin is compiled for Java 21 through the configured toolchain. The repository includes `.java-version` for version managers; verify `java --version` reports 26 before running Gradle. Gradle downloads the target IntelliJ Platform and can provision the Java 21 compilation toolchain automatically:

```bash
./gradlew test
./gradlew runIde
./gradlew buildPlugin
```

The shared **Run Drun Plugin** configuration runs the `runIde` Gradle task. Use its Run action to open a sandbox IDE or its Debug action to launch the sandbox with the debugger attached. If it is not visible immediately after opening the project, reload the Gradle project once.

The same development workflows are available through Drun:

```bash
xdrun build
xdrun test
xdrun run-ide
xdrun package
```

The installable ZIP is written to `build/distributions`. `verifyPluginProjectConfiguration` checks project metadata and `verifyPlugin` runs JetBrains Plugin Verifier against configured IDE releases.

The highlighter is an independent JFlex lexer. When Drun syntax changes, update `src/main/grammar/Drun.flex` and add fixtures based on the canonical samples and grammar tests in `drun-vscode`.
## Marketplace releases

Push a `v*` tag to start the release workflow. The publishing job targets the `publishing` GitHub environment.

Required secrets for publishing are:
- `CS_OFFLINE_CERTIFICATE_CHAIN`
- `CS_OFFLINE_PRIVATE_KEY`
- `CS_OFFLINE_PASSWORD`
- `JB_PUA_TOKEN`

GitHub does not apply approval rules merely because the environment is named in the workflow, so do not publish until the required-reviewer rule is enabled. Once protected, the entire job waits for maintainer approval before it checks out code or receives any secrets. After approval, it tests and signs the plugin, publishes it to JetBrains Marketplace, and attaches the distribution ZIP to a GitHub release.
