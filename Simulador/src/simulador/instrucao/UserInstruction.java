package simulador.instrucao;

public class UserInstruction {
	
	private byte[] instruction;
	private int format;
	
	public UserInstruction (int format) {
		this.format = format;
		this.instruction = new byte [format];
	}
	
	public UserInstruction (byte [] instruction) {
		this.format = instruction.length;
		this.instruction = new byte [this.format];
		for (int i = 0; i < format; i++)
			this.instruction[i] = instruction[i];
	}
	
	public void setOpcode (int opcode) { 
		this.instruction[0] = (byte) opcode;
	}
	
	public void setRegisters (int reg1, int reg2) {
		reg1 = (reg1 << 4) & 0xFF;
		reg2 = reg2 & 0xF;
		this.instruction[1] |= (byte) (reg1 | reg2);
	}
	
	// Define o tipo de endereçamento
	public void setAsDirect () {
		this.instruction[0] |= 0b00000011 ;
	}
	public void setAsIndirect () {
		this.instruction[0] |= 0b00000010;
	}
	public void setAsImmediate () {
		this.instruction[0] |= 0b00000001;
	}
	
	
	
	// Flag X -> Relativo ao registrador X
	public void setFlagX () {
		this.instruction[1] |= 0b10000000 ;
	}
	// Flag B -> Relativo ao registrador Base
	public void setFlagB () {
		this.instruction[1] |= 0b01000000 ;
	}
	// Flag P -> Relativo ao Program Counter
	public void setFlagP () {
		this.instruction[1] |= 0b00100000 ;
	}
	// Flag e -> Formato extendido
	public void setFlagE () {
		this.instruction[1] |= 0b00010000 ;
	}
		
	
	
	// Endereço para formato 3 bytes
	public void setDisp (int disp) {
		this.instruction[1] |= ((disp >> 8) & 0xF);
		this.instruction[2] = (byte) (disp & 0xFF);
	}
	// Endereço para formato 4 bytes
	public void setAddress (int address) {
		this.instruction[1] |= ((address >> 16) & 0xF);
		this.instruction[2] |= ((address >> 8) & 0xFF);
		this.instruction[3] |= (byte) (address & 0xFF);
	}
	
	

	
	// Métodos Getter
	public int getOpcode (){
		if (this.format != 3 && this.format != 4)
			return (int) this.instruction[0];
		
		int opcode;
		opcode = this.instruction[0] & 0b11111100;
		return opcode;
	}
	public byte[] getInstruction () {
		return this.instruction;
	}
	public int getAddress (boolean extended) {
		int address;
		address = (this.instruction[1] & 0xF);
		address = address << 8;
		address |= this.instruction[2];
		if (extended) {
			address = address << 8;
			address |= this.instruction[3];
		}
		
		return address;
	}
	
	public int[] getRegisters () {
		int [] registers = new int[2];
		registers[0] = this.instruction[1] & 0xF0;
		registers[1] = this.instruction[1] & 0x0F;
		return registers;
	}
	
	// Métodos Booleanos
	public boolean isDirect () {
		int mask = 0b00000011;
		int flags = this.instruction[0]&mask;
		if (flags == mask)
			return true;
		return false;
	}
	public boolean isIndirect () {
		int mask = 0b00000011;
		int flags = this.instruction[0]&mask;
		if (flags == 0b00000010)
			return true;
		return false;
	}
	public boolean isImmediate () {
		int mask = 0b00000011;
		int flags = this.instruction[0]&mask;
		if (flags == 0b00000001)
			return true;
		return false;
	}
	
	public boolean isRelativeToX () {
		int mask = 0b10000000;
		int flags = this.instruction[1] & mask;
		if (flags == mask)
			return true;
		else
			return false;
	}
	public boolean isRelativeToBase () {
		int mask = 0b01000000;
		int flags = this.instruction[1] & mask;
		if (flags == mask)
			return true;
		else
			return false;
	}
	public boolean isRelativeToPC () {
		int mask = 0b00100000;
		int flags = this.instruction[1] & mask;
		if (flags == mask)
			return true;
		else
			return false;
	}
	public boolean isExtended () {
		int mask = 0b00010000;
		int flags = this.instruction[1] & mask; // Zera todos menos flag E
		if (flags == mask)						// Se for igual então é extendido.
			return true;
		return false;
	}
	
	
	
	// Métodos de conversão
	@Override
	public String toString() {
		if (format == 2)
			return "Instrução: " + String.format("%16s", Integer.toBinaryString(this.toInteger())).replace(' ', '0');
		else if (format == 3)
			return "Instrução: " + String.format("%24s", Integer.toBinaryString(this.toInteger())).replace(' ', '0');
		else
			return "Instrução: " + String.format("%32s", Integer.toBinaryString(this.toInteger())).replace(' ', '0');
	}
	
	public int toInteger () {
		int integerValue = 0;
		for (byte b : this.instruction) {
			integerValue = (integerValue<< 8) | (b & 0xFF);
		}
		return integerValue;	
	}
}



/*			ILUSTRAÇÕES ABAIXO SERVEM PARA FACILITAR ENTENDIMENTO APENAS
 * 		
 * 			 xxxxxx x x 0 x x x xxxxxxxxxxxx
 * 		OR
 * 			 000000 0 0 1 0 0 0 000000000000
 * Tipo 3 -> 111111 1 1 1 1 1 1 111111111111
 * Tipo 4 -> 111111 1 1 1 1 1 1 11111111111111111111
 * 
 * 
 * Como Byte
 * 
 *           opcodeNI XBPEdisp dispdisp
 * Tipo 3 -> 00000000 00000000 00000000
 * 
 *           opcodeNI XBPEaddr essaddre ssaddres 
 * Tipo 4 -> 00000000 00000000 00000000 00000000
 */


