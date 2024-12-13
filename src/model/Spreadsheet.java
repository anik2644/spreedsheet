package model;

import java.util.*;

/**
 * Represents a spreadsheet that contains cells identified by their coordinates.
 * Supports adding/modifying cells, recalculating values, and handling dependencies.
 */
public class Spreadsheet {
    private Map<String, Cell> cells;                 // Stores the cells by their coordinates (e.g., "A1", "B2")
    private Map<String, Set<String>> dependencies;   // Maps a cell to the cells that depend on it

    // Constructor
    public Spreadsheet() {
        this.cells = new HashMap<>();
        this.dependencies = new HashMap<>();
    }

    /**
     * Adds or modifies a cell's content in the spreadsheet.
     *
     * @param coordinate The cell coordinate (e.g., "A1").
     * @param content    The content to set in the cell.
     */
    public void addOrModifyCell(String coordinate, Content content) {
        Cell cell = cells.getOrDefault(coordinate, new Cell(coordinate));
        cell.setContent(content);
        cells.put(coordinate, cell);

        // Update dependencies if the content is a formula
        if (content instanceof FormulaContent) {
            updateDependencies(coordinate, (FormulaContent) content);
        }

        // Recalculate values of the current cell and its dependents
        recalculateCellAndDependents(coordinate);
    }

    /**
     * Retrieves the cell at the specified coordinate.
     *
     * @param coordinate The cell coordinate.
     * @return The cell object, or null if it does not exist.
     */
    public Cell getCell(String coordinate) {
        return cells.get(coordinate);
    }


    /**
     * Returns the map of all cells in the spreadsheet.
     *
     * @return The map of cells.
     */
    public Map<String, Cell> getCells() {
        return cells;
    }
    /**
     * Recalculates the specified cell and any cells that depend on it.
     *
     * @param coordinate The cell coordinate to recalculate.
     */
    private void recalculateCellAndDependents(String coordinate) {
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(coordinate);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (!visited.contains(current)) {
                visited.add(current);
                Cell cell = cells.get(current);

                try {
                    if (cell.getContent() instanceof FormulaContent) {
                        ((FormulaContent) cell.getContent()).evaluate(cells);
                    }
                } catch (Exception e) {
                    System.err.println("Error evaluating cell " + current + ": " + e.getMessage());
                }

                // Add dependents to the queue for recalculation
                if (dependencies.containsKey(current)) {
                    queue.addAll(dependencies.get(current));
                }
            }
        }
    }

    /**
     * Updates the dependency graph for a cell with a formula.
     *
     * @param coordinate The cell coordinate.
     * @param formulaContent The formula content.
     */
    private void updateDependencies(String coordinate, FormulaContent formulaContent) {
        // Remove existing dependencies for this cell
        dependencies.values().forEach(dependents -> dependents.remove(coordinate));

        // Extract cell references from the formula
        Set<String> referencedCells = extractCellReferences(formulaContent);

        // Add new dependencies
        for (String ref : referencedCells) {
            dependencies.computeIfAbsent(ref, k -> new HashSet<>()).add(coordinate);
        }
    }

    /**
     * Extracts cell references from a formula content.
     *
     * @param formulaContent The formula content.
     * @return A set of cell references found in the formula.
     */
    private Set<String> extractCellReferences(FormulaContent formulaContent) {
        Set<String> references = new HashSet<>();
        String formula = formulaContent.toString().substring(1); // Remove the '=' sign

        // Simple regex to match cell references (e.g., A1, B2, C3)
        String regex = "[A-Z]+\\d+";
        Scanner scanner = new Scanner(formula);
        while (scanner.findInLine(regex) != null) {
            references.add(scanner.match().group());
        }

        return references;
    }

    /**
     * Prints the entire spreadsheet with cell coordinates and their values.
     */
    public void displaySpreadsheet() {
        for (String coordinate : cells.keySet()) {
            Cell cell = cells.get(coordinate);
            System.out.println(coordinate + ": " + cell.getValueAsString());
        }
    }
}
