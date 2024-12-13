package utils;

import java.util.*;
import java.util.regex.*;
import model.*;

/**
 * The Parser class handles parsing of formulas and functions in spreadsheet cells.
 * It validates the syntax and extracts components such as cell references, ranges, and functions.
 */
public class Parser {

    // Supported functions
    private static final Set<String> SUPPORTED_FUNCTIONS = new HashSet<>(Arrays.asList("SUMA", "MIN", "MAX", "PROMEDIO"));

    /**
     * Parses a formula string and returns a list of tokens (cell references, numbers, operators, or functions).
     *
     * @param formula The formula string to parse (e.g., "=SUMA(A1:B2) + 5").
     * @return A list of tokens representing the parsed formula.
     * @throws Exception if the formula has syntax errors.
     */
    public List<String> parseFormula(String formula) throws Exception {
        if (!formula.startsWith("=")) {
            throw new Exception("Invalid formula: Formulas must start with '='");
        }

        String expression = formula.substring(1).trim(); // Remove the '=' sign
        List<String> tokens = new ArrayList<>();

        // Regex patterns for different components in the formula
        String cellReferencePattern = "[A-Z]+\\d+";                  // e.g., A1, B2
        String rangePattern = "[A-Z]+\\d+:[A-Z]+\\d+";               // e.g., A1:B2
        String functionPattern = "\\b(SUMA|MIN|MAX|PROMEDIO)\\b";    // Supported functions
        String numberPattern = "\\d+(\\.\\d+)?";                     // Numbers (integer or decimal)
        String operatorPattern = "[+\\-*/()]";                       // Arithmetic operators

        // Combine all patterns
        String combinedPattern = String.format("(%s)|(%s)|(%s)|(%s)|(%s)",
                functionPattern, rangePattern, cellReferencePattern, numberPattern, operatorPattern);

        Pattern pattern = Pattern.compile(combinedPattern);
        Matcher matcher = pattern.matcher(expression);

        int lastEnd = 0;
        while (matcher.find()) {
            // Check for any unexpected characters between tokens
            if (matcher.start() != lastEnd) {
                throw new Exception("Syntax error near: " + expression.substring(lastEnd, matcher.start()));
            }

            tokens.add(matcher.group());
            lastEnd = matcher.end();
        }

        // Check if there are any trailing unexpected characters
        if (lastEnd != expression.length()) {
            throw new Exception("Syntax error near: " + expression.substring(lastEnd));
        }

        return tokens;
    }

    /**
     * Validates a function token and its arguments.
     *
     * @param functionToken The function token (e.g., "SUMA").
     * @param arguments The list of arguments as strings.
     * @throws Exception if the function or arguments are invalid.
     */
    public void validateFunction(String functionToken, List<String> arguments) throws Exception {
        if (!SUPPORTED_FUNCTIONS.contains(functionToken)) {
            throw new Exception("Unsupported function: " + functionToken);
        }

        if (arguments.isEmpty()) {
            throw new Exception("Function " + functionToken + " requires at least one argument.");
        }

        for (String arg : arguments) {
            if (!arg.matches("[A-Z]+\\d+(:[A-Z]+\\d+)?") && !arg.matches("\\d+(\\.\\d+)?")) {
                throw new Exception("Invalid argument for function " + functionToken + ": " + arg);
            }
        }
    }
}
