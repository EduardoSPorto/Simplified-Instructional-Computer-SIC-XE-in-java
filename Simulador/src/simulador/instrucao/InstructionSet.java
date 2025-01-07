package simulador.instrucao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InstructionSet {

	ArrayList <VMInstruction> instructionSet;
	Operations VMOperations;
	int size;
	
	public InstructionSet () {
		instructionSet = new ArrayList<>();
		VMOperations = new Operations();
		instructionSet.add(new VMInstruction("ADD", 0x18, "3/4", VMOperations::ADD));
		instructionSet.add(new VMInstruction("ADDR", 0x90, "2", VMOperations::ADDR));
		instructionSet.add(new VMInstruction("AND", 0x40, "3/4", VMOperations::AND));
		instructionSet.add(new VMInstruction("CLEAR", 0x04, "2", VMOperations::CLEAR));
		instructionSet.add(new VMInstruction("COMP", 0x28, "3/4", VMOperations::COMP));
		instructionSet.add(new VMInstruction("COMPR", 0xA0, "2", VMOperations::COMPR));
		instructionSet.add(new VMInstruction("DIV", 0x24, "3/4", VMOperations::DIV));
		instructionSet.add(new VMInstruction("DIVR", 0x9C, "2", VMOperations::DIVR));
		instructionSet.add(new VMInstruction("J", 0x3C, "3/4", VMOperations::J));
		instructionSet.add(new VMInstruction("JEQ", 0x30, "3/4", VMOperations::JEQ));
		instructionSet.add(new VMInstruction("JGT", 0x34, "3/4", VMOperations::JGT));
		instructionSet.add(new VMInstruction("JLT", 0x38, "3/4", VMOperations::JLT));
		instructionSet.add(new VMInstruction("JSUB", 0x48, "3/4", VMOperations::JSUB));
		instructionSet.add(new VMInstruction("LDA", 0x00, "3/4", VMOperations::LDA));
		instructionSet.add(new VMInstruction("LDB", 0x68, "3/4", VMOperations::LDB));
		instructionSet.add(new VMInstruction("LDCH", 0x50, "3/4", VMOperations::LDCH));
		instructionSet.add(new VMInstruction("LDL", 0x08, "3/4", VMOperations::LDL));
		instructionSet.add(new VMInstruction("LDS", 0x6C, "3/4", VMOperations::LDS));
		instructionSet.add(new VMInstruction("LDT", 0x74, "3/4", VMOperations::LDT));
		instructionSet.add(new VMInstruction("LDX", 0x04, "3/4", VMOperations::LDX));
		instructionSet.add(new VMInstruction("MUL", 0x20, "3/4", VMOperations::MUL));
		instructionSet.add(new VMInstruction("MULR", 0x98, "2", VMOperations::MULR));
		instructionSet.add(new VMInstruction("OR", 0x44, "3/4", VMOperations::OR));
		instructionSet.add(new VMInstruction("RMO", 0xAC, "2", VMOperations::RMO));
		instructionSet.add(new VMInstruction("RSUB", 0x4C, "3/4", VMOperations::RSUB));
		instructionSet.add(new VMInstruction("SHIFTL", 0xA4, "2", VMOperations::SHIFTL));
		instructionSet.add(new VMInstruction("SHIFTR", 0xA8, "2", VMOperations::SHIFTR));
		instructionSet.add(new VMInstruction("STA", 0x0C, "3/4", VMOperations::STA));
		instructionSet.add(new VMInstruction("STB", 0x78, "3/4", VMOperations::STB));
		instructionSet.add(new VMInstruction("STCH", 0x54, "3/4", VMOperations::STCH));
		instructionSet.add(new VMInstruction("STL", 0x14, "3/4", VMOperations::STL));
		instructionSet.add(new VMInstruction("STS", 0x7C, "3/4", VMOperations::STS));
		instructionSet.add(new VMInstruction("STT", 0x84, "3/4", VMOperations::STT));
		instructionSet.add(new VMInstruction("STX", 0x10, "3/4", VMOperations::STX));
		instructionSet.add(new VMInstruction("SUB", 0x1C, "3/4", VMOperations::SUB));
		instructionSet.add(new VMInstruction("SUBR", 0x94, "2", VMOperations::SUBR));
		instructionSet.add(new VMInstruction("TIX", 0x2C, "3/4", VMOperations::TIX));
		instructionSet.add(new VMInstruction("TIXR", 0xB8, "2", VMOperations::TIXR));	
		
		this.size = instructionSet.size();
	}
	
//	Retorna uma tupla simples (opcode, formato)
	public Map <Integer, String> getInfo (String mnemonic){
		Map <Integer, String> info = new HashMap<>();
		
		for (int i = 0; i < size; i++) {
			VMInstruction instruction = instructionSet.get(i);
			if (instruction.getMnemonic().equals(mnemonic)) {
				info.put(instruction.getOpcode(), instruction.getFormat());
				return info;
			}
		}
		return info;
	}
	
	public String getFormat(int opcode) {
		for (int i = 0; i < size; i ++) {
			VMInstruction instruction = instructionSet.get(i);
			if (instruction.getOpcode() == opcode)
				return instruction.getFormat();
		}
		return "";
	}
	
//	Executa qualquer uma das intruções através do Opcode
//	As instruções não tem parâmetros pq são definidas pelo contexto da máquina, cada instruçã deve interpretar o contexto.
	public void execute (int opcode) {
		for (int i = 0; i < instructionSet.size(); i ++) {
			VMInstruction instruction = instructionSet.get(i);
			if (instruction.getOpcode() == opcode)
				instruction.execute();
		}
	}
	
}
