package simulador.assembler;
//Delete.me, apenas um teste para o MacroProcessor
public class MacroProcessorTest {
    public static void main(String[] args) {
        MacroProcessor processor = new MacroProcessor();
        try {
            // O nome do arquivo, o arquivo dever ser colocado ao lado da pasta, src, pode nao funcionar no linux, porém deve funcionar
            processor.processFile("entrada.asm");
            /*
             * Conteudo entrada:
             * MACRO1 MACRO &ARG1, &ARG2
				    MOV &ARG1, &ARG2
				    ADD &ARG1, #1
				MEND
				START
				MACRO1 R1, R2
				END
				Resultado esperado:
					START
					MOV R1, R2
					ADD R1, #1
					END

             */
            System.out.println("End");
        } catch (Exception e) {//Tecnicamente para testar erros, não se se funcionou
            e.printStackTrace();
        }
    }
}
