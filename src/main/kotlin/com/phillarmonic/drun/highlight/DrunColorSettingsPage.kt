package com.phillarmonic.drun.highlight

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import com.phillarmonic.drun.DrunIcons
import javax.swing.Icon

class DrunColorSettingsPage : ColorSettingsPage {
    override fun getIcon(): Icon = DrunIcons.FILE
    override fun getHighlighter(): SyntaxHighlighter = DrunSyntaxHighlighter()
    override fun getDemoText() = """version: 2.0

@platform("linux", "mac")
task "deploy":
  given ${'$'}environment defaults to "staging"
  requires ${'$'}version as string matching semver_optional_v
  set ${'$'}release_version to "{${'$'}version without prefix 'v'}"
  requires tools:
    go >= "1.26"
  if docker is available:
    step "Deploying to {${'$'}environment}"
    build docker image "myapp:{${'$'}environment}"
    success "Build complete" # ready

  call task package

  get property "pluginVersion" from "gradle.properties" as ${'$'}plugin_version
  check json "/version" in "package.json" differs from "1"
  update yaml "chart.appVersion" in "Chart.yaml" to "2" or add as string
"""
    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey>? = null
    override fun getAttributeDescriptors() = DESCRIPTORS
    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY
    override fun getDisplayName() = "Drun"

    companion object {
        private val DESCRIPTORS = arrayOf(
            AttributesDescriptor("Keyword", DrunTextAttributes.KEYWORD),
            AttributesDescriptor("Built-in action", DrunTextAttributes.ACTION),
            AttributesDescriptor("Sub-statement", DrunTextAttributes.SUB_STATEMENT),
            AttributesDescriptor("Type", DrunTextAttributes.TYPE),
            AttributesDescriptor("Macro", DrunTextAttributes.MACRO),
            AttributesDescriptor("Constant", DrunTextAttributes.CONSTANT),
            AttributesDescriptor("Number", DrunTextAttributes.NUMBER),
            AttributesDescriptor("String", DrunTextAttributes.STRING),
            AttributesDescriptor("String escape", DrunTextAttributes.ESCAPE),
            AttributesDescriptor("String interpolation", DrunTextAttributes.INTERPOLATION),
            AttributesDescriptor("Variable", DrunTextAttributes.VARIABLE),
            AttributesDescriptor("Annotation", DrunTextAttributes.ANNOTATION),
            AttributesDescriptor("Definition", DrunTextAttributes.DEFINITION),
            AttributesDescriptor("Configuration property", DrunTextAttributes.PROPERTY),
            AttributesDescriptor("Operator", DrunTextAttributes.OPERATOR),
            AttributesDescriptor("Comparison and logic operator", DrunTextAttributes.LOGIC_OPERATOR),
            AttributesDescriptor("Word comparison", DrunTextAttributes.WORD_COMPARISON),
            AttributesDescriptor("Punctuation", DrunTextAttributes.PUNCTUATION),
            AttributesDescriptor("Line comment", DrunTextAttributes.LINE_COMMENT),
            AttributesDescriptor("Block comment", DrunTextAttributes.BLOCK_COMMENT),
            AttributesDescriptor("Invalid character", DrunTextAttributes.BAD_CHARACTER),
        )
    }
}
