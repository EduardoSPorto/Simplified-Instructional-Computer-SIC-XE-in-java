package simulador.assembler;

public class MacroProcessorTest {
    public static void main(String[] args) {
        MacroProcessor processor = new MacroProcessor();
        try {
            // Passe o caminho do diretório ou arquivo desejado.
            processor.processFiles("EntradaMacros");
            System.out.println("Processamento concluído.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

/*package simulador.assembler;
//Delete.me, apenas um teste para o MacroProcessor
public class MacroProcessorTest {
    public static void main(String[] args) {
        MacroProcessor processor = new MacroProcessor();
        try {
            // O nome do arquivo, o arquivo dever ser colocado ao lado da pasta, src, pode nao funcionar no linux, porém deve funcionar
            processor.processFile("entrada.asm");
            /*					
				Conteudo entrada(definição em qualquer lugar):
					START
					LOADADD ZERO
					WORD 1
					WORD 2
					WORD 3
					WORD 4
					LOADADD MACRO &VAL
					    LDA &VAL
					    ADD &VAL
					MEND
					END
				Resultado esperado:
					START
					LDA ZERO
					ADD ZERO
					WORD 1
					WORD 2
					WORD 3
					WORD 4
					END
					
				Conteudo entrada(definição em qualquer lugar):
					START
					LOADADD ZERO
					WORD 1
					WORD 2
					WORD 3
					LOADADD T
					WORD 4
					LOADADD MACRO &VAL
					    LDA &VAL
					    ADD &VAL
					MEND
					END
				Resultado esperado:
					START
					LDA ZERO
					ADD ZERO
					WORD 1
					WORD 2
					WORD 3
					LDA T
					ADD T
					WORD 4
					END

             */
/*            System.out.println("End");
        } catch (Exception e) {//Tecnicamente para testar erros, não se se funcionou
            e.printStackTrace();
        }
    }
}*/
