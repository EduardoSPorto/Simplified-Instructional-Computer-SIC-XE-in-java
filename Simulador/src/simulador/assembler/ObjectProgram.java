package simulador.assembler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongConsumer;

public class ObjectProgram {
	String header;
	List<String> text;
	String end;
	int textLines;
	int textLineBytes;
	
	
	public ObjectProgram () {
		header = "H";
		text = new ArrayList<>();
		end = "E000000";
		
		text.add("T");
		textLines = 1;
		textLineBytes = 1;
	}
	
	public void defineName (String name) {
		name = name.substring(0, 6);
		if (name.length() < 6) {
			name = to6BytesAdressingFormat(name);
		}
		this.header.concat(name);
	}
	
	public void defineHeadingAddress (String address) {
		if (address.length() < 6)
			address = to6BytesAdressingFormat(address);
		this.header.concat(address);
	}
	
	public void addTextContent (String objectCode, String LOCCTR) {
		LOCCTR = Integer.toHexString(Integer.parseInt(LOCCTR));
		if (LOCCTR.length() < 6)
			LOCCTR = to6BytesAdressingFormat(LOCCTR);
		
		if (textLineBytes == 1) {
			text.get(textLines).concat(LOCCTR);
			textLineBytes+=6;
		}
		
	}
	
	public String to6BytesAdressingFormat (String oldAddressFormat) {
		return String.format("%6s", oldAddressFormat).replace(' ', '0');
	}
}
