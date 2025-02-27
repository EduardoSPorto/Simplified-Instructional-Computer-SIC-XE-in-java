package simulador.assembler;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.Map;

import simulador.DataUtils;
import simulador.Memory;
import simulador.Registers;
import simulador.SicXeReservedWords;
import simulador.instrucao.InstructionSet;
import simulador.instrucao.UserInstruction;
import simulador.instrucao.VMInstruction;

public class Assembler {
	Memory vmMemory;
	InstructionSet vmInstructions;
	Registers vmRegisters;
	SymbolTable SYMTAB;
	int startAddress;
	int LOCCTR;
	int programLenght;
	ObjectProgram objectProgram;
	
	public Assembler (String[] mnemonics, Memory vmMemory, InstructionSet vmInstructionSet, Registers vmRegisters) {
		this.vmMemory =vmMemory;
		this.vmInstructions = vmInstructionSet;	
		this.vmRegisters = vmRegisters;
		this.SYMTAB = new SymbolTable();
		String [] intermediateFile;
		
		intermediateFile = firstPass (mnemonics);
		secondPass (intermediateFile);
	}
	
	
	
	/*
	 ===================================
	 Assembler::firstPass
	 	Realiza a primeira passagem pelo código mnemonico
	 	Salva Labels na tabela de símbolos;
	 	Atualiza o tamanho do programa;
	 	Garante que a Sintáxe está correta;
	 	Apaga Labels do começo de uma instrução.
	 ===================================
	*/
	public String[] firstPass (String[] mnemonics) {
		int index = 0;
		this.programLenght = 0;		
		String[] intermediateFile = new String[mnemonics.length];
		
		String[] mColumns = mnemonics[index++].split(" ");
		while (mColumns[0].charAt(0) == '.')
			mColumns = mnemonics[index++].split(" ");
		
		String label = "";
		String opcode;
		String address = "";
		if (mColumns[0].equals("START")) {
			opcode = mColumns[0];
			if (mColumns.length >= 2)
				address = mColumns[1];
		}
		else {
			label = mColumns[0];
			opcode = mColumns[1];
			if (mColumns.length >= 3)
				address = mColumns[2];
		}
		
		// Inicializando com Start e definindo endereços iniciais
		if (opcode.equals("START")) {
			if ( address.isBlank())  {
				startAddress = 0;
				this.LOCCTR = 0;
			}
			else { // Se START não especifica endereço, então endereço inicial é zero
				startAddress = Integer.parseInt(address);
				this.LOCCTR = startAddress;
			}
			
			intermediateFile[0] = String.join(" ", mColumns);
		}
		else
			throw new IllegalArgumentException("Lack of initialization with START directive");
		
		
		// Variáveis para facilitar referenciamento
		boolean hasLabel = false;
		boolean isDirective = false;
		boolean isInstruction = false;
		mColumns = mnemonics[index].split(" ");
		
		hasLabel = this.isLabel(mColumns[0]);
		if (hasLabel) {
			label  = mColumns[0];
			opcode = mColumns[1];
		}
		else {
			opcode = mColumns[0];
			label = "";
		}
		
		isDirective = this.isDirective(opcode);
		isInstruction = this.isInstruction(opcode);
		
		while (opcode != "END") {
			boolean extended = false;
			
			if (mColumns[0].contains(".")) 				// Pula linhas de comentário
				continue;
			
			if( hasLabel ) { 
				if (SYMTAB.contains(label)) 
					throw new IllegalArgumentException("Duplicated Label Definition");
				else {
					SYMTAB.insert(label, this.LOCCTR);
					extended = verifyOpcode (opcode, isDirective);
					updateLOCCTR(opcode, mColumns[2], isInstruction, extended);
					mColumns = this.eraseLabel(mColumns); 			// Remove o Label do código texto, Montador conhece ele pela tabela de símbolos
				}
			}
			else { 	
				extended = verifyOpcode (opcode, isDirective);
				updateLOCCTR(opcode, mColumns[1], isInstruction, extended);
			}
			
			// Salva no arquivo intermediario após atualizar tabela de símbolos e remover os labels do começo.
			intermediateFile[index] = String.join(" ", mColumns);
			
			// Inicia Leitura da próxima linha
			index++;
			mColumns = mnemonics[index].split(" ");
			hasLabel = this.isLabel(mColumns[0]);
			if (hasLabel) {
				label  = mColumns[0];
				opcode = mColumns[1];
			}
			else {
				opcode = mColumns[0];
				label = "";
			}			
			isDirective = this.isDirective(opcode);
			isInstruction = this.isInstruction(opcode);
		}
		
		intermediateFile[index] = String.join(" ", mColumns);
		this.programLenght = this.LOCCTR - startAddress; 
		return intermediateFile;
	}
	
	public boolean verifyOpcode (String opcode, boolean isDirective) {
		boolean extended = false;
		if (opcode.contains("+")) {
			opcode = opcode.substring(1);
			extended = true;
			if ((isDirective == false) && !SicXeReservedWords.getInstructionFormat(opcode).equals("3/4"))
				throw new IllegalArgumentException("'+' Symbol is only applyable for instructions of type 4");
		}
		if ( !(SicXeReservedWords.isReservedWord(opcode)) )
			throw new IllegalArgumentException("Operation Undefined");
		
		return extended;
	}
	
	public void updateLOCCTR (String opcode, String operand, boolean isIntruction, boolean extended) {	
		if (isIntruction) {
			if (SicXeReservedWords.getInstructionFormat(opcode) == "3/4") {
				if (extended)
					this.LOCCTR += 4;
				else
					this.LOCCTR += 3;
			} 
			else
				this.LOCCTR = Integer.parseInt(SicXeReservedWords.getInstructionFormat(opcode));
		}
		else if (opcode.equals("WORD"))
			this.LOCCTR +=3;
		else if (opcode.equals("RESW"))
			this.LOCCTR += (3 * Integer.parseInt(operand));
		else if (opcode.equals("RESB"))
			this.LOCCTR += Integer.parseInt(operand);
		else {
			if (operand.charAt(0) == 'H') {
				String hexValue = operand.substring(operand.indexOf('\'')+1, operand.lastIndexOf('\''));  			// O caractére \ Quebra a palavra especial '
				int bytes = Math.ceilDiv(hexValue.length(), 2) ; 	// Ceil arredonda para cima, 3 Caractéres Hexa, equivalem a 2 bytes
				this.LOCCTR += bytes;
			}
			else if (operand.charAt(0) == 'C') {
				String charSequence = operand.substring(operand.indexOf('\'')+1, operand.lastIndexOf('\''));
				this.LOCCTR += charSequence.length();		// Cada Char é um byte.
			}
			else 
				throw new IllegalArgumentException("Operand format Unsuported in BYTE Type");
		}
	}

	public String[] eraseLabel (String [] mColumns) {
		int mnemonicSize = mColumns.length;
		String[] erasedLabelMnemonic = new String[mnemonicSize -1];
		for (int i=0; i<mnemonicSize-1; i++) {
			erasedLabelMnemonic[i] = mColumns[i+1];
		}
		return erasedLabelMnemonic;
	}
	
	
	
	private void secondPass(String[] intermediateFile) {
		int Lline = 0;
		int index = 0;
		this.LOCCTR = this.startAddress;
		
		System.out.println(String.format("%-7s %-7s %-30s %-10s", "Line", "Loc", "Source Statement", "Object Code"));
		System.out.println("-------------------------------------------------------------------------------------------------------------------------------");
		
		//Escrita cabeçalho - INICIO
		String [] mColumns = intermediateFile[index++].split(" ");
		String name = "";
		if (!SicXeReservedWords.isReservedWord(mColumns[0]))
			name = mColumns[0];
		
		objectProgram = new ObjectProgram(	String.format("%-6s", name).replace(' ', '0'), 
											String.format("%6s", Integer.toHexString(startAddress)).replace(' ', '0'), 
											String.format("%6s", Integer.toHexString(programLenght)).replace(' ', '0')	);
		// Escrita de Cabeçalho - FIM
	
		// Escrita de Texto - INICIO
		String opcode = "";
		mColumns = intermediateFile[index].split(" ");
		opcode = mColumns[0];
		if (opcode.charAt(0) == '+') 
			opcode = opcode.substring(1);
		
		do {
			if (SicXeReservedWords.isDirective(opcode)) {
				int value = 0;
				if (opcode.equals("BYTE") == false)
					value = Integer.parseInt(mColumns[1]);
				
				if ( opcode.equals("RESW") || opcode.equals("RESB") ) {
					
					objectProgram.finishTextLine();
					if (opcode.equals("RESW")) 
						LOCCTR += (3*value);
					else if (opcode.equals("RESB"))
						LOCCTR+=value;		
					
					this.listLine(mColumns, Lline++, index);
				}
				else { // if (isInstruction)
					
					if (opcode.equals("WORD")) {
						objectProgram.addToText(Integer.toHexString(value), LOCCTR);
						LOCCTR += 3;
						this.listLine(mColumns, Lline++, LOCCTR, DataUtils.to6BytesAdressingFormat(Integer.toHexString(value)));
					}
					else if (opcode.equals("BYTE")) {
						if (mColumns[1].charAt(0) == 'H') {
							String hexValue = mColumns[1].substring(mColumns[1].indexOf('\'') + 1, mColumns[1].lastIndexOf('\'')); // Retira valor do format H'valor'
							objectProgram.addToText(hexValue, LOCCTR);
							LOCCTR += Math.ceilDiv(hexValue.length(), 2);
							this.listLine(mColumns, Lline++, LOCCTR, hexValue);
						}
						else {
							String charValues =  mColumns[1].substring(mColumns[1].indexOf('\'') + 1, mColumns[1].lastIndexOf('\''));
							String hexASCIIValue = "";
							for (int i = 0; i < charValues.length(); i++) 
								hexASCIIValue = hexASCIIValue.concat(Integer.toHexString((int) charValues.charAt(i)));
							objectProgram.addToText(hexASCIIValue, LOCCTR);
							LOCCTR += charValues.length();
							this.listLine(mColumns, Lline++, LOCCTR, hexASCIIValue);

						}
					}
				}
			}
			else if (SicXeReservedWords.isInstruction(opcode)) {
				UserInstruction binaryInstruction = translateInstruction(mColumns);
				objectProgram.addToText(binaryInstruction.toString(), LOCCTR);
				LOCCTR += binaryInstruction.getFormat();
				this.listLine(mColumns, Lline++, LOCCTR, binaryInstruction.toString());
			}
			
			index++;
			mColumns = intermediateFile[index].split(" ");
			opcode = mColumns[0];
			if (opcode.charAt(0) == '+') 
				opcode = opcode.substring(1);					
		}while (! opcode.equals("END"));		
		if (mColumns.length == 2)
			objectProgram.endObjectProg(mColumns[1]);
		else
			objectProgram.endObjectProg();
	
		
	}
	

	/*
	 =================================== 
	 Assembler::translateInstruction
	 	Transforma mnemônico em código de máquina
	 =================================== 
	*/
	public UserInstruction translateInstruction (String[] mnemonicInstrucion) {
		UserInstruction binaryInstruction;
		int opcode;
		String format;
		int integerFormat;
		
		
		String sOpcode = mnemonicInstrucion[0];				//Symbolic Opcode
		boolean extended = false;
		if (sOpcode.charAt(0) == '+') {
			extended = true;
			sOpcode = sOpcode.substring(1);
		}
		String operandA = "", operandB = "";
		operandA = mnemonicInstrucion[1];
		if (mnemonicInstrucion.length == 3)
			operandB = mnemonicInstrucion[2];
		
		Map<Integer, String> info = vmInstructions.getInfo(sOpcode);
		opcode = (int) info.keySet().toArray()[0];
		format = info.get(opcode);
		if (format.equals("3/4") && extended)
			format = "4";
		else
			format = "3";
		integerFormat = Integer.parseInt(format);
		binaryInstruction = new UserInstruction(integerFormat);
		binaryInstruction.setOpcode(opcode);
		
		
		switch (integerFormat) {
		case 1:
			break;
		case 2:
			int firstRegIndex = vmRegisters.getRegisterValue(operandA);
			int secondRegIndex = vmRegisters.getRegisterValue(operandB);
			binaryInstruction.setRegisters(firstRegIndex, secondRegIndex);
			break;
		case 3:
			if (SYMTAB.contains(operandA))
				operandA = SYMTAB.get(operandA);
			flagSetter(binaryInstruction, integerFormat,operandA);
			
			int dispValue = this.toInteger(operandA);
			binaryInstruction.setDisp(dispValue);
			break;
		case 4:
			if (SYMTAB.contains(operandA))
				operandA = SYMTAB.get(operandA);
			flagSetter(binaryInstruction, integerFormat,operandA);
			binaryInstruction.setFlagE();
			
			int addressValue = this.toInteger(operandA);
			binaryInstruction.setDisp(addressValue);
			break;
		}
		
		return binaryInstruction;
	}
	
	
	public void listLine (String[] mColumns, int line, int loc, String ObjectCode) {
		
		String statmentA = mColumns[0];
		String statmentB = "",statmentC = "";
		if (mColumns.length>=2) 
			statmentB = mColumns[1];
		if (mColumns.length>=3)
			statmentC = mColumns[2];
		
		System.out.println(String.format("%-7s %-7s %-30s %-10s", line*5, loc, statmentA + " " + statmentB + " " + statmentC, ObjectCode));	
	}
	public void listLine (String[] mColumns, int line, int loc) {
		String statmentA = mColumns[1];
		String statmentB = "";
		if (mColumns.length==2)
			statmentB = mColumns[1];
		
		System.out.println(String.format("%-7s %-7s %-30s", line*5, loc, statmentA + " " + statmentB));
	}
	
	/*
	 =================================== 
	 Assembler::flagSetter
	 =================================== 
	*/
	public void flagSetter (UserInstruction binaryInstruction, int format, String operand) {
		// Define o tipo de endereçamento (direto, indireto, imediato)
		if (Character.isDigit(operand.charAt(0)))
			binaryInstruction.setAsDirect();
		else {
			if (operand.charAt(0) == '@')
				binaryInstruction.setAsIndirect();
			else if (operand.charAt(0) == '#')
				binaryInstruction.setAsImmediate();
		}
		
		// Define se tem relatividade a outros registradores (base, X, PC)
		String complement = "";
		if (operand.contains(",")) {
		    complement = operand.split(",")[1];
		}
		switch (complement) {
			case "X":
				binaryInstruction.setFlagX();
				break;
			case "PC":
				binaryInstruction.setFlagP();
				break;
			case "B":
				binaryInstruction.setFlagB();
		}
	}

	
	
	
	/*
	 ===================================
	 Assembler::toInteger
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
	
	
	/*
	 ===================================
	 VMSimulator::isLabel
	 ===================================
	*/
	boolean isLabel (String mnemonic) {
		return ! (SicXeReservedWords.isReservedWord(mnemonic));
	}
	/*
	 ===================================
	 VMSimulator::isDirective
	 ===================================
	*/
	boolean isDirective (String mnemonic) {
		return SicXeReservedWords.isDirective(mnemonic);
	}
	/*
	 ===================================
	 VMSimulator::isDirective
	 ===================================
	*/
	boolean isInstruction (String mnemonic) {
		return SicXeReservedWords.isInstruction(mnemonic);
	}
}

