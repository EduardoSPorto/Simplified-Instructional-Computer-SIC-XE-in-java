package Simulador;
// Auxiliar para manupular dados
// Da para tentar implementar diretamento no código principal
public class DataUtils {
    // Conversão de inteiro para 24 bits
    public static int to24Bit(int value) {
        return value & 0xFFFFFF; // Mascara para 24 bits
    }

    // Conversão de ASCII para byte
    public static byte charToByte(char c) {
        return (byte) c;
    }
}