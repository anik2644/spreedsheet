package model;

import java.util.Map;
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
