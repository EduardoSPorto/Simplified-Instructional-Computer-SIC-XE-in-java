package simulador;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class SicXeReservedWords {
	private static Map<String, String[]> instructions;
	private static Set<String> directives;
	private static HashSet<Character> symbols;
	
	static{
		instructions 	= new HashMap <>();
		directives 		= new HashSet <String>();
		symbols 		= new HashSet <Character> ();
		
		// Instruções
		// Adiciona as instruções ao HashMap
        instructions.put("ADD", new String[]{"0x18", "3/4"});
        instructions.put("ADDR", new String[]{"0x90", "2"});
        instructions.put("AND", new String[]{"0x40", "3/4"});
        instructions.put("CLEAR", new String[]{"0x04", "2"});
        instructions.put("COMP", new String[]{"0x28", "3/4"});
        instructions.put("COMPR", new String[]{"0xA0", "2"});
        instructions.put("DIV", new String[]{"0x24", "3/4"});
        instructions.put("DIVR", new String[]{"0x9C", "2"});
        instructions.put("J", new String[]{"0x3C", "3/4"});
        instructions.put("JEQ", new String[]{"0x30", "3/4"});
        instructions.put("JGT", new String[]{"0x34", "3/4"});
        instructions.put("JLT", new String[]{"0x38", "3/4"});
        instructions.put("JSUB", new String[]{"0x48", "3/4"});
        instructions.put("LDA", new String[]{"0x00", "3/4"});
        instructions.put("LDB", new String[]{"0x68", "3/4"});
        instructions.put("LDCH", new String[]{"0x50", "3/4"});
        instructions.put("LDL", new String[]{"0x08", "3/4"});
        instructions.put("LDS", new String[]{"0x6C", "3/4"});
        instructions.put("LDT", new String[]{"0x74", "3/4"});
        instructions.put("LDX", new String[]{"0x04", "3/4"});
        instructions.put("MUL", new String[]{"0x20", "3/4"});
        instructions.put("MULR", new String[]{"0x98", "2"});
        instructions.put("OR", new String[]{"0x44", "3/4"});
        instructions.put("RMO", new String[]{"0xAC", "2"});
        instructions.put("RSUB", new String[]{"0x4C", "3/4"});
        instructions.put("SHIFTL", new String[]{"0xA4", "2"});
        instructions.put("SHIFTR", new String[]{"0xA8", "2"});
        instructions.put("STA", new String[]{"0x0C", "3/4"});
        instructions.put("STB", new String[]{"0x78", "3/4"});
        instructions.put("STCH", new String[]{"0x54", "3/4"});
        instructions.put("STL", new String[]{"0x14", "3/4"});
        instructions.put("STS", new String[]{"0x7C", "3/4"});
        instructions.put("STT", new String[]{"0x84", "3/4"});
        instructions.put("STX", new String[]{"0x10", "3/4"});
        instructions.put("SUB", new String[]{"0x1C", "3/4"});
        instructions.put("SUBR", new String[]{"0x94", "2"});
        instructions.put("TIX", new String[]{"0x2C", "3/4"});
        instructions.put("TIXR", new String[]{"0xB8", "2"});

		
		// Diretivas
		directives.add("START");
		directives.add("END");
		directives.add("BYTE");
		directives.add("WORD");
		directives.add("RESB");
		directives.add("RESW");
		directives.add("EXTDEF");
		directives.add("EXTREF");
		directives.add("EXTDEF");
		directives.add("EXTREF");
		directives.add("MACRO");
		directives.add("MEND");
		
		//Simbolos
		symbols.add('.');
		symbols.add('$');
		symbols.add(',');
		symbols.add('@');
		symbols.add('#');
		
	}
	
	
	public static boolean isInstruction (String token) {
		return instructions.containsKey(token);
	}
	public static boolean isDirective (String token) {
		return directives.contains(token);
	}
	public static boolean isReservedSymbol (String token) {
		return symbols.contains(token.charAt(0));
	}
	public static boolean isReservedSymbol (char token) {
		return symbols.contains(token);
	}
	public static boolean hasReservedSymbol (String token) {
		boolean contains = false;
		for (char c: token.toCharArray()) {
			if (symbols.contains(c));
				contains = true;
		}
		return contains;
	}
	public static boolean isReservedWord (String token) {
		return isInstruction(token) || isDirective(token) || isReservedSymbol(token);
	}
	
	public static String getInstructionFormat (String token) {
		return instructions.get(token)[1];
	}
}
