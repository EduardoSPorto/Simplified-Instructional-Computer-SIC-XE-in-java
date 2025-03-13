package simulador;


import java.util.ArrayList;
import java.util.List;

import simulador.instrucao.InstructionSet;
import simulador.instrucao.UserInstruction;

class AllocRecord {
	int[] bounds = new int[2];
	int size;
}

public class Memory {
    private byte[] memory;
    private int size;
    List<AllocRecord> allocatedSpots;
    

    public Memory(int size) {
        if (size > 1024 * 1024) { // Inicializa até 1MB
            throw new IllegalArgumentException("Memoria maior que 1 mb.");
        } else if (size < 1024) {
        	throw new IllegalArgumentException("Memória menor que 1 Kb.");
        }
        this.size = size;
        memory = new byte[size];
        allocatedSpots = new ArrayList<AllocRecord>();
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
    
    public int readByte (int address, int b) {
    	if (address < 0 || address + 2 >= memory.length) 
            throw new IndexOutOfBoundsException("Endereço inválido.");
    	return memory[address + b];
    }
    
    public void writeByte (int address, int value, int b) {
    	if (address < 0 || address + 2 >= memory.length) {
            throw new IndexOutOfBoundsException("Endereco invalido");
        }
    	memory[address + b] = (byte) (value & 0xFF);
    }
   
    
    public int getMemorySize () {
    	return this.size;
    }
    
    
    
    /*
    =======================
    writeInstruction
    ======================
   */
    public void writeInstruction (int address, byte[] instruction, int format) {
    	if (address < 0 || address + 2 >= memory.length) {
            throw new IndexOutOfBoundsException("Endereco invalido");
        }
    	memory[address] = instruction[0];
    	if (format >= 2)
    		memory[address + 1] =instruction[1];
    	if (format >= 3)
    		memory[address + 2] = instruction [2];
    	if (format >= 4) {
    		if (address + 5 >= memory.length)
    			memory[address+3] = instruction[3];
    	}
    }
    
    
    /*
    =======================
    isEmpty
    ======================
   */
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
    
    public int alloc (int size) {
    	Integer startAddress = 0;
    	
    	if (allocatedSpots.size()>0) {
    		boolean found = false;
    		int index = 0;
    		while (!found) {
    			AllocRecord rec = allocatedSpots.get(index);
    			int add = startAddress;
    			boolean inMidle = add+size >= rec.bounds[0] || (add+size >= rec.bounds[1] && add < rec.bounds[1]); 
    			if ( inMidle) {
    				startAddress = rec.bounds[1]+1;
    			} else {
    				found = true;
    			}
    		}
    		AllocRecord newRec = new AllocRecord();
    		newRec.bounds[0] = startAddress;
    		newRec.bounds[1] = startAddress + size;
    		newRec.size = size;
    		allocatedSpots.add(newRec);
    	} else
    		startAddress = 0;
    	
    	return startAddress;
    }

	public void update(int address, int bytes, int relocValue) {
		if (address < 0 || address + bytes >= memory.length)
    		throw new IndexOutOfBoundsException("Endereço inválido.");
    	int value = 0;
    	for ( int i=0; i<bytes; i++ ) {
    		value <<= 8;
    		value += (int) memory[address+i] & 0xFF;
    	}
    	
    	value += relocValue;
    	
    	for ( int i=bytes-1; i>=0; i-- ) {
    		memory[address+i] = (byte) (value & 0xFF);
    		value >>=8;
    	}
    	
	}
}
