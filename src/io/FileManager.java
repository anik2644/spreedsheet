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
    /**
     * Loads a spreadsheet from an S2V file.
     *
     * @param filePath    The path of the file to load.
     * @return A populated Spreadsheet object.
     * @throws IOException If an I/O error occurs.
     */

    public void saveSpreadsheet(String filePath, Spreadsheet spreadsheet) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            Map<String, Cell> cells = spreadsheet.getCells();

            // Determine the maximum row and column used
            int maxRow = 0;
            int maxCol = 0;
            for (String coordinate : cells.keySet()) {
                int row = getRowNumber(coordinate);
                int col = columnNameToIndex(getColumnPart(coordinate));
                maxRow = Math.max(maxRow, row);
                maxCol = Math.max(maxCol, col);
            }

            // Write each row
            for (int row = 1; row <= maxRow; row++) {
                List<String> rowContents = new ArrayList<>();
                for (int col = 0; col <= maxCol; col++) {
                    String coordinate = getCellCoordinate(col, row);
                    Cell cell = cells.get(coordinate);

                    if (cell != null && cell.getContent() != null) {
                        String content;
                        Content cellContent = cell.getContent();

                        // If the content is a formula, store the formula string itself
                        if (cellContent instanceof FormulaContent) {
                            content = ((FormulaContent) cellContent).getFormula();
                        } else {
                            content = cellContent.getValueAsString();
                        }

                        // Escape content for file (convert ";" in functions to ",")
                        content = escapeContentForFile(content);
                        rowContents.add(content);
                    } else {
                        rowContents.add(""); // Empty cell
                    }
                }

                // Join the row contents with semicolons
                writer.write(String.join(";", rowContents));
                writer.newLine();
            }
        }
    }

    /**
     * Converts a column name (e.g., "A", "B", "AA") to a zero-based column index.
     *
     * @param columnName The column name.
     * @return The zero-based column index.
     */
    private int columnNameToIndex(String columnName) {
        int index = 0;
        for (char ch : columnName.toCharArray()) {
            index = index * 26 + (ch - 'A' + 1);
        }
        return index - 1; // Convert to zero-based index
    }


    public Spreadsheet loadSpreadsheet(String filePath) throws IOException {
        Spreadsheet spreadsheet = new Spreadsheet();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int rowNumber = 1;

            while ((line = reader.readLine()) != null) {
                // Split line by semicolons
                String[] contents = line.split(";");

                // Process each cell in the row
                for (int colIndex = 0; colIndex < contents.length; colIndex++) {
                    String content = unescapeContentFromFile(contents[colIndex]);

                    // If the content is not empty, process it
                    if (!content.isEmpty()) {
                        String coordinate = getCellCoordinate(colIndex, rowNumber);

                        Content cellContent;
                        if (content.startsWith("=")) {
                            // If the content starts with '=', it is a formula
                            cellContent = new FormulaContent(content);
                        } else {
                            // Otherwise, it could be a numeric or text content
                            cellContent = parseContent(content);
                        }

                        // Add the parsed content to the spreadsheet
                        spreadsheet.addOrModifyCell(coordinate, cellContent);
                    }
                }

                // Increment the row number
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
