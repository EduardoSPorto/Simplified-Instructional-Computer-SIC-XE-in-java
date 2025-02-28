package simulador.assembler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongConsumer;

import simulador.DataUtils;

public class ObjectProgram {
	String header;
	List<String> text;
	String tempText; // Salva escrita em outro lugar para permitir modificar as colunas 8 e 9 que referem-se ao tamanho da linha de texto
	String end;
	int textLine;
	int textColumn;
	
	
	public ObjectProgram (String name, String startAddress, String lenght) {
		header = "H" + name + startAddress + lenght;
		text = new ArrayList<>();
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
	 =================================== 
	*/
	public void addToText (String objectCode, int LOCCTR) {
		String hexLOCCTR = Integer.toHexString(LOCCTR);
		if (hexLOCCTR.length() < 6)
			hexLOCCTR = DataUtils.to6BytesAdressingFormat(hexLOCCTR);
		if (objectCode.length() < 6)
			objectCode = DataUtils.to6BytesAdressingFormat(objectCode);
		
		if (textColumn == 1) {
			text.set(textLine, text.get(textLine).concat(hexLOCCTR));
			textColumn+=8;	// 6 colunas para endereço inicial, 2 Colunas para tamaho da linha (bytes)
		}
		else if ( (textColumn + objectCode.length() ) >= 68) {
			finishTextLine();
			startNewTextLine();
			addToText (objectCode, LOCCTR);
			return;
		}
		
		tempText = tempText.concat(objectCode);
		textColumn+=objectCode.length();
	}
	
	public void finishTextLine () {
		if (textColumn == 1) 
			return;
		int objectCodeLenght = (textColumn - 9) / 2; 
		String t1 = Integer.toHexString(objectCodeLenght).concat(tempText);
		text.set(textLine, text.get(textLine).concat(t1));
		
	}
	public void startNewTextLine () {
			text.add("T");
			tempText = "";
			textLine++;
			textColumn = 1;
	}
	
	public void endObjectProg () {
		this.finishTextLine();
	}
	
	public void endObjectProg (String StartingAddress) {
		this.finishTextLine();
		this.end = "E" + StartingAddress;
	}
	
}
