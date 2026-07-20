package com.phillarmonic.drun.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;

import static com.phillarmonic.drun.lexer.DrunTokenTypes.*;

%%

%class _DrunLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%public

%{
  private int toolListParentIndent;
  private int toolTaskSourceIndent;
  private int[] interpolationReturnStates = new int[8];
  private int interpolationDepth;

  private void beginInterpolation(int returnState) {
    if (interpolationDepth == interpolationReturnStates.length) {
      int[] expanded = new int[interpolationReturnStates.length * 2];
      System.arraycopy(interpolationReturnStates, 0, expanded, 0, interpolationReturnStates.length);
      interpolationReturnStates = expanded;
    }
    interpolationReturnStates[interpolationDepth++] = returnState;
    yybegin(IN_INTERPOLATION);
  }

  private void endInterpolation() {
    yybegin(interpolationReturnStates[--interpolationDepth]);
  }

  private int indentationBeforeCurrentToken() {
    int indentation = 0;
    for (int offset = zzStartRead - 1; offset >= 0; offset--) {
      char character = zzBuffer.charAt(offset);
      if (character != ' ' && character != '\t') break;
      indentation++;
    }
    return indentation;
  }
%}

%state IN_STRING
%state IN_SINGLE_STRING
%state IN_INTERPOLATION
%state IN_INTERPOLATION_SINGLE_STRING
%state EXPECT_NAME
%state AFTER_CALL
%state DEPENDENCY_LIST
%state AFTER_REQUIRES
%state EXPECT_TOOLS_COLON
%state TOOL_LIST_LINE_START
%state TOOL_LIST_ENTRY
%state TOOL_LIST_VALUE
%state EXPECT_TASKS_COLON
%state TOOL_TASK_SOURCE_LINE_START
%state IN_TOOL_STRING

WHITE_SPACE=[ \t\f\r\n]+
IDENT=[A-Za-z_][A-Za-z0-9_.-]*
VARIABLE=\$[A-Za-z_][A-Za-z0-9_.-]*
NUMBER=[0-9]+(\.[0-9]+)*
ANNOTATION=@[A-Za-z_][A-Za-z0-9_]*
ENV_VARIABLE=\$\{[A-Za-z_][A-Za-z0-9_]*(:-[^}\r\n]*)?\}

DECLARATION="version"|"task"|"means"|"mode"|"project"|"scm"|"provisioning"|"sources"|"set"|"let"|"define"|"parameter"|"snippet"|"template"|"mixin"|"requires"|"tools"|"given"|"accepts"|"defaults"|"from"|"to"|"as"|"of"|"depends"|"include"|"use"|"uses"|"includes"|"call"|"with"|"capture"|"service"
CONTROL="if"|"else"|"when"|"otherwise"|"for"|"each"|"in"|"parallel"|"try"|"catch"|"finally"|"throw"|"rethrow"|"ignore"|"break"|"continue"|"before"|"after"|"on"|"then"|"and"|"or"|"not"|"is"|"are"|"older"|"newer"|"than"|"contains"|"matches"|"matching"|"using"|"ordered"|"between"|"exists"|"available"|"running"|"detected"
WORD_OPERATOR="be"|"into"|"at"|"by"|"on"|"off"|"up"|"down"|"with"|"without"|"from"|"to"|"as"|"of"|"range"|"then"|"starting"|"extract"|"remove"|"overwrite"
ACTION="info"|"step"|"warn"|"warning"|"error"|"success"|"fail"|"echo"|"run"|"exec"|"shell"|"output"|"config"|"create"|"copy"|"move"|"delete"|"read"|"write"|"append"|"backup"|"check"|"ensure"|"extract"|"archive"|"build"|"push"|"pull"|"tag"|"remove"|"start"|"starting"|"stop"|"scale"|"deploy"|"rollback"|"wait"|"open"|"ping"|"test"|"expect"|"download"|"upload"|"send"|"receive"|"fetch"|"clone"|"init"|"switch"|"merge"|"add"|"commit"|"status"|"log"|"show"|"detect"|"search"|"update"|"restart"|"orchestrate"|"execute"|"apply"|"describe"|"expose"
FILE_VALUE_ACTION="get"|"check"|"update"
FILE_VALUE_FORMAT="property"|"json"|"yaml"|"toml"|"match"
FILE_VALUE_COMPARISON="equals"|"differs"
DOMAIN="drun"|"drunhub"|"setup"|"teardown"|"docker"|"image"|"container"|"compose"|"replicas"|"rollout"|"pods"|"pod"|"ingress"|"manifest"|"manifests"|"namespace"|"port"|"registry"|"git"|"github"|"gitlab"|"generic"|"cli"|"filesystem"|"latest"|"tags"|"series"|"formats"|"metadata"|"authentication"|"ambient"|"refs"|"date"|"default"|"protected"|"branch"|"branches"|"checkout"|"repository"|"remote"|"changes"|"message"|"files"|"get"|"post"|"put"|"patch"|"head"|"options"|"request"|"response"|"body"|"headers"|"header"|"endpoint"|"api"|"data"|"timeout"|"retry"|"follow"|"redirects"|"verify"|"ssl"|"auth"|"bearer"|"basic"|"token"|"user"|"password"|"content"|"type"|"accept"|"health"|"healthy"|"service"|"services"|"provision"|"ready"|"host"|"connection"|"strategy"|"sequential"|"dependency-based"|"circuit"|"breaker"|"failure"|"threshold"|"recovery"|"interval"|"retries"|"networks"|"external"|"required"|"autoprovision"|"driver"|"condition"|"dns"|"tcp"|"domain"|"record"|"expected"|"ip"|"ips"|"command"|"working"|"workdir"|"missing"|"force"|"recreate"|"deps"|"never"|"always"|"makefile"|"target"|"args"|"pre"|"jobs"|"verbose"|"allocate_tty"|"ssh"|"key"|"fallback"|"delay"|"path"|"startup"|"shutdown"|"discovery"|"metrics"|"enabled"|"labels"|"unavailable"|"max"|"min"|"consul"|"etcd"|"server"|"domains"|"ttl"|"cache"|"memory"|"cpu"|"limit"|"policy"|"orphans"|"period"|"env_file"|"installed"|"tool"|"framework"|"environment"|"node"|"npm"|"yarn"|"pnpm"|"bun"|"python"|"pip"|"go"|"golang"|"cargo"|"java"|"maven"|"gradle"|"ruby"|"gem"|"php"|"composer"|"rust"|"make"|"kubectl"|"helm"|"terraform"|"aws"|"gcp"|"azure"|"ci"|"local"|"production"|"staging"|"development"|"react"|"vue"|"angular"|"django"|"rails"|"express"|"spring"|"laravel"|"line"|"match"|"pattern"|"email"|"format"|"concat"|"split"|"replace"|"secret"|"trim"|"uppercase"|"lowercase"|"prepend"|"join"|"slice"|"length"|"keys"|"values"|"transform"|"subtract"|"multiply"|"divide"|"modulo"|"property"|"filtered"|"sorted"|"reversed"|"unique"|"first"|"last"|"basename"|"dirname"|"extension"|"prefix"|"suffix"|"allow"|"permissions"|"dir"|"file"|"folder"|"any"|"current"|"all"|"locally"|"attached"
TYPE="string"|"number"|"boolean"|"list"|"json"|"xml"|"http"|"https"|"docker"|"git"|"kubernetes"|"namespace"|"network"|"directory"|"file"|"service"|"container"|"image"|"repository"|"branch"|"url"|"email"|"uuid"
MACRO="semver"|"semver_optional_v"|"semver_extended"|"docker_tag"
INTERPOLATION_MACRO="concat"|"split"|"replace"|"trim"|"uppercase"|"lowercase"|"prepend"|"join"|"slice"|"length"|"keys"|"values"|"basename"|"dirname"|"extension"|"prefix"|"suffix"|"filtered"|"sorted"|"reversed"|"unique"|"first"|"last"
INTERPOLATION_BUILTIN="current git commit"|"current git branch"|"available tasks"|"file exists"|"dir exists"|"start progress"|"update progress"|"finish progress"|"start timer"|"stop timer"|"show elapsed time"|"docker compose command"|"docker compose status"|"compose_cmd"|"dns_resolve"|"dns_check"|"dns_validate"|"now"|"pwd"|"hostname"|"env"|"secret"|"current"
CONSTANT="true"|"false"|"empty"|"null"|"yes"|"no"|"linux"|"windows"|"mac"|"darwin"

%%

<YYINITIAL> {
  {WHITE_SPACE}                          { return TokenType.WHITE_SPACE; }
  "#"[^\r\n]*                          { return LINE_COMMENT; }
  "/*"([^*]|\*+[^*/])*\*+"/"          { return BLOCK_COMMENT; }
  "/*"([^*]|\*+[^*/])*                 { return BLOCK_COMMENT; }
  {ANNOTATION}                           { return ANNOTATION; }
  "depends"                             { yybegin(DEPENDENCY_LIST); return KEYWORD; }
  "requires"                            { toolListParentIndent = indentationBeforeCurrentToken(); yybegin(AFTER_REQUIRES); return KEYWORD; }
  "call"                                { yybegin(AFTER_CALL); return KEYWORD; }
  "task"|"snippet"                     { yybegin(EXPECT_NAME); return KEYWORD; }
  "template"[ \t]+"task"               { yybegin(EXPECT_NAME); return KEYWORD; }
  {DECLARATION}|{CONTROL}                { return KEYWORD; }
  {WORD_OPERATOR}                        { return KEYWORD; }
  {FILE_VALUE_ACTION}                    { return ACTION; }
  {FILE_VALUE_FORMAT}                    { return TYPE; }
  {FILE_VALUE_COMPARISON}                { return WORD_COMPARISON; }
  {ACTION}                               { return ACTION; }
  {TYPE}                                 { return TYPE; }
  {MACRO}                                { return MACRO; }
  {DOMAIN}                               { return CONSTANT; }
  {CONSTANT}                             { return CONSTANT; }
  {VARIABLE}                             { return VARIABLE; }
  {NUMBER}                               { return NUMBER; }
  ("executable"|[A-Z_][A-Z0-9_]*)/[ \t]*":" { return PROPERTY; }
  \"                                    { yybegin(IN_STRING); return STRING; }
  "'"                                   { yybegin(IN_SINGLE_STRING); return STRING; }
  "=="|"!="|">="|"<="|"&&"|"||"|"="|">"|"<"|"!" { return LOGIC_OPERATOR; }
  "+"|"-"|"*"|"/"                       { return OPERATOR; }
  "{"                                   { return LBRACE; }
  "}"                                   { return RBRACE; }
  "["                                   { return LBRACKET; }
  "]"                                   { return RBRACKET; }
  "("                                   { return LPAREN; }
  ")"                                   { return RPAREN; }
  ":"                                   { return COLON; }
  ","                                   { return COMMA; }
  {IDENT}                                { return IDENTIFIER; }
  .                                      { return BAD_CHARACTER; }
}

<AFTER_CALL> {
  [ \t]+                                 { return TokenType.WHITE_SPACE; }
  "task"|"snippet"                     { yybegin(EXPECT_NAME); return SUB_STATEMENT; }
  \r?\n                                  { yybegin(YYINITIAL); return TokenType.WHITE_SPACE; }
  .                                      { yybegin(YYINITIAL); yypushback(1); }
}

<IN_TOOL_STRING> {
  {ENV_VARIABLE}                         { return VARIABLE; }
  [^\"\\{$]+                           { return STRING; }
  "$"                                   { return STRING; }
  \\\r?\n                               { return STRING_ESCAPE; }
  \\([\"\\nrtbf]|u[0-9A-Fa-f]{4})      { return STRING_ESCAPE; }
  "{"\$?{IDENT}                         { yypushback(yylength() - 1); beginInterpolation(IN_TOOL_STRING); return INTERPOLATION; }
  \"                                    { yybegin(TOOL_LIST_VALUE); return STRING; }
  "{"                                   { return STRING; }
  \\.                                   { return STRING_ESCAPE; }
}

<AFTER_REQUIRES> {
  [ \t]+                                 { return TokenType.WHITE_SPACE; }
  "tools"                               { yybegin(EXPECT_TOOLS_COLON); return KEYWORD; }
  \r?\n                                  { yybegin(YYINITIAL); return TokenType.WHITE_SPACE; }
  .                                      { yybegin(YYINITIAL); yypushback(1); }
}

<EXPECT_TOOLS_COLON> {
  [ \t]+                                 { return TokenType.WHITE_SPACE; }
  ":"                                    { yybegin(TOOL_LIST_LINE_START); return COLON; }
  \r?\n                                  { yybegin(YYINITIAL); return TokenType.WHITE_SPACE; }
  .                                      { yybegin(YYINITIAL); yypushback(1); }
}

<TOOL_LIST_LINE_START> {
  \r?\n                                  { return TokenType.WHITE_SPACE; }
  [ \t]+                                 {
                                           if (yylength() > toolListParentIndent) {
                                             yybegin(TOOL_LIST_ENTRY);
                                             return TokenType.WHITE_SPACE;
                                           }
                                           yybegin(YYINITIAL);
                                           yypushback(yylength());
                                         }
  .                                      { yybegin(YYINITIAL); yypushback(1); }
}

<TOOL_LIST_ENTRY> {
  [ \t]+                                 { return TokenType.WHITE_SPACE; }
  "#"[^\r\n]*                           { yybegin(TOOL_LIST_LINE_START); return LINE_COMMENT; }
  "from"                                 { toolTaskSourceIndent = indentationBeforeCurrentToken(); yybegin(EXPECT_TASKS_COLON); return KEYWORD; }
  {IDENT}                                { yybegin(TOOL_LIST_VALUE); return CONSTANT; }
  \"                                    { yybegin(IN_TOOL_STRING); return STRING; }
  \r?\n                                  { yybegin(TOOL_LIST_LINE_START); return TokenType.WHITE_SPACE; }
  .                                      { yybegin(YYINITIAL); yypushback(1); }
}

<EXPECT_TASKS_COLON> {
  [ \t]+                                 { return TokenType.WHITE_SPACE; }
  "tasks"                                { return CONSTANT; }
  ":"                                    { yybegin(TOOL_TASK_SOURCE_LINE_START); return COLON; }
  \r?\n                                  { yybegin(TOOL_LIST_LINE_START); return TokenType.WHITE_SPACE; }
  .                                      { yybegin(YYINITIAL); yypushback(1); }
}

<TOOL_TASK_SOURCE_LINE_START> {
  \r?\n                                  { return TokenType.WHITE_SPACE; }
  [ \t]+                                 {
                                           if (yylength() > toolTaskSourceIndent) {
                                             return TokenType.WHITE_SPACE;
                                           }
                                           if (yylength() > toolListParentIndent) {
                                             yybegin(TOOL_LIST_ENTRY);
                                             yypushback(yylength());
                                             return TokenType.WHITE_SPACE;
                                           }
                                           yybegin(YYINITIAL);
                                           yypushback(yylength());
                                         }
  "#"[^\r\n]*                           { return LINE_COMMENT; }
  \"([^\"\\]|\\.)*\"                { return DEFINITION; }
  {IDENT}                                { return DEFINITION; }
  ","                                   { return COMMA; }
  .                                      { yybegin(YYINITIAL); yypushback(1); }
}

<TOOL_LIST_VALUE> {
  [ \t]+                                 { return TokenType.WHITE_SPACE; }
  "#"[^\r\n]*                           { return LINE_COMMENT; }
  \"                                    { yybegin(IN_TOOL_STRING); return STRING; }
  "=="|"!="|">="|"<="|"="|">"|"<"   { return LOGIC_OPERATOR; }
  "provision"                            { return CONSTANT; }
  {NUMBER}                               { return NUMBER; }
  \r?\n                                  { yybegin(TOOL_LIST_LINE_START); return TokenType.WHITE_SPACE; }
  .                                      { yybegin(YYINITIAL); yypushback(1); }
}

<DEPENDENCY_LIST> {
  [ \t]+                                 { return TokenType.WHITE_SPACE; }
  "on"|"and"|"then"|"in"|"parallel"|"sequential" { return KEYWORD; }
  \"([^\"\\]|\\.)*\"                { return DEFINITION; }
  {IDENT}                                { return DEFINITION; }
  "["                                   { return LBRACKET; }
  "]"                                   { return RBRACKET; }
  ","                                   { return COMMA; }
  \r?\n                                  { yybegin(YYINITIAL); return TokenType.WHITE_SPACE; }
  .                                      { yybegin(YYINITIAL); yypushback(1); }
}

<EXPECT_NAME> {
  [ \t]+                                 { return TokenType.WHITE_SPACE; }
  \"([^\"\\]|\\.)*\"                { yybegin(YYINITIAL); return DEFINITION; }
  {IDENT}                                { yybegin(YYINITIAL); return DEFINITION; }
  \r?\n                                  { yybegin(YYINITIAL); return TokenType.WHITE_SPACE; }
  .                                      { yybegin(YYINITIAL); yypushback(1); }
}

<IN_STRING> {
  {ENV_VARIABLE}                         { return VARIABLE; }
  [^\"\\{$]+                           { return STRING; }
  "$"                                   { return STRING; }
  \\\r?\n                               { return STRING_ESCAPE; }
  \\([\"\\nrtbf]|u[0-9A-Fa-f]{4})      { return STRING_ESCAPE; }
  "{"\$?{IDENT}                         { yypushback(yylength() - 1); beginInterpolation(IN_STRING); return INTERPOLATION; }
  \"                                    { yybegin(YYINITIAL); return STRING; }
  "{"                                   { return STRING; }
  \\.                                   { return STRING_ESCAPE; }
}

<IN_INTERPOLATION> {
  [ \t\r\n]+                            { return TokenType.WHITE_SPACE; }
  {VARIABLE}                             { return VARIABLE; }
  {INTERPOLATION_BUILTIN}                { return MACRO; }
  {INTERPOLATION_MACRO}                  { return MACRO; }
  {IDENT}/[ \t]*"("                    { return MACRO; }
  {CONTROL}|{WORD_OPERATOR}              { return KEYWORD; }
  {TYPE}                                 { return TYPE; }
  {CONSTANT}                             { return CONSTANT; }
  {NUMBER}                               { return NUMBER; }
  "=="|"!="|">="|"<="|"&&"|"||"|"="|">"|"<"|"!" { return LOGIC_OPERATOR; }
  "+"|"-"|"*"|"/"|"?"              { return OPERATOR; }
  "("                                   { return LPAREN; }
  ")"                                   { return RPAREN; }
  ":"                                   { return COLON; }
  ","                                   { return COMMA; }
  "'"                                   { yybegin(IN_INTERPOLATION_SINGLE_STRING); return STRING; }
  "}"                                   { endInterpolation(); return INTERPOLATION; }
  {IDENT}                                { return INTERPOLATION; }
  .                                      { return INTERPOLATION; }
}

<IN_INTERPOLATION_SINGLE_STRING> {
  "{"\$?{IDENT}                         { yypushback(yylength() - 1); beginInterpolation(IN_INTERPOLATION_SINGLE_STRING); return INTERPOLATION; }
  [^'\\{]+                              { return STRING; }
  "{"                                   { return STRING; }
  \\\r?\n                               { return STRING_ESCAPE; }
  \\.                                   { return STRING_ESCAPE; }
  "'"                                   { yybegin(IN_INTERPOLATION); return STRING; }
}

<IN_SINGLE_STRING> {
  [^'\\]+                               { return STRING; }
  \\\r?\n                               { return STRING_ESCAPE; }
  \\.                                   { return STRING_ESCAPE; }
  "'"                                   { yybegin(YYINITIAL); return STRING; }
}
