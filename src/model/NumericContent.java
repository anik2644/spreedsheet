package model;

/**
 * Represents numerical content in a cell.
 */
public class NumericContent implements Content {
    private double value;

    // Constructor
    public NumericContent(double value) {
        this.value = value;
    }

    @Override
    public String getValueAsString() {
        return String.valueOf(value);
    }

    @Override
    public double getValueAsNumber() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
