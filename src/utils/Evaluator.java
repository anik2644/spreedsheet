package utils;

import model.*;
import java.util.*;

/**
 * The Evaluator class computes the result of formulas and checks for circular dependencies.
 */
public class Evaluator {

    /**
     * Evaluates a formula in a cell and returns the computed result.
     *
     * @param cell       The cell containing the formula to be evaluated.
     * @param cells      A map of all cells in the spreadsheet.
     * @param visited    A set of cells being visited to detect circular dependencies.
     * @return The numeric result of the formula.
     * @throws Exception If there are syntax errors, circular dependencies, or evaluation issues.
     */
    public double evaluate(Cell cell, Map<String, Cell> cells, Set<String> visited) throws Exception {
        if (cell == null || cell.getContent() == null) {
            throw new Exception("Cell is empty or does not exist.");
        }

        // Check for circular dependencies
        String coordinate = cell.getCoordinate();
        if (visited.contains(coordinate)) {
            throw new Exception("Circular dependency detected at cell: " + coordinate);
        }

        // Mark the current cell as visited
        visited.add(coordinate);

        // If the cell contains a formula, evaluate it
        if (cell.getContent() instanceof FormulaContent) {
            FormulaContent formulaContent = (FormulaContent) cell.getContent();
            return evaluateFormula(formulaContent, cells, visited);
        }

        // For numeric or text content, return the numeric value
        return cell.getContent().getValueAsNumber();
    }

    /**
     * Evaluates the formula content and returns the computed result.
     *
     * @param formulaContent The formula content to be evaluated.
     * @param cells          A map of all cells in the spreadsheet.
     * @param visited        A set of cells being visited to detect circular dependencies.
     * @return The numeric result of the formula.
     * @throws Exception If there are syntax errors or evaluation issues.
     */
    private double evaluateFormula(FormulaContent formulaContent, Map<String, Cell> cells, Set<String> visited) throws Exception {
        String formula = formulaContent.toString().substring(1); // Remove the '=' sign

        // Tokenize the formula
        Parser parser = new Parser();
        List<String> tokens = parser.parseFormula("=" + formula);

        Stack<Double> values = new Stack<>();
        Stack<String> operators = new Stack<>();

        for (String token : tokens) {
            if (isNumber(token)) {
                values.push(Double.parseDouble(token));
            } else if (isCellReference(token)) {
                // Evaluate the referenced cell
                Cell referencedCell = cells.get(token);
                if (referencedCell == null) {
                    throw new Exception("Cell " + token + " does not exist.");
                }
                double cellValue = evaluate(referencedCell, cells, new HashSet<>(visited));
                values.push(cellValue);
            } else if (isOperator(token)) {
                while (!operators.isEmpty() && hasPrecedence(token, operators.peek())) {
                    double result = applyOperator(operators.pop(), values.pop(), values.pop());
                    values.push(result);
                }
                operators.push(token);
            } else {
                throw new Exception("Invalid token in formula: " + token);
            }
        }

        while (!operators.isEmpty()) {
            double result = applyOperator(operators.pop(), values.pop(), values.pop());
            values.push(result);
        }

        return values.pop();
    }

    /**
     * Checks if a token is a number.
     */
    private boolean isNumber(String token) {
        return token.matches("\\d+(\\.\\d+)?");
    }

    /**
     * Checks if a token is a cell reference (e.g., A1, B2).
     */
    private boolean isCellReference(String token) {
        return token.matches("[A-Z]+\\d+");
    }

    /**
     * Checks if a token is an operator (+, -, *, /).
     */
    private boolean isOperator(String token) {
        return token.matches("[+\\-*/]");
    }

    /**
     * Determines operator precedence.
     */
    private boolean hasPrecedence(String op1, String op2) {
        if ((op1.equals("*") || op1.equals("/")) && (op2.equals("+") || op2.equals("-"))) {
            return false;
        }
        return true;
    }

    /**
     * Applies an operator to two operands.
     */
    private double applyOperator(String operator, double b, double a) throws Exception {
        switch (operator) {
            case "+": return a + b;
            case "-": return a - b;
            case "*": return a * b;
            case "/":
                if (b == 0) {
                    throw new Exception("Division by zero.");
                }
                return a / b;
            default: throw new Exception("Unknown operator: " + operator);
        }
    }
}
