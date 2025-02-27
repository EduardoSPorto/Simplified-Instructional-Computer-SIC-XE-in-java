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
	
	
	
	public String[] firstPass (String[] mnemonics) {
		this.programLenght = 0;		
		String[] intermediateFile = new String[mnemonics.length];
		
		String[] mColumns = mnemonics[0].split(" ");
		String label = mColumns[0];
		String opcode = mColumns[1];
		
		// Inicializando com Start e definindo endereços iniciais
		if (label.equals("START")) {
			if ( !(opcode.isBlank()) )  {
				startAddress = Integer.parseInt(mColumns[1]);
				this.LOCCTR = startAddress;
				intermediateFile[0] = String.join(" ", mColumns);
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
		
		
		while (label != "END") {
			if (mColumns[0].contains(".")) 				// Pula linhas de comentário
				continue;
			
			if( hasLabel ) { 
				if (SYMTAB.contains(label)) 
					throw new IllegalArgumentException("Duplicated Label Definition");
				else {
					SYMTAB.insert(label, this.LOCCTR);
					updateLOCCTR(opcode, mColumns[2], isDirective, isInstruction);
					this.eraseLabel(mColumns); 			// Remove o Label do código texto, Montador conhece ele pela tabela de símbolos
				}
			}
			else { 	
				updateLOCCTR(opcode, mColumns[1], isDirective, isInstruction);
			}
			
			intermediateFile[index] = String.join(" ", mColumns);
			
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
		
		this.programLenght = this.LOCCTR - startAddress; 
		return intermediateFile;
	}
	
	public void updateLOCCTR (String opcode, String operand, boolean isDirective, boolean isIntruction) {
		boolean extended = false;
		if (opcode.contains("+")) {
			opcode = opcode.substring(1);
			extended = true;
			if ((isDirective == false) && !SicXeReservedWords.getInstructionFormat(opcode).equals("3/4"))
				throw new IllegalArgumentException("'+' Symbol is only applyable for instructions of type 4");
		}
		
		if ( !(SicXeReservedWords.isReservedWord(opcode)) )
			throw new IllegalArgumentException("Operation Undefined");
		else {
			if (isIntruction) {
				if (SicXeReservedWords.getInstructionFormat(opcode) == "3/4") {
					if (extended)
						this.LOCCTR = 4;
					else
						this.LOCCTR = 3;
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
				this.LOCCTR += 1;
				// Modificar para permitir a representação com valores maiores (Seguindo algoritmo do livro)
			}
		}
		
	}

	/*
	 ===================================
	 VMSimulator::eraseLabel
	 	Retira o Label do começo da linha
	 	Facilitando a manipulação daquele mnemonico
	 ===================================
	*/
	public void eraseLabel (String [] mColumns) {
		int mnemonicSize = mColumns.length;
		for (int i=0; i<mnemonicSize-1; i++) {
			mColumns[i] = mColumns[i+1];
		}
	}
	
	
	
	private void secondPass(String[] intermediateFile) {
		int Lline = 0;
		int index = 0;
		this.LOCCTR = this.startAddress;
		
		String [] mColumns = intermediateFile[index].split(" ");
		System.out.println(String.format("%-7s %-7s %-30s %-10s", "Line", "Loc", "Source Statement", "Object Code"));
		System.out.println("-------------------------------------------------------------------------------------------------------------------------------");
		
		//Escrita cabeçalho - INICIO
		String name = "";
		if (!SicXeReservedWords.isReservedWord(mColumns[0]))
			name = mColumns[0];
		
		objectProgram = new ObjectProgram(	String.format("%-6s", name).replace(' ', '0'), 
											String.format("%6s", Integer.toHexString(startAddress)).replace(' ', '0'), 
											String.format("%6s", Integer.toHexString(programLenght)).replace(' ', '0')	);
	
		String opcode = "";
		do {
			mColumns = intermediateFile[index].split(" ");
			opcode = mColumns[0];
		
			if (SicXeReservedWords.hasReservedSymbol(opcode)) {			// Só verifica e remove símbolos como + para facilitar leitura, Verificação de veracidade deve ser feita na etapa anterior ou com tabela de símbolos
				opcode = opcode.substring(1);					
			}
			
			if (SicXeReservedWords.isDirective(opcode)) {
				int value = Integer.parseInt(mColumns[1]);
				
				if (! (opcode.equals("RESW") || opcode.equals("RESB"))) 
				{
					objectProgram.addToText(Integer.toHexString(value), LOCCTR);
					if (opcode.equals("WORD")) 
						LOCCTR+=3;
					else if (opcode.equals("BYTE"))
						LOCCTR+=1;		// Pode ser mudado se for levar em contra a outra implementação do BYTE
					this.listLine(mColumns, Lline++, value, Integer.toHexString(value));
				}
				else
				{
					objectProgram.addToText();
					if (opcode.equals("WORD"))
						LOCCTR += (3*value);
					else if (opcode.equals("BYTE"))
						LOCCTR += value;
					this.listLine(mColumns, Lline, index);
				}
			}
			else if (SicXeReservedWords.isInstruction(opcode)) {
				UserInstruction currentInstruction = translateInstruction(mColumns);
				objectProgram.addToText(currentInstruction.toString(), LOCCTR);
				LOCCTR += currentInstruction.getFormat();
				this.listLine(mColumns, Lline++, index, currentInstruction.toString());
			}
			
			index++;
		}while (! opcode.equals("END"));		
		
		
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
		
		String statmentA = mColumns[1];
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
	 VMSimulator::flagSetter
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

