package simulador;

import java.util.Map;
import java.util.Scanner;

import simulador.instrucao.InstructionSet;
import simulador.instrucao.UserInstruction;

public class VMSimulator {
	Registers registers;
	Memory memory;
	InstructionSet VMInstructions;
	
	public VMSimulator () {
		registers = new Registers();
		memory = new Memory (2048, this);
		VMInstructions = new InstructionSet();

		
		registers.setRegister("PC", 0);
	}
	
	
	public void initialize() {
		int memoryPointer = 0;
		String [] lines= readUserInstructions().toString().split("\n");
		int numberOfLines = lines.length;
		
		// Armazena na memória as instruções do usuário
		// Precisa ser traduzido primeiro (de string para int (24 bits))
		for (int i = 0; i < numberOfLines; i++) {
			
			// Separa entre a operação e os complementos
			String [] mnemonicInstruction = lines[i].split(" ");
			
			// Separa o mnemonico em operação e operando
			Map<Integer, String> info = VMInstructions.getInfo(mnemonicInstruction[0]);
			int opcode = (int) info.keySet().toArray()[0];
			String strFormat = info.get(opcode);
			int integerFormat;
			
			// Converte para inteiro e identifica o tipo 4 (importante para flag extended)
			if (strFormat == "3/4") 
				integerFormat = (toInteger(mnemonicInstruction[1]) < 4095) ? 3 : 4;
			else
				integerFormat = Integer.parseInt(strFormat);
			
			// Forma a instrução a ser armazenada
			UserInstruction MLInstruction = new UserInstruction(integerFormat); // Machine Language Instruction
			MLInstruction.setOpcode(opcode);

			switch (integerFormat) {
				case 1:
					break;
					
				case 2:
					MLInstruction.setRegisters(Integer.parseInt(mnemonicInstruction[1]), Integer.parseInt(mnemonicInstruction[2]));
					break;
					
				case 3: // A partir de 3 Bytes fica mais complicado por que tem mais flags
					FlagSetter(mnemonicInstruction[1], integerFormat, MLInstruction);
					
					int dispValue = toInteger (mnemonicInstruction[1]);
					MLInstruction.setDisp(dispValue);
					break;
					
				case 4:
					FlagSetter(mnemonicInstruction[1], integerFormat, MLInstruction);
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
			
			System.out.println(MLInstruction);
		}	
	}
	
	public void operate () {
		System.out.println("Agora é a Leitura (Verificar se está igual a escrita) ");
		while (memory.isEmpty(registers.getRegister("PC")) == false) {
			UserInstruction currentInstruction = new UserInstruction(memory.readInstruction(registers.getRegister("PC")));
			System.out.println(currentInstruction);
		}
	}
	
	// Lê as instruções do usuário, dessa forma permite múltiplas linhas
	// VAI SER SUBSTITUÍDO POR INTERFACE GRÁFICA
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
		
		// Garante que teve input
		if (input.isEmpty()) {
			throw new IllegalArgumentException ("Nenhuma instrução especificada");
		}
		
		return input;
	}
	
	
	// Define os valores de flags para instruções do tipo 3 e 4
	public void FlagSetter (String mnemonicInstruction, int format, UserInstruction MLInstruction) {
		String mnemonicAddress = mnemonicInstruction;
		
		// Define o tipo de endereçamento
		if (Character.isDigit(mnemonicAddress.charAt(0)))
			MLInstruction.setAsDirect();
		else {
			if (mnemonicAddress.charAt(0) == '@')
				MLInstruction.setAsIndirect();
			else if (mnemonicAddress.charAt(0) == '#')
				MLInstruction.setAsImmediate();
		}
		
		// Define os bits de endereçamento relativos a registrador X, pc, base, ou absoluto
		String complement = "";
		if (mnemonicAddress.contains(",")) {
		    complement = mnemonicAddress.split(",")[1];
		} else {
		    complement = mnemonicAddress; // Ou qualquer outro comportamento que você queira
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
	
	// Converte para Inteiro a entrada
	// Funciona tanto com Decimal quanto com Hexadecimal
	public int toInteger (String strValue) {
		strValue = strValue.split(",")[0];
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
