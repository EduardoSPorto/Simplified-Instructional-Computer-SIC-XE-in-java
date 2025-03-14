package simulador.assembler;

import java.io.*;
import java.util.*;

public class MacroProcessor {
	
    // Tabela de definições de macro: mnemônico da macro -> definição
    private Map<String, MacroDefinition> macroTable = new HashMap<>();


     //Processa o arquivo fonte, realizando a expansão das macros e gerando o arquivo de saída MASMAPRG.ASM.
    public void processFile(String inputFilePath) throws IOException {
        List<String> outputLines = new ArrayList<>();

        // Abre o arquivo fonte para leitura
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
            processLines(reader, outputLines);
        }

        // Grava o arquivo de saída com o nome MASMAPRG.ASM
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("MASMAPRG.ASM"))) {
            for (String line : outputLines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }


    //Processa as linhas do arquivo fonte, detectando definições de macro e expandindo chamadas de macro
    //Tudo aqui em processLines foi pesadamente modificado com o tempo, parte mais propensa a problema, primeiro lugar para olhar em caso de bugs
    private void processLines(BufferedReader reader, List<String> outputLines) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty())
                continue;

            System.out.println("Linha lida: " + line);  // Debug

            String[] tokens = line.split("\\s+");
            // Verifica se há pelo menos dois tokens e se o segundo token é "MACRO"
            if (tokens.length > 1 && tokens[1].equalsIgnoreCase("MACRO")) {
                System.out.println("Definição de macro detectada: " + line);  // Debug
                MacroDefinition macro = parseMacroDefinition(line, reader);
                macroTable.put(macro.getName(), macro);
                continue;
            }

            // Verifica se a linha é uma chamada de macro
            if (macroTable.containsKey(tokens[0])) {
                System.out.println("Chamada de macro detectada: " + line);  // Debug
                String expansion = expandMacro(line);
                String[] expandedLines = expansion.split("\\r?\\n");
                Collections.addAll(outputLines, expandedLines);
            } else {
                // Linha comum, sem macro: adiciona normalmente
                outputLines.add(line);
            }
        }
    }



 
     // Realiza a leitura de uma definição de macro. Suporta também definições aninhadas

    private MacroDefinition parseMacroDefinition(String headerLine, BufferedReader reader) throws IOException {
        // Exemplo de cabeçalho: "MACRO1 MACRO &ARG1, &ARG2"
        String[] tokens = headerLine.split("\\s+");
        String macroName = tokens[0];
        List<String> parameters = new ArrayList<>();
        
        // Verifica se há pelo menos 3 tokens (nome, "MACRO" e pelo menos um parâmetro)
        if (tokens.length > 2 && tokens[1].equalsIgnoreCase("MACRO")) {
            // Junta os tokens a partir do índice 2 para formar a parte dos parâmetros
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
        String line;
        // Lê o corpo da macro até encontrar "MEND"
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.equalsIgnoreCase("MEND"))
                break;
            // Para macros alinhados
            String[] tokensLine = line.split("\\s+");
            if (tokensLine.length > 1 && tokensLine[1].equalsIgnoreCase("MACRO")) {
                MacroDefinition nestedMacro = parseMacroDefinition(line, reader);
                macroTable.put(nestedMacro.getName(), nestedMacro);
                continue;
            }
            body.add(line);
        }
        return new MacroDefinition(macroName, parameters, body);
    }

    //substitui os parâmetros formais pelos argumentos reais fornecidos na chamada
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

        // Substitui os parâmetros na macro pelo argumento correspondente
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

class MacroDefinition {
    private String name;
    private List<String> parameters;
    private List<String> body;

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
}
