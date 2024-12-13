package io;

import model.*;
import java.io.*;
import java.util.*;

/**
 * FileManager handles reading from and writing to S2V (Semicolon Separated Values) files.
 */
public class FileManager {

    /**
     * Saves the spreadsheet to a file in S2V format.
     *
     * @param filePath    The path of the file to save the spreadsheet.
     * @param spreadsheet The spreadsheet to save.
     * @throws IOException If an I/O error occurs.
     */
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

    /**
     * Loads a spreadsheet from an S2V file.
     *
     * @param filePath    The path of the file to load.
     * @return A populated Spreadsheet object.
     * @throws IOException If an I/O error occurs.
     */
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
                        Content cellContent = parseContent(content);
                        spreadsheet.addOrModifyCell(coordinate, cellContent);
                    }
                }
                rowNumber++;
            }
        }

        return spreadsheet;
    }

    /**
     * Converts a file-safe content back into a standard content format.
     * Converts ',' used in function arguments back to ';'.
     *
     * @param content The content string read from the file.
     * @return The unescaped content string.
     */
    private String unescapeContentFromFile(String content) {
        return content.replace(",", ";");
    }

    /**
     * Converts a content string into a file-safe format.
     * Converts ';' used in function arguments to ','.
     *
     * @param content The content string to be saved to the file.
     * @return The escaped content string.
     */
    private String escapeContentForFile(String content) {
        return content.replace(";", ",");
    }

    /**
     * Converts a column index (e.g., 0 -> A, 1 -> B) and row number into a cell coordinate.
     *
     * @param colIndex  The zero-based column index.
     * @param rowNumber The row number (1-based).
     * @return The cell coordinate (e.g., "A1").
     */
    private String getCellCoordinate(int colIndex, int rowNumber) {
        return getColumnName(colIndex) + rowNumber;
    }

    /**
     * Converts a column index into a column name (e.g., 0 -> A, 1 -> B, 26 -> AA).
     *
     * @param colIndex The zero-based column index.
     * @return The column name.
     */
    private String getColumnName(int colIndex) {
        StringBuilder columnName = new StringBuilder();
        while (colIndex >= 0) {
            columnName.insert(0, (char) ('A' + (colIndex % 26)));
            colIndex = (colIndex / 26) - 1;
        }
        return columnName.toString();
    }

    /**
     * Extracts the row number from a cell coordinate (e.g., "A1" -> 1).
     */
    private int getRowNumber(String coordinate) {
        return Integer.parseInt(coordinate.replaceAll("[A-Z]+", ""));
    }

    /**
     * Extracts the column part from a cell coordinate (e.g., "A1" -> "A").
     */
    private String getColumnPart(String coordinate) {
        return coordinate.replaceAll("\\d+", "");
    }

    /**
     * Parses a string content into the appropriate Content type.
     *
     * @param content The string content to parse.
     * @return A Content object (TextContent, NumericContent, or FormulaContent).
     */
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
