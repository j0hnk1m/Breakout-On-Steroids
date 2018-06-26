import java.awt.*;

/**********************************************************************************
 PowerUp class

 Describes the PowerUp class, where after the brick has been hit
 and disappears, a small "fruit" of a random PowerUp can be collected by Paddle. It
 provides a method to move itself after being hit, check if it has not been catched
 and should be removed, return a new Rectangle with its bounds to check for
 collisions, and draw itself.
 **********************************************************************************/

public class PowerUp {

    private static final int WIDTH = 40;                 // Width of the powerUp
    private static final int HEIGHT = 10;                // Height of the powerUp
    private static final int DY = 3;                     // Y Velocity of the powerUp

    private int x, y;                                    // X and Y coordinate of the powerUp
    private Color color;                                 // Color of the powerUp (depends on the color
                                                                        // of the brick it falls from)

    // Constructor
    public PowerUp(int xIn, int yIn, Color colorIn)
    {
        x = xIn;
        y = yIn;
        color = colorIn;
    }

    // Moves the powerUp
    public void move()
    {
        y += DY;
    }

    // Checks if the powerUp has not been caught and gone through the bottom of the screen
    public boolean disappear(int yHigh)
    {
        if(y > yHigh)
        {
            return true;
        }

        return false;
    }

    // Returns a rectangle with the same boundaries/dimensions as the powerUp
    public Rectangle getBounds()
    {
        return new Rectangle(x, y, WIDTH, HEIGHT);
    }

    // Draws the powerUp
    public void draw(Graphics g)
    {
        g.setColor(color);
        g.drawRect(x, y, WIDTH, HEIGHT);
    }

}
