package project_biu.graph;

import java.util.Date;

/**
 * Represents a message sent between agents.
 * Saves the message data as a byte array and provides convenience getter to the data as different types.
 */
public class Message {
    public final byte[] data;
    public final String asText;
    public final double asDouble;
    public final Date date;

    public Message(byte[] data) {
        this.data = data;
        this.asText = new String(data);
        this.asDouble = convert(this.asText);
        this.date = new Date();
    }

    public Message(String asText) {
        this(asText.getBytes());
    }

    public Message(double asDouble) {
        this(String.valueOf(asDouble));
    }

    public byte[] getData() {
        return data;
    }

    public String getAsText() {
        return asText;
    }

    public double getAsDouble() {
        return asDouble;
    }

    public Date getDate() {
        return date;
    }

    private static double convert(String doubleAsString) {
        try {
            return Double.parseDouble(doubleAsString);
        } catch (NumberFormatException _) {
            return Double.NaN;
        }
    }
}