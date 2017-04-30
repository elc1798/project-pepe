package tech.elc1798.projectpepe.activities.extras.drawing;

/**
 * A "Slave" class used by the DrawingSession to represent a TextBox. In order to save memory, rather than constructing
 * new instances of TextBox objects, only 1 TextBox object should be created. All the fields of this class are public
 * to facilitate this. Treat this class as a holder or a C struct.
 */
public class TextBox {

    /**
     * Whether or not this text box should be visible on the displayed image
     */
    public boolean visible;

    /**
     * The x coordinate of the TOP LEFT corner of the text box
     */
    public int x;

    /**
     * The y coordinate of the TOP LEFT corner of the text box
     */
    public int y;

    /**
     * The text that the text box shows
     */
    public String text;

    /**
     * Creates a TextBox object. By default, the TextBox will be at (0,0), have no text, and be invisible.
     */
    public TextBox() {
        visible = false;
        x = 0;
        y = 0;
        text = "";
    }
}
