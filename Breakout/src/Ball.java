import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

/*********************************************************************************
 Ball class

 Describes the ball's behaviors and attributes. It provides methods to move
 the ball, check for bounces off of the paddle, the wall, and the brick,
 draws itself, add powerUps randomly to an arraylist, and change speed based
 on the color of the brick.
 ********************************************************************************/

public class Ball {

    // DATA:
    private static final int RADIUS = 10;                                // Radius of the ball, doesn't change
    private static final Color COLOR = Color.white;                         // Color of the ball, doesn't change
    private static final int HORIZONTAL_SPACING_BRICKS = 3;              // The horizontal spacing between each brick
    private static final int VERTICAL_SPACING_BRICKS = 2;                // The vertical spacing between each brick

    // The ball's dx and dy velocities change depending on the color of the brick it hits
    private static final int cyanDx = 2;
    private static final int cyanDy = 3;
    private static final int greenDx = 3;
    private static final int greenDy = 4;
    private static final int yellowDx = 4;
    private static final int yellowDy = 5;
    private static final int orangeDx = 5;
    private static final int orangeDy = 6;
    private static final int redDx = 6;
    private static final int redDy = 7;

    private static ArrayList<PowerUp> powerUps = new ArrayList<PowerUp>();  // Arraylist for all kinds of powerUps

    private int count = 0;                                       // Variable to keep count of how many powerUps there are
    private int chosenNumber = 5;                                // Specific integer value used for choosing whether or not to have powerUps
    private Clip paddleBounce;                                   // Clip for the sound effect for a paddle-ball bounce
    private Clip brickBounce;                                    // Clip for the sound effect for a brick-ball bounce
    private int x, y;		                                     // Center of the ball
    private int dx, dy;		                                     // Velocity - how much to change x and y by when it moves

    // METHODS:
    public Ball (int xIn, int yIn, int dxIn, int dyIn)
    {
        // Nothing to do but save the data in the object's data fields.
        x = xIn;
        y = yIn;
        dx = dxIn;
        dy = dyIn;
    }

    // Moves the ball
    public void move()
    {
        x = x + dx;
        y = y + dy;
    }

    // Checks for the ball bouncing off of the walls
    public void bounceOffWall(int xLow, int xHigh, int yLow)
    {
        // Left and Right side of the screen
        if ((x - RADIUS <= xLow && dx < 0) || (x + RADIUS >= xHigh && dx > 0))
        {
            dx = -dx;
        }

        // Top side of the screen
        if ((y - RADIUS <= yLow && dy < 0))
        {
            dy = -dy;
        }
    }

    // Checks if the ball is dead, or has reached the bottom of the screen
    public boolean timeToReset(int yHigh)
    {
        if(y + RADIUS >= yHigh && dy > 0)
        {
            return true;
        }

        else
        {
            return false;
        }
    }

    // Sets the x, y, dx, dy back to the starting position/speed when it is time to reset
    public void reset(int initialX, int initialY, int initialDx, int initialDy)
    {
        x = initialX;
        y = initialY;
        dx = initialDx;
        dy = initialDy;
    }

    // Deletes all the previous powerUps left on the screen and resets the count
    // when it is time to reset
    public void resetPowerUp()
    {
        count = 0;

        for(int i = 0; i < powerUps.size(); i++)
        {
            powerUps.remove(i);
        }
    }

    // Returns the count of powerUps
    public int getCountPowerUp()
    {
        return count;
    }

    // Returns the ArrayList of powerUps
    public ArrayList<PowerUp> getPowerUps()
    {
        return powerUps;
    }

    // Checks for a paddle-ball bounce
    public void bounceOffPaddle(Paddle paddle)
    {
        // Top side, but coming from the left
        if(y + RADIUS >= paddle.getY() && y + RADIUS <= paddle.getY() + paddle.getHeight()
                && x + RADIUS * Math.cos(Math.PI / 4) > paddle.getX() && x + RADIUS *
                Math.cos(Math.PI / 4) < paddle.getX() + paddle.getWidth() && dx > 0)
        {
            if(x < paddle.getX() + (paddle.getWidth() / 8))
            {
                dx = -dx;
            }

            dy = -dy;
            soundEffect(paddleBounce);
        }

        // Top side, but coming from the right
        else if(y + RADIUS >= paddle.getY() && y + RADIUS <= paddle.getY() + paddle.getHeight()
                && x - RADIUS * Math.cos(Math.PI / 4) > paddle.getX() && x - RADIUS *
                Math.cos(Math.PI / 4) < paddle.getX() + paddle.getWidth()  && dx < 0)
        {
            if(x > paddle.getX() + (paddle.getWidth() * 7 / 8))
            {
                dx = -dx;
            }

            dy = -dy;
            soundEffect(paddleBounce);
        }

        // Left side of the paddle
        else if(x + RADIUS * Math.cos(Math.PI / 4) > paddle.getX() && x + RADIUS *
                Math.cos(Math.PI / 4) < paddle.getX() + paddle.getWidth() && dx > 0
                && y > paddle.getY() && y < paddle.getY() + paddle.getHeight())
        {
            dx = -dx;
            soundEffect(paddleBounce);
        }

        // Right side of the paddle
        else if(x - RADIUS * Math.cos(Math.PI / 4) > paddle.getX() && x - RADIUS *
                Math.cos(Math.PI / 4) < paddle.getX() + paddle.getWidth() && dx < 0
                && y > paddle.getY() && y < paddle.getY() + paddle.getHeight())
        {
            dx = -dx;
            soundEffect(paddleBounce);
        }
    }

    // Checks for a brick-ball bounce
    public void bounceOffBrick(Brick brick)
    {
        Random generator = new Random();
        int randomNum = 5;

        // Bottom side of the brick
        if(y - RADIUS < brick.getY() + brick.getHeight() && y - RADIUS > brick.getY() &&
                x > brick.getX() - HORIZONTAL_SPACING_BRICKS / 2 && x < brick.getX() + brick.getWidth()
                + HORIZONTAL_SPACING_BRICKS / 2 && dy < 0)
        {
            dy = -dy;
            soundEffect(brickBounce);
            brick.isAlive(false);

            // Change the speed of the brick if the brick is a different color
            changeSpeed(brick);

            // Randomly chooses if a powerUp should be added and used when a brick is hit
            if(randomNum == chosenNumber)
            {
                powerUps.add(new PowerUp(brick.getX() + 7, brick.getY() + 7, brick.getColor()));
                count++;
            }
        }

        // Right side of the brick
        else if(x - RADIUS < brick.getX() + brick.getWidth() && x - RADIUS > brick.getX() &&
                y > brick.getY() - VERTICAL_SPACING_BRICKS && y < brick.getY() + brick.getHeight()
                + VERTICAL_SPACING_BRICKS && dx < 0)
        {
            dx = -dx;
            soundEffect(brickBounce);
            brick.isAlive(false);

            // Change the speed of the brick if the brick is a different color
            changeSpeed(brick);

            // Randomly chooses if a powerUp should be added and used when a brick is hit
            if(randomNum == chosenNumber)
            {
                powerUps.add(new PowerUp(brick.getX() + 6, brick.getY() + 7, brick.getColor()));
                count++;
            }
        }

        // Left side of the brick
        else if(x + RADIUS > brick.getX() && x + RADIUS < brick.getX() + brick.getWidth() &&
                y > brick.getY() - VERTICAL_SPACING_BRICKS && y < brick.getY() + brick.getHeight()
                + VERTICAL_SPACING_BRICKS && dx > 0)
        {
            dx = -dx;
            soundEffect(brickBounce);
            brick.isAlive(false);

            // Change the speed of the brick if the brick is a different color
            changeSpeed(brick);

            // Randomly chooses if a powerUp should be added and used when a brick is hit
            if(randomNum == chosenNumber)
            {
                powerUps.add(new PowerUp(brick.getX() + 6, brick.getY() + 7, brick.getColor()));
                count++;
            }
        }

        // Top side of the brick
        else if(y + RADIUS > brick.getY() && y + RADIUS < brick.getY() + brick.getHeight() &&
                x > brick.getX() - HORIZONTAL_SPACING_BRICKS / 2 && x < brick.getX() + brick.getWidth()
                + HORIZONTAL_SPACING_BRICKS / 2 && dy > 0)
        {
            dy = -dy;
            soundEffect(brickBounce);
            brick.isAlive(false);

            // Change the speed of the brick if the brick is a different color
            changeSpeed(brick);

            // Randomly chooses if a powerUp should be added and used when a brick is hit
            if(randomNum == chosenNumber)
            {
                powerUps.add(new PowerUp(brick.getX() + 6, brick.getY() + 7, brick.getColor()));
                count++;
            }
        }
    }

    // Plays different sound effects for bounces
    public void soundEffect(Clip clip)
    {
        if(clip == paddleBounce)
        {
            try
            {
                File file = new File("Paddle_bounce.wav");
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);

                paddleBounce = AudioSystem.getClip();
                paddleBounce.open(audioIn);
                paddleBounce.start();
            }

            catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        else if(clip == brickBounce)
        {
            try
            {
                File file = new File("brick_bounce.wav");
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);

                clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
            }

            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    // Changes the speed of the ball based on the color of the brick
    public void changeSpeed(Brick brick)
    {
        if(brick.getColor() == Color.CYAN)
        {
            if(dx > 0)
            {
                dx = cyanDx;
            }
            else
            {
                dx = -cyanDx;
            }
            if(dy > 0)
            {
                dy = cyanDy;
            }
            else
            {
                dy = -cyanDy;
            }
        }

        else if(brick.getColor() == Color.GREEN)
        {
            if(dx > 0)
            {
                dx = greenDx;
            }
            else
            {
                dx = -greenDx;
            }
            if(dy > 0)
            {
                dy = greenDy;
            }
            else
            {
                dy = -greenDy;
            }
        }

        else if(brick.getColor() == Color.YELLOW)
        {
            if(dx > 0)
            {
                dx = yellowDx;
            }
            else
            {
                dx = -yellowDx;
            }
            if(dy > 0)
            {
                dy = yellowDy;
            }
            else
            {
                dy = -yellowDy;
            }
        }

        else if(brick.getColor() == Color.ORANGE)
        {
            if(dx > 0)
            {
                dx = orangeDx;
            }
            else
            {
                dx = -orangeDx;
            }
            if(dy > 0)
            {
                dy = orangeDy;
            }
            else
            {
                dy = -orangeDy;
            }
        }

        else if(brick.getColor() == Color.RED)
        {
            if(dx > 0)
            {
                dx = redDx;
            }
            else
            {
                dx = -redDx;
            }
            if(dy > 0)
            {
                dy = redDy;
            }
            else
            {
                dy = -redDy;
            }
        }
    }

    public int getDx()
    {
        return dx;
    }

    public int getDy()
    {
        return dy;
    }

    public void setDx(int newDx)
    {
        dx = newDx;
    }

    public void setDy(int newDy)
    {
        dy = newDy;
    }

    // Draws the ball and the powerUps
    public void draw(Graphics g)
    {
        g.setColor(COLOR);
        g.drawOval(x - RADIUS, y - RADIUS, 2 * RADIUS, 2 * RADIUS);

        for(int i = 0; i < powerUps.size(); i++)
        {
            powerUps.get(i).draw(g);
        }
    }

}
