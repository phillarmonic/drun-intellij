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

%state IN_STRING
%state EXPECT_NAME

WHITE_SPACE=[ \t\f\r\n]+
IDENT=[A-Za-z_][A-Za-z0-9_.-]*
VARIABLE=\$[A-Za-z_][A-Za-z0-9_.-]*
NUMBER=[0-9]+(\.[0-9]+)*
ANNOTATION=@[A-Za-z_][A-Za-z0-9_]*

DECLARATION="version"|"task"|"means"|"mode"|"project"|"provisioning"|"sources"|"set"|"let"|"define"|"parameter"|"snippet"|"template"|"mixin"|"requires"|"tools"|"given"|"accepts"|"defaults"|"from"|"to"|"as"|"of"|"depends"|"include"|"use"|"uses"|"includes"|"call"|"with"|"capture"|"service"
CONTROL="if"|"else"|"when"|"otherwise"|"for"|"each"|"in"|"parallel"|"try"|"catch"|"finally"|"throw"|"rethrow"|"ignore"|"break"|"continue"|"before"|"after"|"on"|"then"|"and"|"or"|"not"|"is"|"are"|"contains"|"matches"|"matching"|"between"|"exists"|"available"|"running"|"detected"
WORD_OPERATOR="be"|"into"|"at"|"by"|"on"|"off"|"up"|"down"|"with"|"without"|"from"|"to"|"as"|"of"|"range"|"then"|"starting"|"extract"|"remove"|"overwrite"
ACTION="info"|"step"|"warn"|"error"|"success"|"fail"|"echo"|"run"|"exec"|"shell"|"output"|"config"|"create"|"copy"|"move"|"delete"|"read"|"write"|"append"|"backup"|"check"|"extract"|"archive"|"build"|"push"|"pull"|"tag"|"remove"|"start"|"starting"|"stop"|"scale"|"deploy"|"rollback"|"wait"|"open"|"ping"|"test"|"expect"|"download"|"upload"|"send"|"receive"|"fetch"|"clone"|"init"|"switch"|"merge"|"add"|"commit"|"status"|"log"|"show"|"detect"|"search"|"update"|"restart"|"orchestrate"|"execute"|"apply"|"describe"|"expose"
DOMAIN="drun"|"drunhub"|"setup"|"teardown"|"docker"|"image"|"container"|"compose"|"replicas"|"rollout"|"pods"|"pod"|"ingress"|"manifest"|"manifests"|"namespace"|"port"|"registry"|"git"|"branch"|"checkout"|"repository"|"remote"|"changes"|"message"|"files"|"get"|"post"|"put"|"patch"|"head"|"options"|"request"|"response"|"body"|"headers"|"header"|"endpoint"|"api"|"data"|"timeout"|"retry"|"follow"|"redirects"|"verify"|"ssl"|"auth"|"bearer"|"basic"|"token"|"user"|"password"|"content"|"type"|"accept"|"health"|"healthy"|"service"|"services"|"provision"|"ready"|"host"|"connection"|"strategy"|"sequential"|"dependency-based"|"circuit"|"breaker"|"failure"|"threshold"|"recovery"|"interval"|"retries"|"networks"|"external"|"required"|"autoprovision"|"driver"|"condition"|"dns"|"tcp"|"domain"|"record"|"expected"|"ip"|"ips"|"command"|"working"|"workdir"|"missing"|"force"|"recreate"|"deps"|"never"|"always"|"makefile"|"target"|"args"|"pre"|"jobs"|"verbose"|"allocate_tty"|"ssh"|"key"|"fallback"|"delay"|"path"|"startup"|"shutdown"|"discovery"|"metrics"|"enabled"|"labels"|"unavailable"|"max"|"min"|"consul"|"etcd"|"server"|"domains"|"ttl"|"cache"|"memory"|"cpu"|"limit"|"policy"|"orphans"|"period"|"env_file"|"installed"|"tool"|"framework"|"environment"|"node"|"npm"|"yarn"|"pnpm"|"bun"|"python"|"pip"|"go"|"golang"|"cargo"|"java"|"maven"|"gradle"|"ruby"|"gem"|"php"|"composer"|"rust"|"make"|"kubectl"|"helm"|"terraform"|"aws"|"gcp"|"azure"|"ci"|"local"|"production"|"staging"|"development"|"react"|"vue"|"angular"|"django"|"rails"|"express"|"spring"|"laravel"|"line"|"match"|"pattern"|"email"|"format"|"concat"|"split"|"replace"|"secret"|"trim"|"uppercase"|"lowercase"|"prepend"|"join"|"slice"|"length"|"keys"|"values"|"transform"|"subtract"|"multiply"|"divide"|"modulo"|"property"|"filtered"|"sorted"|"reversed"|"unique"|"first"|"last"|"basename"|"dirname"|"extension"|"prefix"|"suffix"|"allow"|"permissions"|"dir"|"file"|"folder"|"any"|"current"|"all"|"locally"|"attached"
TYPE="string"|"number"|"boolean"|"list"|"json"|"xml"|"http"|"https"|"docker"|"git"|"kubernetes"|"namespace"|"network"|"directory"|"file"|"service"|"container"|"image"|"repository"|"branch"|"url"|"email"|"uuid"|"semver"|"semver_extended"|"docker_tag"
CONSTANT="true"|"false"|"empty"|"null"|"yes"|"no"|"linux"|"windows"|"mac"|"darwin"

%%

<YYINITIAL> {
  {WHITE_SPACE}                          { return TokenType.WHITE_SPACE; }
  "#"[^\r\n]*                          { return LINE_COMMENT; }
  "/*"([^*]|\*+[^*/])*\*+"/"          { return BLOCK_COMMENT; }
  "/*"([^*]|\*+[^*/])*                 { return BLOCK_COMMENT; }
  {ANNOTATION}                           { return ANNOTATION; }
  "task"|"snippet"                     { yybegin(EXPECT_NAME); return KEYWORD; }
  "template"[ \t]+"task"               { yybegin(EXPECT_NAME); return KEYWORD; }
  {DECLARATION}|{CONTROL}                { return KEYWORD; }
  {WORD_OPERATOR}                        { return OPERATOR; }
  {ACTION}                               { return ACTION; }
  {TYPE}                                 { return TYPE; }
  {DOMAIN}                               { return CONSTANT; }
  {CONSTANT}                             { return CONSTANT; }
  {VARIABLE}                             { return VARIABLE; }
  {NUMBER}                               { return NUMBER; }
  \"                                    { yybegin(IN_STRING); return STRING; }
  "=="|"!="|">="|"<="|"&&"|"||"|"="|">"|"<"|"+"|"-"|"*"|"/"|"!" { return OPERATOR; }
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

<EXPECT_NAME> {
  [ \t]+                                 { return TokenType.WHITE_SPACE; }
  \"([^\"\\]|\\.)*\"                { yybegin(YYINITIAL); return DEFINITION; }
  {IDENT}                                { yybegin(YYINITIAL); return DEFINITION; }
  \r?\n                                  { yybegin(YYINITIAL); return TokenType.WHITE_SPACE; }
  .                                      { yybegin(YYINITIAL); yypushback(1); }
}

<IN_STRING> {
  [^\"\\{]+                            { return STRING; }
  \\([\"\\nrtbf]|u[0-9A-Fa-f]{4})      { return STRING_ESCAPE; }
  "{"\$?{IDENT}"}"                     { return INTERPOLATION; }
  \"                                    { yybegin(YYINITIAL); return STRING; }
  "{"                                   { return STRING; }
  \\.                                   { return STRING_ESCAPE; }
}
