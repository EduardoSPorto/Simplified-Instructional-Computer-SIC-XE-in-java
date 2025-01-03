package Simulador;

public class Memory {
    private byte[] memory;

    public Memory(int size) {
        if (size > 1024 * 1024) { // Inicializa até 1MB
            throw new IllegalArgumentException("Memoria maior que 1 mb.");
        }
        memory = new byte[size];
    }

    // Leitura de palavras de 24 bits
    // COmbina tres bytes consecutivos em uma palavra, endereço inicial é o menor
    public int readWord(int address) {
        if (address < 0 || address + 2 >= memory.length) {
            throw new IndexOutOfBoundsException("Endereço inválido.");
        }
        return ((memory[address] & 0xFF) << 16) | ((memory[address + 1] & 0xFF) << 8) | (memory[address + 2] & 0xFF);
    }

    // Escrita de palavra
    // Divide o valor de 24 bits em tres bytes
    public void writeWord(int address, int value) {
        if (address < 0 || address + 2 >= memory.length) {
            //throw new IndexOutOfBoundsException("Endereço fora de do espaço valido.");
            throw new IndexOutOfBoundsException("Endereco invalido");
        }
        memory[address] = (byte) ((value >> 16) & 0xFF);
        memory[address + 1] = (byte) ((value >> 8) & 0xFF);
        memory[address + 2] = (byte) (value & 0xFF);
    }
}
