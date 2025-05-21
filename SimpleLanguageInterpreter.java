package projectSample;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

public class SimpleLanguageInterpreter {
    private Map<String, Integer> variables = new HashMap<>();
    private int currentPosition = 0;
    private String[] tokens;
    private boolean hasError = false;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your program (end with empty line):");

        StringBuilder program = new StringBuilder();
        while (true) {
            String line = scanner.nextLine();
            if (line.isEmpty()) {
                break;
            }
            program.append(line).append("\n");
        }

        SimpleLanguageInterpreter interpreter = new SimpleLanguageInterpreter();
        interpreter.interpret(program.toString());
    }

    public void interpret(String program) {
        // Tokenize the input
        tokenize(program);

        // Parse and execute assignments
        while (currentPosition < tokens.length && !hasError) {
            if (tokens[currentPosition].equals(";")) {
                currentPosition++; // Skip empty statements
                continue;
            }

            parseAssignment();
        }

        if (!hasError) {
            System.out.println("\nFinal variable values:");
            for (Map.Entry<String, Integer> entry : variables.entrySet()) {
                System.out.println(entry.getKey() + " = " + entry.getValue());
            }
        }
    }

    private void tokenize(String program) {
        // Remove comments and split into tokens
        String cleaned = program.replaceAll("//.*|/\\*.*?\\*/", "");
        tokens = cleaned.split("(?<=[=+\\-*();])|(?=[=+\\-*();])|\\s+");

        // Filter out empty strings
        tokens = Pattern.compile("\\s+").splitAsStream(cleaned)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }

    private void parseAssignment() {
        if (!isIdentifier(tokens[currentPosition])) {
            error("Expected identifier at the beginning of assignment");
            return;
        }

        String varName = tokens[currentPosition++];

        if (currentPosition >= tokens.length || !tokens[currentPosition].equals("=")) {
            error("Expected '=' after identifier");
            return;
        }
        currentPosition++; // Skip '='

        int value = parseExp();

        if (currentPosition >= tokens.length || !tokens[currentPosition].equals(";")) {
            error("Expected ';' at the end of assignment");
            return;
        }
        currentPosition++; // Skip ';'

        if (!hasError) {
            variables.put(varName, value);
        }
    }

    private int parseExp() {
        int value = parseTerm();

        while (currentPosition < tokens.length &&
                (tokens[currentPosition].equals("+") || tokens[currentPosition].equals("-"))) {
            String op = tokens[currentPosition++];
            int term = parseTerm();

            if (op.equals("+")) {
                value += term;
            } else {
                value -= term;
            }
        }

        return value;
    }

    private int parseTerm() {
        int value = parseFact();

        while (currentPosition < tokens.length && tokens[currentPosition].equals("*")) {
            currentPosition++; // Skip '*'
            int fact = parseFact();
            value *= fact;
        }

        return value;
    }

    private int parseFact() {
        if (currentPosition >= tokens.length) {
            error("Unexpected end of input in expression");
            return 0;
        }

        String token = tokens[currentPosition];

        if (token.equals("(")) {
            currentPosition++; // Skip '('
            int value = parseExp();

            if (currentPosition >= tokens.length || !tokens[currentPosition].equals(")")) {
                error("Expected ')'");
                return 0;
            }
            currentPosition++; // Skip ')'

            return value;
        } else if (token.equals("+")) {
            currentPosition++; // Skip '+'
            return parseFact();
        } else if (token.equals("-")) {
            currentPosition++; // Skip '-'
            return -parseFact();
        } else if (isLiteral(token)) {
            currentPosition++;
            return Integer.parseInt(token);
        } else if (isIdentifier(token)) {
            currentPosition++;
            if (!variables.containsKey(token)) {
                error("Uninitialized variable: " + token);
                return 0;
            }
            return variables.get(token);
        } else {
            error("Unexpected token: " + token);
            return 0;
        }
    }

    private boolean isIdentifier(String token) {
        if (token == null || token.isEmpty()) return false;

        // First character must be a letter
        if (!Character.isLetter(token.charAt(0))) return false;

        // Subsequent characters can be letters or digits
        for (int i = 1; i < token.length(); i++) {
            if (!Character.isLetterOrDigit(token.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    private boolean isLiteral(String token) {
        if (token == null || token.isEmpty()) return false;

        // Check for single @ symbol (assuming this represents 0)
        if (token.equals("@")) return true;

        // First character must be a non-zero digit
        if (token.charAt(0) < '1' || token.charAt(0) > '9') return false;

        // Subsequent characters must be digits
        for (int i = 1; i < token.length(); i++) {
            if (!Character.isDigit(token.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    private void error(String message) {
        System.err.println("Error: " + message);
        hasError = true;
    }
}
