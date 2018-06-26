import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.awt.image.BufferStrategy;
import java.util.Random;

/**************************************************************************
 Name: John Kim
 Block: B
 Date: 5/4/16
 Program: Breakout
 Description: A game similar to Atari's Breakout and Arkanoid, but it
              implements power-ups once certain bricks are hit with the
              ball and is caught with the paddle. The user controls the
              paddle with the mouse/cursor, and to win, all of the bricks
              must be destroyed.
 **************************************************************************/

public class BreakoutGame extends JFrame implements
        ActionListener, KeyListener, MouseMotionListener
{
    private static final int MAX_WIDTH = 600;		             // Window size
    private static final int MAX_HEIGHT = 725;		             // Window size
    private static final int TOP_OF_WINDOW = 22;	             // Top of the visible window
    private static final int DELAY_IN_MILLISEC = 15;             // Time delay between screen updates

    // About initial positioning and velocities
    private static final int INITIAL_PADDLE_X = 250;             // Initial x coordinate of the paddle
    private static final int INITIAL_PADDLE_Y = 650;             // Initial y coordinate of the paddle
    private static final int INITIAL_PADDLE_WIDTH = 100;         // Initial width of the paddle
    private static final int INITIAL_PADDLE_HEIGHT = 10;         // Initial height of the paddle
    private static final int INITIAL_BALL_X = MAX_WIDTH / 2;     // Initial x coordinate of the ball
    private static final int INITIAL_BALL_Y = 450;               // Initial y coordinate of the ball
    private static final int INITIAL_BALL_DX = 2;                // Initial x velocity of the ball
    private static final int INITIAL_BALL_DY = 3;                // Initial y velocity of the ball

    private static final int NUM_ROWS = 5;                       // Number of rows in the layout of bricks
    private static final int NUM_COLS = 9;                       // Number of columns in the layout of bricks
    private static final int HORIZONTAL_SPACING_BRICKS = 3;      // Visual-horizontal separation/spacing between the bricks
    private static final int VERTICAL_SPACING_BRICKS = 2;        // Visual-vertical separation/spacing between the bricks
    private static final int BRICK_WIDTH = 63;                   // Width of each brick
    private static final int BRICK_HEIGHT = 25;                  // Length of each brick
    private static final int BRICK_XPOS = 5;                     // The x coordinate of the FIRST brick (top left)
    private static final int BRICK_YPOS = 50 + TOP_OF_WINDOW;    // The y coordinate of the FIRST brick (top left)

    private static Paddle paddle;                                   // The paddle
    private static ArrayList<Ball> balls = new ArrayList<Ball>();      // ArrayList of balls (more can be added with powerUps)
    private static ArrayList<Brick> bricks = new ArrayList<Brick>();    // ArrayList of bricks
    private static ArrayList<Bullet> bullets = new ArrayList<Bullet>();  // ArrayList of bullets from the left gun
    private static Image cursor;
    private int score;
    private Color brickColor;

    // About current game states
    private boolean isTitleScreen = true;
    private boolean isGameOverScreen = false;
    private boolean isYouWinScreen = false;
    private boolean isGameScreen = false;
    private boolean isControlScreen = false;

    // About all the power-ups
    private boolean isTwoBallPowerUp = false;
    private boolean isPaddleShortPowerUp = false;
    private boolean isPaddleLongPowerUp = false;
    private boolean isSlowBallPowerUp = false;
    private boolean isFastBallPowerUp = false;
    private boolean isGunPowerUp = false;
    private int livesLeft = 3;              // Keeps track of how many lives left the user has

    // About different musics and sound effects
    private Clip themeClip;                 // Clip for the theme music for the title screen
    private Clip beginningClip;             // Clip for the background music for the ready screen
    private Clip endClip;                   // Clip for the background music for the game over screen
    private Clip shootClip;                 // Clip for the gun sound effect when shooting a bullet

    private double lastPressProcessed = 0;  // Used for delay between firing bullets out of the paddle

    // Main method
    public static void main(String args[])
    {
        // Create the JFrame
        BreakoutGame bg = new BreakoutGame();
        bg.addKeyListener(bg);
        bg.addMouseMotionListener(bg);

        cursor = new ImageIcon("Cursor.png").getImage();
    }

    // Constructor
    public BreakoutGame()
    {
        setSize(MAX_WIDTH, MAX_HEIGHT);
        setVisible(true);
        setTitle("Breakout");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Play the theme music for the starting screen
        playThemeClip();

        // Create the paddle and ball
        paddle = new Paddle(INITIAL_PADDLE_X, INITIAL_PADDLE_Y, INITIAL_PADDLE_WIDTH, INITIAL_PADDLE_HEIGHT);
        balls.add(new Ball(INITIAL_BALL_X, INITIAL_BALL_Y, INITIAL_BALL_DX, INITIAL_BALL_DY));

        // Add bricks to an arrayList and space them out
        createBricks();

        // Double Buffer Strategy
        createBufferStrategy(2);

        // Start the timer
        Timer clock = new Timer(DELAY_IN_MILLISEC, this);
        clock.start();
    }

    // Where everything repaints itself and moves...
    public void actionPerformed(ActionEvent e)
    {
        // Does all these things in actionPerformed only when the user starts
        // the game and if the beginning music has stopped playing
        if(isGameScreen == true && beginningClip.isRunning() == false)
        {
            moveBallsAndPowerUps();

            // If the user still has lives left, then reset the ball when the ball touches the bottom boundary
            endRound();
            brickCollision();

            // If all the bricks have been destroyed, then switch to the you-win screen
            if(bricks.size() == 0)
            {
                isGameScreen = false;
                isYouWinScreen = true;
            }

            // Checks for bullet collisions and if it hits something, then remove it
            for(int i = 0; i < bullets.size(); i++)
            {
                if(bullets.get(i).isAlive() == false)
                {
                    bullets.remove(i);
                }
            }

            // Move the bullets but if they hit the top of the screen or a brick
            for(int i = 0; i < bullets.size(); i++)
            {
                bullets.get(i).move();

                for(int j = 0; j < bricks.size(); j++)
                {
                    if(bullets.get(i).hitBrick(bricks.get(j)))
                    {
                        bricks.get(j).isAlive(false);
                        bullets.get(i).isAlive(false);
                    }

                    else if(bullets.get(i).disappear(TOP_OF_WINDOW) == true)
                    {
                        bullets.get(i).isAlive(false);
                    }
                }
            }
        }

        repaint();
    }

    public void choosePowerUp()
    {
        Random generator = new Random();
        int randomNum = generator.nextInt(6) + 1;

        // Add another ball to the game
        if(randomNum == 1)
        {
            activateTwoBallPowerUp();
            balls.add(new Ball(INITIAL_BALL_X, INITIAL_BALL_Y, INITIAL_BALL_DX, INITIAL_BALL_DY));
            paddle.setWidth(INITIAL_PADDLE_WIDTH);
        }

        // Make the paddle shorter
        else if(randomNum == 2)
        {
            activateShortPaddlePowerUp();
            paddle.setWidth(INITIAL_PADDLE_WIDTH + 50);
        }

        // Make the paddle longer
        else if(randomNum == 3)
        {
            activateLongPaddlePowerUp();
            paddle.setWidth(INITIAL_PADDLE_WIDTH - 50);
        }

        // Allows the user to shoot bullets from the middle of the paddle
        else if(randomNum == 4)
        {
            activateGunPowerUp();
        }

        else if(randomNum == 5)
        {
            activateSlowBallPowerUp();

            for(int i = 0; i < balls.size(); i++)
            {
                balls.get(i).setDx(balls.get(i).getDx() / 2);
                balls.get(i).setDy(balls.get(i).getDy() / 2);
            }
        }

        else if(randomNum == 6)
        {
            activateFastBallPowerUp();

            for(int i = 0; i < balls.size(); i++)
            {
                balls.get(i).setDx(balls.get(i).getDx() * 2);
                balls.get(i).setDy(balls.get(i).getDy() * 2);
            }
        }
    }

    // Moves the balls and checks for bounces off of the walls and paddle and also deals with powerUps
    public void moveBallsAndPowerUps()
    {
        for(int i = 0; i < balls.size(); i++)
        {
            balls.get(i).bounceOffWall(0, MAX_WIDTH, TOP_OF_WINDOW);
            balls.get(i).bounceOffPaddle(paddle);
            balls.get(i).move();

            if(balls.get(i).getCountPowerUp() != 0)
            {
                for(int j = 0; j < balls.get(i).getPowerUps().size(); j++)
                {
                    // If the paddle catches the powerUp
                    if(balls.get(i).getPowerUps().get(j).getBounds().intersects(paddle.getBounds()))
                    {
                        // Removes the powerUp on the screen, and then activates it
                        balls.get(i).getPowerUps().remove(j);
                        choosePowerUp();
                    }

                    // If it did not catch it, then let the powerUp keep on moving
                    else
                    {
                        balls.get(i).getPowerUps().get(j).move();

                        // If the powerUp has reached the bottom, then remove it from the ArrayList
                        if(balls.get(i).getPowerUps().get(j).disappear(MAX_HEIGHT) == true)
                        {
                            balls.get(i).getPowerUps().remove(j);
                        }
                    }
                }
            }
        }
    }

    // Activates a powerUp where the user now deals with another ball
    public void activateTwoBallPowerUp()
    {
        isTwoBallPowerUp = true;
        isPaddleShortPowerUp = false;
        isPaddleLongPowerUp = false;
        isGunPowerUp = false;
    }

    // Activates a powerUp where the paddle is shortened
    public void activateShortPaddlePowerUp()
    {
        isSlowBallPowerUp = false;
        isFastBallPowerUp = false;
        isTwoBallPowerUp = false;
        isPaddleShortPowerUp = true;
        isPaddleLongPowerUp = false;
        isGunPowerUp = false;
    }

    // Activates a powerUp where the paddle is lengthened
    public void activateLongPaddlePowerUp()
    {
        isSlowBallPowerUp = false;
        isFastBallPowerUp = false;
        isTwoBallPowerUp = false;
        isPaddleShortPowerUp = false;
        isPaddleLongPowerUp = true;
        isGunPowerUp = false;
    }

    // Activates a powerUp where the paddle now has a gun
    public void activateGunPowerUp()
    {
        isSlowBallPowerUp = false;
        isFastBallPowerUp = false;
        isTwoBallPowerUp = false;
        isPaddleShortPowerUp = false;
        isPaddleLongPowerUp = false;
        isGunPowerUp = true;
    }

    // Activates a powerUp where the ball moves slower
    public void activateSlowBallPowerUp()
    {
        isSlowBallPowerUp = true;
        isFastBallPowerUp = false;
        isTwoBallPowerUp = false;
        isPaddleShortPowerUp = false;
        isPaddleLongPowerUp = false;
        isGunPowerUp = false;
    }

    // Activates a powerUp where the ball moves faster
    public void activateFastBallPowerUp()
    {
        isSlowBallPowerUp = false;
        isFastBallPowerUp = true;
        isTwoBallPowerUp = false;
        isPaddleShortPowerUp = false;
        isPaddleLongPowerUp = false;
        isGunPowerUp = false;
    }

    // Checks for a brick collision with the ball and increases the score if it hit
    public void brickCollision()
    {
        for(int i = 0; i < bricks.size(); i++)
        {
            if(bricks.get(i).isAlive() == true)
            {
                for(int j = 0; j < balls.size(); j++)
                {
                    balls.get(j).bounceOffBrick(bricks.get(i));
                }
            }

            else
            {
                if(bricks.get(i).getColor() == Color.cyan)
                {
                    score += 100;
                }

                else if(bricks.get(i).getColor() == Color.green)
                {
                    score += 200;
                }

                else if(bricks.get(i).getColor() == Color.yellow)
                {
                    score += 300;
                }

                else if(bricks.get(i).getColor() == Color.orange)
                {
                    score += 400;
                }

                else if(bricks.get(i).getColor() == Color.red)
                {
                    score += 500;
                }

                bricks.remove(i);
            }
        }
    }

    // Deals with the end of each round
    public void endRound()
    {
        // If there are still lives left, then reset the screen
        if(livesLeft != 0)
        {
            for(int i = 0; i < balls.size(); i++)
            {
                // If the ball touches the bottom, then reset and decrease the lives left
                if(balls.get(i).timeToReset(MAX_HEIGHT) == true)
                {
                    if(balls.size() == 1)
                    {
                        balls.get(i).resetPowerUp();
                        livesLeft--;
                        balls.get(i).reset(INITIAL_BALL_X, INITIAL_BALL_Y, INITIAL_BALL_DX, INITIAL_BALL_DY);
                        paddle.reset(INITIAL_PADDLE_X, INITIAL_PADDLE_Y);

                        // Reset all powerUps to default
                        isTwoBallPowerUp = false;
                        isPaddleShortPowerUp = false;
                        isPaddleLongPowerUp = false;
                        isGunPowerUp = false;
                    }

                    // Removes the one ball that hit the screen, lives still stay the same
                    else
                    {
                        balls.remove(i);
                    }
                }
            }
        }

        // If not, then proceed to the gameOver screen and play the dramatic dun-dun-dun music
        else
        {
            isGameScreen = false;
            isGameOverScreen = true;
            playEndClip();
        }
    }

    public void keyPressed(KeyEvent e)
    {
        int keyCode = e.getKeyCode();

        // The title screen state
        if(isTitleScreen == true)
        {
            // When it is the title screen
            if(keyCode == KeyEvent.VK_ENTER)
            {
                if (themeClip.isRunning())
                {
                    themeClip.stop();
                }

                playBeginningClip();

                isTitleScreen = false;
                isGameScreen = true;
            }

            if(keyCode == KeyEvent.VK_Q)
            {
                isTitleScreen = false;
                isControlScreen = true;
            }
        }

        // WHen you hit all the bricks and win the gamew
        else if(isYouWinScreen == true)
        {
            // Restart button
            if(keyCode == KeyEvent.VK_ENTER)
            {
                isYouWinScreen = false;
                isTitleScreen = true;
                livesLeft = 3;
                score = 0;

                // Remove all the previous balls and add one to start the next game
                balls.clear();
                balls.add(new Ball(INITIAL_BALL_X, INITIAL_BALL_Y, INITIAL_BALL_DX, INITIAL_BALL_DY));

                // Reset the paddle's position and the width
                paddle.reset(INITIAL_PADDLE_X, INITIAL_PADDLE_Y);
                paddle.setWidth(INITIAL_PADDLE_WIDTH);

                // Delete all the previous powerUps
                for(int i = 0; i < balls.size(); i++)
                {
                    balls.get(i).getPowerUps().clear();
                }

                isTwoBallPowerUp = false;
                isPaddleShortPowerUp = false;
                isPaddleLongPowerUp = false;
                isGunPowerUp = false;

                // Delete all the bullets
                bullets.clear();

                // Delete all previous bricks
                bricks.clear();

                // Create a new set of bricks
                createBricks();

                playThemeClip();
            }
        }

        // When you die and it's game over
        else if(isGameOverScreen == true)
        {
            // Restart button
            if(keyCode == KeyEvent.VK_ENTER)
            {
                if(endClip.isRunning())
                {
                    endClip.stop();
                }

                playBeginningClip();

                isGameOverScreen = false;
                isGameScreen = true;
                livesLeft = 3;
                score = 0;

                // Remove all the previous ball and add a new ball to the ArrayList
                balls.clear();
                balls.add(new Ball(INITIAL_BALL_X, INITIAL_BALL_Y, INITIAL_BALL_DX, INITIAL_BALL_DY));

                // Reset the paddle's position and the width
                paddle.reset(INITIAL_PADDLE_X, INITIAL_PADDLE_Y);
                paddle.setWidth(INITIAL_PADDLE_WIDTH);

                // Delete all the previous powerUps
                for(int i = 0; i < balls.size(); i++)
                {
                    balls.get(i).getPowerUps().clear();
                }

                isTwoBallPowerUp = false;
                isPaddleShortPowerUp = false;
                isPaddleLongPowerUp = false;
                isGunPowerUp = false;

                // Delete all the bullets
                bullets.clear();

                // Delete all previous bricks
                bricks.clear();

                // Create a new set of bricks
                createBricks();
            }
        }

        // When teh game is still going on
        else if(isGameScreen == true)
        {
            // If the user has a gun powerUp activated, then allow the user to shoot bullets
            if(isGunPowerUp == true)
            {
                // Source: StackOverflow
                // http://stackoverflow.com/questions/5199581/java-keylistener-keypressed-method-fires-too-fast
                if(System.currentTimeMillis() - lastPressProcessed > 250)
                {
                    // Shoots a new bullet out of the paddle
                    if(keyCode == KeyEvent.VK_S)
                    {
                        bullets.add(new Bullet(paddle.getX() + paddle.getWidth() / 2, paddle.getY()));
                        playGunClip();
                    }

                    lastPressProcessed = System.currentTimeMillis();
                }
            }
        }

        // When it is the control or "how to play" screen
        else if(isControlScreen == true)
        {
            // Go back to the title screen
            if(keyCode == KeyEvent.VK_Q)
            {
                isControlScreen = false;
                isTitleScreen = true;
            }
        }
    }

    // Can keep these methods empty, but they just need to be there not to get an error...
    public void keyTyped(KeyEvent e)
    {
    }

    public void keyReleased(KeyEvent e)
    {
    }

    // Move the paddle based on where the the mouse
    public void mouseMoved(MouseEvent e)
    {
        int x = e.getX();

        // Only when all music has stopped playing the user can move the paddle
        if(!themeClip.isRunning() && !beginningClip.isRunning())
        {
            paddle.setDx(x - paddle.getWidth() / 2);
        }
    }

    public void mouseDragged(MouseEvent e)
    {

    }

    // Change the font to any font and size
    public void setFont(Graphics g, int fontSize)
    {
        try
        {
            Font f1 = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(new File("retganon.ttf"))).deriveFont(Font.PLAIN, fontSize);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(f1);

            g.setFont(f1);
        }

        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    // Music for the title screen, and it loops continuously until the user starts the game
    public void playThemeClip()
    {
        try
        {
            File file = new File("Theme.wav");
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);

            themeClip = AudioSystem.getClip();
            themeClip.open(audioIn);
            themeClip.start();
            themeClip.loop(Clip.LOOP_CONTINUOUSLY);
        }

        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    // Music for the start of the first round
    public void playBeginningClip()
    {
        try
        {
            File file = new File("Beginning.wav");
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);

            beginningClip = AudioSystem.getClip();
            beginningClip.open(audioIn);
            beginningClip.start();
        }

        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    // Sound effect when the user shoots a bullet with the powerUp
    public void playGunClip()
    {
        try
        {
            File file = new File("Gun.wav");
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);

            shootClip = AudioSystem.getClip();
            shootClip.open(audioIn);
            shootClip.start();
        }

        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    // Music for the game-over screen
    public void playEndClip()
    {
        try
        {
            File file = new File("End.wav");
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);

            endClip = AudioSystem.getClip();
            endClip.open(audioIn);
            endClip.start();
        }

        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    // Creates a set of bricks, each row being a different color
    public void createBricks()
    {
        for(int row = 0; row < NUM_ROWS; row++)
        {
            for (int column = 0; column < NUM_COLS; column++)
            {
                if(row == 0)
                {
                    brickColor = Color.red;
                }

                else if(row == 1)
                {
                    brickColor = Color.orange;
                }

                else if(row == 2)
                {
                    brickColor = Color.yellow;
                }

                else if(row == 3)
                {
                    brickColor = Color.green;
                }

                else if(row == 4)
                {
                    brickColor = Color.cyan;
                }

                bricks.add(new Brick(column * (BRICK_WIDTH + HORIZONTAL_SPACING_BRICKS) + BRICK_XPOS,
                        row * (BRICK_HEIGHT + VERTICAL_SPACING_BRICKS) + BRICK_YPOS, brickColor));
            }
        }
    }

    // Implemented with Buffer Strategy
    // Source: Mr. Steinberg
    public void paint(Graphics g)
    {
        BufferStrategy bf = this.getBufferStrategy();
        if (bf == null)
            return;

        Graphics g2 = null;

        try
        {
            g2 = bf.getDrawGraphics();
            myPaint(g2);
        }
        finally
        {
            g2.dispose();
        }

        bf.show();
        Toolkit.getDefaultToolkit().sync();
    }

    // Draws every component and parts of the game together
    public void myPaint(Graphics g)
    {
        if(isTitleScreen == true)
        {
            g.setColor(Color.black);
            g.fillRect(0, 0, MAX_WIDTH, MAX_HEIGHT);

            g.setColor(Color.WHITE);
            setFont(g, 175);
            g.drawString("BREAKOUT", 40, 250);

            setFont(g, 40);
            g.drawString("PRESS ENTER TO START", 135, 450);
            g.drawString("PRESS Q FOR CONTROLS", 135, 500);
        }

        else if(isControlScreen == true)
        {
            g.setColor(Color.black);
            g.fillRect(0, 0, MAX_WIDTH, MAX_HEIGHT);

            g.setColor(Color.WHITE);
            setFont(g, 50);
            g.drawString("CONTROLS", 10, TOP_OF_WINDOW + 75);

            setFont(g, 25);
            g.drawString("- YOU ARE A PADDLE.", 10, 150);
            g.drawString("- BLOCK THE INCOMING BALL FROM ENTERING YOUR SIDE.", 10, 175);
            g.drawString("- ALONG THE WAY, YOU WILL BE PROVIDED WITH POWER-UPS.", 10, 200);
            g.drawString("- THE POWERUPS CAN BE: SHORTENING YOU, LENGTHENING YOU, ", 10, 225);
            g.drawString("  ADDING ANOTHER BALL FOR YOU TO DEAL WITH, HAVING A GUN", 10, 250);
            g.drawString("  SLOWING THE BALL, AND MAKING IT GO FASTER ...", 10, 275);
            g.drawString("- YOU STAY WITH THE POWER-UP UNTIL YOU COLLECT ANOTHER ONE...", 10, 300);

            g.drawString("SHOOT", 55, 378);
            g.drawRect(20, 355, 25, 25);
            g.drawString("S", 30, 378);
            g.drawImage(cursor, 150, 355, 25, 25, this);
            g.drawString("MOVE", 180, 378);

            setFont(g, 40);
            g.drawString("PRESS Q TO CLOSE", 170, 500);
        }

        else if(isGameOverScreen == true)
        {
            g.setColor(Color.black);
            g.fillRect(0, 0, MAX_WIDTH, MAX_HEIGHT);
            g.setColor(Color.WHITE);

            setFont(g, 200);
            g.drawString("GAME", 150, 300);
            g.drawString("OVER", 150, 450);

            setFont(g, 50);
            g.drawString("PRESS ENTER TO RESTART", 95, 550);
        }

        else if(isYouWinScreen == true)
        {
            g.setColor(Color.black);
            g.fillRect(0, 0, MAX_WIDTH, MAX_HEIGHT);
            g.setColor(Color.WHITE);

            setFont(g, 200);
            g.drawString("YOU", 180, 300);
            g.drawString("WIN", 200, 450);

            setFont(g, 50);
            g.drawString("PRESS ENTER TO PLAY AGAIN", 80, 550);
        }

        else if(isGameScreen == true)
        {
            g.setColor(Color.black);
            g.fillRect(0, 0, MAX_WIDTH, MAX_HEIGHT);

            paddle.draw(g);

            if(isGunPowerUp == true)
            {
                paddle.drawGun(g);
            }

            for(int i = 0; i < balls.size(); i++)
            {
                balls.get(i).draw(g);
            }

            for(int i = 0; i < bullets.size(); i++)
            {
                bullets.get(i).draw(g);
            }

            for(int i = 0; i < bricks.size(); i++)
            {
                bricks.get(i).draw(g);
            }

            g.setColor(Color.white);
            setFont(g, 25);
            g.drawString("LIVES: " + livesLeft, 10, MAX_HEIGHT - 25);
            g.drawString("SCORE: " + score, 10, 35 + TOP_OF_WINDOW);

            // If the music for the start of the first round is playing, then print "READY" on the screen
            if(beginningClip.isRunning())
            {
                setFont(g, 50);
                g.drawString("READY", 255, 400);
            }
        }

    }

}
