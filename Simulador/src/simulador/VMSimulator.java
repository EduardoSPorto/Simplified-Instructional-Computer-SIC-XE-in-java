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
		memory = new Memory (2048);
		VMInstructions = new InstructionSet();

		
		registers.setRegister("PC", 0);
		
//		somenteParaTeste();
		initialize();
	}
	
	
	
	public void somenteParaTeste () {
	// Testando os registradores
    // Definir valores nos registradores padrão
    registers.setRegister("A", 0x123456);
    registers.setRegister("X", 0x654321);

    // Definir valor no registrador F (48 bits)
    registers.setRegisterF(0x123456789ABCL); // Adicionado "L" para indicar long

    // Imprimir os valores dos registradores
    registers.printRegisters();
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
			
			for (int j = 0; j < mnemonicInstruction.length; j++) {
				
				if (j == 0) {	
					Map<Integer, String> info = VMInstructions.getInfo(mnemonicInstruction[0]);
					int opcode = (int) info.keySet().toArray()[0];
					String tempFormat = info.get(opcode);
					int format;
					
					// Converte para inteiro e identifica o tipo 4 (importante para flag extended)
					if (tempFormat == "3/4") 
						format = (toInteger(mnemonicInstruction[1]) < 16777215) ? 3 : 4;
					else
						format = Integer.parseInt(tempFormat);
					
//					// Forma a instrução a ser armazenada
					// Machine Language Instruction
					UserInstruction MLInstruction = new UserInstruction(format); 
					switch (format) {
						case 1:
							memory.writeInstruction(memoryPointer, MLInstruction.getInstruction(), format);
							break;
							
						case 2:
							MLInstruction.setOpcode(opcode, format);
							MLInstruction.setRegisters(Integer.parseInt(mnemonicInstruction[1]), Integer.parseInt(mnemonicInstruction[2]));
							memory.writeInstruction(memoryPointer, MLInstruction.getInstruction(), format);
							
						case 3: // A partir de 3 Bytes fica mais complicado por que tem mais flags
							MLInstruction.setOpcode(opcode, format);
							FlagSetter(mnemonicInstruction[1], format, MLInstruction);
							
							int dispValue = toInteger (mnemonicInstruction[1]);
							MLInstruction.setDisp(dispValue);
							
							memory.writeInstruction(memoryPointer, MLInstruction.getInstruction(), format);
							break;
							
						case 4:
							MLInstruction.setOpcode(opcode, format);
							FlagSetter(mnemonicInstruction[1], format, MLInstruction);
							MLInstruction.setFlagE(format);
							
							int addressValue = Integer.parseInt (mnemonicInstruction[1].split(",")[0]);
							MLInstruction.setAddress(addressValue);
							
							memory.writeInstruction(memoryPointer, MLInstruction.getInstruction(), format);
							memoryPointer+=3;
							break;
					}
					memoryPointer+=3;
					System.out.println(MLInstruction);
				}
			}
		}
		
	}
	
	
	public StringBuilder readUserInstructions () {
		
		Scanner scanner = new Scanner (System.in);
		StringBuilder input = new StringBuilder();
		System.out.println("HLT to Stop! \n");
		while (true) {
			String line = scanner.nextLine();
			
			if (line.equals("HLT")) {
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
	
	public void FlagSetter (String mnemonicInstruction, int format, UserInstruction MLInstruction) {
		String mnemonicAddress = mnemonicInstruction;
		
		
		// Define o tipo de endereçamento
		if (Character.isDigit(mnemonicAddress.charAt(0)))
			MLInstruction.setAsDirect(format);
		else {
			if (mnemonicAddress.charAt(0) == '@')
				MLInstruction.setAsIndirect(format);
			else if (mnemonicAddress.charAt(0) == '#')
				MLInstruction.setAsImmediate(format);
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
				MLInstruction.setFlagX(format);
				break;
			case "PC":
				MLInstruction.setFlagP(format);
				break;
			case "B":
				MLInstruction.setFlagB(format);
		}
	}
	
	// Converte para Inteiro a entrada
	// Funciona tanto com Decimal quanto com Hexadecimal
	public int toInteger (String strValue) {
		strValue = strValue.split(",")[0];
		if (strValue.contains("0x"))
			return Integer.parseInt(strValue.substring(2));
		return Integer.parseInt(strValue);
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
