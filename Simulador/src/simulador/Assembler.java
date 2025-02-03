package simulador;

import java.util.Map;

import simulador.instrucao.InstructionSet;
import simulador.instrucao.UserInstruction;
import simulador.instrucao.VMInstruction;

public class Assembler {
	Memory vmMemory;
	VMInstruction vmInstructions;
	Registers vmRegisters;
	
	
	public Assembler (String[] mnemonics, Memory vmMemory, InstructionSet vmInstructionSet, Registers vmRegisters) {
		this.vmMemory =vmMemory;		
		int numberOfLines = mnemonics.length;
		int memoryPointer = 0;
		
		for (int i = 0; i < numberOfLines; i++) {
			
			String [] mnemonicInstruction = mnemonics[i].split(" "); // [Operação] [Operando A (se tiver)] [Operando B (se tiver)] 
			
			// Extração de opcode e formato (bytes) da instrução
			Map<Integer, String> info = vmInstructionSet.getInfo(mnemonicInstruction[0]); 
			int opcode = (int) info.keySet().toArray()[0];
			String strFormat = info.get(opcode);
			
			
			// Coloca formato em inteiro, levando em conta sintexa de endereçamento (@, #)
			int integerFormat;
			if (strFormat == "3/4") {
				
				boolean isDirect = Character.isDigit(mnemonicInstruction[1].charAt(0));
				
				if (isDirect)
					integerFormat = (toInteger(mnemonicInstruction[1]) < 4095) ? 3 : 4;
				else
					integerFormat = (toInteger(mnemonicInstruction[1].substring(1)) < 4095) ? 3 : 4;
				
			} else 
				integerFormat = Integer.parseInt(strFormat);
				
			
			// T.A.D. que permite melhor manipulação sobre a instrução
			UserInstruction binaryInstruction = new UserInstruction(integerFormat);
			binaryInstruction.setOpcode(opcode);
			switch (integerFormat) {
				case 1:
					break;
					
				case 2:
					int firstIndex = vmRegisters.getRegisterIndex(mnemonicInstruction[1]);
					int secondIndex = vmRegisters.getRegisterIndex(mnemonicInstruction[2]);
					binaryInstruction.setRegisters(firstIndex, secondIndex);
					break;
					
				case 3: 	// Instruções 3 e 4 precisam de maior atenção devido às flags
					flagSetter(mnemonicInstruction[1], integerFormat, binaryInstruction);
					
					int dispValue = toInteger (mnemonicInstruction[1]);
					binaryInstruction.setDisp(dispValue);
					break;
					
				case 4:
					flagSetter(mnemonicInstruction[1], integerFormat, binaryInstruction);
					binaryInstruction.setFlagE();
					
					int addressValue = toInteger (mnemonicInstruction[1]);
					binaryInstruction.setAddress(addressValue);
					break;
			}
			
			vmMemory.writeInstruction(memoryPointer, binaryInstruction.getInstruction(), integerFormat);
			if (integerFormat!= 4)
				memoryPointer += 3;
			else
				memoryPointer += 6;
		}
	}
	
	
	
	
	/*
	 =================================== 
	 VMSimulator::flagSetter
	 =================================== 
	*/
	public void flagSetter (String mnemonicOperand, int format, UserInstruction MLInstruction) {
		// Define o tipo de endereçamento (direto, indireto, imediato)
		if (Character.isDigit(mnemonicOperand.charAt(0)))
			MLInstruction.setAsDirect();
		else {
			if (mnemonicOperand.charAt(0) == '@')
				MLInstruction.setAsIndirect();
			else if (mnemonicOperand.charAt(0) == '#')
				MLInstruction.setAsImmediate();
		}
		
		// Define se tem relatividade a outros registradores (base, X, PC)
		String complement = "";
		if (mnemonicOperand.contains(",")) {
		    complement = mnemonicOperand.split(",")[1];
		}
		switch (complement) {
			case "X":
				MLInstruction.setFlagX();
				break;
			case "PC":
				MLInstruction.setFlagP();
				break;
			case "B":
				MLInstruction.setFlagB();
		}
	}
	

	
	
	
	/*
	 ===================================
	 VMSimulator::toInteger
	 	Coversão de string (decimal ou hexadecimal) para Integer
	 ===================================
	*/
	public int toInteger (String strValue) {
		strValue = strValue.split(",")[0];
		if (strValue.contains("#") || strValue.contains("@"))
			strValue = strValue.substring(1);
		if (strValue.contains("0x") || strValue.contains("0X"))
			return Integer.parseUnsignedInt(strValue.substring(2), 16);
		return Integer.parseInt(strValue);
	}
}
