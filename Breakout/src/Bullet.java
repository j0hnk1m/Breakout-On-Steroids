import java.awt.*;

/************************************************************************
 Bullet class

 Describes a projectile or a bullet's behavior and attributes. It
 provides a method to move itself, check if it has hit a brick or the
 top of the screen, return a new Rectangle with its bounds to check
 for collisions, and draw itself.
 ***********************************************************************/

public class Bullet {

    private static final int WIDTH = 2;              // Width of the bullet
    private static final int HEIGHT = 5;             // Height of the bullet
    private static final Color COLOR = Color.white;  // Color of the bullet
    private static final int DY = 7;                 // Y velocity of the bullet

    private boolean isAlive;             // Boolean that stores a true or false if the bullet is "dead"
    private int x, y;                    // Left and top corner of the bullet

    // Constructor
    public Bullet(int xIn, int yIn)
    {
        x = xIn;
        y = yIn;
        isAlive = true;
    }

    // Moves the bullet
    public void move()
    {
        y -= DY;
    }

    // Returns true or false whether or not the bullet is still alive
    public boolean isAlive()
    {
        return isAlive;
    }

    // Changes the state of the bullet whether it is dead or alive
    public void isAlive(boolean hit)
    {
        isAlive = hit;
    }

    // Checks if the bullet has gone off the screen
    public boolean disappear(int yLow)
    {
        // If the bullet hits the top, returns true
        if (y < yLow)
        {
            return true;
        }

        return false;
    }

    // Checks if the bullet has hit the brick
    public boolean hitBrick(Brick brick)
    {
        if(getBounds().intersects(brick.getBounds()))
        {
            return true;
        }

        return false;
    }

    // Returns a new rectangle with the bounds of the bullet to check collision with bricks
    public Rectangle getBounds()
    {
        return new Rectangle(x, y, WIDTH, HEIGHT);
    }

    // Draws the bullet
    public void draw(Graphics g)
    {
        g.setColor(COLOR);
        g.drawRect(x, y, WIDTH, HEIGHT);
    }

}
