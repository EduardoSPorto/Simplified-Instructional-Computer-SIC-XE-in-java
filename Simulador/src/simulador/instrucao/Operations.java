package simulador.instrucao;

import simulador.Memory;
import simulador.Registers;

public class Operations {
	private Registers registers;
	private Memory memory;
	private int ciAddress; //Current Instruction Address
	
	public Operations (Memory memory, Registers registers) {
		this.memory = memory;
		this.registers = registers;
	}
	
	
	public void ADD(UserInstruction instruction) {
		updateAddress(instruction);
		int result;
		
		if (instruction.isImmediate())
			result = registers.getRegisterValue("A") + ciAddress;
		else
			result = registers.getRegisterValue("A") + memory.readWord(ciAddress);
			
		registers.setRegisterValue("A", result);
	}

	public void ADDR(UserInstruction instruction) { 
		int [] regs = instruction.getRegisters();
		int result = memory.readWord(registers.getRegisterValue(regs[1])) + memory.readWord(registers.getRegisterValue(regs[0]));
		registers.setRegisterValue(regs[1], result); 
	}

	public void AND(UserInstruction instruction) {
		updateAddress(instruction);
		int result;
		
		if (instruction.isImmediate())
			result = registers.getRegisterValue("A") & ciAddress;
		else
			result = registers.getRegisterValue("A") & memory.readWord(ciAddress);
		registers.setRegisterValue("A", result);
	}

	public void CLEAR(UserInstruction instruction) { 
		int [] regs = instruction.getRegisters();
		registers.setRegisterValue(regs[0], 0);
	}

	public void COMP(UserInstruction instruction) {
	    updateAddress(instruction);
	    int result;
	    
	    if (instruction.isImmediate())
	    	result = registers.getRegisterValue("A") - ciAddress;
	    else
	    	result = registers.getRegisterValue("A") - memory.readWord(ciAddress);
	    
	    if (result > 0)
	    	registers.setRegisterValue("SW", 1);
	    else if (result == 0)
	    	registers.setRegisterValue("SW", 0);
	    else
	    	registers.setRegisterValue("SW", -1);
	}

	public void COMPR(UserInstruction instruction) { 
	    int [] regs = instruction.getRegisters();
	    int result = registers.getRegisterValue(regs[0]) - registers.getRegisterValue(regs[1]);
	    if (result == 0)
	    	registers.setRegisterValue("SW", 0);
	    else if (result > 1)
	    	registers.setRegisterValue("SW", 1);
	    else
	    	registers.setRegisterValue("SW", -1);
	}

	public void DIV(UserInstruction instruction) {
	    updateAddress(instruction);
	    int result;
	    
	    if (instruction.isImmediate())
	    	result = registers.getRegisterValue("A") / ciAddress;
	    else
	    	result = registers.getRegisterValue("A") / memory.readWord(ciAddress);
	    registers.setRegisterValue("A", result);
	}

	public void DIVR(UserInstruction instruction) {  
		int [] regs = instruction.getRegisters();
		int result;
		result = registers.getRegisterValue(regs[1]) / registers.getRegisterValue(regs[0]);
		registers.setRegisterValue(regs[1], result);
	}

	public void J(UserInstruction instruction) {  
		updateAddress(instruction);
		
		if (instruction.isImmediate())
			registers.setRegisterValue("PC", ciAddress);
		else
			registers.setRegisterValue("PC", memory.readWord(ciAddress));
	
	}

	public void JEQ(UserInstruction instruction) {
	    updateAddress(instruction);
	    
	    if (registers.getRegisterValue("SW") == 0) {
	    	if (instruction.isImmediate())
				registers.setRegisterValue("PC", ciAddress);
			else
				registers.setRegisterValue("PC", memory.readWord(ciAddress));
	    }
		
	}

	public void JGT(UserInstruction instruction) {	
	    updateAddress(instruction);
	    
	    if (registers.getRegisterValue("SW") > 0) {
	    	if (instruction.isImmediate())
				registers.setRegisterValue("PC", ciAddress);
			else
				registers.setRegisterValue("PC", memory.readWord(ciAddress));
	    }
	}

	public void JLT(UserInstruction instruction) {	
		updateAddress(instruction);
		
		if (registers.getRegisterValue("SW") < 0) {
	    	if (instruction.isImmediate())
				registers.setRegisterValue("PC", ciAddress);
			else
				registers.setRegisterValue("PC", memory.readWord(ciAddress));
	    }
	}

	public void JSUB(UserInstruction instruction) {
	    updateAddress(instruction);
	    
	    registers.setRegisterValue("L", registers.getRegisterValue("PC"));
	    
	    if (instruction.isImmediate())
	    	registers.setRegisterValue("PC", ciAddress);
	    else
	    	registers.setRegisterValue("PC", memory.readWord(ciAddress));
	}

	public void LDA(UserInstruction instruction) {
	    updateAddress(instruction);
	    
	    if (instruction.isImmediate())
	    	registers.setRegisterValue("A", ciAddress);
    	else
    		registers.setRegisterValue("A", memory.readWord(ciAddress));	
	}

	public void LDB(UserInstruction instruction) {
		updateAddress(instruction);
	    
	    if (instruction.isImmediate())
	    	registers.setRegisterValue("B", ciAddress);
    	else
    		registers.setRegisterValue("B", memory.readWord(ciAddress));	
	}

	public void LDCH(UserInstruction instruction) {
		updateAddress(instruction);
	    
	    if (instruction.isImmediate()) {
	    	if (ciAddress > 255)
	    		throw new IllegalArgumentException("Size " + ciAddress + " For Byte doesn't match it's value bounds");
	    	registers.setRegisterValue("A", ciAddress);
	    }
    	else
    		registers.setRegisterValue("A", memory.readByte(ciAddress,3));	
		
	}

	public void LDL(UserInstruction instruction) {
		updateAddress(instruction);
	    
	    if (instruction.isImmediate())
	    	registers.setRegisterValue("L", ciAddress);
    	else
    		registers.setRegisterValue("L", memory.readWord(ciAddress));	
	}

	public void LDS(UserInstruction instruction) {
		updateAddress(instruction);
	    
	    if (instruction.isImmediate())
	    	registers.setRegisterValue("S", ciAddress);
    	else
    		registers.setRegisterValue("S", memory.readWord(ciAddress));
	}

	public void LDT(UserInstruction instruction) {
		updateAddress(instruction);
	    
	    if (instruction.isImmediate())
	    	registers.setRegisterValue("T", ciAddress);
    	else
    		registers.setRegisterValue("T", memory.readWord(ciAddress));
	}

	public void LDX(UserInstruction instruction) {
		updateAddress(instruction);
	    
	    if (instruction.isImmediate())
	    	registers.setRegisterValue("X", ciAddress);
    	else
    		registers.setRegisterValue("X", memory.readWord(ciAddress));	
	}

	public void MUL(UserInstruction instruction) {
	    updateAddress(instruction);
	    int result;
	    
	    if (instruction.isImmediate())
	    	result = registers.getRegisterValue("A") * ciAddress;
	    else
	    	result = registers.getRegisterValue("A") * memory.readWord(ciAddress);
	    
	    registers.setRegisterValue("A", result);
	}

	public void MULR(UserInstruction instruction) { 
		int [] regs = instruction.getRegisters();
		int result;
		
		result = registers.getRegisterValue(regs[1]) * registers.getRegisterValue(regs[0]);
		
		registers.setRegisterValue(regs[1], result);
	}

	public void OR(UserInstruction instruction) {
		updateAddress(instruction);
		int result;
		
		if (instruction.isImmediate())
			result = registers.getRegisterValue("A") | ciAddress;
		else
			result = registers.getRegisterValue("A") | memory.readWord(ciAddress);
			

		registers.setRegisterValue("A", result);
	}

	public void RMO(UserInstruction instruction) { 
		int [] regs = instruction.getRegisters();
		registers.setRegisterValue(regs[1], registers.getRegisterValue(regs[0]));
	}

	public void RSUB(UserInstruction instruction) {
		registers.setRegisterValue("PC", registers.getRegisterValue("PC"));
	}

	public void SHIFTL(UserInstruction instruction) { 
	    int [] regs = instruction.getRegisters();
	    int value = registers.getRegisterValue(regs[0]) << regs[1];
	    registers.setRegisterValue(regs[0], value);
	}

	public void SHIFTR(UserInstruction instruction) { 
		int [] regs = instruction.getRegisters();
	    int value = registers.getRegisterValue(regs[0]) >> regs[1];
	    registers.setRegisterValue(regs[0], value);
	}

	public void STA(UserInstruction instruction) {
		updateAddress(instruction);
		
		if (instruction.isImmediate())
			memory.writeWord(ciAddress, registers.getRegisterValue("A"));
		else
			memory.writeWord(memory.readWord(ciAddress), registers.getRegisterValue("A"));

	}

	public void STB(UserInstruction instruction) {
		updateAddress(instruction);

		if (instruction.isImmediate())
			memory.writeWord(ciAddress, registers.getRegisterValue("B"));
		else
			memory.writeWord(memory.readWord(ciAddress), registers.getRegisterValue("B"));
	}

	public void STCH(UserInstruction instruction) { 
		updateAddress(instruction);
		
		if (instruction.isImmediate())
			memory.writeByte(ciAddress, registers.getRegisterValue("A"), 3);
		else
			memory.writeByte(memory.readWord(ciAddress), registers.getRegisterValue("A"), 3);
	}

	public void STL(UserInstruction instruction) {
		updateAddress(instruction);
		
		if (instruction.isImmediate())
			memory.writeWord(ciAddress, registers.getRegisterValue("L"));
		else
			memory.writeWord(memory.readWord(ciAddress), registers.getRegisterValue("L"));
	}

	public void STS(UserInstruction instruction) {
		updateAddress(instruction);
		
		if (instruction.isImmediate())
			memory.writeWord(ciAddress, registers.getRegisterValue("S"));
		else
			memory.writeWord(memory.readWord(ciAddress), registers.getRegisterValue("S"));
	}

	public void STT(UserInstruction instruction) {
		updateAddress(instruction);
		
		if (instruction.isImmediate())
			memory.writeWord(ciAddress, registers.getRegisterValue("T"));
		else
			memory.writeWord(memory.readWord(ciAddress), registers.getRegisterValue("T"));
	}

	public void STX(UserInstruction instruction) {
		updateAddress(instruction);
		
		if (instruction.isImmediate())
			memory.writeWord(ciAddress, registers.getRegisterValue("X"));
		else
			memory.writeWord(memory.readWord(ciAddress), registers.getRegisterValue("X"));
	}

	public void SUB(UserInstruction instruction) {
	    updateAddress (instruction);
	    int result; 
	    
	    if (instruction.isImmediate())
	    	result = registers.getRegisterValue("A") - ciAddress;
	    else
	    	result = registers.getRegisterValue("A") - memory.readWord(ciAddress);
	    	    
	    registers.setRegisterValue("A", result);
	}

	public void SUBR(UserInstruction instruction) {
		int [] regs = instruction.getRegisters();
		int result;
		
		result = registers.getRegisterValue(regs[1]) - registers.getRegisterValue(regs[0]);
		
		registers.setRegisterValue(regs[1], result);
		
	}

	public void TIX(UserInstruction instruction) {
	    updateAddress(instruction);
	    int result;
	    
	    registers.setRegisterValue("X", registers.getRegisterValue("X") + 1);
	    
	    
	    if (instruction.isImmediate())
	    	result = registers.getRegisterValue("X") - ciAddress;
	    else
	    	result = registers.getRegisterValue("X") - memory.readWord(ciAddress);
	    
	    if (result > 0)
	    	registers.setRegisterValue("SW", 1);
	    else if (result == 0)
	    	registers.setRegisterValue("SW", 0);
	    else
	    	registers.setRegisterValue("SW", -1);
	}

	public void TIXR(UserInstruction instruction) { 
	    int [] regs = instruction.getRegisters();
	    int result;
	    
	    registers.setRegisterValue("X", registers.getRegisterValue("X") + 1);
	    
	    result = registers.getRegisterValue("X") - registers.getRegisterValue(regs[0]);
	    
	    if (result > 0)
	    	registers.setRegisterValue("SW", 1);
	    else if (result == 0)
	    	registers.setRegisterValue("SW", 0);
	    else
	    	registers.setRegisterValue("SW", -1);
	}
	
	
	
	/*
    =======================
    updateAddress
    ======================
   */
	public int updateAddress (UserInstruction instruction) {
		if (instruction.isExtended()) 
			this.ciAddress = instruction.getAddress(true);
		else
			this.ciAddress = instruction.getAddress(false);
		
		if (instruction.isImmediate())
			return ciAddress;
		
		if (instruction.isIndirect())
			ciAddress = memory.readWord(ciAddress);
		// Se instrução for Direta, não faz nada
		
		if (instruction.isRelativeToX())
			ciAddress = ciAddress + registers.getRegisterValue("X");
		else if (instruction.isRelativeToBase())
			ciAddress = ciAddress + registers.getRegisterValue("B");
		else if (instruction.isRelativeToPC())
			ciAddress = ciAddress + registers.getRegisterValue("PC");
		
		return ciAddress;
		
	}
	
		
}
