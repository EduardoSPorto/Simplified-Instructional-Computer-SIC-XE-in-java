package simulador.assembler;

import java.util.Map;

import simulador.Memory;
import simulador.Registers;
import simulador.SicXeReservedWords;
import simulador.instrucao.InstructionSet;
import simulador.instrucao.UserInstruction;
import simulador.instrucao.VMInstruction;

public class Assembler {
	Memory vmMemory;
	VMInstruction vmInstructions;
	Registers vmRegisters;
	SymbolTable SYMTAB;
	int LOCCTR;
	int programLenght;
	
	public Assembler (String[] mnemonics, Memory vmMemory, InstructionSet vmInstructionSet, Registers vmRegisters) {
		this.vmMemory =vmMemory;		
		SYMTAB = new SymbolTable();
		String [] intermediateFile;
		
		intermediateFile = firstPass (mnemonics);
		secondPass (intermediateFile);
	}
	
	
	public String[] firstPass (String[] mnemonics) {
		int startAddress;
		this.programLenght = 0;		
		String[] intermediateFile = new String[mnemonics.length];
		
		String[] mLine = mnemonics[0].split(" ");
		String opcode = mLine[0];
		
		// Inicializando com Start e definindo endereços iniciais
		if (opcode.equals("START")) {
			if ( !(mLine[1].isBlank()) )  {
				startAddress = Integer.parseInt(mLine[1]);
				this.LOCCTR = startAddress;
				intermediateFile[0] = String.join(" ", mLine);
			}
			else { // Se START não especifica endereço, então endereço inicial é zero
				startAddress = 0;
				this.LOCCTR = 0;
			}
				
		}
		else
			throw new IllegalArgumentException("Lack of initialization with START directive");
		
		
		// Variáveis para facilitar referenciamento
		int index = 1;
		mLine = mnemonics[index].split(" ");
		opcode = mLine[0];
		
		
		while (opcode != "END") {
			if (opcode.contains(".")) 				// Pula linhas de comentário
				continue;
			
			if( !(SicXeReservedWords.isReservedWord(opcode)) ) { //Verifica se tem Label 
				if (SYMTAB.contains(opcode)) 
					throw new IllegalArgumentException("Duplicated Label Definition");
				else {
					SYMTAB.insert(opcode, this.LOCCTR);
					firstPassOperation(mLine[1], mLine[2]);					
				}
			}
			else { 	//Se não tem Label
				firstPassOperation(opcode, mLine[1]);
			}
			
			intermediateFile[index] = String.join(" ", mLine);
			index++;
			mLine = mnemonics[index].split(" ");
			opcode = mLine[0];
		}
		
		this.programLenght = this.LOCCTR - startAddress; 
		return intermediateFile;
	}
	
	
	
	private void secondPass(String[] intermediateFile) {
		int Lline;
		int index = 0;
		String [] inputLine = intermediateFile[0].split(" ");
		String opcode = inputLine[0];
		
		
		
	}

	
	
	
	
	public void listLine (String[] inputLine, int line, int loc, int objectCode) {
		if (line == 5) {
			System.out.println(String.format("%-7s %-7s %-30s %-10s", "Line", "Loc", "Source Statement", "Object Code"));
			System.out.println("-------------------------------------------------------------------------------------------------------------------------------");
		}
		
		String StatmentA = inputLine[1];
		String StatmentB = "",StatmentC = "";
		if (inputLine.length>=2) 
			StatmentB = inputLine[1];
		if (inputLine.length>=3)
			StatmentC = inputLine[2];
		
		System.out.println(String.format("%-7s %-7s %-30s %-10s", line, loc, StatmentA + " " + StatmentB + " " + StatmentC, Integer.toHexString(objectCode)));	
	}
	
	
	public void firstPassOperation (String opcode, String operand) {
		boolean extended = false;
		if (opcode.contains("+")) {
			opcode = opcode.substring(1);
			extended = true;
			if (!(SicXeReservedWords.isInstruction(opcode)) && !SicXeReservedWords.getInstructionFormat(opcode).equals("3/4"))
				throw new IllegalArgumentException("'+' Symbol is only applyable for instructions of type 4");
		}
		
		if ( !(SicXeReservedWords.isReservedWord(opcode)) )
			throw new IllegalArgumentException("Operation Undefined");
		else {
			if (SicXeReservedWords.isInstruction(opcode)) {
				if (SicXeReservedWords.getInstructionFormat(opcode) == "3/4") {
					if (extended)
						this.LOCCTR = 4;
					else
						this.LOCCTR = 3;
				} else
					this.LOCCTR = Integer.parseInt(SicXeReservedWords.getInstructionFormat(opcode));
			}
			else if (opcode.equals("WORD"))
				this.LOCCTR +=3;
			else if (opcode.equals("RESW"))
				this.LOCCTR += (3 * Integer.parseInt(operand));
			else if (opcode.equals("RESB"))
				this.LOCCTR += Integer.parseInt(operand);
			else {
				this.LOCCTR += 1;
				// Modificar para permitir a representação com valores maiores (Seguindo algoritmo do livro)
			}
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
	 VMSimulator::eraseLabel
	 	Retira o Label do começo da linha
	 	Facilitando a manipulação daquele mnemonico
	 ===================================
	*/
	
	public String[] eraseLabel (String [] mnemonicInstruction) {
		int mnemonicSize = mnemonicInstruction.length;
		String[] newMnemonic = new String [mnemonicSize-1];
		for (int i=0; i<mnemonicSize-1; i++) {
			newMnemonic[i] = mnemonicInstruction[i+1];
		}
		return newMnemonic;
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





//memoryPointer = 0;
//int baseAddress = 0;
//boolean started = false;
//boolean finished = false;
//
//for (int i = 0; finished != true; i++) { /*-=-=-=-=-=-=-=--=-=-=-=-=  SEGUNDA PASSADA  -=-=-=-=-=-=-=-=-=--=-=-=-=-=-
// 												Execução do programa e Substituição dos Labels						*/
//	
//	if (mnemonics[i].charAt(0) == '.') //Ignora comentários  									
//		continue;
//	
//	String [] mnemonicInstruction = mnemonics[i].split(" "); 					// [Label (se Tiver)] [Operação] [Operando A] [Operando B (se tiver)] | [Label] [Diretiva]
//	if (SicXeReservedWords.isReservedWord(mnemonicInstruction[0]) == false) { 	// Se tiver, remove o Label
//		mnemonicInstruction = this.eraseLabel(mnemonicInstruction); 			// [Operação] 	[Operando A] 	[Operando B (se tiver)]	|  	[Diretiva]	[Complemento]
//	}																			
//	// Abaixo é uma facilitação para referenciar 									mFirst		 mSecond		 mThird					|	mFirst		mSecond				
//	String mFirst = mnemonicInstruction[0];
//	String mSecond = mnemonicInstruction[1];
//	String mThird = null;
//	if (mnemonicInstruction[2] != null) {
//		mThird = mnemonicInstruction[2];				
//	}
//	
//	
//	if (!started) {
//		if (mFirst.equals("START") == false) { 
//			throw new IllegalArgumentException("Programa não foi iniciado");
//		}
//		else {
//			started = true;
//			baseAddress = Integer.parseInt(mSecond);
//		} 
//	}
//	
//	else 																				//A partir daqui assume que o programa foi iniciado 
//	{ 
//		if (mFirst.equals("START"))
//			throw new IllegalArgumentException("Programa já iniciado anteriormente");
//		else if (mFirst.equals("END")) {
//			finished = true;
//			continue;
//		}
//		
//		else if (SicXeReservedWords.isDirective(mFirst)) { 	//-=--=-=-=-= DIRETIVAS -=-=-=-=-=--=-=-
//															// NÃO Considera START e END. Pois já são tratadas antes
//			if (mFirst == "WORD") {
//				int absoluteAddress = baseAddress + memoryPointer;
//				vmMemory.writeWord(absoluteAddress, i);
//			}
//			
//			
//			
//			memoryPointer++;
//		}
//		
//		else { 												//-=-=-=-=-=- INSTRUÇÃO -=-=-=-=-==-=-=-
//			
//			// Extração de opcode e formato (bytes) da instrução
//			Map<Integer, String> info = vmInstructionSet.getInfo(mFirst); 
//			int opcode = (int) info.keySet().toArray()[0];
//			String strFormat = info.get(opcode);
//			
//			// Coloca formato em inteiro, levando em conta sintexa de endereçamento (@, #)
//			int integerFormat;
//			if (strFormat == "3/4") {
//				
//				boolean isDirect = Character.isDigit(mSecond.charAt(0));
//				
//				if (isDirect)
//					integerFormat = (toInteger(mSecond) < 4095) ? 3 : 4;
//				else
//					integerFormat = (toInteger(mSecond.substring(1)) < 4095) ? 3 : 4;
//				
//			} else 
//				integerFormat = Integer.parseInt(strFormat);
//
//			// T.A.D. que permite melhor manipulação sobre a instrução
//			UserInstruction binaryInstruction = new UserInstruction(integerFormat);
//			binaryInstruction.setOpcode(opcode);
//			
//			if (integerFormat == 3 || integerFormat == 4) {
//				if (SicXeReservedWords.isReservedSymbol(String.valueOf(mSecond.charAt(0)))) {  //String.Value converte o char na posição 0 em uma String de tam 1
//					continue;
//				}
//				else {
//					if (sTable.contains(mSecond)) {
//						mSecond = sTable.get(mSecond);	//Substitui Label por valor
//					}
//				}
//			}
//
//			switch (integerFormat) {
//			case 1:
//				break;
//				
//			case 2:
//				int firstRegIndex = vmRegisters.getRegisterIndex(mSecond);
//				int secondRegIndex = vmRegisters.getRegisterIndex(mThird);
//				binaryInstruction.setRegisters(firstRegIndex, secondRegIndex);
//				break;
//				
//			case 3: 	// Instruções 3 e 4 precisam de maior atenção devido às flags
//				flagSetter(mSecond, integerFormat, binaryInstruction);
//				
//				int dispValue = toInteger (mSecond);
//				binaryInstruction.setDisp(dispValue);
//				break;
//				
//			case 4:
//				flagSetter(mSecond, integerFormat, binaryInstruction);
//				binaryInstruction.setFlagE();
//				
//				int addressValue = toInteger (mSecond);
//				binaryInstruction.setAddress(addressValue);
//				break;
//			}
//			
//			vmMemory.writeInstruction(memoryPointer, binaryInstruction.getInstruction(), integerFormat);
//			if (integerFormat!= 4)
//				memoryPointer += 3;
//			else
//				memoryPointer += 6;
//		}	
//	}
//}
//}
//

