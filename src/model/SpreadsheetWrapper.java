package model;

import java.util.Map;

public class SpreadsheetWrapper extends Spreadsheet {
    private Map<String, Cell> cells;

    public SpreadsheetWrapper(Map<String, Cell> cells) {
        this.cells = cells;
    }

    @Override
    public Map<String, Cell> getCells() {
        return cells;
    }
}
