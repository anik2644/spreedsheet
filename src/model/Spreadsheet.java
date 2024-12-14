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
        // Convert coordinate to uppercase to handle case insensitivity
        coordinate = coordinate.toUpperCase();

        if (content instanceof FormulaContent) {
            FormulaContent formulaContent = (FormulaContent) content;

            // Check for circular dependency before adding the cell
            if (hasCircularDependency(coordinate, formulaContent)) {
                System.out.println("Circular dependency detected! Cannot add this formula to cell " + coordinate);
                return;
            }

            // Update cell content and dependencies
            updateDependencies(coordinate, formulaContent);
        }

        Cell cell = cells.getOrDefault(coordinate, new Cell(coordinate));
        cell.setContent(content);
        cells.put(coordinate, cell);

        // Recalculate values of the current cell and its dependents
        recalculateCellAndDependents(coordinate);
    }

    /**
     * Checks for a circular dependency if adding the given formula to the specified cell.
     *
     * @param coordinate     The cell coordinate where the formula is being added.
     * @param formulaContent The formula content to check.
     * @return true if a circular dependency is detected, false otherwise.
     */
    public boolean hasCircularDependency(String coordinate, FormulaContent formulaContent) {
        // Temporarily add the new dependencies for the check
        Set<String> referencedCells = extractCellReferences(formulaContent);
        updateDependencies(coordinate, formulaContent);

        // Perform DFS to detect cycles starting from this cell
        Set<String> visited = new HashSet<>();
        Set<String> stack = new HashSet<>();

        boolean hasCycle = detectCycle(coordinate, visited, stack);

        // Revert the temporary update to the dependency tree
        dependencies.values().forEach(dependents -> dependents.remove(coordinate));

        return hasCycle;
    }

    /**
     * Helper method to perform DFS and detect cycles in the dependency tree.
     *
     * @param node    The current cell being visited.
     * @param visited Set of visited cells.
     * @param stack   Set of cells in the current DFS path (to detect cycles).
     * @return true if a cycle is detected, false otherwise.
     */
    private boolean detectCycle(String node, Set<String> visited, Set<String> stack) {
        if (stack.contains(node)) {
            return true; // Cycle detected
        }

        if (visited.contains(node)) {
            return false; // Already processed this node
        }

        visited.add(node);
        stack.add(node);

        Set<String> dependents = dependencies.getOrDefault(node, Collections.emptySet());
        for (String dependent : dependents) {
            if (detectCycle(dependent, visited, stack)) {
                return true;
            }
        }

        stack.remove(node);
        return false;
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
     * @param coordinate     The cell coordinate.
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
    /**
     * Displays the spreadsheet in a tabular format with rows and columns.
     */
    public void displaySpreadsheet() {
        // Determine the maximum row and column dynamically based on the current cell keys
        int maxRow = 0;
        char maxCol = 'A';

        for (String coordinate : cells.keySet()) {
            String colPart = coordinate.replaceAll("\\d", ""); // Extract letters (column part)
            int rowPart = Integer.parseInt(coordinate.replaceAll("\\D", "")); // Extract numbers (row part)

            if (rowPart > maxRow) {
                maxRow = rowPart;
            }
            if (colPart.compareTo(String.valueOf(maxCol)) > 0) {
                maxCol = colPart.charAt(0);
            }
        }

        // Print the header row (column labels)
        System.out.print("    ");
        for (char col = 'A'; col <= maxCol; col++) {
            System.out.print(String.format("%-10s", col));
        }
        System.out.println();

        // Print each row with its row label and cell values
        for (int row = 1; row <= maxRow; row++) {
            System.out.print(String.format("%-4d", row));

            for (char col = 'A'; col <= maxCol; col++) {
                String coordinate = "" + col + row;
                Cell cell = cells.get(coordinate);

                // Print cell value or empty string if cell is not defined
                String value = (cell != null) ? cell.getValueAsString() : "";
                System.out.print(String.format("%-10s", value));
            }
            System.out.println();
        }
    }

}
