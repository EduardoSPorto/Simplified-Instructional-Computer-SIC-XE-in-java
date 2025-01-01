package Simulador;

public class Main {
    public static void main(String[] args) {
        Registers registers = new Registers();
		// Testando os registradores
        // Definir valores nos registradores padrão
        registers.setRegister("A", 0x123456);
        registers.setRegister("X", 0x654321);
        registers.setRegister("PC", 0xABCD);

        // Definir valor no registrador F (48 bits)
        registers.setRegisterF(0x123456789ABCL); // Adicionado "L" para indicar long

        // Imprimir os valores dos registradores
        registers.printRegisters();
        
        //Testando memoria

    }
}