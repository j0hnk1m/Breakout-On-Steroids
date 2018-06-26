import java.awt.*;

/**********************************************************************
 Paddle class

 Describes the paddle/bat's behaviors and attributes. It provides
 methods to move the paddle, reset the paddle's position, change the
 width based on a powerUp, draws itself and if the user has collected
 the gun powerUp, then draw a gun.
 *********************************************************************/

public class Paddle {

    private static final Color COLOR = new Color(204, 0, 102);
    private static final int HEIGHT = 10;

    private int gunWidth = 7;                 // Width of the small gun on the paddle
    private int gunHeight = 10;               // Height of the small gun on the paddle
    private int width, height;                // Width of the paddle
    private int x, y;                         // Height of the paddle

    // Constructor
    public Paddle(int xIn, int yIn, int widthIn, int heightIn)
    {
        x = xIn;
        y = yIn;
        width = widthIn;
        height = heightIn;
    }

    // Moves/sets the location to wherever the mouse is
    public void setDx(int mouseX)
    {
        x = mouseX;
    }

    // Resets the paddle to starting position (middle ofthe screen) at the start of every round
    public void reset(int startingX, int startingY)
    {
        x = startingX;
        y = startingY;
    }

    // Accessor methods
    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return HEIGHT;
    }

    // Changes the paddle's width depending on the powerUP
    public void setWidth(int newPaddleWidth)
    {
        width = newPaddleWidth;
    }

    // Returns a new rectangle with the bounds of the paddle to check for
    // collision when catching powerUps
    public Rectangle getBounds()
    {
        return new Rectangle(x, y, width, HEIGHT);
    }

    // Draws the paddle
    public void draw(Graphics g)
    {
        g.setColor(COLOR);
        g.fillRect(x, y, width, HEIGHT);
    }

    // Draws the gun when the gun powerUp is activated
    public void drawGun(Graphics g)
    {
        g.drawRect(x + width / 2 - gunWidth / 2, y - gunHeight, gunWidth, gunHeight);
    }
}
