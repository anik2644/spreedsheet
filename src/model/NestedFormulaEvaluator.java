package model;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NestedFormulaEvaluator {

    public static double evaluate(String formula, Map<String, Cell> cells) throws Exception {
        if (!formula.startsWith("=")) {
            throw new Exception("Invalid formula: " + formula);
        }

        // Remove '=' at the start
        String expression = formula.substring(1).replace(" ", "");

        return evaluateExpression(expression, cells);
    }

    private static double evaluateExpression(String expression, Map<String, Cell> cells) throws Exception {
        // Base case: if the expression is a simple number or cell reference
        if (isSimpleToken(expression)) {
            return getValue(expression, cells);
        }

        // Find innermost parentheses and evaluate them first
        while (expression.contains("(")) {
            Pattern pattern = Pattern.compile("\\([^()]+\\)");
            Matcher matcher = pattern.matcher(expression);

            if (matcher.find()) {
                String subExpression = matcher.group();
                double result = evaluateSimpleExpression(subExpression.substring(1, subExpression.length() - 1), cells);
                expression = expression.replace(subExpression, String.valueOf(result));
            }
        }

        // Evaluate the simplified expression
        return evaluateSimpleExpression(expression, cells);
    }

    private static double evaluateSimpleExpression(String expression, Map<String, Cell> cells) throws Exception {
        Stack<Double> values = new Stack<>();
        Stack<Character> operators = new Stack<>();

        int i = 0;
        while (i < expression.length()) {
            char ch = expression.charAt(i);

            if (Character.isDigit(ch) || Character.isLetter(ch)) {
                StringBuilder sb = new StringBuilder();

                while (i < expression.length() && (Character.isDigit(expression.charAt(i)) ||
                        expression.charAt(i) == '.' || Character.isLetter(expression.charAt(i)))) {
                    sb.append(expression.charAt(i));
                    i++;
                }

                double value = getValue(sb.toString(), cells);
                values.push(value);
                continue;
            }

            if (ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '%') {
                while (!operators.isEmpty() && hasPrecedence(ch, operators.peek())) {
                    double val2 = values.pop();
                    double val1 = values.pop();
                    char op = operators.pop();
                    values.push(applyOperation(op, val1, val2));
                }
                operators.push(ch);
                i++;
                continue;
            }

            i++;
        }

        while (!operators.isEmpty()) {
            double val2 = values.pop();
            double val1 = values.pop();
            char op = operators.pop();
            values.push(applyOperation(op, val1, val2));
        }

        return values.pop();
    }

    private static boolean isSimpleToken(String token) {
        return token.matches("[A-Z]+\\d+") || token.matches("\\d+(\\.\\d+)?");
    }

    private static double getValue(String token, Map<String, Cell> cells) throws Exception {
        if (cells.containsKey(token)) {
            return cells.get(token).getValueAsNumber();
        }
        try {
            return Double.parseDouble(token);
        } catch (NumberFormatException e) {
            throw new Exception("Invalid token: " + token);
        }
    }

    private static boolean hasPrecedence(char op1, char op2) {
        return (op2 == '*' || op2 == '/' || op2 == '%') && (op1 == '+' || op1 == '-');
    }

    private static double applyOperation(char operator, double a, double b) throws Exception {
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
}
