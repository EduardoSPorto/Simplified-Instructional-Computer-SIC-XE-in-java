package simulador.assembler;

import java.util.ArrayList;
import java.util.List;
import simulador.DataUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

class DefRecordFormat {
	public String symbol;
	public String address;
}
//class RefRecordFormat {
//	public String symbol;
//}
class ModRecordFormat {
	String startAddress; // 6 Columns
	String modifiableLenght; // 2 Columns -> Representa um Half Byte (4 bits)
	char modFlag;	// + or -
	String externalSymbol; 	// Ex: BUFFER ->  M00001805+^BUFFER^   
}


public class ObjectProgram {
	private String header;
	private List<String> text;
	private List<DefRecordFormat> defExtrn;
	private List<String> refExtrn;
	private List<ModRecordFormat> modRecord;
	private String tempText; // Salva escrita em outro lugar para permitir modificar as colunas 8 e 9 que referem-se ao tamanho da linha de texto
	private String end;
	private int textLine, textColumn;
	
	
	
	public ObjectProgram (String name, String startAddress, String lenght) {
		header = "H" + name + startAddress + lenght;
		text = new ArrayList<>();
		defExtrn = new ArrayList <>();
		refExtrn = new ArrayList <>();
		modRecord = new ArrayList<>();
		end = "E"+startAddress; // Por padrão é ele, mas se for indicado um novo endereço, então substitui
		
		text.add("T");
		tempText = "";
		textLine = 0;
		textColumn = 1;
	}
	
	
	/*
	 =================================== 
	 ObjectProgram::addTextContent
	 	Assume que o objectCode já está
	 	em formato hexádecimal
	 	
	 	2 Colunas representam 1 byte
	 	Cada coluna é um hexa
	 	NBits -> nColunas
	 	
	 =================================== 
	*/
	public void addToText (String objectCode, int type, int LOCCTR) {
		String hexLOCCTR = Integer.toHexString(LOCCTR);
		if (hexLOCCTR.length() < 6)
			hexLOCCTR = DataUtils.to6BitsAdressingFormat(hexLOCCTR);
		if (objectCode.length() < (type * 2))
			objectCode = DataUtils.toNBitsAddressingFormat(objectCode, type*2);
		
		if (textColumn == 1) {
			text.set(textLine, text.get(textLine).concat(hexLOCCTR));
			textColumn+= 9 ;	// 6 colunas para endereço inicial, 2 Colunas para tamaho da linha (bytes)
		}
		else if ( (textColumn + objectCode.length() ) >= 68 ) {
			finishTextLine();
			startNewTextLine();
			addToText (objectCode, type, LOCCTR);
			return;
		}
		
		tempText = tempText.concat(objectCode);
		textColumn+=objectCode.length();
		
	}
	public void finishTextLine () {
		if (textColumn == 1) 
			return;
		
		int objectCodeLenght = (textColumn - 9) / 2;
		String hexCodeLenght = Integer.toHexString(objectCodeLenght);
		if (hexCodeLenght.length() == 1)
			hexCodeLenght = "0" + hexCodeLenght;
		
		String t1 = hexCodeLenght + " " + this.tempText;		
		
		text.set( textLine, text.get(textLine).concat(" "+t1) );
		
	}
	public void startNewTextLine () {
			text.add("T");
			tempText = "";
			textLine++;
			textColumn = 1;
	}
	
	public void addModificationRecord (int LOCCTR, int hbLenght, char modFlag, String externalSymbol) {
		String hexLOCCTR = Integer.toHexString(LOCCTR);
		if (hexLOCCTR.length() < 6)
			hexLOCCTR = DataUtils.to6BitsAdressingFormat(hexLOCCTR);
		String hexLenght = Integer.toHexString(hbLenght);
		if (hexLenght.length() == 1)
			hexLenght = "0".concat(hexLenght);
		ModRecordFormat entry = new ModRecordFormat();
		entry.startAddress = hexLOCCTR;
		entry.modifiableLenght = hexLenght;
		entry.modFlag = modFlag;
		entry.externalSymbol = externalSymbol;
		this.modRecord.add(entry);

	}
	
	public void addToDefine (String symbol) {
		DefRecordFormat entry = new DefRecordFormat();
		
		
		if (symbol.length()>6)
			symbol = symbol.substring(0,6);
		else if (symbol.length()<6)
			symbol = String.format("%-6s", symbol);
			
		
		entry.symbol = symbol;
		
		this.defExtrn.add(entry);
	}
	public void setDefineRecordAddress (String Symbol, int LOCCTR) {
		String hexLOCCTR = Integer.toHexString(LOCCTR);
		if (hexLOCCTR.length() < 6)
			hexLOCCTR = DataUtils.to6BitsAdressingFormat(hexLOCCTR);
		
		DefRecordFormat entry = new DefRecordFormat();
		entry.symbol = Symbol;
		entry.address = hexLOCCTR;
		
		for (int i = 0; i < this.defExtrn.size(); i++) {
			String defName = this.defExtrn.get(i).symbol;
			if ( defName.equals(Symbol) ) {
				this.defExtrn.remove(i);
				this.defExtrn.add(i, entry);
				break;
			}
				
		}
		
	}
	public void addToRef (String symbol) {
		if (symbol.length()>6)
			symbol = symbol.substring(0,6);
		else if (symbol.length()<6)
			symbol = String.format("%-6s", symbol);
			
		this.refExtrn.add(symbol);
	}
	
	
	public void endObjectProg (SymbolTable SYMTAB) {
		this.finishTextLine();
		this.saveOnFile(SYMTAB);
	}
	public void endObjectProg (String StartingAddress, SymbolTable SYMTAB) {
		this.finishTextLine();
		this.end = "E" + StartingAddress;
		this.saveOnFile(SYMTAB);
	}
	
	public void saveOnFile (SymbolTable SYMTAB) {
		int i = 0;
		boolean fileCreated = false;
		File obj=null;
		FileWriter writer = null;

		
		try {
			
			while (fileCreated == false) {
					obj = new File ("ObjectProgs"+File.separator+"Prog" + i + ".obj");
					
					if (obj.createNewFile() == true)
						fileCreated = true;
					else
						i++;						
			}
			writer = new FileWriter (obj);
			writer.write(header+"\n");
			
			this.writeDefRecord(writer);
			
			this.writeRefRecord(writer);
			
			this.writeText(writer);
			
			this.writeModificationRecord(writer);
			
			writer.write(end);
			writer.close();
			
		}
		catch (IOException e) {
			System.out.println("An error ocurred.");
			e.printStackTrace();
		}
		
	}
	
	
	public void writeDefRecord (FileWriter writer) throws IOException {
		String buffer;
		int i = 0;
		
		buffer = "D"; 
		for ( i=0; i<this.defExtrn.size(); i++ ) {
			DefRecordFormat entry = defExtrn.get(i);
			String t1 = entry.symbol;
			t1 = t1.concat(entry.address);
			buffer = buffer.concat(" " + t1 );
		}
		
		if (i > 0) {
			writer.write(buffer + "\n");		
		}		
		
	}
	public void writeRefRecord (FileWriter writer) throws IOException {
	
		String buffer;
		int i = 0;
		
		buffer = "R";
		for ( i=0; i<this.refExtrn.size(); i++ ) {
			String t1 = refExtrn.get(i);
			buffer = buffer.concat(" " + t1);
		}
		if ( i>0 ) {
			writer.write(buffer + "\n");
		}
	}
	
	public void writeText (FileWriter writer) throws IOException {
		
		for(int i = 0; i <= this.textLine; i++) {
			String tLine = this.text.get(i);
			if (tLine.length() != 1)
				writer.write(this.text.get(i)+"\n");
		}
	}
	
	public void writeModificationRecord (FileWriter writer) throws IOException{
		
		for ( int i=0; i<this.modRecord.size(); i++ ) {
			ModRecordFormat entry = this.modRecord.get(i);
			String t1 = "M";
			t1 = t1.concat(	entry.startAddress + " " + 
							entry.modifiableLenght + " " +
							entry.modFlag + " " +
							entry.externalSymbol);
			writer.write(t1 + "\n");
		}
	}
}






/*
 * H (6 col | ProgName) (6 Col | Endereço Inicial) ( 6 | Tamanho em bytes (Hex) ) 
 * T (6 Col | Endereço inicial ) (2 Col | Tamanho do campo text em bytes (hex) ( 3 Col | Relocabilidade 
 * E
 */
