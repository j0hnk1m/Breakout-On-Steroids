import java.awt.*;

/************************************************************************
 Brick class

 Describes the brick's behaviors and attributes. It provides methods to
 change whether or not the brick is dead, draw itself, and return a
 new rectangle with its bounds to check for collisions.
 ***********************************************************************/

public class Brick {

    private static final int WIDTH = 63;                         // Width of the brick
    private static final int HEIGHT = 25;                        // Height of the brick
    private static final int HORIZONTAL_SPACING_BRICKS = 3;      // Visual-horizontal separation/spacing between the bricks
    private static final int VERTICAL_SPACING_BRICKS = 2;        // Visual-vertical separation/spacing between the bricks

    private int x, y;                                            // X and Y coordinate of the powerUP
    private Color color;                                         // Color of the brick
    private boolean isAlive;                                     // Stores whether or not the brick has been hit yet

    // Constructor
    public Brick(int xIn, int yIn, Color colorIn)
    {
        x = xIn;
        y = yIn;
        color = colorIn;
        isAlive = true;
    }

    // Returns true or false whether or not the brick is still alive
    public boolean isAlive()
    {
        return isAlive;
    }

    public void isAlive(boolean hitOrMiss)
    {
        isAlive = hitOrMiss;
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
        return WIDTH;
    }

    public int getHeight()
    {
        return HEIGHT;
    }

    public Color getColor()
    {
        return color;
    }

    // Returns a new rectangle with the bounds of the brick to check for
    // collisions with the bullet
    public Rectangle getBounds()
    {
        return new Rectangle(x - HORIZONTAL_SPACING_BRICKS / 2,
                y - VERTICAL_SPACING_BRICKS / 2, WIDTH + HORIZONTAL_SPACING_BRICKS,
                HEIGHT + VERTICAL_SPACING_BRICKS);
    }

    // Draws the brick
    public void draw(Graphics g)
    {
        g.setColor(color);
        g.fillRect(x, y, WIDTH, HEIGHT);
    }

}
