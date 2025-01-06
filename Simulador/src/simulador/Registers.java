package simulador;

import java.util.HashMap;
import java.util.Map;

public class Registers {
    private final Map<String, Integer> registerMap; // Mapeamento para associar mnemônicos aos índices
    private final int[] registers; // Valores dos registradores (24 bits)
    private long registerF; // Registrador F (48 bits)

    // Construtor
    public Registers() {
        registers = new int[10]; // 10 registradores em SIC/XE
        registerMap = new HashMap<>();
        initializeRegisterMap();
    }

    // Inicializar mapeamento de mnemônicos
    private void initializeRegisterMap() {
        // SIC
        registerMap.put("A", 0);//Acumulador
        registerMap.put("X", 1);//Registrador de índice
        registerMap.put("L", 2);//Registrador de ligação
        registerMap.put("PC", 8); // Contador de programa
        registerMap.put("SW", 9); // Palavra de status

        // SIC/XE adicionais
        registerMap.put("B", 3); // Registrador base
        registerMap.put("S", 4);// Reg. uso geral
        registerMap.put("T", 5); // REg. uso geral
        // F é um acumulador para ponto flutuante
        //Este registro não é armazenado em um array de inteiros
        //Tratado como um long
        registerMap.put("F", 6); // F é tratado separadamente (48 bits)
    }

    // Definir valor no registrador (24 bits)
    public void setRegister(String mnemonic, int value) {
        if (mnemonic.equals("F")) {
            throw new IllegalArgumentException("Use setRegisterF para definir o registrador F.");
        }
        Integer index = registerMap.get(mnemonic);
        if (index == null) {
            throw new IllegalArgumentException("Mnemônico inválido: " + mnemonic);
        }
        registers[index] = value & 0xFFFFFF; // Apenas 24 bits
    }

    // Obter valor do registrador (24 bits)
    public int getRegister(String mnemonic) {
        if (mnemonic.equals("F")) {
            throw new IllegalArgumentException("Use getRegisterF para obter o registrador F.");
        }
        Integer index = registerMap.get(mnemonic);
        if (index == null) {
            throw new IllegalArgumentException("Mnemônico inválido: " + mnemonic);
        }
        return registers[index];
    }

    // Definir valor no registrador F (48 bits)
    public void setRegisterF(long value) {
        registerF = value & 0xFFFFFFFFFFFFL; // Apenas 48 bits
    }

    // Obter valor do registrador F (48 bits)
    public long getRegisterF() {
        return registerF;
    }

    // Lista os valores de todos os registradores
    public void printRegisters() {
        System.out.println("Registradores:");
        for (Map.Entry<String, Integer> entry : registerMap.entrySet()) {
            String mnemonic = entry.getKey();
            if (!mnemonic.equals("F")) { // Ignorar F neste loop
                int value = getRegister(mnemonic);
                System.out.printf("%s: %06X\n", mnemonic, value);
            }
        }
        System.out.printf("F: %012X\n", registerF); // Imprime F em 12 dígitos hexadecimais (48 bits)
    }
}