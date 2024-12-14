package io;

import model.*;
import java.io.*;
import java.util.*;


public class FileManager {




/*
    public void saveSpreadsheet(String filePath, Spreadsheet spreadsheet) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            Map<String, Cell> cells = spreadsheet.getCells();

            // Group cells by row based on their coordinates (e.g., A1 -> 1, B2 -> 2)
            Map<Integer, List<Cell>> rows = new TreeMap<>();
            for (Cell cell : cells.values()) {
                int rowNumber = getRowNumber(cell.getCoordinate());
                rows.computeIfAbsent(rowNumber, k -> new ArrayList<>()).add(cell);
            }

            for (List<Cell> row : rows.values()) {
                // Sort cells by their column (e.g., A before B)
                row.sort(Comparator.comparing(c -> getColumnPart(c.getCoordinate())));

                List<String> rowContents = new ArrayList<>();
                for (Cell cell : row) {
                    String content = cell.getContent() != null ? cell.getContent().getValueAsString() : "";
                    rowContents.add(escapeContentForFile(content));
                }

                // Join the contents of the row with semicolons
                writer.write(String.join(";", rowContents));
                writer.newLine();
            }
        }
    }
*/

    public void saveSpreadsheet(String filePath, Spreadsheet spreadsheet) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            Map<String, Cell> cells = spreadsheet.getCells();

            int maxRow = 0;
            int maxCol = 0;
            for (String coordinate : cells.keySet()) {
                int row = getRowNumber(coordinate);
                int col = columnNameToIndex(getColumnPart(coordinate));
                maxRow = Math.max(maxRow, row);
                maxCol = Math.max(maxCol, col);
            }

            for (int row = 1; row <= maxRow; row++) {
                List<String> rowContents = new ArrayList<>();
                for (int col = 0; col <= maxCol; col++) {
                    String coordinate = getCellCoordinate(col, row);
                    Cell cell = cells.get(coordinate);

                    if (cell != null && cell.getContent() != null) {
                        String content;
                        Content cellContent = cell.getContent();

                        if (cellContent instanceof FormulaContent) {
                            content = ((FormulaContent) cellContent).getFormula();
                        } else {
                            content = cellContent.getValueAsString();
                        }

                        content = escapeContentForFile(content);
                        rowContents.add(content);
                    } else {
                        rowContents.add("");
                    }
                }

                writer.write(String.join(";", rowContents));
                writer.newLine();
            }
        }
    }

    private int columnNameToIndex(String columnName) {
        int index = 0;
        for (char ch : columnName.toCharArray()) {
            index = index * 26 + (ch - 'A' + 1);
        }
        return index - 1;
    }

    public Spreadsheet loadSpreadsheet(String filePath) throws IOException {
        Spreadsheet spreadsheet = new Spreadsheet();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int rowNumber = 1;

            while ((line = reader.readLine()) != null) {
                String[] contents = line.split(";");

                for (int colIndex = 0; colIndex < contents.length; colIndex++) {
                    String content = unescapeContentFromFile(contents[colIndex]);

                    if (!content.isEmpty()) {
                        String coordinate = getCellCoordinate(colIndex, rowNumber);

                        Content cellContent;
                        if (content.startsWith("=")) {
                            cellContent = new FormulaContent(content);
                        } else {
                            cellContent = parseContent(content);
                        }

                        spreadsheet.addOrModifyCell(coordinate, cellContent);
                    }
                }

                rowNumber++;
            }
        }

        return spreadsheet;
    }




    private String unescapeContentFromFile(String content) {
        return content.replace(",", ";");
    }


    private String escapeContentForFile(String content) {
        return content.replace(";", ",");
    }


    private String getCellCoordinate(int colIndex, int rowNumber) {
        return getColumnName(colIndex) + rowNumber;
    }


    private String getColumnName(int colIndex) {
        StringBuilder columnName = new StringBuilder();
        while (colIndex >= 0) {
            columnName.insert(0, (char) ('A' + (colIndex % 26)));
            colIndex = (colIndex / 26) - 1;
        }
        return columnName.toString();
    }


    private int getRowNumber(String coordinate) {
        return Integer.parseInt(coordinate.replaceAll("[A-Z]+", ""));
    }


    private String getColumnPart(String coordinate) {
        return coordinate.replaceAll("\\d+", "");
    }


    private Content parseContent(String content) {
        if (content.startsWith("=")) {
            return new FormulaContent(content);
        } else {
            try {
                return new NumericContent(Double.parseDouble(content));
            } catch (NumberFormatException e) {
                return new TextContent(content);
            }
        }
    }
}
