package model;

/**
 * The Content interface defines methods for different types of cell content.
 * All content types (text, numeric, or formula) must implement these methods.
 */
public interface Content {

    /**
     * Returns the value of the content as a string.
     *
     * @return The string representation of the content.
     */
    String getValueAsString();

    /**
     * Returns the value of the content as a number.
     *
     * @return The numeric representation of the content.
     * @throws Exception if the content cannot be represented as a number.
     */
    double getValueAsNumber() throws Exception;
}
