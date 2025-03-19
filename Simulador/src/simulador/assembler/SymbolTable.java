package simulador.assembler;

import java.util.HashMap;
import java.util.Map;

class SymbolData {
	String Address;
	boolean extern;
}

public class SymbolTable {
	
	Map <String, SymbolData> table;
	
	
	public SymbolTable() {
		table = new HashMap<>();
	}
	
	public void insert (String symbol, Integer address, boolean extern) {
		SymbolData entry = new SymbolData();
		String hexAddress = "-1";
		if (address != null)  
			hexAddress = Integer.toHexString(address);
		entry.Address = hexAddress;
		entry.extern = extern;
		
		table.put(symbol, entry);
	}
	
	public String getHexAddress (String symbol) {
		
		return (table.get(symbol).Address);
	}
	public boolean isExtern (String symbol) {
		return table.get(symbol).extern;
	}
	
	
	public void modify (String symbol, Integer address, boolean extern) {
		String hexAddress = Integer.toHexString(address);
		SymbolData entry = new SymbolData();
		entry.Address = hexAddress;
		entry.extern = extern;
		
		table.replace(symbol, entry);
	}
	
	public boolean contains (String symbol) {
		return table.containsKey(symbol);
	}
	
	public String toString () {
		String asString = "";
		
		for (String key : table.keySet()) {
			asString = asString.concat(key + "," + this.getHexAddress(key)+ "\n");
		}
		return asString;
	}
}



