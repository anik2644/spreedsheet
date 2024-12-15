package model;

import java.util.*;

public class Spreadsheet {
    public static  Map<String, String> coordinateMap = new HashMap<>();
    private Map<String, Cell> cells;
    private Map<String, Set<String>> dependencies;

    public Spreadsheet() {
        this.cells = new HashMap<>();
        this.dependencies = new HashMap<>();
    }

    public void addOrModifyCell(String coordinate, Content content) {
        coordinate = coordinate.toUpperCase();

        if (content instanceof FormulaContent) {
            FormulaContent formulaContent = (FormulaContent) content;










            if (hasCircularDependency(coordinate, formulaContent)) {
                System.out.println("Circular dependency detected! Cannot add this formula to cell " + coordinate);
                return;
            }

            updateDependencies(coordinate, formulaContent);
        }

        Cell cell = cells.getOrDefault(coordinate, new Cell(coordinate));
        cell.setContent(content);
        cells.put(coordinate, cell);

        recalculateCellAndDependents(coordinate);
    }

    public boolean hasCircularDependency(String coordinate, FormulaContent formulaContent) {
        Set<String> referencedCells = extractCellReferences(formulaContent);
        updateDependencies(coordinate, formulaContent);

        Set<String> visited = new HashSet<>();
        Set<String> stack = new HashSet<>();

        boolean hasCycle = detectCycle(coordinate, visited, stack);

        dependencies.values().forEach(dependents -> dependents.remove(coordinate));

        return hasCycle;
    }

    private boolean detectCycle(String node, Set<String> visited, Set<String> stack) {
        if (stack.contains(node)) {
            return true;
        }

        if (visited.contains(node)) {
            return false;
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

    public Cell getCell(String coordinate) {
        return cells.get(coordinate);
    }

    public Map<String, Cell> getCells() {
        return cells;
    }

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

                if (dependencies.containsKey(current)) {
                    queue.addAll(dependencies.get(current));
                }
            }
        }
    }

    private void updateDependencies(String coordinate, FormulaContent formulaContent) {
        dependencies.values().forEach(dependents -> dependents.remove(coordinate));

        Set<String> referencedCells = extractCellReferences(formulaContent);

        for (String ref : referencedCells) {
            dependencies.computeIfAbsent(ref, k -> new HashSet<>()).add(coordinate);
        }
    }

    private Set<String> extractCellReferences(FormulaContent formulaContent) {
        Set<String> references = new HashSet<>();
        String formula = formulaContent.toString().substring(1);

        String regex = "[A-Z]+\\d+";
        Scanner scanner = new Scanner(formula);
        while (scanner.findInLine(regex) != null) {
            references.add(scanner.match().group());
        }

        return references;
    }

    public void displaySpreadsheet() {
        int maxRow = 0;
        char maxCol = 'A';

        for (String coordinate : cells.keySet()) {
            String colPart = coordinate.replaceAll("\\d", "");
            int rowPart = Integer.parseInt(coordinate.replaceAll("\\D", ""));

            if (rowPart > maxRow) {
                maxRow = rowPart;
            }
            if (colPart.compareTo(String.valueOf(maxCol)) > 0) {
                maxCol = colPart.charAt(0);
            }
        }

        System.out.print("    ");
        for (char col = 'A'; col <= maxCol; col++) {
            System.out.print(String.format("%-10s", col));
        }
        System.out.println();

        for (int row = 1; row <= maxRow; row++) {
            System.out.print(String.format("%-4d", row));

            for (char col = 'A'; col <= maxCol; col++) {
                String coordinate = "" + col + row;
                Cell cell = cells.get(coordinate);

                String value = (cell != null) ? cell.getValueAsString() : "";
                System.out.print(String.format("%-10s", value));
            }
            System.out.println();
        }
    }
}