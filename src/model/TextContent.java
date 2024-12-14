package model;


public class TextContent implements Content {
    private String value;


    public TextContent(String value) {
        this.value = value;
    }

    @Override
    public String getValueAsString() {
        return value;
    }

    @Override
    public double getValueAsNumber() throws Exception {
        if (value.isEmpty()) {
            return 0;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new Exception("Cannot convert text to number: " + value);
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
