START
. int limite = 10
. int count = 1
. int num = 3
. int results[10] 
. J Calcula
.
.
EXTDEF NUM COUNT
EXTREF CALC
LIM WORD 10
COUNT WORD 1
NUM WORD 2
RESLT RESW 10
.
. Multiplica aqui
ITER J CALC
RET STA RESLT
INC COUNT
INCR RESLT
. 
. Agora testa para ver se teve fim
LDS COUNT
LDT LIM
COMPR S T
JLT ITER
.
INC MACRO &VAL
    LDA &VAL
    ADD #1
MEND
INCR MACRO &VAL
    LDA &VAL
    ADD #3
MEND    
END
