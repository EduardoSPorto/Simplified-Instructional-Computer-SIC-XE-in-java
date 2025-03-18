package simulador.assembler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectStreamConstants;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import simulador.DataUtils;
import simulador.Main;
import simulador.Memory;
import simulador.Registers;
import simulador.SicXeReservedWords;
import simulador.instrucao.InstructionSet;
import simulador.instrucao.UserInstruction;
import simulador.instrucao.VMInstruction;
import simulador.loader.Loader;

class IntermediateFile {
	List <String> mnemonics;
	List <String> DefSymbols;
	List <String> RefSymbols;
}


public class Assembler {
	Memory vmMemory;
	InstructionSet vmInstructions;
	Registers vmRegisters;
	Loader loader;
	int startAddress;
	int LOCCTR;
	int programLenght;
	ObjectProgram objectProgram;
	
	public Assembler (Memory vmMemory, InstructionSet vmInstructionSet, Registers vmRegisters, Loader loader) {
		this.vmMemory =vmMemory;
		this.vmInstructions = vmInstructionSet;	
		this.vmRegisters = vmRegisters;
		this.loader = loader;	
	}
	
	public void execute () throws IOException {
		String fileName = "MASMAPRG";
		List< String[] > mnemonics = new ArrayList<>();
		int index = 0;
		boolean fileExists = true;
		while (fileExists) {
			try {
				mnemonics.add(readFile("AssemblyCodes"+File.separator+ fileName + index + ".ASM"));
				index++;
			} catch (IOException e) {
				fileExists = false;
			}
		}
		
		for (int i = 0; i < index; i++) {
			SymbolTable SYMTAB = new SymbolTable ();
			IntermediateFile intermediateFile = new IntermediateFile();
			intermediateFile.DefSymbols = new ArrayList<>();
			intermediateFile.RefSymbols = new ArrayList<>();
			
			
			firstPass (mnemonics.get(i), SYMTAB, intermediateFile);
			secondPass (intermediateFile, SYMTAB);
		}
		loader.firstPass();
		
	}
	
	
	/*
	 ===================================
	 Assembler::readFile
	 	Lê arquivo.ASM e retorna um  
	 	String[] mnemonicCode com o conteúdo
	 ===================================
	*/
	public String [] readFile (String filePath) throws FileNotFoundException {
		List <String> mnemoniCode = new ArrayList<String>();
		String codeLine = "";
		
		BufferedReader buffReader = null;
		buffReader = new BufferedReader (new FileReader (filePath));
		try {
			do {
				codeLine = buffReader.readLine();
				mnemoniCode.add(codeLine);
			} while (codeLine != null);
			buffReader.close();
		} catch (IOException e) {
			System.err.println(filePath + " Presents an Error in Coding syntax");
			e.printStackTrace();
		}
		
		return mnemoniCode.toArray(new String[0]);
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
	public void firstPass (String[] mnemonics, SymbolTable SYMTAB, IntermediateFile intermediateFile) {
		int counter = 0;
		this.programLenght = 0;		
		intermediateFile.mnemonics = new ArrayList<>();
		
		String[] mColumns = mnemonics[counter++].split(" ");
		
		while (mColumns[0].charAt(0) == '.')
			mColumns = mnemonics[counter++].split(" ");
		
		ControlData controlData = new ControlData();
		
		this.processInstruction(mColumns, controlData);

		if (controlData.opcode.equals("START")) {
			boolean startAddressSpecified = controlData.operandA != null;
			
			if ( startAddressSpecified )  {
				startAddress = Integer.parseInt(controlData.operandA);
				this.LOCCTR = startAddress;
			} else {
				startAddress = 0;
				this.LOCCTR = 0;
			}
			
			intermediateFile.mnemonics.add( String.join(" ", mColumns) ) ;
		}
		else
			throw new IllegalArgumentException("Lack of initialization with START directive");
		
		mColumns = mnemonics[counter++].split(" ");
		this.processInstruction(mColumns, controlData);
				
		while (controlData.opcode.equals("END") == false) {
			boolean ignoreLine = false;
			
			if (mColumns[0].contains(".")) { 				
				ignoreLine = true;
			}
			
			// Add ExtREF and ExtDEF to Intermediate File
			if (controlData.opcode.equals("EXTREF")) {
				for ( int j = 1; j < mColumns.length; j ++) {
					SYMTAB.insert(mColumns[j], null, true);
					intermediateFile.RefSymbols.add(mColumns[j]);
				}
				ignoreLine = true;
			} else if ( controlData.opcode.equals("EXTDEF") ) {
				for ( int j = 1; j < mColumns.length; j ++) {
					SYMTAB.insert(mColumns[j], null, true);
					intermediateFile.DefSymbols.add(mColumns[j]);
				}
				ignoreLine = true;
			}
			
			// Verify for other Labels
			if (ignoreLine == false) {
				boolean extended;
				if( controlData.hasLabel ) { 
					if (SYMTAB.contains( controlData.label )) { 
						if (SYMTAB.isExtern( controlData.label )) {
							SYMTAB.modify( controlData.label, this.LOCCTR, true);
						} else
							throw new IllegalArgumentException("Duplicated Label definition");
					} else {
						SYMTAB.insert(controlData.label, this.LOCCTR, false);
					}
					extended = verifyOpcode (controlData.opcode, controlData.isDirective);
					updateLOCCTR(controlData.opcode, mColumns[2], controlData.isInstruction, extended);	
					mColumns = this.eraseLabel(mColumns); 			// Remove o Label do código texto, Montador conhece ele pela tabela de símbolos
				}
				else { 	
					extended = verifyOpcode (controlData.opcode, controlData.isDirective);
					updateLOCCTR(controlData.opcode, mColumns[1], controlData.isInstruction, extended);
				}
				// Salva no arquivo intermediario após atualizar tabela de símbolos e remover os labels do começo.
				intermediateFile.mnemonics.add(String.join(" ", mColumns));				
			}
			mColumns = mnemonics[counter++].split(" ");
			this.processInstruction(mColumns, controlData);

		}
		
		intermediateFile.mnemonics.add( String.join(" ", mColumns) );
		this.programLenght = this.LOCCTR - startAddress; 
	}
	
	
	public void processInstruction (String[] mnemonics, ControlData controlData) {
		controlData.hasLabel = this.isLabel(mnemonics[0]);
		if (controlData.hasLabel) {
			controlData.label 	= mnemonics[0];
			controlData.opcode 	= mnemonics[1];
			if (mnemonics.length == 3)
				controlData.operandA= mnemonics[2];
			if (mnemonics.length == 4)
				controlData.operandB = mnemonics[3];
			else
				controlData.operandB = "";	
		} else {
			controlData.opcode 	= mnemonics[0];
			controlData.label 	= "";
			if (mnemonics.length == 2)
				controlData.operandA= mnemonics[1];
			if (mnemonics.length == 3)
				controlData.operandB = mnemonics[2];
			else
				controlData.operandB = "";
		}
		if (this.isDirective(controlData.opcode)) {
			controlData.isDirective = true;
			controlData.isInstruction = false;
		} else {
			controlData.isInstruction = true;
			controlData.isDirective = false;
		}
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
	
	
	
	private void secondPass(IntermediateFile intermediateFile, SymbolTable SYMTAB) {
		int Lline = 0;
		int index = 0;
		this.LOCCTR = this.startAddress;
		
		System.out.println(String.format("%-7s %-7s %-30s %-10s", "Line", "Loc", "Source Statement", "Object Code"));
		System.out.println("-------------------------------------------------------------------------------------------------------------------------------");
		
		//Escrita cabeçalho - INICIO
		String [] mColumns = intermediateFile.mnemonics.removeFirst().split(" ");
		String name = "";
		if (!SicXeReservedWords.isReservedWord(mColumns[0]))
			name = mColumns[0];
		
		objectProgram = new ObjectProgram(	String.format("%-6s", name), 
											String.format("%6s", Integer.toHexString(startAddress)).replace(' ', '0'), 
											String.format("%6s", Integer.toHexString(programLenght)).replace(' ', '0')	);
		// Escrita de Cabeçalho - FIM
		// Escrita de DEF e REF - INICIO
		for (int i = 0; i < intermediateFile.DefSymbols.size() ; i ++) {
			String symbol = intermediateFile.DefSymbols.get(i);
			objectProgram.addToDefine(symbol, SYMTAB.getHexAddress(symbol));
		}
		for ( int i=0; i<intermediateFile.RefSymbols.size(); i++ ) {
			objectProgram.addToRef( intermediateFile.RefSymbols.get(i) );
		}
		// Escrita de DEF e REF - FIM
		
	
		// Escrita de Texto - INICIO
		String opcode = "";
		mColumns = intermediateFile.mnemonics.removeFirst().split(" ");
		opcode = mColumns[0];
		if (opcode.charAt(0) == '+') 
			opcode = opcode.substring(1);
		
		do {
			if (SicXeReservedWords.isDirective(opcode)) {
				int value = 0;
				if (opcode.equals("BYTE") == false)			// Byte tem campo com letras
					value = Integer.parseInt(mColumns[1]); 
				
				if ( opcode.equals("RESW") || opcode.equals("RESB") ) {
					
					this.listLine(mColumns, Lline++, LOCCTR);
					
					if (opcode.equals("RESW")) 
						LOCCTR += (3*value);
					else if (opcode.equals("RESB"))
						LOCCTR+=value;		
					
				} else { 
					
					if (opcode.equals("WORD")) {
						this.listLine(mColumns, Lline++, LOCCTR, DataUtils.to6BitsAdressingFormat(Integer.toHexString(value), true));
						
						objectProgram.addToText(Integer.toHexString(value), 3,LOCCTR, 'a', 'd');
						LOCCTR += 3;
					} else if (opcode.equals("BYTE")) {
						int numBytes;
						if (mColumns[1].charAt(0) == 'H') {
							String hexValue = mColumns[1].substring(mColumns[1].indexOf('\'') + 1, mColumns[1].lastIndexOf('\'')); // Retira valor do format H'valor'
							this.listLine(mColumns, Lline++, LOCCTR, hexValue);
							numBytes = Math.ceilDiv(hexValue.length(), 2);
							
							objectProgram.addToText(hexValue, numBytes,LOCCTR, 'a','d');
							LOCCTR += numBytes;
						}
						else { // Byte como Caractere
							String charValues =  mColumns[1].substring(mColumns[1].indexOf('\'') + 1, mColumns[1].lastIndexOf('\''));
							String hexASCIIValue = "";
							for (int i = 0; i < charValues.length(); i++) 
								hexASCIIValue = hexASCIIValue.concat(Integer.toHexString((int) charValues.charAt(i)));
							this.listLine(mColumns, Lline++, LOCCTR, hexASCIIValue);
							numBytes = charValues.length();
							
							objectProgram.addToText(hexASCIIValue, numBytes, LOCCTR, 'a', 'd');
							LOCCTR += numBytes;

						}
					}
				}
			} else if (SicXeReservedWords.isInstruction(opcode)) {
				UserInstruction binaryInstruction = translateInstruction(mColumns, SYMTAB);
				this.listLine(mColumns, Lline++, LOCCTR, binaryInstruction.toString());
				int numBytes = binaryInstruction.getFormat();
				
				if ( binaryInstruction.isImmediate() ) {
					objectProgram.addToText(binaryInstruction.toString(), numBytes, LOCCTR, 'a', 'i');
				} else {
					if (binaryInstruction.getFormat() == 2) {
						objectProgram.addToText(binaryInstruction.toString(), numBytes, LOCCTR, 'a', 'i');						
					} else {
						if ( SicXeReservedWords.isReservedWord(mColumns[1]) )
							objectProgram.addToText(binaryInstruction.toString(), numBytes, LOCCTR, 'a', 'i');
						else {
							if (SYMTAB.isExtern(mColumns[1]))
								objectProgram.addToText(binaryInstruction.toString(), numBytes, LOCCTR, 'a', 'i');
							else
								objectProgram.addToText(binaryInstruction.toString(), numBytes, LOCCTR, 'r', 'i');
						}
						
					}
				}
					
				
				LOCCTR += numBytes;
			}
			
			
			mColumns = intermediateFile.mnemonics.removeFirst().split(" ");
			opcode = mColumns[0];
			if (opcode.charAt(0) == '+') 
				opcode = opcode.substring(1);					
		}while (opcode.equals("END") == false);		
		if (mColumns.length == 2)
			objectProgram.endObjectProg(mColumns[1], SYMTAB);
		else
			objectProgram.endObjectProg(SYMTAB);
		
		
		
	}
	

	/*
	 =================================== 
	 Assembler::translateInstruction
	 	Transforma mnemônico em código de máquina
	 =================================== 
	*/
	public UserInstruction translateInstruction (String[] mnemonicInstrucion, SymbolTable SYMTAB) {
		UserInstruction binaryInstruction;
		int opcode;
		String format;
		int integerFormat;
		boolean extended;
		
		
		String sOpcode = mnemonicInstrucion[0];				//Symbolic Opcode
		extended = false;
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
		
		
		
		if (integerFormat == 2) {
			int firstRegIndex = vmRegisters.getRegisterValue(operandA);
			int secondRegIndex = vmRegisters.getRegisterValue(operandB);
			binaryInstruction.setRegisters(firstRegIndex, secondRegIndex);
		}
		else { // Desconsidera Existência de instruções tipo 1
			
			int address = 0;
			Character complement = null;
			boolean startWithComplement = Character.isLetter(operandA.charAt(0)) == false;
			if (startWithComplement) {
				complement = operandA.charAt(0);
				operandA = operandA.substring(1);
			}
			
			if (SYMTAB.contains(operandA)) {
				boolean hasExternalSymbol = SYMTAB.getHexAddress(operandA).equals("-1");
				if (hasExternalSymbol) {
					
					objectProgram.setDefineRecordAddress(operandA, LOCCTR);
					
					Character modFlag;
					if (complement != null && ( complement == '+' || complement == '-') )
						modFlag = complement;
					else
						modFlag = '+';
					
					if (extended)
						objectProgram.addModificationRecord(LOCCTR+1, 5, modFlag, operandA);
					else
						objectProgram.addModificationRecord(LOCCTR+1, 3, modFlag, operandA);
					
					binaryInstruction.setAsDirect();
					address = 0;
				}
				else {
					operandA = SYMTAB.getHexAddress(operandA);
					flagSetter(binaryInstruction, integerFormat, operandA);
					address = this.toInteger(operandA);
				}
			} else {
				if (Character.isLetter(operandA.charAt(0)))
					throw new IllegalArgumentException("Symbol" + operandA + "not Defined");
				flagSetter(binaryInstruction, integerFormat, complement+operandA);
				address = this.toInteger(operandA);
			}
			
			if (extended) {
				binaryInstruction.setFlagE();
				binaryInstruction.setAddress(address);
			} else {
				binaryInstruction.setDisp(address);
			}
				
		}
		
		return binaryInstruction;
	}
	
	
	public void listLine (String[] mColumns, int line, int loc, String ObjectCode) {
		
		String hexLoc = DataUtils.to6BitsAdressingFormat(Integer.toHexString(loc), true);

		String statmentA = mColumns[0];
		String statmentB = "",statmentC = "";
		if (mColumns.length>=2) 
			statmentB = mColumns[1];
		if (mColumns.length>=3)
			statmentC = mColumns[2];
		
		System.out.println(String.format("%-7s %-7s %-30s %-10s", line*5, hexLoc, statmentA + " " + statmentB + " " + statmentC, ObjectCode));	
	}
	public void listLine (String[] mColumns, int line, int loc) {
		String hexLoc = DataUtils.to6BitsAdressingFormat(Integer.toHexString(loc), true);
		String statmentA = mColumns[0];
		String statmentB = "";
		if (mColumns.length==2)
			statmentB = mColumns[1];
		
		System.out.println(String.format("%-7s %-7s %-30s", line*5, hexLoc, statmentA + " " + statmentB));
	}
	
	/*
	 =================================== 
	 Assembler::flagSetter
	 	Ajusta flasg de Endereçamento e
	 	relatividade
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

	
	public int toInteger (String strValue) {
		strValue = strValue.split(",")[0];
		if (strValue.contains("#") || strValue.contains("@"))
			strValue = strValue.substring(1);
		if (strValue.contains("X") || strValue.contains("x"))
			return Integer.parseUnsignedInt(strValue.substring(1), strValue.lastIndexOf('\''));
		return Integer.parseInt(strValue,16);
		
	}	
	public boolean isLabel (String mnemonic) {
		return ! (SicXeReservedWords.isReservedWord(mnemonic));
	}
	public boolean isDirective (String mnemonic) {
		return SicXeReservedWords.isDirective(mnemonic);
	}
	public boolean isInstruction (String mnemonic) {
		return SicXeReservedWords.isInstruction(mnemonic);
	}
	
}


class ControlData {
	public String operandB;
	String label;
	String opcode;
	String operandA;
	boolean hasLabel;
	boolean isDirective;
	boolean isInstruction;
}


