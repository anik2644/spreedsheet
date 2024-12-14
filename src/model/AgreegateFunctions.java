package model;
import java.util.*;

public class AgreegateFunctions {
    private final Map<String, Double> cells = new HashMap<>();

    // Set the value of a cell
    public void setCell(String cell, double value) {
        cells.put(cell, value);
    }

    // Get the value of a cell
    public double getCell(String cell) {
        return cells.getOrDefault(cell, 0.0);
    }

    // Parse and evaluate a formula
    public double parseFormula(String formula) {
        if (formula.startsWith("=")) {
            String expression = formula.substring(1);
            return evaluateExpression(expression);
        } else {
            return Double.parseDouble(formula);
        }
    }

    // Evaluate an expression with functions and arithmetic operations
    private double evaluateExpression(String expression) {
        expression = replaceFunctions(expression);
        return evaluateArithmetic(expression);
    }

    // Replace functions with their computed values
    private String replaceFunctions(String expression) {
        while (expression.matches(".*(SUMA|MIN|MAX|PROMEDIO)\\(.*?\\).*")) {
            expression = expression.replaceAll("SUMA\\((.*?)\\)", match -> String.valueOf(suma(match.group(1))));
            expression = expression.replaceAll("MIN\\((.*?)\\)", match -> String.valueOf(min(match.group(1))));
            expression = expression.replaceAll("MAX\\((.*?)\\)", match -> String.valueOf(max(match.group(1))));
            expression = expression.replaceAll("PROMEDIO\\((.*?)\\)", match -> String.valueOf(promedio(match.group(1))));
        }
        return expression;
    }

    // Evaluate arithmetic expressions
    private double evaluateArithmetic(String expression) {
        try {
            return new ExpressionEvaluator().evaluate(expression);
        } catch (Exception e) {
            System.err.println("Error evaluating expression: " + e.getMessage());
            return 0.0;
        }
    }

    // SUMA function
    private double suma(String args) {
        return getValuesFromArgs(args).stream().mapToDouble(Double::doubleValue).sum();
    }

    // MIN function
    private double min(String args) {
        return getValuesFromArgs(args).stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
    }

    // MAX function
    private double max(String args) {
        return getValuesFromArgs(args).stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
    }

    // PROMEDIO function
    private double promedio(String args) {
        List<Double> values = getValuesFromArgs(args);
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    // Get values from arguments (cells, ranges, or numbers)
    private List<Double> getValuesFromArgs(String args) {
        List<Double> values = new ArrayList<>();
        String[] splitArgs = args.split(";");
        for (String arg : splitArgs) {
            arg = arg.trim();
            if (arg.matches("[A-Z]+\\d+:[A-Z]+\\d+")) {
                values.addAll(getRangeValues(arg));
            } else if (arg.matches("[A-Z]+\\d+")) {
                values.add(getCell(arg));
            } else {
                values.add(Double.parseDouble(arg));
            }
        }
        return values;
    }

    // Get values from a cell range (e.g., A1:B2)
    private List<Double> getRangeValues(String range) {
        String[] parts = range.split(":");
        String startCell = parts[0];
        String endCell = parts[1];

        String startCol = getColumnPart(startCell);
        int startRow = getRowPart(startCell);
        String endCol = getColumnPart(endCell);
        int endRow = getRowPart(endCell);

        List<Double> values = new ArrayList<>();
        for (String col : iterateColumns(startCol, endCol)) {
            for (int row = startRow; row <= endRow; row++) {
                values.add(getCell(col + row));
            }
        }
        return values;
    }

    // Split cell into column part
    private String getColumnPart(String cell) {
        return cell.replaceAll("\\d", "");
    }

    // Split cell into row part
    private int getRowPart(String cell) {
        return Integer.parseInt(cell.replaceAll("\\D", ""));
    }

    // Iterate through columns from start to end
    private List<String> iterateColumns(String startCol, String endCol) {
        List<String> columns = new ArrayList<>();
        int startIndex = colToIndex(startCol);
        int endIndex = colToIndex(endCol);
        for (int i = startIndex; i <= endIndex; i++) {
            columns.add(indexToCol(i));
        }
        return columns;
    }

    // Convert column letter to index
    private int colToIndex(String col) {
        int result = 0;
        for (char ch : col.toCharArray()) {
            result = result * 26 + (ch - 'A' + 1);
        }
        return result;
    }

    // Convert index to column letter
    private String indexToCol(int index) {
        StringBuilder col = new StringBuilder();
        while (index > 0) {
            index--;
            col.insert(0, (char) ('A' + (index % 26)));
            index /= 26;
        }
        return col.toString();
    }

    // Main method for demonstration
    public static void main(String[] args) {
        Spreadsheet sheet = new Spreadsheet();

        // Set some cell values
        sheet.setCell("A1", 10);
        sheet.setCell("A2", 5);
        sheet.setCell("A3", 15);
        sheet.setCell("B1", 20);
        sheet.setCell("B2", 25);
        sheet.setCell("B3", 30);

        // Example formulas
        System.out.println(sheet.parseFormula("=SUMA(A1;A2;A3)"));  // Output: 30
        System.out.println(sheet.parseFormula("=MIN(A1;B1;A2)"));   // Output: 5
        System.out.println(sheet.parseFormula("=MAX(A1:B3)"));      // Output: 30
        System.out.println(sheet.parseFormula("=PROMEDIO(A1:B2)")); // Output: 15
    }
}
