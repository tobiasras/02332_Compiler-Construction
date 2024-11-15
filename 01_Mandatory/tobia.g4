grammar tobia;

start : spec=specification up=updates simi=siminputs EOF;

specification: hw=hardware in=inputs ou=outputs la=latches defs=definitions;

hardware: 'hardware:' IDENT ;
inputs: 'inputs:'IDENT+ ;
outputs: 'outputs:'IDENT+;
latches: 'latches:'IDENT+ ;

definitions: (def)*;
def: 'def:' function '=' exp;

updates: 'updates:' (update)*;
update: IDENT '=' exp;

function: name=IDENT'('p1=(IDENT | LATCH) (','pn+=(IDENT | LATCH) )*')';

exp     : x=LATCH                       # Latch
        | y=IDENT                       # Ident
        | f=function                    # Func
        | n='/' e1=exp                  # Not
        | e1=exp op='*'? e2=exp         # And
        | e1=exp op='+' e2=exp          # Or
        | '(' e1=exp ')'                # Paren
        ;

siminputs: 'siminputs:' (input)+;
input: IDENT'='BIT;

LATCH: IDENT'\'';
IDENT: [a-zA-Z_]+ [a-zA-Z0-9_]*;

//FUNCTION: IDENT'('(IDENT | LATCH) (','(IDENT | LATCH))*')';

BIT: [01]+;

// TOKENS  lexer
WHITESPACE : [ \t\n\r]+ -> skip;

COMMENT : '//' ~[\n]* -> skip;
MULTICOMMENT : '/*' (~[*] | '*'~[/])* '*/' -> skip;

ANYTHING: .;