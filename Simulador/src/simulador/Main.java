package simulador;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.JToggleButton;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;

import simulador.assembler.Assembler;
import simulador.assembler.MacroProcessor;
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
	static MacroProcessor macroProcessor;
	
	/*
	=======================
	main
	======================
	 */
    public static void main(String[] args) throws IOException {
    	File dir = new File("AssemblyCodes/");
        if (dir.exists() == false) 
        	dir.mkdir();
         
        dir = new File ("ObjectProgs/");
        if (dir.exists() == false)
        	dir.mkdir();
    	
        vmMemory = new Memory(1025);
        vmRegisters = new Registers();
        vmOperations = new Operations(vmMemory, vmRegisters);
        vmInstructionSet = new InstructionSet(vmOperations);
        vmSimulator = new VMSimulator(vmMemory, vmRegisters, vmInstructionSet);
        loader = new Loader (vmMemory, vmSimulator);
        assembler = new Assembler(vmMemory, vmInstructionSet, vmRegisters, loader);
        macroProcessor = new MacroProcessor();
        
        macroProcessor.processFiles("EntradaMacros"+File.separator);
        assembler.execute();
        
        SwingUtilities.invokeLater(() -> createAndShowGUI(vmRegisters, vmMemory));
    }
    
    
    
    
    /*
    =======================
    CreateAndShowGUI
    ======================
   */
    private static void createAndShowGUI(Registers vmRegisters, Memory vmMemory) {
        // Create main frame
        JFrame frame = new JFrame("SIC VM - Monitor de Registradores & Memória");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1024, 768);

        // Register Table Setup
        JTable registerTable = new JTable();
        DefaultTableModel registerModel = new DefaultTableModel(new Object[]{"Registrador", "Valor", "Valor (Hex)"}, 0);
        registerTable.setModel(registerModel);
        
        vmRegisters.getRegisterMap().forEach((name, value) -> 
            registerModel.addRow(new Object[]{
                name, 
                String.format("%d", value),
                String.format("0x%06X", value)
            })
        );
        JScrollPane registerPane = new JScrollPane(registerTable);

        // Memory Table Setup - Modo Byte (1 byte por linha)
        JTable memoryTable = new JTable();
        DefaultTableModel byteModel = new DefaultTableModel(new Object[]{"Endereço", "Byte (Decimal)", "Byte (Hex)"}, 0);
        memoryTable.setModel(byteModel);
        
        // Preenche o modelo de bytes (1 byte por linha)
        int memorySize = vmMemory.getMemorySize();
        for (int addr = 0; addr < memorySize; addr++) {
            try {
                // Como não temos acesso direto ao byte na posição addr, temos que calculá-lo
                int wordAddr = addr - (addr % 3); // Endereço da palavra que contém este byte
                int byteOffset = addr % 3;        // Deslocamento do byte dentro da palavra (0, 1, ou 2)
                
                // Verifica se o endereço da palavra está dentro dos limites
                if (wordAddr + 2 < memorySize) {
                    int wordValue = vmMemory.readWord(wordAddr);
                    
                    // Extrair o byte específico da palavra
                    int byteValue;
                    if (byteOffset == 0) {
                        byteValue = (wordValue >> 16) & 0xFF; // Byte mais significativo
                    } else if (byteOffset == 1) {
                        byteValue = (wordValue >> 8) & 0xFF;  // Byte do meio
                    } else {
                        byteValue = wordValue & 0xFF;         // Byte menos significativo
                    }
                    
                    byteModel.addRow(new Object[]{
                        String.format("0x%04X", addr),
                        String.format("%d", byteValue),
                        String.format("0x%02X", byteValue)
                    });
                } else {
                    // Se não pudermos ler uma palavra completa, mostramos um valor vazio
                    byteModel.addRow(new Object[]{
                        String.format("0x%04X", addr),
                        "-",
                        "-"
                    });
                }
            } catch (IndexOutOfBoundsException ignored) {
                // Ignora exceções de fora dos limites
                byteModel.addRow(new Object[]{
                    String.format("0x%04X", addr),
                    "Erro",
                    "Erro"
                });
            }
        }
        
        // Ajusta larguras de coluna
        memoryTable.getColumnModel().getColumn(0).setPreferredWidth(100); // Endereço
        memoryTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Decimal
        memoryTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Hex
        
        JScrollPane memoryPane = new JScrollPane(memoryTable);

        // Layout configuration
        JSplitPane splitPane = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT, 
            registerPane, 
            memoryPane
        );
        splitPane.setResizeWeight(0.3);
        
        // Adiciona um rótulo informativo no topo
        JPanel infoPanel = new JPanel();
        infoPanel.add(new JLabel("Memória exibida em bytes individuais (8 bits)"));
        
        frame.add(infoPanel, BorderLayout.NORTH);
        frame.add(splitPane, BorderLayout.CENTER);
        
        frame.setVisible(true);
    }
}
