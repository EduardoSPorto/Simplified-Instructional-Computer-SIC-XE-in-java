package simulador.assembler;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
	
	Map <String, Integer> table;
	
	
	public SymbolTable() {
		table = new HashMap();
	}
	
	public void insert (String symbol, Integer address) {
		table.put(symbol, address);
	}
	
	public String get (String symbol) {
		return String.valueOf(table.get(symbol));
	}
	
	public void modify (String symbol, Integer address) {
		table.replace(symbol, address);
	}
	
	public boolean contains (String symbol) {
		return table.containsKey(symbol);
	}
}
