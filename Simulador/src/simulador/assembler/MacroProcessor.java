package simulador.assembler;

import java.io.*;
import java.util.*;

public class MacroProcessor {

    // Tabela de definições de macro: mnemônico da macro -> definição
    private Map<String, MacroDefinition> macroTable = new HashMap<>();


     //Processa todos os arquivos de entrada encontrados em 'inputPath'
     //Se 'inputPath' for um diretório, ele lê todos os arquivos com extensão .asm
     // Se for um arquivo, processa apenas esse arquivo
     
     // Cada arquivo de entrada gera um arquivo de saída único na pasta "AssemblyCodes"
     
    public void processFiles(String inputPath) throws IOException {
        List<String> inputFilePaths = new ArrayList<>();
        File inFile = new File(inputPath);

        if (inFile.isDirectory()) {
            // Caso o input seja um diretório, pega todos os arquivos .asm
            File[] files = inFile.listFiles((dir, name) -> name.toLowerCase().endsWith(".asm"));
            if (files != null) {
                Arrays.sort(files, Comparator.comparing(File::getName));
                for (File file : files) {
                    inputFilePaths.add(file.getAbsolutePath());
                }
            }
        } else {
            // Input é um arquivo único
            inputFilePaths.add(inputPath);
        }

        // Processa cada arquivo individualmente
        for (String filePath : inputFilePaths) {
            processFile(filePath);
        }
    }

     // Processa o arquivo fonte, realizando a expansão das macros e
     // gerando um arquivo de saída com nome único na pasta AssemblyCodes
     // Essa implementação realiza duas passagens:
     //   	Coleta todas as definições de macro.
     //   	Expande as chamadas de macro no restante do código.
       
    public void processFile(String inputFilePath) throws IOException {
        // Reinicia a tabela de macros para cada arquivo
        macroTable = new HashMap<>();

        // Lê todas as linhas do arquivo fonte
        List<String> allLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                allLines.add(line);
            }
        }

        // Primeira passagem: coleta todas as definições de macro e constrói lista de linhas sem as definições
        List<String> nonMacroLines = new ArrayList<>();
        for (int i = 0; i < allLines.size(); i++) {
            String line = allLines.get(i).trim();
            if (line.isEmpty())
                continue;
            String[] tokens = line.split("\\s+");
            // Se houver pelo menos dois tokens e o segundo for "MACRO", é uma definição
            if (tokens.length > 1 && tokens[1].equalsIgnoreCase("MACRO")) {
                MacroDefinition macro = parseMacroDefinitionFromList(allLines, i);
                macroTable.put(macro.getName(), macro);
                // Pula todas as linhas da definição (até o "MEND")
                i = macro.getEndLineIndex();
            } else {
                nonMacroLines.add(line);
            }
        }

        // Segunda passagem: expande chamadas de macro nas linhas restantes
        List<String> outputLines = new ArrayList<>();
        for (String line : nonMacroLines) {
            String[] tokens = line.split("\\s+");
            // Se o primeiro token for o nome de uma macro registrada, é chamada de macro
            if (tokens.length > 0 && macroTable.containsKey(tokens[0])) {
                String expansion = expandMacro(line);
                String[] expandedLines = expansion.split("\\r?\\n");
                Collections.addAll(outputLines, expandedLines);
            } else {
                outputLines.add(line);
            }
        }

        // Gera o diretório de saída "AssemblyCodes"
        File outputDir = new File("AssemblyCodes");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        // Gera um nome único para o arquivo de saída (MASMAPRG0.ASM, MASMAPRG1.ASM, ...)
        String baseName = "MASMAPRG";
        String extension = ".ASM";
        int counter = 0;
        File outputFile = new File(outputDir, baseName + counter + extension);
        while (outputFile.exists()) {
            counter++;
            outputFile = new File(outputDir, baseName + counter + extension);
        }

        // Grava o arquivo de saída
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            for (String outLine : outputLines) {
                writer.write(outLine);
                writer.newLine();
            }
        }
        System.out.println("Arquivo processado: " + inputFilePath);
        System.out.println("Saída gerada: " + outputFile.getAbsolutePath());
    }

     // Lê uma definição de macro a partir da lista de linhas
     // Retorna a MacroDefinition e registra o índice da linha final (onde ocorre "MEND")
     //
    private MacroDefinition parseMacroDefinitionFromList(List<String> lines, int startIndex) {
        String headerLine = lines.get(startIndex).trim();
        String[] tokens = headerLine.split("\\s+");
        String macroName = tokens[0];
        List<String> parameters = new ArrayList<>();

        if (tokens.length > 2 && tokens[1].equalsIgnoreCase("MACRO")) {
            StringBuilder paramsBuilder = new StringBuilder();
            for (int i = 2; i < tokens.length; i++) {
                paramsBuilder.append(tokens[i]).append(" ");
            }
            String paramsPart = paramsBuilder.toString().trim();
            if (!paramsPart.isEmpty()) {
                String[] params = paramsPart.split(",");
                for (String param : params) {
                    parameters.add(param.trim());
                }
            }
        }

        List<String> body = new ArrayList<>();
        int endIndex = startIndex;
        for (int i = startIndex + 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.equalsIgnoreCase("MEND")) {
                endIndex = i;
                break;
            }
            body.add(line);
        }
        MacroDefinition macro = new MacroDefinition(macroName, parameters, body);
        macro.setEndLineIndex(endIndex);
        return macro;
    }
    
     // Expande uma chamada de macro substituindo os parâmetros formais
     // pelos argumentos reais fornecidos na chamada.
     
    private String expandMacro(String callLine) {
        String[] parts = callLine.split("\\s+", 2);
        String macroName = parts[0];
        MacroDefinition macro = macroTable.get(macroName);
        Map<String, String> argMap = new HashMap<>();

        if (parts.length > 1) {
            String argsPart = parts[1];
            String[] args = argsPart.split(",");
            for (int i = 0; i < args.length && i < macro.getParameters().size(); i++) {
                argMap.put(macro.getParameters().get(i), args[i].trim());
            }
        }

        StringBuilder expanded = new StringBuilder();
        for (String bodyLine : macro.getBody()) {
            String expandedLine = bodyLine;
            for (String param : macro.getParameters()) {
                if (argMap.containsKey(param)) {
                    expandedLine = expandedLine.replace(param, argMap.get(param));
                }
            }
            expanded.append(expandedLine).append("\n");
        }
        return expanded.toString().trim();
    }
}

 // Classe auxiliar que representa a definição de uma macro.
 
class MacroDefinition {
    private String name;
    private List<String> parameters;
    private List<String> body;
    private int endLineIndex; // Índice da linha "MEND" no arquivo original

    public MacroDefinition(String name, List<String> parameters, List<String> body) {
        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }

    public String getName() {
        return name;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public List<String> getBody() {
        return body;
    }

    public int getEndLineIndex() {
        return endLineIndex;
    }

    public void setEndLineIndex(int endLineIndex) {
        this.endLineIndex = endLineIndex;
    }
}