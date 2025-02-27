package simulador.assembler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongConsumer;

public class ObjectProgram {
	String header;
	List<String> text;
	String tempText; // Salva escrita em outro lugar para permitir modificar as colunas 8 e 9 que referem-se ao tamanho da linha de texto
	String end;
	int textLines;
	int textColumn;
	
	
	public ObjectProgram (String name, String startAddress, String lenght) {
		header = "H" + name + startAddress + lenght;
		text = new ArrayList<>();
		end = "E000000";
		
		text.add("T");
		textLines = 0;
		textColumn = 1;
	}
	
	/*
	 =================================== 
	 ObjectProgram::addTextContent
	 	Assume que o objectCode já está
	 	em formato hexádecimal
	 =================================== 
	*/
	public void addToText (String objectCode, int LOCCTR) {
		String hexLOCCTR = Integer.toHexString(LOCCTR);
		if (hexLOCCTR.length() < 6)
			hexLOCCTR = to6BytesAdressingFormat(hexLOCCTR);
		
		if (textColumn == 1) {
			text.get(textLines).concat(hexLOCCTR);
			textColumn+=8;	// 6 colunas para endereço inicial, 2 Colunas para tamaho da linha (bytes)
		}
		else if ( (textColumn + objectCode.length() ) >= 68) {
			finishTextLine(objectCode);
			addToText (objectCode, LOCCTR);
			return;
		}
		
		tempText.concat(objectCode);
		textColumn+=objectCode.length();
	}
	
	public void addToText () {
		if (textColumn == 1) 
			return;
		int objectCodeLenght = (textColumn - 9) / 2; 
		text.get(textLines).concat(Integer.toHexString(objectCodeLenght));
		text.get(textLines).concat(tempText);
		
		text.add("T");
		textLines++;
		textColumn = 1;
		
	}
	
	public void finishTextLine (String objectCode) {
		int objectCodeLenght = (textColumn - 9) / 2; 
		text.get(textLines).concat(Integer.toHexString(objectCodeLenght));
		text.get(textLines).concat(tempText);
		
		// Começa nova linha de text
		text.add("T");
		textLines++;
		textColumn = 1;
	}
	
	public String to6BytesAdressingFormat (String oldAddressFormat) {
		return String.format("%6s", oldAddressFormat).replace(' ', '0');
	}
}
