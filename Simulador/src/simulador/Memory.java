package simulador;

import simulador.instrucao.InstructionSet;
import simulador.instrucao.UserInstruction;

public class Memory {
    private byte[] memory;
    private int size;
    private VMSimulator VirtualMachine;

    public Memory(int size, VMSimulator VM) {
        if (size > 1024 * 1024) { // Inicializa até 1MB
            throw new IllegalArgumentException("Memoria maior que 1 mb.");
        } else if (size < 1024) {
        	throw new IllegalArgumentException("Memória menor que 1 Kb.");
        }
        this.size = size;
        memory = new byte[size];
        
        this.VirtualMachine = VM;
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
    
    public int getMemorySize () {
    	return this.size;
    }
    
    // Escrita de instruções
    public void writeInstruction (int address, byte[] instruction, int format) {
    	if (address < 0 || address + 2 >= memory.length) {
            throw new IndexOutOfBoundsException("Endereco invalido");
        }
    	if (format != 4) {
    		memory[address] = instruction [0];
            memory[address + 1] = instruction [1];
            memory[address + 2] = instruction [2];
    	}
    	else {
    		memory[address] = instruction [0];
        	memory[address + 1] = instruction [1];
        	memory[address + 2] = instruction [2];
        	
        	memory[address + 3] = instruction [3];
        	memory[address + 4] = 0;
        	memory[address + 5] = 0;
    	}
    }
    
    // Leitura de instruções
    public byte [] readInstruction (int address) {
    	if (address < 0 || address + 2 >= memory.length) 
            throw new IndexOutOfBoundsException("Endereco invalido");
    	
    	byte [] instruction = null;
    	byte opcode = memory[address];
    	String format;
    	InstructionSet instructions = this.VirtualMachine.getVMInstructions();
    	Registers registers = this.VirtualMachine.getRegisters();
    	
    	registers.setRegister("PC", registers.getRegister("PC") + 3);
    	
    	opcode &= 0b11111100; // Deixa o opcode e tira as flags
    	format = instructions.getFormat(opcode);
    	
    	switch (format) {
		case "1":
			instruction = new byte [1];
			instruction[0] = memory[address];
			break;
		case "2":
			instruction = new byte [2];
			instruction[0] = memory[address];
			instruction[1] = memory[address + 1];
			break;
		case "3/4":
			boolean isExtended = false;		// Define se é tipo 3 ou tipo 4
			byte flags = memory[address + 1];
			byte mask = 0b00010000; 		// Isso zera todos os valores menos o flag extended
			
			if (flags == mask) {		
				isExtended = true;
				instruction = new byte [4];
			} else
				instruction = new byte [3];
			
			instruction[0] = memory[address];
			instruction[1] = memory[address+1];
			instruction[2] = memory[address+2];
			
			if (isExtended) {
				instruction[3] = memory[address+3];
				registers.setRegister("PC", registers.getRegister("PC") + 3);
			}
    	}
    	
    	return instruction;
    	
    }
    
    
    // Instruções de Testes Booleanas
    public boolean isEmpty (int address) {
    	if (address < 0 || address + 2 >= memory.length) {
            throw new IndexOutOfBoundsException("Endereco invalido");
        }
    	int integerValue = 0;
    	byte [] word = new byte [3];
    	word [0] = memory[address];
    	word [1] = memory[address + 1];
    	word [2] = memory[address + 2];
    	
    	for (byte b : word)
    		integerValue = (integerValue << 8) | (b & 0xFF);
    	
    	if (integerValue == 0)
    		return true;
    	return false;
    }
}
