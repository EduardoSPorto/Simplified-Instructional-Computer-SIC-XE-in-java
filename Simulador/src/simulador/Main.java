package simulador;


public class Main {
    public static void main(String[] args) {
        
        VMSimulator simulador = new VMSimulator();
        simulador.initialize();
        simulador.operate();
    }
}