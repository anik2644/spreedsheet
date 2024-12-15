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
    private String formula;
    private double cachedValue;
    private boolean isEvaluated;

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

    private double evaluateExpression(String expression, Map<String, Cell> cells) throws Exception {


        return parseAndEvaluate(expression, cells);
    }

    private double parseAndEvaluate(String expression, Map<String, Cell> cells) throws Exception {
        Stack<Double> values = new Stack<>();
        Stack<Character> operators = new Stack<>();
        int i = 0;

        while (i < expression.length()) {
            char ch = expression.charAt(i);

            if (ch == ' ') {
                i++;
                continue;
            }

            if (Character.isDigit(ch) || Character.isLetter(ch)) {
                StringBuilder sb = new StringBuilder();

                while (i < expression.length() && (Character.isDigit(expression.charAt(i)) ||
                        expression.charAt(i) == '.' ||
                        Character.isLetter(expression.charAt(i)) ||
                        expression.charAt(i) == ':' ||
                        expression.charAt(i) == ',' ||
                        expression.charAt(i) == ';' ||
                        expression.charAt(i) == '(' ||
                        expression.charAt(i) == ')')) {
                    sb.append(expression.charAt(i));
                    i++;
                }

                String token = sb.toString();
                // System.out.println("Token: " + token);

                if (token.startsWith("SUMA(") || token.startsWith("MIN(") ||
                        token.startsWith("MAX(") || token.startsWith("PROMEDIO(")) {
                    // System.out.println("Detected function: " + token);
                    values.push(evaluateFunction(token, cells));
                } else {
                    double value;
                    if (cells.containsKey(token)) {
                        value = cells.get(token).getValueAsNumber();
                        // System.out.println("Cell reference " + token + " value: " + value);
                    } else {
                        try {
                            value = Double.parseDouble(token);
                            // System.out.println("Parsed number: " + value);
                        } catch (NumberFormatException e) {
                            throw new Exception("Invalid token: " + token);
                        }
                    }
                    values.push(value);
                }
                continue;
            }

            if (ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '%') {
                while (!operators.isEmpty() && hasPrecedence(ch, operators.peek())) {
                    double val2 = values.pop();
                    double val1 = values.pop();
                    char op = operators.pop();
                    double result = applyOperation(op, val1, val2);
                    // System.out.println("Applied operator " + op + " to " + val1 + " and " + val2 + " result: " + result);
                    values.push(result);
                }
                operators.push(ch);
                // System.out.println("Pushed operator: " + ch);
                i++;
                continue;
            }

            if (ch == '(') {
                operators.push(ch);
                // System.out.println("Pushed opening parenthesis: (");
                i++;
                continue;
            } else if (ch == ')') {
                while (!operators.isEmpty() && operators.peek() != '(') {
                    double val2 = values.pop();
                    double val1 = values.pop();
                    char op = operators.pop();
                    double result = applyOperation(op, val1, val2);
                    // System.out.println("Applied operator " + op + " to " + val1 + " and " + val2 + " result: " + result);
                    values.push(result);
                }
                operators.pop();
                // System.out.println("Popped opening parenthesis: (");
                i++;
                continue;
            }

            i++;
        }

        while (!operators.isEmpty()) {
            double val2 = values.pop();
            double val1 = values.pop();
            char op = operators.pop();
            double result = applyOperation(op, val1, val2);
            // System.out.println("Applied operator " + op + " to " + val1 + " and " + val2 + " result: " + result);
            values.push(result);
        }

        double finalResult = values.pop();
        // System.out.println("Final result: " + finalResult);
        return finalResult;
    }

    private double evaluateFunction(String token, Map<String, Cell> cells) throws Exception {
        if (!token.endsWith(")")) {
            throw new Exception("Invalid function syntax: " + token);
        }

        int start = token.indexOf('(') + 1;
        int end = token.lastIndexOf(')');
        String arguments = token.substring(start, end);

        List<Double> values = getValuesFromArguments(arguments, cells);
        //   System.out.println("Values for function " + token + ": " + values);
        //System.out.println("Anik Evaluating function token: " + token);


        if (token.startsWith("SUMA")) {
            //System.out.println("hello Evaluating function token: " + token);

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

    private List<Double> getValuesFromArguments(String arguments, Map<String, Cell> cells) throws Exception {
        List<Double> values = new ArrayList<>();
        String[] parts = arguments.split("[,;]");

        for (String part : parts) {
            part = part.trim();
            if (cells.containsKey(part)) {
                values.add(cells.get(part).getValueAsNumber());
            } else if (part.contains(":")) {

                values.addAll(getValuesFromRange(part, cells));
            } else {
                values.add(Double.parseDouble(part));
            }
        }
        return values;
    }


    private List<Double> getValuesFromRange(String range, Map<String, Cell> cells) throws Exception {
        List<Double> values = new ArrayList<>();

        if (range.contains(";")) {
            String[] cellRefs = range.split(";");
            for (String numStr : cellRefs) {
                values.add(Double.parseDouble(numStr.trim())); // Parse each number
            }
            return values;

//            for (String cellRef : cellRefs) {
//                cellRef = cellRef.trim();
//                if (cells.containsKey(cellRef)) {
//                    values.add(cells.get(cellRef).getValueAsNumber());
//                } else {
//                    throw new Exception("Invalid cell reference: " + cellRef);
//                }
//            }
//            return values;
        }

        if (range.contains(",")) {
            String[] numStrings = range.split(",");
            for (String numStr : numStrings) {
                values.add(Double.parseDouble(numStr.trim()));
            }
            return values;
        }

        if (range.contains(":")) {
            String[] bounds = range.split(":");

            if (bounds.length != 2) {
                throw new Exception("Invalid range syntax: " + range);
            }

            String start = bounds[0].trim();
            String end = bounds[1].trim();


            int startRow = getRowFromCell(start);
            int startCol = getColFromCell(start);
            int endRow = getRowFromCell(end);
            int endCol = getColFromCell(end);

            for (String cellRef : cells.keySet()) {
                int cellRow = getRowFromCell(cellRef);
                int cellCol = getColFromCell(cellRef);


                if (cellRow >= startRow && cellRow <= endRow && cellCol >= startCol && cellCol <= endCol) {
                    values.add(cells.get(cellRef).getValueAsNumber());
                }
            }
            return values;
        }


        values.add(cells.get(range).getValueAsNumber());
        return values;
    }

    private int getRowFromCell(String cellRef) {
        StringBuilder rowBuilder = new StringBuilder();
        for (char ch : cellRef.toCharArray()) {
            if (Character.isDigit(ch)) {
                rowBuilder.append(ch);
            }
        }
        return Integer.parseInt(rowBuilder.toString());
    }


    private int getColFromCell(String cellRef) {
        StringBuilder colBuilder = new StringBuilder();
        for (char ch : cellRef.toCharArray()) {
            if (Character.isLetter(ch)) {
                colBuilder.append(ch);
            }
        }


        int colNum = 0;
        for (char ch : colBuilder.toString().toUpperCase().toCharArray()) {
            colNum = colNum * 26 + (ch - 'A' + 1);
        }
        return colNum;
    }

    private boolean isInRange(String cellRef, String start, String end) {
        // Implement actual logic to determine if cellRef is within the start and end bounds
        return cellRef.compareTo(start) >= 0 && cellRef.compareTo(end) <= 0;
    }

    private boolean hasPrecedence(char op1, char op2) {


        if ((op1 == '*' || op1 == '/' || op1 == '%') && (op2 == '+' || op2 == '-')) {
            return false;
        }
        return true;
    }

    private double applyOperation(char operator, double b, double a) throws Exception {
        switch (operator) {
            case '+': return a + b;
            case '-': return b - a;
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