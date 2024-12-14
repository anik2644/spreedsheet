package model;
import java.util.Map;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;


import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Represents formula-based content in a cell.
 */
public class FormulaContent implements Content {
    private String formula;  // The raw formula string (e.g., "=A1+B2")
    private double cachedValue;  // Cached result of the formula after evaluation
    private boolean isEvaluated;  // Indicates if the formula has been evaluated

    // Constructor
    public FormulaContent(String formula) {
        this.formula = formula;
        this.isEvaluated = false;
    }




    /*
    // Evaluate the formula and update the cached value
    public void evaluate(Map<String, Cell> cells) throws Exception {
        if (!formula.startsWith("=")) {
            throw new Exception("Invalid formula: " + formula);
        }

        // Remove the '=' sign and any whitespace from the formula
        String expression = formula.substring(1).replace(" ", "");

        // Evaluate the expression using the updated evaluateExpression method
        double result = evaluateExpression(expression, cells);
        cachedValue = result;
        isEvaluated = true;
    }

    // Helper method to evaluate the expression with support for aggregate functions
    private double evaluateExpression(String expression, Map<String, Cell> cells) throws Exception {
        // Create an instance of FunctionEvaluator to handle aggregate functions
        FunctionEvaluator functionEvaluator = new FunctionEvaluator(new SpreadsheetWrapper(cells));

        // Replace all function calls with their computed values
        expression = replaceFunctionsWithValues(expression, functionEvaluator);

        // Evaluate the final expression (supports basic arithmetic operations)
        return evaluateArithmeticExpression(expression, cells);
    }


    */
    public void evaluate(Map<String, Cell> cells) throws Exception {
        if (!formula.startsWith("=")) {
            throw new Exception("Invalid formula: " + formula);
        }

        String expression = formula.substring(1).replace(" ", "");
        double result = evaluateExpression(expression, cells);
        cachedValue = result;
        isEvaluated = true;
    }

    // Helper method to evaluate expressions with basic operators (+, -, *, /, %)
    private double evaluateExpression(String expression, Map<String, Cell> cells) throws Exception {


        return parseAndEvaluate(expression, cells);
    }

    // Function to parse and evaluate the expression with operator precedence
    // Function to parse and evaluate the expression with operator precedence and functions
    private double parseAndEvaluate(String expression, Map<String, Cell> cells) throws Exception {
        Stack<Double> values = new Stack<>();
        Stack<Character> operators = new Stack<>();
        int i = 0;

        while (i < expression.length()) {
            char ch = expression.charAt(i);

            // Skip spaces
            if (ch == ' ') {
                i++;
                continue;
            }

            // Handle numbers, cell references, and functions
            if (Character.isDigit(ch) || Character.isLetter(ch)) {
                StringBuilder sb = new StringBuilder();

                // Extract the token (number, cell reference, or function argument)
                while (i < expression.length() && (Character.isDigit(expression.charAt(i)) ||
                        expression.charAt(i) == '.' ||
                        Character.isLetter(expression.charAt(i)) ||
                        expression.charAt(i) == ':' ||  // For ranges (e.g., A1:B2)
                        expression.charAt(i) == ',' || // For function arguments
                        expression.charAt(i) == '(' || // Handle opening parentheses for functions
                        expression.charAt(i) == ')')) { // Handle closing parentheses for functions
                    sb.append(expression.charAt(i));
                    i++;
                }

                String token = sb.toString();

               //  System.out.println("Token: " + token);

                // Check for functions (SUMA, MIN, MAX, PROMEDIO)
                if (token.startsWith("SUMA(") || token.startsWith("MIN(") ||
                        token.startsWith("MAX(") || token.startsWith("PROMEDIO(")) {

                    // System.out.println("Detected function: " + token);
                    values.push(evaluateFunction(token, cells));
                } else {
                    // Handle cell reference or number
                    double value;
                    if (cells.containsKey(token)) {
                        value = cells.get(token).getValueAsNumber();
                    } else {
                        try {
                            value = Double.parseDouble(token);  // Parse as number
                        } catch (NumberFormatException e) {
                            throw new Exception("Invalid token: " + token);  // Handle invalid tokens
                        }
                    }
                    values.push(value);
                }

                continue;
            }

            // Handle operators
            if (ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '%') {
                // Apply operator precedence
                while (!operators.isEmpty() && hasPrecedence(ch, operators.peek())) {
                    values.push(applyOperation(operators.pop(), values.pop(), values.pop()));
                }
                operators.push(ch);  // Push the current operator
                i++;  // Move past the operator
                continue;
            }

            // Handle parentheses (for grouping expressions)
            if (ch == '(') {
                operators.push(ch);  // Push the opening parenthesis
                i++;
                continue;
            } else if (ch == ')') {
                // Apply operators until matching '(' is found
                while (!operators.isEmpty() && operators.peek() != '(') {
                    values.push(applyOperation(operators.pop(), values.pop(), values.pop()));
                }
                operators.pop();  // Pop the '('
                i++;
                continue;
            }

            i++;
        }

        // Apply remaining operations
        while (!operators.isEmpty()) {
            values.push(applyOperation(operators.pop(), values.pop(), values.pop()));
        }

        return values.pop();
    }

    // Evaluate aggregate functions: SUMA, MIN, MAX, PROMEDIO
    private double evaluateFunction(String token, Map<String, Cell> cells) throws Exception {
        if (!token.endsWith(")")) {
            throw new Exception("Invalid function syntax: " + token);
        }

        int start = token.indexOf('(') + 1;
        int end = token.lastIndexOf(')');
        String arguments = token.substring(start, end);

        List<Double> values = getValuesFromArguments(arguments, cells);
     //   System.out.println("Values for function " + token + ": " + values);

        if (token.startsWith("SUMA")) {
          //  System.out.println("come to suma");
            return values.stream().mapToDouble(Double::doubleValue).sum();
        } else if (token.startsWith("MIN")) {
            return values.stream().mapToDouble(Double::doubleValue).min().orElseThrow(() -> new Exception("No values for MIN"));
        } else if (token.startsWith("MAX")) {
            return values.stream().mapToDouble(Double::doubleValue).max().orElseThrow(() -> new Exception("No values for MAX"));
        } else if (token.startsWith("PROMEDIO")) {
            return values.stream().mapToDouble(Double::doubleValue).average().orElseThrow(() -> new Exception("No values for PROMEDIO"));
        }

        throw new Exception("Unknown function: " + token);
    }

    // Helper to get values from function arguments
    private List<Double> getValuesFromArguments(String arguments, Map<String, Cell> cells) throws Exception {
        List<Double> values = new ArrayList<>();
        String[] parts = arguments.split(",");

        for (String part : parts) {
            part = part.trim();
            if (cells.containsKey(part)) {
                values.add(cells.get(part).getValueAsNumber());
            } else if (part.contains(":")) {
                // Handle ranges (e.g., A1:B2)
                values.addAll(getValuesFromRange(part, cells));
            } else {
                values.add(Double.parseDouble(part));
            }
        }
        return values;
    }

    // Helper to get values from a range (e.g., A1:B2)
    private List<Double> getValuesFromRange(String range, Map<String, Cell> cells) throws Exception {
        List<Double> values = new ArrayList<>();

        // First, check if the range is a simple comma-separated list of numbers
        if (range.contains(",")) {
            // Split by commas and treat each as an individual number
            String[] numStrings = range.split(",");
            for (String numStr : numStrings) {
                values.add(Double.parseDouble(numStr.trim()));  // Parse each number
            }
            return values;
        }

        // If the range has a colon ":", it refers to a range of cells
        if (range.contains(":")) {
            String[] bounds = range.split(":");

            if (bounds.length != 2) {
                throw new Exception("Invalid range syntax: " + range);
            }

            String start = bounds[0].trim();
            String end = bounds[1].trim();

            // For the range of cells, we need to figure out the coordinates
            int startRow = getRowFromCell(start);
            int startCol = getColFromCell(start);
            int endRow = getRowFromCell(end);
            int endCol = getColFromCell(end);

            // Iterate through the range of cells and collect values
            for (String cellRef : cells.keySet()) {
                int cellRow = getRowFromCell(cellRef);
                int cellCol = getColFromCell(cellRef);

                // Check if the cell is within the bounds of the range
                if (cellRow >= startRow && cellRow <= endRow && cellCol >= startCol && cellCol <= endCol) {
                    values.add(cells.get(cellRef).getValueAsNumber());
                }
            }
            return values;
        }

        // If it's not a comma-separated list or a range, it's an individual cell reference
        // This case is treated as a degenerate range (start and end are the same)
        values.add(cells.get(range).getValueAsNumber());
        return values;
    }

    // Helper method to extract row from cell reference (e.g., A1 -> 1)
    private int getRowFromCell(String cellRef) {
        StringBuilder rowBuilder = new StringBuilder();
        for (char ch : cellRef.toCharArray()) {
            if (Character.isDigit(ch)) {
                rowBuilder.append(ch);
            }
        }
        return Integer.parseInt(rowBuilder.toString());
    }

    // Helper method to extract column from cell reference (e.g., A1 -> A)
    private int getColFromCell(String cellRef) {
        StringBuilder colBuilder = new StringBuilder();
        for (char ch : cellRef.toCharArray()) {
            if (Character.isLetter(ch)) {
                colBuilder.append(ch);
            }
        }

        // Convert the column letters to a number (e.g., A -> 1, B -> 2)
        int colNum = 0;
        for (char ch : colBuilder.toString().toUpperCase().toCharArray()) {
            colNum = colNum * 26 + (ch - 'A' + 1);
        }
        return colNum;
    }

    // Dummy method to check if a cell reference is within a range (needs proper implementation)
    private boolean isInRange(String cellRef, String start, String end) {
        // Implement actual logic to determine if cellRef is within the start and end bounds
        return cellRef.compareTo(start) >= 0 && cellRef.compareTo(end) <= 0;
    }
    // Check operator precedence
    private boolean hasPrecedence(char op1, char op2) {


        if ((op1 == '*' || op1 == '/' || op1 == '%') && (op2 == '+' || op2 == '-')) {
            return false;
        }
        return true;
    }

    // Apply an operation to two operands
    private double applyOperation(char operator, double b, double a) throws Exception {
        switch (operator) {
            case '+': return a + b;
            case '-': return a - b;
            case '*': return a * b;
            case '/':
                if (b == 0) throw new Exception("Division by zero");
                return a / b;
            case '%':
                if (b == 0) throw new Exception("Division by zero");
                return a % b;
            default: throw new Exception("Invalid operator: " + operator);
        }
    }


    /**
     * Replaces aggregate function calls in the expression with their computed values.
     *
     * @param expression The original expression containing function calls.
     * @param functionEvaluator The FunctionEvaluator instance to evaluate functions.
     * @return The expression with function calls replaced by their computed values.
     * @throws Exception If there's an error evaluating the functions.
     */
    private String replaceFunctionsWithValues(String expression, FunctionEvaluator functionEvaluator) throws Exception {
        // Regex to match aggregate function calls (e.g., SUMA(A1:B3;C1;3))
        Pattern pattern = Pattern.compile("(SUMA|MIN|MAX|PROMEDIO)\\(([^()]+)\\)");
        Matcher matcher = pattern.matcher(expression);

        // Replace each function call with its computed value
        while (matcher.find()) {
            String functionCall = matcher.group(0);
            String result = String.valueOf(functionEvaluator.evaluateFunction(functionCall));
            expression = expression.replace(functionCall, result);
        }

        return expression;
    }

    /**
     * Evaluates a basic arithmetic expression (supports +, -, *, /).
     *
     * @param expression The arithmetic expression to evaluate.
     * @param cells The map of cells to get values from.
     * @return The computed result of the expression.
     * @throws Exception If there's an error during evaluation.
     */
    private double evaluateArithmeticExpression(String expression, Map<String, Cell> cells) throws Exception {
        // Replace cell references with their numeric values
        Pattern cellPattern = Pattern.compile("[A-Z]+\\d+");
        Matcher matcher = cellPattern.matcher(expression);

        while (matcher.find()) {
            String cellRef = matcher.group();
            Cell cell = cells.get(cellRef);
            double cellValue = (cell != null) ? cell.getValueAsNumber() : 0.0;
            expression = expression.replace(cellRef, String.valueOf(cellValue));
        }

        // Evaluate the arithmetic expression using JavaScript's built-in engine for simplicity
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");

        try {
            return ((Number) engine.eval(expression)).doubleValue();
        } catch (Exception e) {
            throw new Exception("Error evaluating expression: " + expression);
        }
    }


    @Override
    public String getValueAsString() {
        return isEvaluated ? String.valueOf(cachedValue) : "Uncomputed";
    }

    @Override
    public double getValueAsNumber() throws Exception {
        if (!isEvaluated) {
            throw new Exception("Formula not evaluated yet: " + formula);
        }
        return cachedValue;
    }

    @Override
    public String toString() {
        return formula;
    }

    public String getFormula() {
        return  formula;
    }
}
