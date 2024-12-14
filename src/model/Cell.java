package model;

public class Cell {
    private String coordinate;
    private Content content;

    public Cell(String coordinate) {
        this.coordinate = coordinate;
        this.content = null;
    }

    public String getCoordinate() {
        return coordinate;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    public Content getContent() {
        return content;
    }

    public String getValueAsString() {
        if (content == null) {
            return "";
        }
        return content.getValueAsString();
    }

    public double getValueAsNumber() throws Exception {
        if (content == null) {
            return 0;
        }
        return content.getValueAsNumber();
    }

    public boolean isEmpty() {
        return content == null;
    }

    @Override
    public String toString() {
        return coordinate + ": " + (content != null ? content.getValueAsString() : "Empty");
    }
}
