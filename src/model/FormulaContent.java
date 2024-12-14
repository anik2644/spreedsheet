package model;

import java.util.Map;

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

        String expression = formula.substring(1).replace(" ", "");
        double result = evaluateExpression(expression, cells);
        cachedValue = result;
        isEvaluated = true;
    }

    // Helper method to evaluate the expression
    private double evaluateExpression(String expression, Map<String, Cell> cells) throws Exception {
        // This is a simplified evaluator; for more complex expressions, consider using a full parser
        String[] tokens = expression.split("\\+");

        double result = 0;
        for (String token : tokens) {
            if (cells.containsKey(token)) {
                Cell cell = cells.get(token);
                result += cell.getValueAsNumber();
            } else {
                result += Double.parseDouble(token);
            }
        }

        return result;
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
