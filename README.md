# Drun Language Support for JetBrains IDEs

Native JetBrains editor support for the [Drun](https://github.com/phillarmonic/drun) automation language.

## Features

- Native syntax highlighting for `.drun` files
- Comments, matching and automatic pairs, indentation-aware folding, and color-scheme settings
- Parser-backed diagnostics and task/keyword completion through `xdrun cmd:lsp`
- Global language-server settings with per-project overrides

The plugin targets IntelliJ Platform 2025.3 and newer. Native syntax highlighting and editor support work in Android Studio and other IntelliJ Platform products. The `xdrun` language server activates automatically only in products that contain JetBrains' LSP module; Android Studio currently receives syntax/editor support without diagnostics or completion.

## Language server

Install `xdrun` and ensure it is available on the IDE's `PATH`, or configure its absolute path under **Settings | Languages & Frameworks | Drun**. The server starts lazily when a `.drun` file opens and runs as:

```text
xdrun cmd:lsp
```

Global settings default to an enabled server and the command `xdrun`. Project settings can inherit, enable, or disable the server. A blank project path inherits the global path; a nonblank project path takes precedence. If the executable is unavailable, native highlighting continues to work.

## Development

The build uses Java 21. The repository includes `.java-version` for version managers; verify `java --version` reports 21 before running Gradle. On macOS with Homebrew OpenJDK, use `brew install openjdk@21` and set `JAVA_HOME="$(brew --prefix openjdk@21)/libexec/openjdk.jdk/Contents/Home"`. Gradle downloads the target IntelliJ Platform and can provision its compilation toolchain automatically, but Gradle itself should also run on Java 21:

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

Push a `v*` tag to run the release workflow. It tests and signs the plugin, publishes it to JetBrains Marketplace, and attaches the distribution ZIP to a GitHub release. Configure these repository secrets first:

- `CERTIFICATE_CHAIN`
- `PRIVATE_KEY`
- `PRIVATE_KEY_PASSWORD`
- `PUBLISH_TOKEN`
