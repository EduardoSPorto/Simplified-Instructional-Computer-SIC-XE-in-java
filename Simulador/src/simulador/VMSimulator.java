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
					
					if (tempFormat == "3/4") 
						format = (Integer.parseInt(mnemonicInstruction[1]) < 16777215) ? 3 : 4;
					else
						format = Integer.parseInt(tempFormat);
					
//					// Forma a instrução a ser armazenada
					UserInstruction MLInstruction = new UserInstruction(format); 
					switch (format) {
						case 1:
							memory.writeInstruction(memoryPointer, MLInstruction, format);
							break;
						case 2:
							MLInstruction.setOpcode(opcode, 2);
							MLInstruction.setRegisters(Integer.parseInt(mnemonicInstruction[1]), Integer.parseInt(mnemonicInstruction[2]));
							memory.writeWord(memoryPointer, 2);
							
					}
					memoryPointer+=3;
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
}
