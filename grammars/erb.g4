grammar erb;

// Referencing fuzzy parsing in https://github.com/antlr/antlr4/blob/master/doc/wildcard.md
// Referencing modes in https://stackoverflow.com/questions/18243283/lexer-mode-in-antlr

erb : .*? (pair .*?)* EOF ;

pair : S .*? E ;

S : ( START | START_ECHO ) ;
E : ( END | END_NO_NL ) ;

START      : '<%';
START_ECHO : '<%=';

END        : '%>';
END_NO_NL  : '-%>';

OTHER : .*? ;


/*
erb : tag* EOF
     ;

tag : START ECHO? .*? NONL END;

ECHO : '=';
NONL : '-';

x : (ignored | imp)*;
ignored: ~START;

imp : START (ign2 | END)*;

ign2 : ~ END;






M: S MID? E ;
S : START;
E : END ;
MID : .+? ;
*/

