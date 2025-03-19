package simulador.assembler;

import java.util.ArrayList;
import java.util.List;
import simulador.DataUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;




public class ObjectProgram {
	private String header;
	private List<String> text;
	private List<DefRecordFormat> defExtrn;
	private List<String> refExtrn;
	private List<ModRecordFormat> modRecord;
	private String end;
	
	
	
	public ObjectProgram (String name, String startAddress, String lenght) {
		header = "H" + name + startAddress + lenght;
		text = new ArrayList<>();
		defExtrn = new ArrayList <>();
		refExtrn = new ArrayList <>();
		modRecord = new ArrayList<>();
		end = "E"+startAddress; // Por padrão é ele, mas se for indicado um novo endereço, então substitui
	
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
	public void addToText (String objectCode, int type, int LOCCTR, char relocMode, char typeDef) {
		String hexLOCCTR = Integer.toHexString(LOCCTR);
		if (hexLOCCTR.length() < 6)
			hexLOCCTR = DataUtils.to6BitsAdressingFormat(hexLOCCTR, true);
		if (objectCode.length() < (type * 2))
			objectCode = DataUtils.toNBitsAddressingFormat(objectCode, type*2, true);
		
		text.add("T"+hexLOCCTR+relocMode+typeDef+objectCode);
		
	}
	
	public void addModificationRecord (int LOCCTR, int hbLenght, char modFlag, String externalSymbol) {
		String hexLOCCTR = Integer.toHexString(LOCCTR);
		if (hexLOCCTR.length() < 6)
			hexLOCCTR = DataUtils.to6BitsAdressingFormat(hexLOCCTR, true);
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
	
	public void addToDefine (String symbol, String hexAddress) {
		DefRecordFormat entry = new DefRecordFormat();
		
		
		if (symbol.length()>6)
			symbol = symbol.substring(0,6);
		else if (symbol.length()<6)
			symbol = String.format("%-6s", symbol);
		
		if (hexAddress.length() < 6 )
			hexAddress = DataUtils.to6BitsAdressingFormat(hexAddress, true);
		
		entry.symbol = symbol;
		entry.address = hexAddress;
		
		this.defExtrn.add(entry);
	}
	public void setDefineRecordAddress (String Symbol, int LOCCTR) {
		String hexLOCCTR = Integer.toHexString(LOCCTR);
		if (hexLOCCTR.length() < 6)
			hexLOCCTR = DataUtils.to6BitsAdressingFormat(hexLOCCTR, true);
		
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
		this.saveOnFile(SYMTAB);
	}
	public void endObjectProg (String StartingAddress, SymbolTable SYMTAB) {
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
			buffer = buffer.concat(t1 );
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
			buffer = buffer.concat(t1);
		}
		if ( i>0 ) {
			writer.write(buffer + "\n");
		}
	}
	
	public void writeText (FileWriter writer) throws IOException {
		for(String textLine : this.text) {
			writer.write(textLine + "\n");
		}
	}
	
	public void writeModificationRecord (FileWriter writer) throws IOException{
		
		for ( int i=0; i<this.modRecord.size(); i++ ) {
			ModRecordFormat entry = this.modRecord.get(i);
			String t1 = "M";
			t1 = t1.concat(	entry.startAddress +  
							entry.modifiableLenght +
							entry.modFlag + 
							entry.externalSymbol);
			writer.write(t1 + "\n");
		}
	}
}




class DefRecordFormat {
	public String symbol; 	//6 Cplumns
	public String address;	//6 Columns
	
	public String toString () {
		return symbol + address;
	}
}
//class RefRecordFormat {
//	public String symbol;
//}
class ModRecordFormat {
	String startAddress; // 6 Columns
	String modifiableLenght; // 2 Columns -> Representa um Half Byte (4 bits)
	char modFlag;	// + or -
	String externalSymbol; 	// Ex: BUFFER ->  M00001805+^BUFFER^   
	
	public String toString() {
		return startAddress + modifiableLenght + modFlag + externalSymbol;
	}
}

/*
 * H (6 col | ProgName) (6 Col | Endereço Inicial) ( 6 | Tamanho em bytes (Hex) ) 
 * T (6 Col | Endereço inicial ) (2 Col | Tamanho do campo text em bytes (hex) ( 3 Col | Relocabilidade 
 * E
 */
