package model;

import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

public class FunctionEvaluator {

    private Spreadsheet spreadsheet;

    public FunctionEvaluator(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    /**
     * Evaluates a function expression and returns the result.
     *
     * @param expression The function expression (e.g., "SUMA(A1:B3;C1;3)").
     * @return The computed result as a double.
     * @throws IllegalArgumentException if the function is unknown or arguments are invalid.
     */
    public double evaluateFunction(String expression) {
        // Match function name and arguments
        Pattern pattern = Pattern.compile("(\\w+)\\((.*)\\)");
        Matcher matcher = pattern.matcher(expression);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid function expression: " + expression);
        }

        String functionName = matcher.group(1).toUpperCase();
        String arguments = matcher.group(2);

        List<Double> values = parseArguments(arguments);

        switch (functionName) {
            case "SUMA":
                return values.stream().mapToDouble(Double::doubleValue).sum();
            case "MIN":
                return values.stream().mapToDouble(Double::doubleValue).min().orElseThrow(() ->
                        new IllegalArgumentException("MIN function requires at least one argument"));
            case "MAX":
                return values.stream().mapToDouble(Double::doubleValue).max().orElseThrow(() ->
                        new IllegalArgumentException("MAX function requires at least one argument"));
            case "PROMEDIO":
                return values.stream().mapToDouble(Double::doubleValue).average().orElseThrow(() ->
                        new IllegalArgumentException("PROMEDIO function requires at least one argument"));
            default:
                throw new IllegalArgumentException("Unknown function: " + functionName);
        }
    }

    /**
     * Parses the arguments of a function and returns a list of numerical values.
     *
     * @param arguments The arguments string (e.g., "A1:B3;C1;3").
     * @return A list of doubles representing the evaluated arguments.
     */
    private List<Double> parseArguments(String arguments) {
        List<String> args = Arrays.asList(arguments.split(";"));
        List<Double> values = new ArrayList<>();

        for (String arg : args) {
            arg = arg.trim();
            if (arg.matches("\\d+(\\.\\d+)?")) {
                // Numerical value
                values.add(Double.parseDouble(arg));
            } else if (arg.matches("[A-Z]+\\d+:[A-Z]+\\d+")) {
                // Range (e.g., A1:B3)
                values.addAll(getValuesFromRange(arg));
            } else if (arg.matches("[A-Z]+\\d+")) {
                // Individual cell (e.g., A1)
                values.add(getValueFromCell(arg));
            } else if (arg.matches("\\w+\\(.*\\)")) {
                // Nested function
                values.add(evaluateFunction(arg));
            } else {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
        }

        return values;
    }

    /**
     * Retrieves the values from a range (e.g., "A1:B3").
     *
     * @param range The range string.
     * @return A list of doubles representing the values in the range.
     */
    private List<Double> getValuesFromRange(String range) {
        String[] parts = range.split(":");
        String startCell = parts[0];
        String endCell = parts[1];

        int startRow = getRowNumber(startCell);
        int startCol = getColumnIndex(startCell);
        int endRow = getRowNumber(endCell);
        int endCol = getColumnIndex(endCell);

        List<Double> values = new ArrayList<>();

        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                String coordinate = getCellCoordinate(col, row);
                values.add(getValueFromCell(coordinate));
            }
        }

        return values;
    }

    /**
     * Retrieves the value of an individual cell.
     *
     * @param coordinate The cell coordinate (e.g., "A1").
     * @return The cell's numeric value.
     */
    private Double getValueFromCell(String coordinate) {
        Cell cell = spreadsheet.getCells().get(coordinate);
        if (cell == null || cell.getContent() == null) {
            return 0.0; // Treat empty cells as 0
        }

        Content content = cell.getContent();
        if (content instanceof NumericContent) {
            return ((NumericContent) content).getValueAsNumber();
        } else if (content instanceof FormulaContent) {
            return evaluateFunction(((FormulaContent) content).getFormula());
        } else {
            throw new IllegalArgumentException("Cell " + coordinate + " does not contain a numeric value");
        }
    }

    /**
     * Converts a cell coordinate to a zero-based column index (e.g., "B" -> 1).
     */
    private int getColumnIndex(String coordinate) {
        return columnNameToIndex(coordinate.replaceAll("\\d+", ""));
    }

    /**
     * Extracts the row number from a cell coordinate (e.g., "A1" -> 1).
     */
    private int getRowNumber(String coordinate) {
        return Integer.parseInt(coordinate.replaceAll("[A-Z]+", ""));
    }

    /**
     * Converts a column letter (e.g., "A") to a zero-based index.
     */
    private int columnNameToIndex(String columnName) {
        int index = 0;
        for (char ch : columnName.toCharArray()) {
            index = index * 26 + (ch - 'A' + 1);
        }
        return index - 1;
    }

    /**
     * Generates a cell coordinate (e.g., col=1, row=2 -> "B2").
     */
    private String getCellCoordinate(int col, int row) {
        return getColumnName(col) + row;
    }

    /**
     * Converts a zero-based column index to a column name (e.g., 1 -> "B").
     */
    private String getColumnName(int colIndex) {
        StringBuilder columnName = new StringBuilder();
        while (colIndex >= 0) {
            columnName.insert(0, (char) ('A' + (colIndex % 26)));
            colIndex = (colIndex / 26) - 1;
        }
        return columnName.toString();
    }
}
