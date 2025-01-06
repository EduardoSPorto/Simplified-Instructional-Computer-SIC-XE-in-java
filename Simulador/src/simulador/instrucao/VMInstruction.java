package simulador.instrucao;


public class VMInstruction {

	private int opcode;
	private String mnemonic;
	private String format; // Num bytes 1 2 3/4
	private Runnable operation;
	
		
	public VMInstruction (String mnemonic, int opcode, String format, Runnable operation) {
		this.mnemonic = mnemonic;
		this.opcode = opcode;
		this.format = format;
		this.operation = operation;
	}


	public int getOpcode() {
		return opcode;
	}

	public String getMnemonic() {
		return mnemonic;
	}

	public String getFormat() {
		return format;
	}

	public void execute () {
		this.operation.run();
	}
	
}


//public Instruction (int instruction, int type) {
//	if (type == 1) {
//		this.instruction = instruction;
//		this.opcode = instruction;
//	}
//	else if (type == 2) {
//		this.instruction = instruction;
//		this.opcode = (instruction >> 8) & 0xFF;
//		this.r1 = (instruction >> 4) & 0xF;
//		this.r2 = instruction & 0xF;
//	}
//	
//	else if (type == 3) {
//		this.instruction = instruction;
//		this.opcode = (instruction >> 18) & 0x3F;
//		this.n = (instruction >> 17) & 0x1;
//		this.i= (instruction >> 16) & 0x1;
//		this.x = (instruction >> 15) & 0x1;
//		this.b = (instruction >> 14) & 0x1;
//		this.p = (instruction >> 13) & 0x1;
//		this.e = (instruction >> 12) & 0x1;
//		this.disp = instruction & 0xFFF;
//	}
//	
//	else if( type == 4) {
//		this.instruction = instruction;
//		this.opcode = (instruction >> 18) & 0x3F;
//		this.n = (instruction >> 17) & 0x1;
//		this.i= (instruction >> 16) & 0x1;
//		this.x = (instruction >> 15) & 0x1;
//		this.b = (instruction >> 14) & 0x1;
//		this.p = (instruction >> 13) & 0x1;
//		this.e = (instruction >> 12) & 0x1;
//		this.adress = instruction & 0xFFFFF;
//	}
//}
//
