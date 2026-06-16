package project_biu.graph;

/**
 * An interface for classes that has a mathematical expression
 */
public interface MathematicalDescribable {
    /**
     * Returns the mathematical representation of the class with the given inputs
     *
     * @param inputs The inputs to the math operation
     * @return The mathematical representation of the inputs
     */
    String getMathPattern(String... inputs);

    /**
     * Returns the <strong>Total</strong> mathematical representation of the class
     *
     * @return The math expression of the class
     */
    String getMathRepresentation();
}