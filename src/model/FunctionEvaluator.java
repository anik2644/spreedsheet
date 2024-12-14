package model;

import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

public class FunctionEvaluator {

    private Spreadsheet spreadsheet;

    public FunctionEvaluator(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    public double evaluateFunction(String expression) {
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

    private List<Double> parseArguments(String arguments) {
        List<String> args = Arrays.asList(arguments.split(";"));
        List<Double> values = new ArrayList<>();

        for (String arg : args) {
            arg = arg.trim();
            if (arg.matches("\\d+(\\.\\d+)?")) {
                values.add(Double.parseDouble(arg));
            } else if (arg.matches("[A-Z]+\\d+:[A-Z]+\\d+")) {
                values.addAll(getValuesFromRange(arg));
            } else if (arg.matches("[A-Z]+\\d+")) {
                values.add(getValueFromCell(arg));
            } else if (arg.matches("\\w+\\(.*\\)")) {
                values.add(evaluateFunction(arg));
            } else {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
        }

        return values;
    }

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

    private Double getValueFromCell(String coordinate) {
        Cell cell = spreadsheet.getCells().get(coordinate);
        if (cell == null || cell.getContent() == null) {
            return 0.0;
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

    private int getColumnIndex(String coordinate) {
        return columnNameToIndex(coordinate.replaceAll("\\d+", ""));
    }

    private int getRowNumber(String coordinate) {
        return Integer.parseInt(coordinate.replaceAll("[A-Z]+", ""));
    }

    private int columnNameToIndex(String columnName) {
        int index = 0;
        for (char ch : columnName.toCharArray()) {
            index = index * 26 + (ch - 'A' + 1);
        }
        return index - 1;
    }

    private String getCellCoordinate(int col, int row) {
        return getColumnName(col) + row;
    }

    private String getColumnName(int colIndex) {
        StringBuilder columnName = new StringBuilder();
        while (colIndex >= 0) {
            columnName.insert(0, (char) ('A' + (colIndex % 26)));
            colIndex = (colIndex / 26) - 1;
        }
        return columnName.toString();
    }
}
