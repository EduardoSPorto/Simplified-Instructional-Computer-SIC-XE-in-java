package simulador.linker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

import simulador.DataUtils;
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
	private Map <Integer, Integer> execAddresses;
	private FinalObjProg linkedObjCode;
	
	
	public Loader (Memory memory, VMSimulator vmSimulator) {
		this.vmMemory = memory;
		this.vmSimulator = vmSimulator;
		TSG = new HashMap<>();
		execAddresses = new HashMap<>();
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
			String defineRecord = "";
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
			if (!defineRecord.isEmpty()) {
				defineRecord = defineRecord.substring(1);
				int count = 0;
				while (count != defineRecord.length()) {
				String symbol 		= defineRecord.substring(count, count+6);
				String hexAddress 	= defineRecord.substring(count+6, count+12);
				count+=12;
				TSG.put(symbol.trim(), hexAddress);
				}	
			}
			while (line.charAt(0) != 'E') {
				// Atualiza os endereços para segmentos 2,3,... Relocação  (Referência Externa é resolvida na segunda passagem)
				if (line.charAt(0) == 'T') {
					line = updateAddress(line);
					linkedObjCode.Text.add(line);
				} else if (line.charAt(0) == 'M') {
					line = updateAddress(line);
					linkedObjCode.ModificationRecord.add(line);
				}
				line = bufferedReader.readLine();
			}
			linkedObjCode.END = line;
			
			this.programSize += Integer.parseInt(header.substring(13,19),16);
		}
		
		this.ipla = this.vmMemory.alloc(programSize);
		this.cleanFiles();
		this.secondPass();
	}
	
	public String updateAddress (String line) {
		char prefix = line.charAt(0);
		String hexAddress = line.substring(1,7);
		String updatedHexAddress = Integer.toHexString( Integer.parseInt(hexAddress,16) + this.programSize );
		updatedHexAddress = DataUtils.to6BitsAdressingFormat(updatedHexAddress, true);
		String updatedLine = prefix + updatedHexAddress + line.substring(7);
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
		final String FILEPATH = "ObjectProgs" + File.separator + "Prog";
		final String EXTENSION = ".obj";
		int index = 0;
		
		while (new File(FILEPATH+index+EXTENSION).isFile()) {
			modules.add(FILEPATH+index+EXTENSION);
			index++;
		}
		
		return modules.toArray(new String[0]);
	}


	public void secondPass () {
		int execStartPoint = -1;
		int segStart = this.ipla;
		String line ; 
		int oldAddress;
		int address;
		boolean isInstruction;
		
		// Writing Values in memory 						Se necessário, modifica valor de objectCode
		line = linkedObjCode.Text.removeFirst();
		address = Integer.parseInt(line.substring(1,7), 16);
		while( linkedObjCode.Text.isEmpty() == false) {		// Aqui foi mantido o while, pq a iteração final é diferente
			isInstruction = false;
			String objectCode = line.substring(9);
			
			if (line.charAt(7) == 'r') {
				Relocate(objectCode, segStart);
			}
			if (line.charAt(8) == 'i') {
				if (execStartPoint == -1) 
					execStartPoint = address;
				isInstruction = true;
			}
			
			int size =  objectCode.length()/2 ;
			byte[] instruction = new byte[size];
			for ( int i=0, j = 0; j<size; i+=2, j++) {
				instruction[j] = Byte.valueOf(objectCode.substring(i,i+2), 16);
			}
			this.vmMemory.writeInstruction(address, instruction, size);
			
			// update Values
			oldAddress = address;
			line = linkedObjCode.Text.removeFirst();
			address = Integer.parseInt(line.substring(1,7), 16);
			
			if (isInstruction == true)
				execAddresses.put(oldAddress, address);
		} 
		// Writing Last Line
		isInstruction = false;
		String objectCode = line.substring(9);
		if (line.charAt(7) == 'r') {
			Relocate(objectCode, segStart);
		}
		if (line.charAt(8) == 'i') {
			isInstruction = true;
		}
		int size =  objectCode.length()/2 ;
		byte[] instruction = new byte[size];
		for ( int i=0, j = 0; j<size; i+=2, j++) {
			instruction[j] = Byte.valueOf(objectCode.substring(i,i+2), 16);
		}
		this.vmMemory.writeInstruction(address, instruction, size);
		execAddresses.put(address, -1);
		
		// Update in memory the needed modifications
		for (int i = 0; i <=  linkedObjCode.ModificationRecord.size(); i++) {	//Aqui foi usado for pq todas iterações são iguais, e o teste é mais intuitivo
			line = linkedObjCode.ModificationRecord.removeFirst();
			String hexAddress = line.substring(1,7);						//Actual memory Position
			String halfBytes = line.substring(7,9);
			int bytes = halfBytes.equals("03")? 2 : 3;
			char modFlag = line.charAt(9);
			
			String extSymbol = line.substring(10);
			int symbolAddress = Integer.parseInt(TSG.get(extSymbol), 16); 	// Only for relocation
			if (modFlag == '-')
				symbolAddress *= -1;
			
			this.vmMemory.update(Integer.parseInt(hexAddress,16), bytes, symbolAddress);
			
			if (linkedObjCode.Text.isEmpty() == false)
				line = linkedObjCode.Text.removeFirst();
			else 
				line = linkedObjCode.END;
		}
	
		vmSimulator.operate(execStartPoint, this.execAddresses);
		
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




class FinalObjProg {
	String 			header = "H";
	List<String>	Text = new ArrayList<String>();
	List<String> 	ModificationRecord = new ArrayList<String>();
	String			END = "E";
	
	public FinalObjProg (String name, String startAddress) {
		this.header = header.concat(name + startAddress);
	}
}



/*
 * Mudar Formato do Text
 * 
 * T <Endereço|6 cols><Absoluto|Relativo><ObjectCode>
 * 
 */

/* H Endereço inicio Tamanho do segmento
 * T 
 * ...
 * E 
 * 
 */
