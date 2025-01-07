package simulador.instrucao;

public class UserInstruction {
	
	private int instruction;
	private int format;
	
	public UserInstruction (int format) {
		this.format = format;
		this.instruction = 0;
	}

	public void setOpcode (int opcode, int format) { 
		switch (format) {
			case 1:
				this.instruction = opcode;
				break;
			case 2:
				this.instruction = (this.instruction & 0x00FF) | ((opcode << 8) & 0xFF00);
				break;
			case 3:
				this.instruction = (this.instruction & 0x00FFFF) | ((opcode << 16) & 0xFF0000);
				break;
			case 4:
				this.instruction = (this.instruction & 0x00FFFFFF) | ((opcode <<24) & 0xFF000000);
				break;
			default:
				throw new IllegalArgumentException("Formato do tipo" + format + " Inexistente!");
		}
	}
	
	public void setRegisters (int reg1, int reg2) {
		reg1 = (reg1 << 4) & 0xFF;
		reg2 = reg2 & 0xF;
		this.instruction = this.instruction | reg1 | reg2;
	}
	
	// Define o tipo de endereçamento
	public void setAsDirect (int format) {
		switch (format) {
			case 3:
				this.instruction = this.instruction | 0b110000000000000000;
				break;
			case 4:
				this.instruction = this.instruction | 0b11000000000000000000000000;
				break;
			default:
				throw new IllegalArgumentException("Formato do tipo" + format + " Inexistente!");
		}
	}
	
	public void setAsIndirect (int format) {
		switch (format) {
			case 3:
				this.instruction = this.instruction | 0b100000000000000000;
				break;
			case 4:
				this.instruction = this.instruction | 0b10000000000000000000000000;
				break;
			default:
				throw new IllegalArgumentException("Formato do tipo" + format + " Inexistente!");
		}
	}
	
	public void setAsImmediate (int format) {
		switch (format) {
			case 3:
				this.instruction = this.instruction | 0b010000000000000000;
				break;
			case 4:
				this.instruction = this.instruction | 0b01000000000000000000000000;
				break;
			default:
				throw new IllegalArgumentException("Formato do tipo" + format + " Inexistente!");
		}
	}
	
	
	// Definição das Flags
	
	// Flag X -> Relativo ao registrador X
	public void setFlagX (int format) {
		switch (format) {
			case 3:
				this.instruction = this.instruction | 0b1000000000000000;
				break;
			case 4:
				this.instruction = this.instruction | 0b100000000000000000000000;
				break;
			default:
				throw new IllegalArgumentException("Formato do tipo" + format + " Inexistente!");
		}
	}
	
	// Flag B -> Relativo ao registrador Base
	public void setFlagB (int format) {
		switch (format) {
		case 3:
			this.instruction = this.instruction | 0b100000000000000;
			break;
		case 4:
			this.instruction = this.instruction | 0b10000000000000000000000;
			break;
		default:
			throw new IllegalArgumentException("Formato do tipo" + format + " Inexistente!");
		}
	}
	
	// Flag P -> Relativo ao Program Counter
	public void setFlagP (int format) {
		switch (format) {
		case 3:
			this.instruction = this.instruction | 0b10000000000000;
			break;
		case 4:
			this.instruction = this.instruction | 0b1000000000000000000000;
			break;
		default:
			throw new IllegalArgumentException("Formato do tipo" + format + " Inexistente!");
		}
	}
	
	public void setDisp (int disp) {
		disp = disp & 0xFFF;
		this.instruction = this.instruction | disp;
	}
	
	public void setAddress (int address) {
		address = address & 0xFFFFF;
		this.instruction = this.instruction | address;
	}
	
	// Flag e -> Formato extendido
	public void setFlagE (int format) {
		switch (format) {
		case 3:
			this.instruction = this.instruction | 0b1000000000000;
			break;
		case 4:
			this.instruction = this.instruction | 0b100000000000000000000;
			break;
		default:
			throw new IllegalArgumentException("Formato do tipo" + format + " Inexistente!");
		}
	}
	
	
	
	public int getOpcode (){
		int opcode;
		switch (this.format) {
			case 1:
				opcode = this.instruction;
				return opcode;
			case 2:
				opcode = this.instruction >> 8;
				return opcode;
			case 3:
				opcode = this.instruction >> 16;
				return opcode;
			case 4:
				opcode = this.instruction >> 24;
				return opcode;
		}
		return -1;
	}
	
	public int getInstruction () {
		return this.instruction;
	}
	
	
	@Override
	public String toString() {
		return "Instrução: " + Integer.toBinaryString(instruction);
	}
	

}



/*			
 * 
 *            00000000 IIII UUUU
 * 
 * Tipo 2 ->  11111111 0000 0000
 * 
 * 
 * 
 * 		
 * 			 xxxxxx x x 0 x x x xxxxxxxxxxxx
 * 		OR
 * 			 000000 0 0 1 0 0 0 000000000000
 * Tipo 3 -> 111111 1 1 1 1 1 1 111111111111
 * Tipo 4 -> 111111 1 1 1 1 1 1 11111111111111111111
 */


