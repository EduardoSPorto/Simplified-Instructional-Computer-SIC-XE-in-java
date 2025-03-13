package simulador.linker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import simulador.Memory;
import simulador.VMSimulator;
import simulador.assembler.ObjectProgram;


public class Loader {
	private int programSize;
	private int ipla;			//initialProgramLoadAddress;
	private String[] modules;
	private Memory vmMemory;
	private VMSimulator vmSimulator;
	private Map <String, String> TSG;
	private FinalObjProg linkedObjCode;
	
	
	public Loader (Memory memory, VMSimulator vmSimulator) {
		this.vmMemory = memory;
		this.vmSimulator = vmSimulator;
		TSG = new HashMap<>();
	}
	
	
	
	public void firstPass () throws IOException {
		this.programSize = 0;
		this.modules = getModules();
		FileReader fileReader;
		BufferedReader bufferedReader;
		linkedObjCode = null;
		
		// Passa pelos módulos para pegar tamanho total, montar TSG e gerar ObjectProgram final
		for (String module : modules) {
			fileReader = new FileReader (module);
			bufferedReader = new BufferedReader(fileReader);
			
			String 	header = bufferedReader.readLine();
			String 	mName = header.substring(1,7);
			int 	mStartAddress = this.programSize + Integer.parseInt(header.substring(7,13));
			String defineRecord = null;
			String line;
			
			if (linkedObjCode == null) 
				linkedObjCode = new FinalObjProg(mName, Integer.toHexString(mStartAddress));
			
			
			line = bufferedReader.readLine();
			
			// Updating TSG
			while (line.charAt(0) != 'T') {
				if (line.charAt(0) == 'D')
					defineRecord = line;
				line = bufferedReader.readLine();
			}			
			TSG.put(mName, Integer.toHexString(mStartAddress));
			if (defineRecord.isBlank() == false) {
				defineRecord = defineRecord.substring(1);
				String[] definitions = defineRecord.split(" ");
				
				for(int i = 0; i < definitions.length; i+=2) {
					String symbol = definitions[i];
					String hexAddress = definitions[i+1];
					if (programSize!=0) {
						int updatedAddress = programSize + Integer.parseInt(hexAddress, 16);
						hexAddress = Integer.toHexString(updatedAddress);
					}					
					TSG.put(symbol, hexAddress);
				}	
			}
			while (line.charAt(0) != 'E') {
				// Atualiza os endereços para segmentos 2,3,... Relocação  (Referência Externa é resolvida na segunda passagem)
				if (line.charAt(0) == 'T') {
					line = updateAddress(line);
					linkedObjCode.Text.add(line);
				}
				line = bufferedReader.readLine();
			}
			linkedObjCode.END = line;
			
			this.programSize += Integer.parseInt(header.substring(13,19));
		}
		
		this.ipla = this.vmMemory.alloc(programSize);
	}
	
	public String updateAddress (String line) {
		String hexAddress = line.substring(1,7);
		String updatedHexAddress = Integer.toHexString( Integer.parseInt(hexAddress,16) + this.programSize );
		String updatedLine = "T" + updatedHexAddress + line.substring(7);
		return updatedLine;
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


	public void secondPass () {
		int execStartPoint = this.ipla;
		int segStart = this.ipla;
		String line = linkedObjCode.Text.removeFirst();
		
		while( line.charAt(0) == 'T') {
			int address = Integer.parseInt(line.substring(1,7));
			String objectCode = line.substring(8);
			
			if (line.charAt(7) == 'r') {
				Relocate(objectCode, segStart);
			}
			
			int size =  objectCode.length()/2 ;
			byte[] instruction = new byte[size];
			for ( int i=0; i<size*2; i+=2) {
				instruction[i] = Byte.valueOf(objectCode.substring(i,i+1), 16);
			}
			this.vmMemory.writeInstruction(address, instruction, size);
			
			line = linkedObjCode.Text.removeFirst();
		}
		
		line = linkedObjCode.Text.removeFirst();
		while (line.charAt(0) == 'M') {
			String hexAddress = line.substring(1,7);						//Actual memory Position
			String halfBytes = line.substring(7,9);
			int bytes = halfBytes.equals("03")? 2 : 3;
			char modFlag = line.charAt(9);
			
			String extSymbol = line.substring(10);
			int symbolAddress = Integer.parseInt(TSG.get(extSymbol), 16); 	// Only for relocation
			if (modFlag == '-')
				symbolAddress *= -1;
			
			this.vmMemory.update(Integer.parseInt(hexAddress,16), bytes, symbolAddress);
			
			line = linkedObjCode.Text.removeFirst();
		}
	
		vmSimulator.operate(execStartPoint, ipla + this.programSize);
		
	}
	
	
	public String Relocate(String instructionCode, int segStart) {
		int instruction = Integer.parseInt(instructionCode, 16);
		int relocated = instruction + segStart;
		
		return Integer.toHexString(relocated);
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




class RelocTableEntry {
	String 	hexAddress;
	String 	halfBytes;
	char	modFlag;
	String 	extSymbol;
}
class FinalObjProg {
	String 			header = "H";
	List<String>	Text = new ArrayList<String>();
	String			END = "E";
	
	public FinalObjProg (String name, String startAddress) {
		this.header = header.concat(name + startAddress);
	}
}



/*
 * Mudar Formato do Text
 * 
 * T <Endereço|6 cols> <Absoluto|Relativo> <ObjectCode>
 * 
 */
