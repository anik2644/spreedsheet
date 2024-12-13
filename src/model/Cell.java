package model;

public class Cell {
    private String coordinate;
    private Content content;

    // Constructor
    public Cell(String coordinate) {
        this.coordinate = coordinate;
        this.content = null;
    }

    // Get the coordinate of the cell (e.g., "A1", "B2")
    public String getCoordinate() {
        return coordinate;
    }

    // Set the content of the cell
    public void setContent(Content content) {
        this.content = content;
    }

    // Get the content of the cell
    public Content getContent() {
        return content;
    }

    // Get the value of the cell as a string
    public String getValueAsString() {
        if (content == null) {
            return "";
        }
        return content.getValueAsString();
    }

    // Get the value of the cell as a number (if applicable)
    public double getValueAsNumber() throws Exception {
        if (content == null) {
            return 0;
        }
        return content.getValueAsNumber();
    }

    // Check if the cell is empty
    public boolean isEmpty() {
        return content == null;
    }

    @Override
    public String toString() {
        return coordinate + ": " + (content != null ? content.getValueAsString() : "Empty");
    }
}
