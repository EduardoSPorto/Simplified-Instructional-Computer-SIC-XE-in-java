package simulador;

import java.util.Map;
import java.util.Scanner;

import simulador.instrucao.InstructionSet;
import simulador.instrucao.Operations;
import simulador.instrucao.UserInstruction;

public class VMSimulator {
	Registers registers;
	Memory memory;
	InstructionSet VMInstructions;
	Operations operations;
	
	
	/*
	 ===================================
	 VMSimulator
	 ===================================
	*/
	public VMSimulator () {
		registers = new Registers();
		memory = new Memory (2048, this);
		operations = new Operations(memory, registers);
		VMInstructions = new InstructionSet(this.operations);
	
		registers.setRegisterValue("PC", 0);
	}

	
	
	/*
	 ===================================
	 VMSimulator::initialize
	 ===================================
	*/
	public void initialize() {		
		int memoryPointer = 0;
		
		String [] lines= readUserInstructions().toString().split("\n"); // Leitura e separação de cada linha
		int numberOfLines = lines.length;
		
		// Loop para traduzir (24/32 bits) e armazenar na memória
		for (int i = 0; i < numberOfLines; i++) {
			
			String [] mnemonicInstruction = lines[i].split(" "); // [Operação] [Operando A (se tiver)] [Operando B (se tiver)] 
			
			// Extração de opcode e formato (bytes) da instrução
			Map<Integer, String> info = VMInstructions.getInfo(mnemonicInstruction[0]); 
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
			UserInstruction MLInstruction = new UserInstruction(integerFormat); // Machine Language Instruction
			MLInstruction.setOpcode(opcode);
			switch (integerFormat) {
				case 1:
					break;
					
				case 2:
					int firstIndex = registers.getRegisterIndex(mnemonicInstruction[1]);
					int secondIndex = registers.getRegisterIndex(mnemonicInstruction[2]);
					MLInstruction.setRegisters(firstIndex, secondIndex);
					break;
					
				case 3: 	// Instruções 3 e 4 precisam de maior atenção devido às flags
					flagSetter(mnemonicInstruction[1], integerFormat, MLInstruction); 	
					int dispValue = toInteger (mnemonicInstruction[1]);
					MLInstruction.setDisp(dispValue);
					break;
					
				case 4:
					flagSetter(mnemonicInstruction[1], integerFormat, MLInstruction);
					MLInstruction.setFlagE();
					int addressValue = toInteger (mnemonicInstruction[1]);
					MLInstruction.setAddress(addressValue);
					break;
			}
			
			memory.writeInstruction(memoryPointer, MLInstruction.getInstruction(), integerFormat);
			
			if (integerFormat!= 4)
				memoryPointer += 3;
			else
				memoryPointer += 6;
			
			// System.out.println(MLInstruction);
		}	
	}
	
	
	/*
	 ===================================
	 VMSimulator::operate
	 ===================================
	*/	
	public void operate () {
		while (memory.isEmpty(registers.getRegisterValue("PC")) == false) {
			UserInstruction currentInstruction = new UserInstruction(memory.readInstruction(registers.getRegisterValue("PC")));
			System.out.println(currentInstruction);
			
			VMInstructions.execute(currentInstruction);
			
			// PC + 3
			registers.setRegisterValue("PC", registers.getRegisterValue("PC") + 3); 
		}
	}
	
	
	
	
	/*
	 =================================== 
	 VMSimulator::readUserinstruction
	 =================================== 
	*/
	public StringBuilder readUserInstructions () {
		
		Scanner scanner = new Scanner (System.in);
		StringBuilder input = new StringBuilder();
		
		System.out.println("HLT to Stop! \n");
		while (true) {
			
			String line = scanner.nextLine();
			
			if (line.equals("HLT")) {
//				input.append("HLT");
				break;
			}
			
			input.append(line).append("\n");
		}
		
		if (input.isEmpty()) throw new IllegalArgumentException ("Nenhuma instrução especificada");
		return input;
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

	// Getters

	public Memory getMemory () {
		return this.memory;
	}
	public InstructionSet getVMInstructions () {
		return this.VMInstructions;
	}
	public Registers getRegisters () {
		return this.registers;
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
