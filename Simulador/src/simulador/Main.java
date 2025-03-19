package simulador;

import java.io.IOException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import simulador.assembler.Assembler;
import simulador.instrucao.InstructionSet;
import simulador.instrucao.Operations;
import simulador.loader.Loader;

public class Main {
	static Memory vmMemory;
	static Registers vmRegisters;
	static Operations vmOperations; 
	static InstructionSet vmInstructionSet; 
	static Assembler assembler;
	static Loader loader;
	static VMSimulator vmSimulator;
	
	/*
	=======================
	main
	======================
	 */
    public static void main(String[] args) throws IOException {
        vmMemory = new Memory(1025);
        vmRegisters = new Registers();
        vmOperations = new Operations(vmMemory, vmRegisters);
        vmInstructionSet = new InstructionSet(vmOperations);
        vmSimulator = new VMSimulator(vmMemory, vmRegisters, vmInstructionSet);
        loader = new Loader (vmMemory, vmSimulator);
        assembler = new Assembler(vmMemory, vmInstructionSet, vmRegisters, loader);
        
        assembler.execute();
        
        System.out.println("Saiu");
        SwingUtilities.invokeLater(() -> createAndShowGUI(vmRegisters, vmMemory));
    }
    
    
    
    
    /*
    =======================
    CreateAndShowGUI
    ======================
   */
    private static void createAndShowGUI(Registers vmRegisters, Memory vmMemory) {
        // Create main frame
        JFrame frame = new JFrame("SIC VM - Register & Memory Monitor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1024, 768);

        // Register Table Setup
        JTable registerTable = new JTable();
        DefaultTableModel registerModel = new DefaultTableModel(new Object[]{"Register", "Value"}, 0);
        registerTable.setModel(registerModel);
        
        vmRegisters.getRegisterMap().forEach((name, value) -> 
            registerModel.addRow(new Object[]{
                name, 
                String.format("%d", value)
            })
        );
        JScrollPane registerPane = new JScrollPane(registerTable);

        // Memory Table Setup
        JTable memoryTable = new JTable();
        DefaultTableModel memoryModel = new DefaultTableModel(new Object[]{"Address", "Word Value"}, 0);
        memoryTable.setModel(memoryModel);
        
        int memorySize = vmMemory.getMemorySize();
        for (int addr = 0; addr <= memorySize - 3; addr += 3) {
            try {
            	
                int wordValue = vmMemory.readWord(addr);
                memoryModel.addRow(new Object[]{
                    String.format("0x%04X", addr),
                    String.format("%d", wordValue)
                });
            } catch (IndexOutOfBoundsException ignored) {
                // Handle edge cases if memory size not divisible by 3
            }
        }
        JScrollPane memoryPane = new JScrollPane(memoryTable);

        // Layout configuration
        JSplitPane splitPane = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT, 
            registerPane, 
            memoryPane
        );
        splitPane.setResizeWeight(0.3);
        
        frame.add(splitPane);
        frame.setVisible(true);
    }
}
