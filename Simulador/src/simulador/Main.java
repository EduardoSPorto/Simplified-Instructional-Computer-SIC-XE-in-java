package simulador;

import simulador.instrucao.InstructionSet;
import simulador.instrucao.Operations;

public class Main {
    public static void main(String[] args) {
    	
    	Memory vmMemory = new Memory(1025);
    	Registers vmRegisters = new Registers();
    	Operations vmOperations = new Operations(vmMemory, vmRegisters);
        InstructionSet vmInstructionSet = new InstructionSet(vmOperations);
        
        String [] input = {"ADD #5", "STA 100", "ADD #10", "STA 103"};
        
        Assembler assembler = new Assembler(input, vmMemory, vmInstructionSet, vmRegisters);
    	
        VMSimulator vmSimulator = new VMSimulator(vmMemory, vmRegisters, vmInstructionSet);
        
    }
}