package com.aviperl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoxObfuscator {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Expected Lox file as argument");
            System.exit(64);
        }

        byte[] bytes = new byte[0];

        try {
            bytes = Files.readAllBytes(Paths.get(args[0]));
        } catch (IOException e) {
            System.err.println("Unable to open file: " + args[0]);
            System.exit(64);
        }

        String loxCode = new String(bytes, Charset.defaultCharset());

        int fileExtensionPosition = args[0].lastIndexOf('.');
        String fileName = args[0].substring(0, fileExtensionPosition);
        String fileExtension = args[0].substring(fileExtensionPosition);

        String newFileName = fileName + "_obf" + fileExtension;

        try (FileWriter fileWriter = new FileWriter(newFileName);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
             PrintWriter printWriter = new PrintWriter(bufferedWriter)
        ) {
            printWriter.println(getObfuscatedCode(loxCode));
            System.out.println("Successfully obfuscated code");
        } catch (IOException e) {
            System.err.println("Unable to write to file");
            System.exit(64);
        }
    }

    private static String getObfuscatedCode(String loxCode) {
        List<String> keywords = List.of("and", "class", "else", "false", "fun", "for",
                "if", "nil", "or", "print", "return", "super", "this",
                "true", "var", "while", "clock", "init");

        Map<String, String> identifiers = new HashMap<>();

        keywords.forEach(word -> identifiers.put(word, word));

        String[] lines = loxCode.lines().toArray(String[]::new);

        StringBuilder obfuscatedCode = new StringBuilder();

        int count = 0;

        boolean previousIsIdentifier = false;

        for (String line : lines) {
            for (int i = 0; i < line.length(); i++) {
                char symbol = line.charAt(i);

                if (isAlpha(symbol)) {

                    for (int lookAhead = i + 1; lookAhead < line.length(); lookAhead++) {
                        if (!isAlphaNumeric(line.charAt(lookAhead))) {
                            String identifier = line.substring(i, lookAhead);

                            if (!identifiers.containsKey(identifier)) {
                                identifiers.put(identifier, "var" + count++);
                            }

                            if (previousIsIdentifier) {
                                obfuscatedCode.append(' ');
                            }

                            obfuscatedCode.append(identifiers.get(identifier));

                            i = lookAhead - 1;
                            previousIsIdentifier = true;
                            break;
                        }
                    }

                } else if (symbol == '"') {

                    for (int lookAhead = i + 1; lookAhead < line.length(); lookAhead++) {
                        if (line.charAt(lookAhead) == '"') {
                            obfuscatedCode.append(line, i, lookAhead + 1);
                            i = lookAhead;
                            previousIsIdentifier = false;
                            break;
                        }
                    }

                } else if (!Character.isWhitespace(symbol)) {
                    obfuscatedCode.append(symbol);
                    previousIsIdentifier = false;
                }
            }

            obfuscatedCode.append('\n');
        }

        return obfuscatedCode.toString();
    }

    private static boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z')
                || c == '_';
    }

    private static boolean isAlphaNumeric(char c) {
        return isAlpha(c) || (c >= '0' && c <= '9');
    }
}