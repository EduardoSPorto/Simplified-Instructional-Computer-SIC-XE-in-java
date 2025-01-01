package Simulador;

public class Main {
    public static void main(String[] args) {
        // Inicializando memória e registradores
        Memory memory = new Memory(32768); // Memória SIC padrão
        Registers registers = new Registers();

        // Teste: Escrevendo e lendo da memória
        memory.writeWord(0, 0x123456); // Escreve a palavra 0x123456 na memória
        int word = memory.readWord(0); // Lê a palavra da posição 0
        System.out.println("Palavra lida da memória: " + Integer.toHexString(word));

        // Teste: Manipulando registradores
        registers.setRegister("A", 42); // Define o valor do registrador A
        int regAValue = registers.getRegister("A"); // Obtém o valor do registrador A
        System.out.println("Valor do registrador A: " + regAValue);
    }
}