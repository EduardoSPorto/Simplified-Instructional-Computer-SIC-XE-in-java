package simulador.linker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import simulador.Memory;
import simulador.assembler.ObjectProgram;

class SymbolTableEntry {
	String symbol;
	String hexAddress;
}

public class Loader {
	private int programSize;
	private int ipla;			//initialProgramLoadAddress;
	private String[] modules;
	private Memory vmMemory;
	private List<SymbolTableEntry> TSG;
	
	
	public Loader (Memory memory) {
		this.vmMemory = memory;
		TSG = new ArrayList<SymbolTableEntry>();
	}
	
	
	
	public void firstPass () throws IOException {
		this.programSize = 0;
		this.modules = getModules();
		
		for (String module : modules) {
			FileReader fileReader = new FileReader (module);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			
			String 	header = bufferedReader.readLine();
			String 	mName = header.substring(1,7);
			int 	mStartAddress = this.programSize + Integer.parseInt(header.substring(7,13));
			
			String defineRecord = null;
			String line;
			
			line = bufferedReader.readLine();
			
			while (line.charAt(0) != 'T') {
				if (line.charAt(0) == 'D')
					defineRecord = line;
				line = bufferedReader.readLine();
			}
			
			// Updating TSG
			SymbolTableEntry entry = new SymbolTableEntry();
			entry.symbol = mName;
			entry.hexAddress = Integer.toHexString(mStartAddress);
			TSG.add(entry);
			
			if (defineRecord.isBlank() == false) {
				updateTSG (defineRecord, this.programSize);
			}
			
			this.programSize += Integer.parseInt(header.substring(13,19));
		}
		
		this.ipla = this.vmMemory.alloc(programSize);
	}
	
	
	public void updateTSG (String defineRecord, int programSize) {
		defineRecord = defineRecord.substring(1);
		String[] definitions = defineRecord.split(" ");
		
		for(int i = 0; i < definitions.length; i+=2) {
			SymbolTableEntry entry = new SymbolTableEntry();
			entry.symbol = definitions[i];
			entry.hexAddress = definitions[i+1];
			
			if (programSize!=0) {
				int updatedAddress = programSize + Integer.parseInt(entry.hexAddress, 16);
				entry.hexAddress = Integer.toHexString(updatedAddress);
			}
			
			TSG.add(entry);
		}
	}
	
	public int getModuleSize (String line) {
		int value = -1;
		String hexValue = line.substring(13,19);
		try {
			value = Integer.parseInt(hexValue);
		} catch (Exception e) { //Só pra Debug 
			System.err.println("Substring Limits poorly defined");
		}
		return value;
	}
	
	public String[] getModules () {
		List<String> modules = new ArrayList<String>();
		final String FILEPATH = "ObjectProgs" + File.separator + "MASMAPRG";
		final String EXTENSION = ",obj";
		int index = 0;
		
		while (new File(FILEPATH+index+EXTENSION).isFile()) {
			index++;
			modules.add(FILEPATH+index+EXTENSION);
		}
		
		return modules.toArray(new String[0]);
	}
	
	public void cleanFiles () {
		File dir = new File ("AssemblyCodes");
//		
//		for (File file : dir.listFiles()){
//			if (file.isFile())
//				file.delete();
//		}
		
		dir = new File ("ObjectProgs");
		
		for (File file : dir.listFiles()){
			if (file.isFile())
				file.delete();
		}
	}
}


