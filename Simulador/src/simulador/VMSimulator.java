package simulador;

import java.util.Map;
import java.util.Scanner;

import simulador.instrucao.InstructionSet;
import simulador.instrucao.Operations;
import simulador.instrucao.UserInstruction;

public class VMSimulator {
	Registers vmRegisters;
	Memory vmMemory;
	InstructionSet vmInstructionSet;
	Operations vmOperations;
	
	
	/*
	 ===================================
	 VMSimulator
	 ===================================
	*/
	public VMSimulator (Memory vmMemory, Registers vmRegisters, InstructionSet vmInstructionSet) {
		this.vmMemory = vmMemory;
		this.vmRegisters = vmRegisters;
		this.vmInstructionSet = vmInstructionSet;
		
		vmRegisters.setRegisterValue("PC", 0);
	}
	
	
	/*
	 ===================================
	 VMSimulator::operate
	 ===================================
	*/	
	public void operate (int ipla, Map<Integer, Integer> instructionPositions) {
		int memoryValue, opcode, eFlag;
		final int max24BitsValue = 16777215;	// -1 se passado pela mask & FFFFFF armazena esse valor
		String format;
		UserInstruction currentInstruction;
		byte [] instruction; 
		this.vmRegisters.setRegisterValue("PC", ipla);
		
		while (vmRegisters.getRegisterValue("PC") != max24BitsValue) {
			
			memoryValue = vmMemory.readWord(vmRegisters.getRegisterValue("PC"));
			
			opcode = (memoryValue>> 16) & 0b11111100;
			format = vmInstructionSet.getFormat(opcode);
			
			if (format != "") {
				if (format == "1") {
					instruction = new byte [1];
					instruction[0] =  (byte) ((memoryValue >> 16) & 0xFF);
				}
				else if (format == "2") {
					instruction = new byte[2];
					instruction[0] = (byte) ((memoryValue >> 16) & 0xFF);
					instruction[1] = (byte) ((memoryValue >> 8)  & 0XFF);
				}
				
				else {
					eFlag = (memoryValue >> 16) & 0b00010000; // extended Flag
					
					if (eFlag == 0b0001000) {
						memoryValue<<= 8;
						memoryValue += vmMemory.readByte(vmRegisters.getRegisterValue("PC"), 0);
						
						
						instruction = new byte [4];
						instruction[0] = (byte) ((memoryValue >> 24 ) & 0xFF);
						instruction[1] = (byte) ((memoryValue >> 16 ) & 0xFF);
						instruction[2] = (byte) ((memoryValue >> 8 ) & 0xFF);
						instruction[3] = (byte) ( memoryValue & 0xFF);
					}
					else {
						instruction = new byte [3];
						instruction[0] = (byte) ((memoryValue >> 16) & 0xFF);
						instruction[1] = (byte) ((memoryValue >> 8) & 0xFF);
						instruction[2] = (byte) ( memoryValue & 0xFF);
					}	
				}				
				currentInstruction = new UserInstruction(instruction);				
				
				int pcValueBefore = vmRegisters.getRegisterValue("PC");
				vmInstructionSet.execute(currentInstruction);
				boolean jInstruction = pcValueBefore != vmRegisters.getRegisterValue("PC");
				if (!jInstruction)
					vmRegisters.setRegisterValue("PC", instructionPositions.get(vmRegisters.getRegisterValue("PC"))); // Novo valor é salvo pra não dar conflito com jujmp
			}
			 
		}
	}
	
	// Getters

	public Memory getMemory () {
		return this.vmMemory;
	}
	public InstructionSet getVMInstructions () {
		return this.vmInstructionSet;
	}
	public Registers getRegisters () {
		return this.vmRegisters;
	}
}




	













/*
 * Na falta de criatividade, defini os flags x, b, p como era no Mips
 * INST val, X  -> Relativo ao registrador X
 * INST val, B  -> Relativo ao registrador Base
 * INST val, PC -> Relativo ao Program Counter
 * INST val     -> Endereço Absoluto
 * 
 * */
