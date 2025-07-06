/* Arkanoid.java
 * Ricky L.
 * This file contains all of the code that makes this game runnable. This includes the creation of a window (where the game will be played in) within the Arkanoid class (where it creates a new ArkanoidPanel), along with different classes that contain different methods and values,such as the ArkanoidPanel class, where it has core methods, like loading images, playing music, or constantly calling on the draw methods of everything that needs to be drawn, etc, or the ArkanoidUtil class, where this class primarily handles all of the main game methods like drawing the scores, lives, loading in new stages, spawning in powerups, or the block class, where that class contains methods that help draw and manage that singular block that was created, or the ball class, where that class contains methods like draw and move amongst the other methods in that class that help make the ball function properly, and the slider class, where it contains methods like draw, move, modifyTargetWidth, and so on that help make the slider function properly.
 * Where I got the scoring process from: https://strategywiki.org/wiki/Arkanoid/Gameplay
 * 
 * (Make sure you edit the filePath variable on line 64 to where your ArkanoidGameProject folder is at in order to ensure that the game properly loads in all of the sounds and images)
 */

// importing a bunch of classes, including all of the java.util classes, all of the java.awt classes, all of the java.awt.event classes, all of the java.swing classes, java.swing.Timer (to prevent timer conflicts from the util and swing classes), java.imageIO.ImageIO, all of the java.io classes, and all of the javax.sound.sampled classes
import java.util.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;
import javax.sound.sampled.*;

public class Arkanoid extends JFrame { // setting up the main class (arkanoid), where JFrame applies
    ArkanoidPanel aGame = new ArkanoidPanel(); // creates a new arkanoid panel

    public Arkanoid() { // constructor
        super("Arkanoid"); // sets the window title to "Arkanoid"
        setResizable(false); // makes the window not resizable
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // makes it so the code stops running if the user closes the window
        add(aGame); // adds the arkanoid panel
        pack(); // ensures the window is set to the preferred size
        setVisible(true); // makes the window visible
        setLocationRelativeTo(null); // makes it so the location of the window isnt fixed
        setLayout(null); // makes it so there's no specific layout of the window
        addWindowListener(new WindowAdapter() { // creates a new window listener that checks for when the window closes, of which then it calls on the saveHighScore() method of the "aGame" object in order to ensure the highscore is saved prior to the window closing
            public void windowClosing(WindowEvent e){
                aGame.saveHighScore();
            }
        });
    }

    public static void main(String[] arguments) { // main method
        new Arkanoid(); // runs a new arkanoid game
    }
}

class ArkanoidPanel extends JPanel implements KeyListener, ActionListener, MouseListener { // main "jpanel" / gamepanel
    private Timer timer; // new timer
    private Slider slider; // stores the slider that the game will use / control
    private int[] screen_size = new int[2]; // creates a new int array with a size of 2 to hold the x and y sizes of the screen

    private ArkanoidUtil gameInfo; // variable that will allow this class to access the methods within the "ArkanoidUtil" class

    private boolean gameStarted = false; // determines whether the game has started or not
    private boolean gameEnded = false; // determines whether or not if the game has ended or not

    private final Rectangle startRect = new Rectangle(289,301,222,84); // the rectangle that contains the "play" button
    private final Rectangle quitRect = new Rectangle(289,430,222,84); // the rectangle that contains the "quit" button

    private final BufferedImage startScreen = loadBuffImg("startScreen.png"); // the image for the start screen
    private final BufferedImage endScreenPass = loadBuffImg("endScreenPass.png"); // the image for the end screen (if the user beats the game)
    private final BufferedImage endScreenFail = loadBuffImg("endScreenFail.png"); // the image for the end screen (if the user doesnt beat the game)
    private final BufferedImage backgroundImg = loadBuffImg("background.png"); // the background image
    private Image backgroundImgResized; // the background image that's resized to fit properly in the window
    private Clip backgroundClip; // the "clip" thats used to play the background music

    private final String filePath = "C:/Users/Ricky/Downloads/ICS4U - Grade 12 Computer Science/ArkanoidGameProject/"; // string that represents the path to the folder that all these game files are under


    
    public ArkanoidPanel() { // constructor method
        
        gameInfo = new ArkanoidUtil(this); // sets the gameInfo to a new ArkanoidUtil object, with "this" being passed in the parameter for the ArkanoidPanel
        screen_size[0] = gameInfo.getScreenSize()[0]; // sets screen_size[0] to what gameInfo.getScreenSize() returns (at the 0'th index)
        screen_size[1] = gameInfo.getScreenSize()[1]; // sets screen_size[1] to what gameInfo.getScreenSize() returns (at the 1'st index)
        slider = new Slider(screen_size[0] / 2,gameInfo,this); // creates the new slider with the width of the screen divided by 2 (for its starting position), and gameInfo
        gameInfo.setSlider(slider); // sets the active slider to the newly created slider

        backgroundImgResized = gameInfo.resizeImg(backgroundImg, screen_size[0],screen_size[1]);
        
        setPreferredSize(new Dimension(screen_size[0], screen_size[1])); // sets the screen's preferred size to the dimensions that are in screen_size[0] and screen_size[1]
        setFocusable(true); // makes the window focusable
        requestFocusInWindow(); // asks for focus 
        addKeyListener(this); // adds key listeners
        addMouseListener(this); // adds mouse listeners
        playBackgroundMusic("background_music.wav",true); // plays the background music

        timer = new Timer(50/gameInfo.getSpeedMultiplier(), this); // creates a new timer with a tick speed divided by the current speed multiplier, and "this" being where responses are directed to
        timer.start(); // starts the timer (the whole purpose is to allow this to continously check for events and paint stuff onto the screen)
    }

    public int loadHighScore(){ // method that loads the saved high score from the "HighScore.txt" file
        try{
            BufferedReader read = new BufferedReader(new FileReader(filePath+"HighScore.txt")); // creates a new buffered reader to read the contents from the highscore text file
            String savedHighScore = read.readLine(); // reads the first line (which has the highscore)
            read.close(); // closes the reader
            if(savedHighScore != null && !savedHighScore.equals("")){ // if the highscore exists and isnt blank
                return Integer.parseInt(savedHighScore); // returns the savedHighScore as an integer
            }
        }catch(Exception e){ // otherwise if there's an issue, it prints out that it couldnt load the highscore and prints out the error
            System.out.println("Could not load in the saved high score.");
            e.printStackTrace();
        }
        return 0; // returns 0 if it couldnt load in the highscore
    }

    public void saveHighScore(){ // method that saves the high score to the "HighScore.txt" file
        try{
            int highScore = gameInfo.getHighScore(); // gets the current high score
            BufferedWriter write = new BufferedWriter(new FileWriter(filePath+"HighScore.txt")); // creates a new bufferedwriter to write stuff into the highscore text file
            write.write(Integer.toString(highScore)); // replaces the first line with the current highscore
            write.close(); // closes the writer
        }catch(Exception e){ // otherwise it prints out that it couldnt save the highscore and prints out the error
            System.out.println("Could not save the high score.");
            e.printStackTrace();
        }
    }

    public BufferedImage loadBuffImg(String img){ // method that loads in a image (this method was taken from Mr. Mckenzie)
        try { // tries to load in the image, and if it cant, then it prints out the error
    		return ImageIO.read(new File(filePath+img));
		} 
		catch (IOException e) {
			System.out.println(e);
		}
        return null; // returns null if it couldnt successfully load in an image
    }

    public void updateTimerDelay(int newDelay){ // method that changes how fast the timer triggers something (speeding up or slowing down the game)
        if(timer != null){ // if the timer exists
            timer.stop(); // stops the timer
            timer.setDelay((int)50/newDelay); // sets the new delay value to be the result of integer division of 1000/newDelay
            timer.start(); // starts the timer again
        }
    }

    private void playBackgroundMusic(String audioFileName, boolean repeat) { // method that plays a sound file for background music (method is private since no other piece of code needs to access this method)
        String defaultPath = filePath+"Sounds/"; // path to the sound files
        try{
            if(backgroundClip != null && backgroundClip.isRunning()) { // if there's a baackground clip, and it's currently playing something
             backgroundClip.stop(); // stops the current audio that's being played
            }
            File audioFile = new File(defaultPath + audioFileName); // gets the audio file
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            if(backgroundClip == null){ // if the current background clip doesnt exist
                backgroundClip = AudioSystem.getClip(); // gets a new clip to use
            }
            backgroundClip.open(audioStream); // opens the audio stream
            if(repeat){ // if the audio should be repeated
                backgroundClip.loop(Clip.LOOP_CONTINUOUSLY); // infinitely loops the audio
            }else{ // otherwise......
                backgroundClip.loop(-1); // play it just one time
            }
            backgroundClip.start();
        } catch (Exception e){ // if there's an error, then it prints the error
            e.printStackTrace(); // prints the error
        }
    }

    public void playSoundFX(String audioFileName) { // method that plays a sound effect
        String defaultPath = filePath+"Sounds/"; // path to the sound files
        try{
            File audioFile = new File(defaultPath + audioFileName); // gets the audio file
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            Clip clip = AudioSystem.getClip(); // gets a new clip to use
            clip.open(audioStream); // opens the audio stream
            clip.start(); // plays the audio
        }catch(Exception e) { // if there's an error, then it prints the error
            e.printStackTrace(); 
        }
    }

    public void drawStartingScreen(Graphics g){ // draws the image for the starting screen
        g.drawImage(startScreen,0,0,null);
    }

    public void drawEndingScreen(Graphics g){ // draws the ending screen
        if(gameInfo.getCurrentLives() > 0){ // if the player has more than 0 lives (didnt run out of lives)
            g.drawImage(endScreenPass,0,0,null); // draws the ending screen that says that the player beat the game
        }else{ // otherwise it draws the ending screen that says that says "you tried your best"
            g.drawImage(endScreenFail,0,0,null);
        }
        
    }

    public boolean getGameStarted(){ // returns the status of whether gameStarted was set to true or false
        return gameStarted;
    }

    public void setGameEnded(boolean val){ // changes the "gameEnded" value to 'val'
        gameEnded = val;
        if(val){ // if the 'val' is true, then it sets gameStarted to false (since the game ended)
            gameStarted = false;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) { // if an action has been performed
        repaint(); // repaint screen
        if(gameStarted){ // if the game has started, then move all the balls
            gameInfo.moveBalls();
        }
    }

    @Override
    public void keyPressed(KeyEvent ke) { // if a key has been pressed, then it calls the keyPressed method that belongs to the slider and checks if the space button has been pressed (of which then it releases all the balls attached to the slider) if the game has started
        if(gameStarted){ 
            slider.keyPressed(ke);

            if(ke.getKeyCode() == KeyEvent.VK_SPACE){
                gameInfo.releaseAttachedBalls();
            }
        }
        
    }

    @Override
    public void keyReleased(KeyEvent ke) { // if a key has been released, then it calls the keyReleased method that belongs to the slider if the game has started
        if(gameStarted){
            slider.keyReleased(ke);
        }
    }

    @Override
    public void keyTyped(KeyEvent ke) {} // if a key has been "typed"

    @Override
    public void mouseEntered(MouseEvent me) {} // if the mouse has "entered" the screen / window

    @Override
    public void mouseExited(MouseEvent me) {} // if the mouse has left the screen / window

    @Override
    public void mouseClicked(MouseEvent me) { // if the mouse is clicked
        if(!gameStarted && me.getButton() == me.BUTTON1){ // if the game hasnt started, and the left mouse button was pressed
            Point cPoint = me.getPoint(); // gets the current mouse position
            if(quitRect.contains(cPoint)){ // if the mouse position was in the quit box when it was clicked, then it quits the program
                System.exit(0);
            }else if(startRect.contains(cPoint)){ // otherwise if the mouse was in the start box when it was clicked, it sets gameStarted to true, gameEnded to false, and loads in the first stage
                gameInfo.resetStats();
                gameStarted = true;
                gameEnded = false;
                gameInfo.loadStage(0,false);
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent me) {} // if a mouse button was released

    @Override
    public void mousePressed(MouseEvent me) { // if the mouse was pressed
        if(gameStarted){ // if the game has started
            gameInfo.releaseAttachedBalls(); // it releases all the current balls that are attached to the slider
        }
    } 

    @Override
    public void paint(Graphics g) { // paints stuff onto the screen
        super.paint(g); // calls the paint method of the "super class"
        setBackground(new Color(0,0,0)); // sets the background to black
        if(backgroundImgResized != null){ // if the resized background image isnt null, then it draws it onto the window / screen
            g.drawImage(backgroundImgResized,0,0,null);
        }
        if(gameStarted){ // if the game has started
            slider.draw(g); // draws the slider
            gameInfo.drawBalls(g); // draws all the balls
            gameInfo.drawBlocks(g); // draws all the blocks
            gameInfo.drawScores(g); // draws the current score
            gameInfo.drawRemainingLives(g); // draws the remaining lives that the player has
            gameInfo.drawStage(g); // draws the current stage
            gameInfo.drawFallingPowerups(g, slider); // draws the current powerups
        }else{
            if(!gameStarted && !gameEnded){ // if the game hasnt started and the game hasnt ended (basically when the game gets loaded up)
                drawStartingScreen(g); // draws the starting screen
            }else if(gameEnded){ // if the game has ended
                drawEndingScreen(g); // draws the ending screen
            }
        }
    } 
}

class ArkanoidUtil{ // class that contains a bunch of useful methods and handles a bunch of the critical components of the game

    private int dx = 800, dy = 600; // stores how big the screen is (in this case, 800x600)
    private int[] screen_size = {dx, dy}; // screensize as an int[] array
    private Slider slider; // stores the slider object when arkanoid util get's intialized
    private int speed_multiplier = 3; // speed multiplier for the balls
    private int currentStage = 0; // keeps track of the current stage
    private int currentScore = 0; // keeps track of current score
    private int highScore = 0; // keeps track of the highscore
    private int prevScore = currentScore; // keeps track of the score prior to starting the new stage
    private int nextTargetStage = currentStage+1;
    private int nearbyRadius = 100; // defines what is "nearby" (in terms of a nearby radius)
    private ArkanoidPanel arkPanel; // used to access any methods that belong to the ArkanoidPanel class
    private int currLives = 3; // the number of lives the player has left
    private int originalSpeedMultiplier = speed_multiplier; // the original speed multiplier when the game first starts
    private final Color[] defaultBlockColors = { // array that contains all the different block colors, where the higher the index number is for that color, the more its worth (0 = worth the least, 8 = worth the most (excluding the hidden blocks))
        new Color(255,255,255),
        new Color(255,165,0),
        new Color(0,255,255),
        new Color(128,0,128),
        new Color(10,255,0),
        new Color(255,0,0),
        new Color(0,0,205),
        new Color(255,0,255),
        new Color(255,255,0),
    };

    Block[][] stages;// all the stages in this arkanoid game, where each stage has a bunch of blocks positioned in different places

    ArrayList<Ball>balls = new ArrayList<>(); // stores all the "ball" objects
    ArrayList<Block>blocks = new ArrayList<>(); // stores all the "block" objects
    ArrayList<Rectangle>fallingPowerupBoxes = new ArrayList<>(); // stores all the falling powerup rectangle objects ("boxes")
    ArrayList<String>fallingPowerupNames = new ArrayList<>(); // stores all the falling powerup names

    public ArkanoidUtil(ArkanoidPanel arkp){// constructor method
        arkPanel = arkp; // sets the arkPanel to arkp
        highScore = arkPanel.loadHighScore();
        stages = new Block[][]{ // sets the 2d block array (stages) to the current 2d array below, which contains where the blocks are positioned and what tyep of blocks they are, etc
            {new Block(25,50,100,100,this,"fastforward",null,0),
            new Block(75,150,100,100,this,"increasesize",null,0),
            new Block(125,250,100,100,this,"undestroyable",null,0),
            new Block(dx/2-100,dy/3,100,100,this,"multiball",null,0),
            new Block(dx-225,250,100,100,this,"hidden",null,0),
            new Block(dx-125,150,100,100,this,"undestroyable",null,0),
           },

           {new Block(10,50,100,100,this,"",null,1),
            new Block(124,200,100,100,this,"",null,1),
            new Block(238,50,100,100,this,"",null,1),
            new Block(352,200,100,100,this,"multiball",null,1),
            new Block(466,50,100,100,this,"",null,1),
            new Block(580,200,100,100,this,"",null,1),
            new Block(694,50,100,100,this,"",null,1),
           },
   
           {new Block(10,50,100,100,this,"",null,2),
            new Block(10,200,100,100,this,"hidden",null,2),
            new Block(10,350,100,100,this,"undestroyable",null,2),
            new Block(dx/2-50,dy/2-100,100,100,this,"multiball",null,2),
            new Block(dx-110,350,100,100,this,"undestroyable",null,2),
            new Block(dx-110,200,100,100,this,"hidden",null,2),
            new Block(dx-110,50,100,100,this,"",null,2),
           },

           {new Block(10,120,50,50,this,"hidden",null,4),
            new Block(10,screen_size[1]-170,50,50,this,"hidden",null,4),
            new Block(100,250,50,50,this,"hidden",null,4),
            new Block(190,120,50,50,this,"hidden",null,4),
            new Block(190,screen_size[1]-170,50,50,this,"hidden",null,4),
            new Block(375,250,50,50,this,"multiball",null,4),
            new Block(screen_size[0]-60,120,50,50,this,"hidden",null,4),
            new Block(screen_size[0]-60,screen_size[1]-170,50,50,this,"hidden",null,4),
            new Block(screen_size[0]-150,250,50,50,this,"hidden",null,4),
            new Block(screen_size[0]-240,120,50,50,this,"hidden",null,4),
            new Block(screen_size[0]-240,screen_size[1]-170,50,50,this,"hidden",null,4),
           },
        };
    } 

    public Image resizeImg(BufferedImage img,int width,int height){ // to return a resized image
        if(img != null){ // if the image isnt null
            Image resizedImage = img.getScaledInstance(width,height,Image.SCALE_SMOOTH); // the resized image, where it gets the scaled instance of the original image, scales to the target width and height, and sets the type of scaling to smooth
            return resizedImage; // returns the resized image
        }
        return null; // returns null if the img that was provided is null
    }

    public ArkanoidPanel getArkanoidPanel(){ // method that returns the current ArkanoidPanel object thats being used
        return arkPanel;
    }

    public int getHighScore(){
        return highScore;
    }

    public Color[] getBlockColors(){ // returns the current block colors that are in an array
        return defaultBlockColors;
    }

    public boolean getGameStarted(){ // method that gets the status of whether the game has started or not
        return arkPanel.getGameStarted(); // returns the value when running "arkPanel.getGameStarted()" (since the gameStarted variable comes from there)
    }

    public void spawnPowerUp(Block block){ // spawns in a powerup for the block
        if(!block.getPowerUp().equals("") && !block.getPowerUp().equals("hidden") && !block.getPowerUp().equals("undestroyable")) //if the block has a powerup, and its not hidden or undestroyable then
        fallingPowerupNames.add(block.getPowerUp()); // adds the powerup name into the fallingPowerupNames arraylist
        Rectangle box = new Rectangle(new Point(block.getX(),block.getY())); // creates a new "box" (rect object") that contains the starting position and size of where the powerup spawned from
        box.setSize(block.getWidth()/2,block.getHeight()/2); // sets the size of the powerup to 1/2 the size of the block
        fallingPowerupBoxes.add(box); // adds the rect object into the fallingPowerupBoxes arraylist
    }
    
    public int[] getScreenSize(){ // returns the screen_size array, which contains the x and y sizes
        return screen_size;
    }

    public int getCurrentStage(){ // returns the current stage that the game is on
        return currentStage;
    }

    public ArrayList<Block> getBlocks(){ // returns all the current blocks that are in the "blocks" arraylist
        return blocks;
    }

    public ArrayList<Ball> getBalls(){ // returns all the current balls that are in the "balls" arraylist
        return balls;
    
    }

    public void resetStats(){ // method that helps to reset everything back to "square 1"
        currentStage = 0; // sets the currentStage to 0
        nextTargetStage = currentStage+1; // sets the nextTargetStage to the currentStage + 1
        currentScore = 0; // sets the currentScore to 0
        prevScore = 0; // sets the prevScore to 0
        currLives = 3; // sets currLives to 3 (since by default, you only start with 3 lives)
        setSpeedMultiplier(originalSpeedMultiplier); // resets the speed multiplier back to its original
    }

    public int getCurrentLives(){ // returns the amount of lives the user currently has
        return currLives;
    }

    public boolean blocksOnlyContainsUndestroyableBlocks(){ // checks if the remaining blocks left are undestroyable
        int currSize = blocks.size(); // gets the current size of the blocks arraylist
        while(currSize > 0){ // while it's greater than 0
            if(!blocks.get(currSize-1).getPowerUp().equals("undestroyable")){ // if the current block isn't destroyable, then return false since that means the blocks arraylist still contains destroyable blocks
                return false;
            }
            currSize -=1; // subtract 1 from currSize
        }
        return true; // returns true if all blocks within the blocks arraylist are not destroyable
    }

    public void setSlider(Slider slide){ // sets the slider to the "slide" object that gets passed in the arguments
        slider = slide;
    }

    public Slider getSlider(){ // returns the current slider object that has been set (or NULL if nothing was set)
        return slider;
    }

    public void setSpeedMultiplier(int s){ // sets the current speed multiplier to whatever is passed in the argument for "s"
        speed_multiplier = s;
        arkPanel.updateTimerDelay(speed_multiplier);
    }

    public int getSpeedMultiplier(){ // returns the current speed multiplier
        return speed_multiplier;
    }

    public void increaseScore(Block block){ // increases the current score based on what block was hit and what color it is
        if(block.getPowerUp() == "hidden"){
            currentScore += 50*(currentStage+1); // increases the score by 50 times whatever the current stage is +1 (since the currentStage variable uses 0 based indexing)
        }else{
            Color currColor = block.getColor(); // gets the current block color
            int i=0; // sets i to 0, used in the for loop to keep track of where the color is at within the defaultBlockColors array
            for(i=0;i<defaultBlockColors.length;i++){ // loops through all of the block colors
                if(defaultBlockColors[i] == currColor){ // if at the i'th position in the defaultBlockColors array equals to the currColor (which is the block color), then it breaks the for loop
                    break;
                }
            }
            currentScore += 50 + i*10; // increases the score by 50 + i*10
        }

        if(currentScore > highScore){ // if the current score is higher than the recorded high score, then set highScore to currentScore
            highScore = currentScore;
        }
    }

    public void setScore(int s){ // sets the current score to "s"
        currentScore = s; 
    }

    public void setNextStage(){ // calculates what the next stage should be
        if(balls.size() == 0 && !blocksOnlyContainsUndestroyableBlocks()){ // if there's no balls left, and there's still destroyable blocks, then it resets the stage by loading the current stage
            loadStage(currentStage,true);
        }else if(balls.size() > 0 && blocksOnlyContainsUndestroyableBlocks()){ // if there's still balls left and there's only undestroyable blocks left, then...
            loadStage(nextTargetStage,false); // loads the nextTargetStage
            nextTargetStage+=1; // increases the nextTargetStage by 1
        }
    }

    public void addBlock(Block block){ // adds the "block" that get's passed as an argument into the "blocks" arraylist (and making sure that block isnt already in the arraylist)
        if(!blocks.contains(block)){
            blocks.add(block);
        }
    }

    public ArrayList<Block> getNearbyBlocks(Block block){ // gets the nearby undestroyable blocks
        Rectangle rect = new Rectangle(block.getX()-nearbyRadius/2,block.getY()-nearbyRadius/2,block.getWidth()+nearbyRadius,block.getHeight()+nearbyRadius); // creates a "rectangle" that is composed of the nearby radius
        ArrayList<Block> nearbyBlocks = new ArrayList<>(); // arraylist containing all of the "nearby blocks"
        ArrayList<Block> copyOfBlocks = new ArrayList<>(blocks); // copy of the blocks arraylist
        for(Block nBlock: copyOfBlocks){ // loops through all of those blocks
            Rectangle nRect = new Rectangle(nBlock.getX(),nBlock.getY(),nBlock.getWidth(),nBlock.getHeight()); // creates a new rect based on the current block's position and size
            if(!nBlock.equals(block)&& !nBlock.getPowerUp().equals("undestroyable")){ // if the block is a destroyable block
                if(rect.intersects(nRect)){ // if the block is within the "nearby radius"
                    nearbyBlocks.add(nBlock); // adds it to the nearbyBlocks arraylist
                }
            }
        }
        return nearbyBlocks; // returns the nearbyBlocks arraylist
    }

    public void fadeBlock(Block block){ // fades out the block
        block.fadeOut();
    }

    public void removeBlock(Block block,boolean fadeoutCompleted){ // remove block method
        if(fadeoutCompleted && blocks.contains(block)){ // if the block faded out and it current exists (exists as in is present within the blocks arraylist)
            blocks.remove(block); // removed from the blocks arraylist
            if(blocksOnlyContainsUndestroyableBlocks() && !block.getPowerUp().equals("undestroyable")){ // if there's only undestroyable blocks, and the block destroyed isnt an undestroyable block, and the stage is currently not being loaded in, then...
                setNextStage(); //runs the setNextStage() method
            }
        }else if(!fadeoutCompleted && blocks.contains(block)){ // else if the block didnt fade out, and it exists within the blocks arraylist
            block.setDestroyed(true); // block's destroyed value is set to true
            fadeBlock(block); // block is faded out
            if(!block.getPowerUp().equals("undestroyable") && balls.size() > 0){ // if the block isnt undestroyable, and there's at least 1 active ball, then it increases the score by 1
                increaseScore(block); // increases the score
            }
        }
    }

    public void addBall(Ball ball){ // adds the "ball" into the balls arraylist if the balls arraylist doesnt already contain that "ball" object that was passed in the parameters
        if(!balls.contains(ball)){
            balls.add(ball);
        }
    }

    public void removeBall(Ball ball){ // removes the "ball" from the balls arraylist if the balls arraylist contains that "ball" object that was passed in the parameters
        if(balls.contains(ball)){
            ball.setDestroyed(true);
            balls.remove(ball);
        }
    }

    public void moveBalls(){ // moves all the balls in the "balls" arraylist (if it's not empty). 
        if(balls.size() > 0){
            for(int i=balls.size()-1;i>=0;i--){ // uses a for loop to loop through eveyr single ball in the balls arraylist before moving it || this uses a for loop instead of a "for each" loop due to the fact that balls could get removed while a 'for each' loop is looping through them, which could cause an error
                balls.get(i).move(slider);
            }
        }
    }

    public void releaseAttachedBalls(){ // releases all the balls that are attached to the slider
        ArrayList<Ball> currAttachedBalls = slider.getAttachedBalls(); // gets all the attached balls 
        while(currAttachedBalls.size() > 0){ // while loop that goes through each ball that is attached to the slider
            Ball currBall = currAttachedBalls.get(0); // gets the ball object
            currBall.recalculateMoveDirection(); // makes it recalculate it's "direction" prior to being released
            slider.detachBall(currBall); // releases the ball that was attached
        }
    }

    public void triggerMultiBall(int px,int py){ // multiball powerup, where multiple balls spawn at the current ball's location
        int multiballCount = 3; // how many balls spawn in
        int delay = 100; // how long it takes for each ball to spawn in

        for (int i=0; i<multiballCount;i++) { // for loop, where each time it spawns in a new ball
            Timer ballSpawnTimer = new Timer(i * delay, new ActionListener() { // i*delay = how long before the ball spawns in
                @Override
                public void actionPerformed(ActionEvent e) { // action performed because the timer object needs a method / function to execute after timer is up
                    Ball newBall = new Ball(px, py, getScreenSize(), ArkanoidUtil.this,arkPanel); // creates a new ball at the "px" and "py" co-ordinates
                    addBall(newBall); // adds this new ball into the balls arraylist
                    ((Timer) e.getSource()).stop(); // stops the timer
                }
            });
            ballSpawnTimer.setRepeats(false); // stops the timer from repeating itself (only executes once),"(timer)" is there for typecasting
            ballSpawnTimer.start(); // runs the timer
        }
    }

    public void setSliderTrail(Graphics g,int dir) { // creates a slider trail
        int trailLength = 10; // length of the trail
        int maxTransparency = 150; // max transparency of the first segment
        int decay = maxTransparency / trailLength; // how long the decay will be
    
        for (int i=0;i<trailLength;i++) { // creates each segment with a lower transparency value the higher "i" gets
            int transparency = maxTransparency - (decay * i); // what the transparency will be for the current segment
            if (transparency < 0) { // ensures transparency doesnt go into the negatives
                transparency = 0;
            }
    
            int trailX = slider.getX() - dir*(i * 5); // position of the transparent segment
            g.setColor(new Color(255, 255, 255, transparency)); // sets the color (white) with a modified alpha (transparency) value
            g.fillRoundRect(trailX, slider.getY(), slider.getWidth(), slider.getHeight(),10,10); // draws it onto the screen
        }
    }
    
    


    public void setBallTrail(Ball ball, Graphics g) { // creates a trail for the ball
        if (ball != null) { // if the ball provided in the parameters / arguments isnt nothing, then
            int trailLength = 3; // sets the trail length
            int currTransparency = 255; // stores the current transparency value, where 255 = opaque, and 0 = transparent
            int decay = currTransparency / trailLength; // what the "decay" is (or how much the transparency of the ball increases each time)
    
            for (int i=0;i<trailLength;i++) { // for loop that draws the trail, where the end segment (i = trailLength-1) is almost transparent, while the first segment is slightly transparent
                int x = ball.prevX - (i*ball.getXSpeed() / 2); // gets the x position of the segment
                int y = ball.prevY - (i*ball.getYSpeed() / 2); // gets the y position of the segment
    
                currTransparency -= decay; // subtracts the current transparency value by the decay value
    
                if (currTransparency < 0) { // if the currTransparency value is smaller than 0, then it sets the current transparency to 0
                    currTransparency = 0;
                }
                    
                g.setColor(new Color(255, 255, 255, currTransparency)); // sets the color to white, with the "currTransparency" amount
                g.fillOval(x, y, ball.getDiameter(), ball.getDiameter()); // draws it onto the screen
            }
        }
    }
    

    public void loadStage(int s,boolean canDeleteLives){ // loads stage "s"
        if(balls.size() > 0){ // if there's still balls that exist
            int currSize = balls.size(); // gets the current size of the balls arraylist
            while(currSize > 0){ // while currSize is greater than 0
                balls.get(currSize-1).fadeOut(); // fades out that ball
                currSize -= 1; // subtracts 1 from currSize
            }
        }
        if(blocks.size() > 0){ // if there's still remaining blocks in the blocks arraylist
            while(blocks.size() > 0){ // while there's still blocks in the blocks arraylist
                removeBlock(blocks.get(0),false); // removes the block at index 0
            }
        }
        fallingPowerupNames.clear(); // clears the current stage of any falling powerups (but for all of the names of the powerups)
        fallingPowerupBoxes.clear(); // clears the current stage of any falling powerups (but for all the rect objects)
       
        
        if(s<stages.length){ // if stage "s" is smaller than the length of the stages 2d block array (because otherwise, there wont be a stage to load in)
            setSpeedMultiplier(originalSpeedMultiplier); // sets the speedMultiplier to the originalSpeedMultiplier
            slider.resetSliderPos(); // resets the slider's position to the middle of the screen

            ArrayList<Block> loadBlocks = new ArrayList<>(); // arraylist that stores the blocks to be loaded
            for(int i=0;i<stages[s].length;i++){
                Block newBlock = new Block(stages[s][i].getX(),stages[s][i].getY(),stages[s][i].getWidth(),stages[s][i].getHeight(),this,stages[s][i].getPowerUp(),stages[s][i].getColor(),stages[s][i].getDesignatedStageNum()); // creates a block with the same characteristics as the previous block that existed at it's index within the stages 2d block array
                loadBlocks.add(newBlock); // adds the this "new block" to the loadBlocks arraylist
            }
            
            for(int i=0;i<loadBlocks.size();i++){ // for loop that goes through all the blocks that need to be loaded in stage "s"
                blocks.add(loadBlocks.get(i)); // adds that block to the blocks arraylist
            }

            if(s == currentStage){ // if the target stage equals to the current stage, then it sets the currentscore to the previous score prior to starting the current stage
                setScore(prevScore);
                if(canDeleteLives){ // if the game has already began
                    currLives -= 1; // subtracts 1 from the total lives
                    if(currLives <= 0){ // if currLives is smaller or equal to 0, then it sets the gameEnded status to true
                        arkPanel.setGameEnded(true);
                    }
                }
            }else{ // otherwise it sets the previous score to the current score
                prevScore = currentScore;
            }
            currentStage = s; // sets currentStage to "s"
        }else{
            arkPanel.setGameEnded(true);
        }
    }

    public void drawFallingPowerups(Graphics g,Slider slider){ // method that draws the falling powerups
        if(fallingPowerupNames.size() > 0){ // if there's at least 1 falling powerup
            for(int i=0;i<fallingPowerupNames.size();i++){ // loops through all of the falling powerups
                Rectangle currBox = fallingPowerupBoxes.get(i); // gets the rect object for the current falling powerup
                Rectangle newBox = new Rectangle((int) currBox.getX(),(int)currBox.getY()+3,(int)currBox.getWidth(),(int)currBox.getHeight()); // calculates the new rect for the falling powerup
                Rectangle sliderBox = new Rectangle(slider.getX(),slider.getY(),slider.getWidth(),slider.getHeight()); // creates the rect object for the slider box
                boolean removed = false; // determines whether this powerup was removed or not

                if(newBox.intersects(sliderBox) || currBox.intersects(sliderBox)){ // if the next position or the current position of the powerup collides with the slider, then
                    if(fallingPowerupNames.get(i).equals("multiball")){ // if the powerup is multiball
                        if(newBox.intersects(sliderBox)){ // if it's the next position
                            triggerMultiBall((int)newBox.getCenterX(),(int)newBox.getCenterY()); // spawns the multiball at that next position
                        }else{ // otherwise spawns the multiball at the current position
                            triggerMultiBall((int)currBox.getCenterX(),(int)currBox.getCenterY());
                        }
                        fallingPowerupBoxes.remove(i); // removes the powerup rect from the fallingPowerupBoxes arraylist
                        fallingPowerupNames.remove(i); // removes the powerup name from the fallingPowerupNames arraylist
                        removed = true; // sets removed to true
                    }else if(fallingPowerupNames.get(i).equals("increasesize")){ // if the powerup is "increasesize"
                        slider.modifyTargetWidth(slider.getWidth()*2);
                        fallingPowerupBoxes.remove(i); // removes the powerup rect from the fallingPowerupBoxes arraylist
                        fallingPowerupNames.remove(i); // removes the powerup name from the fallingPowerupNames arraylist
                        removed = true; // sets removed to true
                    }else if(fallingPowerupNames.get(i).equals("decreasesize")){ // if the powerup is "decreasesize"
                        slider.modifyTargetWidth(slider.getWidth()/2);
                        fallingPowerupBoxes.remove(i); // removes the powerup rect from the fallingPowerupBoxes arraylist
                        fallingPowerupNames.remove(i); // removes the powerup name from the fallingPowerupNames arraylist
                        removed = true; // sets removed to true
                    }
                }else{
                    if(fallingPowerupNames.get(i).equals("multiball")){
                        g.setColor(new Color(34,139,34)); // sets color to a mediocre green
                        g.drawOval((int) currBox.getX(),(int)currBox.getY(),(int)currBox.getWidth(),(int)currBox.getHeight()); // draws it onto the screen
                    }else if(fallingPowerupNames.get(i).equals("increasesize")){
                        g.setColor(new Color(100,149,237)); // sets the color to a shade of blue
                        g.drawRect((int) currBox.getX(),(int)currBox.getY(),(int)currBox.getWidth(),(int)currBox.getHeight()); // draws it onto the screen
                    }else if(fallingPowerupNames.get(i).equals("decreasesize")){
                        g.setColor(new Color(255,160,122)); // sets the color to a shade of peach
                        g.drawRect((int) currBox.getX(),(int)currBox.getY(),(int)currBox.getWidth(),(int)currBox.getHeight()); // draws it onto the screen
                    }
                }
                
                if(newBox.getY() > screen_size[1]){ // if the powerup falls below the screen
                    if(!removed){ // if its not already removed
                        removed = true; // sets removed to true
                        fallingPowerupBoxes.remove(i); // removes the powerup rect from the fallingPowerupBoxes arraylist
                        fallingPowerupNames.remove(i); // removes the powerup name from the fallingPowerupNames arraylist
                    }
                }

                if(!removed){ // if it hasnt been removed
                    fallingPowerupBoxes.set(i,newBox); // sets the current rect object to the "next" rect (which is the newBox)
                }
            }
        }
    }


    public void drawBalls(Graphics g){ // draws all the balls that currently exist
        if(balls.size() > 0){ // if there's balls that exist within the balls arraylist
            for(int i=0;i<balls.size();i++){ // for loop that goes through each ball
                balls.get(i).draw(g); // gets the ball at the "i'th" index and draws it
            }
        }
    }

    public void drawBlocks(Graphics g){ // draws all the blocks that currently exist
        if(blocks.size() > 0){ // if there's blocks that exist within the blocks arraylist
            for(int i=0;i<blocks.size();i++){ // for loop that goes through each block
                blocks.get(i).draw(g); // gets the block at the "i'th" index and draws it
            }
        }
    }

    public void drawScores(Graphics g){ // draws the score
        g.setColor(Color.WHITE); // sets the color to white
        g.setFont(new Font("Arial", Font.BOLD, 20)); // sets the font to size 20, font class "Arial", and bolded
        g.drawString("Score: "+Integer.toString(currentScore), 20, 30); // draws the score
        g.drawString("High Score: "+Integer.toString(highScore),20,60); // draws the highscore
    }

    public void drawRemainingLives(Graphics g){ // draws the amount of lives the player current has
        g.setColor(Color.WHITE); // sets the color to white
        g.setFont(new Font("Arial", Font.BOLD, 20)); // sets the font to size 20, font class "Arial", and bolded
        g.drawString("Lives: "+Integer.toString(currLives), screen_size[0]-100, 60); // draws the score
    }

    public void drawStage(Graphics g){ // draws the current stage number
        g.setColor(Color.WHITE); // sets the color to white
        g.setFont(new Font("Arial", Font.BOLD, 20)); // sets the font to size 20, font class "Arial", and bolded
        g.drawString("Stage: "+Integer.toString(currentStage+1), screen_size[0]-100, 30); // draws the current stage number
    }
}

class Block{ // block class that contains all the methods and variables that are neccessary for the "block" to function
    private int px,py; // stores location of top left corner of block
    private int width,height; // stores dimensions of block
    private ArkanoidUtil gInfo; // the "ArkanoidUtil" class that handles majority of the calculations and game handling
    private boolean isDestroyed = false; // determines if this block has been destroyed or not
    private int currTransparency = 255; // current transparency value, where 255 = opaque, and 0 = transparent
    private boolean isFading = false; // determines if the block is fading or not
    private String specialAbility = ""; // what the block's special ability (powerup) is
    private Color blockColor; // what color the block is
    private int designatedStageNum; // what stage number this block belongs to
    private int hitTimes = 0; // keeps track of how many times the block has been hit
    private Image blockImage; // stores the image to be drawn over the block if there's an existing powerup

    public Block(int x,int y,int w,int h,ArkanoidUtil gameInfo,String powerUp,Color clr,int stageNum){ // constructor that contains all the neccessary information, including top left corner position, size, the gameInfo handler that is the ArkanoidUtil class, powerup, color, and stage number, and sets all the variables to whatever corresponds to the values given in the constructor
        px = x;
        py = y;
        width = w;
        height = h;
        gInfo = gameInfo;
        
        specialAbility = powerUp;
        designatedStageNum = stageNum;

        if(clr == null){ //if the color that passed through the parameters is null, then
            Random rand = new Random(); // new random object
            Color[] allColors = gInfo.getBlockColors(); // gets all the different block colors
            blockColor = allColors[rand.nextInt(allColors.length)]; // sets a random block color
        }else{ // otherwise it sets the blockColor to the color passed in the parameters
            blockColor = clr;
        }

        ArkanoidPanel arkPanel = gInfo.getArkanoidPanel();
        if(powerUp == "hidden"){ // if the power up is hidden, then set the default block transparency to 0, and sets the block color to silver
            currTransparency = 0;
            blockColor = new Color(192,192,192);
        }else if(powerUp == "explode"){ // if the power up is a exploding block, then it loads in the "exclamation mark" png for it, and resizes it accordingly
            BufferedImage tempImg = arkPanel.loadBuffImg("exclamation_mark.png");
            blockImage = gInfo.resizeImg(tempImg,width,height);
        }else if(powerUp == "multiball"){ // if the power up is "multiball", then it loads in the "multiball.png" picture and resizes it accordingly
            BufferedImage tempImg = arkPanel.loadBuffImg("multiball.png");
            blockImage = gInfo.resizeImg(tempImg,width,height);
        }else if(powerUp == "undestroyable"){ // sets the block color to a shade of gold if it's undestroyable
            blockColor = new Color(230,240,140);
        }else if(powerUp == "increasesize"){ // if the power up is "increassize", then it loads in the slider_expand.png image and sets the block image to that image
            BufferedImage tempImg = arkPanel.loadBuffImg("slider_expand.png");
            blockImage = gInfo.resizeImg(tempImg,width,height);
        }else if(powerUp == "decreasesize"){ // if the power up is "decreasesize", then it loads in the slider_shrink.png image and sets the block image to that image
            BufferedImage tempImg = arkPanel.loadBuffImg("slider_shrink.png");
            blockImage = gInfo.resizeImg(tempImg,width,height);
        }else if(powerUp == "fastforward"){ // if the power up is "fastforward", then it loads in the fastforward.png image and sets the block image to that image
            BufferedImage tempImg = arkPanel.loadBuffImg("fastforward.png");
            blockImage = gInfo.resizeImg(tempImg,width,height);
        }else if(powerUp == "slowdown"){ // if the power up is "slowdown", then it loads in the slowdown.png image and sets the block image to that image
            BufferedImage tempImg = arkPanel.loadBuffImg("slowdown.png");
            blockImage = gInfo.resizeImg(tempImg,width,height);
        }

        if(blockImage == null){ // if there's no selected blockImage, then it loads in the default block texture / image, and resizes it to fit onto the block
            BufferedImage tempImg = arkPanel.loadBuffImg("default_block_texture.png");
            blockImage = gInfo.resizeImg(tempImg,width,height);
        }
    }

    public void increaseHitTime(){ // method that increases the amount of time the block has been hit (this method is meant for hidden blocks)
        hitTimes+=1; // increases it by 1
        if(hitTimes == 1){ // if it's hit once, then it sets the currTransparency to 255
            currTransparency = 255;
        }else if(hitTimes > 1){ // else if it's hit more than once, then it calls on the ArkanoidUtil class to remove the block
            gInfo.removeBlock(this,false);
        }
    }

    public String getPowerUp(){ // returns the block's current powerup
        return specialAbility;
    }

    public Color getColor(){ // returns the current block's color
        return blockColor;
    }

    public int getDesignatedStageNum(){ // returns the stage number that this block belongs to
        return designatedStageNum;
    }

    public int getY() { // returns the y position of the top left corner
        return py;
    }

    public int getX() { // returns the x position of the top left corner
        return px;
    }

    public int getWidth() { // returns the width of this block
        return width;
    }

    public int getHeight(){ // returns the height of this block
        return height;
    }

    public boolean ballTouchedBlock(Ball ball) { // checks if the ball passed in the parameters / arguments touched this block
        int ballX = ball.getX(); // gets the ball's current x position
        int ballY = ball.getY(); // gets the ball's current y position

        int ballNextX = ballX + ball.getXSpeed(); //gets the ball's next x position
        int ballNextY = ballY + ball.getYSpeed(); // gtes the ball's next y position

        

        boolean hitHorizontally = ballNextX + ball.getDiameter() >= px && ballNextX <= px + width; // determines if the ball's next "x" position goes into the block
        boolean hitVertically = ballNextY + ball.getDiameter() >= py && ballNextY <= py + height; // determines if the ball's next "y" position goes into the block

        // the only reason why I checked if the ball is within the block is due to the tendency for the ball to go through blocks if the ball goes into a gap that's extremely small between 2 blocks
        boolean isInBlockHorizontal = ballX + ball.getDiameter() >= px && ballX <= px+width; // determines if the ball's current "x" position is within the block
        boolean isInBlockVertical = ballY + ball.getDiameter() >= py && ballY <= py+height; // determines if the ball's current "y" position is within the block

        return ((hitHorizontally && hitVertically) || (isInBlockHorizontal &&  isInBlockVertical)) && !isDestroyed && !isFading && gInfo.getCurrentStage() == designatedStageNum; // returns true if the ball's "x" and "y"'s next positions (or current positions) go into the block, and if this block isnt fading, isnt destroyed, and the current stage number equals to this block (otherwise it returns false)
    }
    
    

    public void setDestroyed(boolean val){ // sets the destroyed boolean to whatever is passed in the parameters
        if(specialAbility != "undestroyable"){ // if the block is an undestroyable block (as you dont want to set an undestroyable block to destroyable)
            isDestroyed = val; // sets the "isDestroyed" boolean to whatever "val" is
        } 
    }

    public void fadeOut() { // progressively lowers the transparency value of this block
        if(!isFading){ // if the block isnt fading, then set "isFading" to true
            isFading = true;
        }
        if(currTransparency-15 < 0){ // if currTransparency minus 15 is smaller than 0 then set the current transparency of the block to 0, and remove this block from the game
            currTransparency = 0;
            gInfo.removeBlock(this,true);
        }else{
            currTransparency -= 15; // subtracts 15 from currTransparency
        }
    }


    public void draw(Graphics g){ // draws the block
        if(gInfo.getCurrentStage() == designatedStageNum){ // if the block's designated stage number equals to what the current stage number is
            if(!isDestroyed && !isFading){ // if the block isnt destroyed or fading
                g.setColor(new Color(blockColor.getRed(),blockColor.getGreen(),blockColor.getBlue(),currTransparency)); // set the color to the block's current color, and add in an extra transparency parameter
                g.fillRect(px,py,width,height); // draws a rectangle on the screen with the block's top left corner position and size
                if(blockImage != null){ // if the block image isnt null
                    if(!specialAbility.equals("hidden")){ // if the powerup doesnt equal to "hidden", then it draws the block image onto the block
                        g.drawImage(blockImage,px,py,null);
                    }else{ // otherwise, if the block has been hit more than once, then it draws the block image onto the block
                        if(hitTimes > 0){
                            g.drawImage(blockImage,px,py,null);
                        }
                    }
                }
            }else if(isFading){ // if the block is fading
                fadeOut(); // run the fadeout method
                g.setColor(new Color(blockColor.getRed(),blockColor.getGreen(),blockColor.getBlue(),currTransparency)); // set the color to the block's current color, and add in an extra transparency parameter
                g.fillRect(px, py, width, height); // draws the rectangle on the screen with the block's top left corner and size
            }
        }
    }
}

class Ball { // the class that holds all the important stuff for the balls
    private int px, py; // variables to store x and y co-ordinates of the ball
    public int prevX,prevY; // variables to store previous x and y co-ordinates of the ball
    private int diameter = 15; // size of the ball
    private int screen_x, screen_y; // store's the screen's x and y size
    private int x_speed = 4; // speed in the x-direction for the ball
    private int y_speed = -4; // speed in the y-direction for the ball
    private boolean isAttached = false; // determines if the ball is attached to the slider or not
    private boolean ballDestroyed = false; // determines whether the ball is destroyed or not
    private boolean isFading = false; // determines if the ball is fading or not
    private int currTransparency = 255; // current transparency of the ball,255 = opaque, 0 = transparent
    private ArkanoidUtil gInfo; // stores the ArkanoidUtil object that holds the critical components for this game
    private ArkanoidPanel arkPanel; // stores the ArkanodPanel object that holds certain components (like playing sounds, loading images, etc) for this game

    public Ball(int x, int y, int[] dimensions,ArkanoidUtil gameInfo,ArkanoidPanel arkanoidPanel) { // constructor
        px = x; // sets the x-cordinate of the ball to 'x'
        py = y; // sets the y-cordinate of the ball to 'y'
        prevX = px; // sets the previous x-cordinate to px
        prevY = py; // sets the previous y-cordinate to py
        screen_x = dimensions[0]; // sets the size of the 'x' size of the screen to dimensions[0]
        screen_y = dimensions[1]; // sets the size of the 'y' size of the screen to dimensions[1]
        gInfo = gameInfo; // sets gInfo to the 'gameInfo' variable that was passed through the parameters of the constructor
        arkPanel = arkanoidPanel; // sets the 'arkPanel' variable that was passed through the parameters of the constructor
        Random rng = new Random(); // new Random() object
        if(rng.nextInt(2) == 0){ // if it's equal to 0, then it flips the x_speed around
            x_speed = -x_speed;
        }
        if(rng.nextInt(2) == 1){ // if it's equal to 1, then it flips the y_speed around
            y_speed = -y_speed;
        }
    }

    public void setAttached(boolean val) { // sets the 'isAttached' variable to whatever 'val' is || used to attach and de-attach the ball from the slider (basically allowing the ball to not move or move)
        isAttached = val;
    }

    public void manualMove(int x) { // manually moves the ball by adding 'x' onto px
        px += x;
    }

    public void setDestroyed(boolean val){ // sets the 'ballDestroyed' variable to whatever 'val' is || used to determine to 'destroy' the ball
        ballDestroyed = val;
    }

    public void recalculateMoveDirection(Block block) { // to recaulate the movement direction if the ball if it hits the edges of the block
        boolean hitTopOrBottom = (py + diameter <= block.getY() && py + diameter >= block.getY() - y_speed) ||
                                 (py >= block.getY() + block.getHeight() && py <= block.getY() + block.getHeight() - y_speed); // if the ball hits the top or bottom of the block || calculated by checking if the ball's next position goes into the block from the top or bottom
        
        boolean hitSides = (px + diameter <= block.getX() && px + diameter >= block.getX() - x_speed) ||
                           (px >= block.getX() + block.getWidth() && px <= block.getX() + block.getWidth() - x_speed); // if the ball hit the sides of the block || calculated by checking if the ball's next position goes into the block from the sides
    
        if (hitTopOrBottom) { // if it hit the top or bottom of the block, then it flips the y_speed
            y_speed = -y_speed; 
        }
        if (hitSides) { // if it hit the sides of the block, then it flips the x_speed
            x_speed = -x_speed;
        }
    }
    
    public void recalculateMoveDirection() { // to recalculate the movement direction if the ball hits the slider
        int hitPoint = px + (diameter / 2) - gInfo.getSlider().getX();  // gets the x-position that the ball hit the slider at
        double relativeHitPos = (double) hitPoint / gInfo.getSlider().getWidth();  // gets the hit position relative to the slider
    
        double angle = (relativeHitPos - 0.5) * Math.PI / 3; // calculates the new angle, where the minimum is 30 degrees in either direction

        x_speed = (int) (6*Math.sin(angle)); // sets x_speed to what the "horizontal" component would be of the final velocity
        y_speed = -(int) (6*Math.cos(angle)); // sets y_speed to what the "vertical" component would be of the final velocity
    

        if (Math.abs(x_speed) < 2) { // checks if the x_speed  is smaller than 2
            x_speed = (x_speed < 0) ? -2 : 2; // sets minimum x-speed to either -2 or 2 (-2 if its going the opposite direction) to prevent it from going 90 degrees up and down, or to 
        }
    }
    
    public void move(Slider slider) { // method that moves the slider
        if (!isAttached && !ballDestroyed && !isFading) { // if the ball isnt attached, isnt destroyed and isnt fading, then
            px += x_speed; // increases the x position by x_speed
            py += y_speed; // increases the y position by y_speed
    
            if (px <= 0 || px >= screen_x - diameter) { // if px is smaller than or equal to 0 or px is greater or equal to the width of the screen minus the diameter of the ball
                arkPanel.playSoundFX("hit_wall.wav"); // plays the "hit_wall" sound effect
                x_speed = -x_speed; // flips the x_speed
                px = Math.max(0, Math.min(px, screen_x - diameter));  // ensures that px doesnt go below 0 or past the screen's horizontal borders
            }
    

            if (py <= 0) { // if py is smaller or equal to 0
                arkPanel.playSoundFX("hit_wall.wav"); // plays the "hit_wall" sound effect
                y_speed = -y_speed; // flips the y_speed 
                py = Math.max(0, py); // ensures that py doesnt go into the negatives (which means doesnt go through the top of the screen)
            }
    
            for (int i=0;i<gInfo.getBlocks().size();i++) { // loops through all the current active blocks
                Block block = gInfo.getBlocks().get(i); // gets that block
                if (block.ballTouchedBlock(this)) { // if the ball has touched a block
                    arkPanel.playSoundFX("bounce.wav"); // plays the "bounce" sound effect
                    recalculateMoveDirection(block); // recalculates the movement direction for the ball
                    if(!block.getPowerUp().equals("undestroyable") && !block.getPowerUp().equals("hidden")){ // if the block isnt undestroyable and isnt hidden
                        gInfo.removeBlock(block,false); // removes the block
                    } 
                    if(block.getPowerUp().equals("multiball") || block.getPowerUp().equals("increasesize") || block.getPowerUp().equals("decreasesize")){ // if the powerup is multiball,  increasesize or decreasesize
                        gInfo.spawnPowerUp(block); // spawns a powerup thing
                    }else if(block.getPowerUp().equals("explode")){ // if the powerup is explode
                        arkPanel.playSoundFX("explode.wav"); // plays the "explode" sound effect
                        ArrayList<Block> toExplode = gInfo.getNearbyBlocks(block); // gets the block nearby the explode block
                        while(toExplode.size() > 0){ // while there's still existing blocks in the nearby blocks
                            gInfo.removeBlock(toExplode.get(0),false); // removes the block
                            toExplode.remove(0); // removes the block at the 0'th index from the toExplode arraylist
                        }
                    }else if(block.getPowerUp().equals("hidden")){ // if the powerup is hidden
                        block.increaseHitTime(); // increases the amount of times the block is hit
                    }else if(block.getPowerUp().equals("fastforward")){ // if the powerup is "fastforward"
                        gInfo.setSpeedMultiplier(gInfo.getSpeedMultiplier()*2); // sets the speedMultiplier to twice its original value
                    }else if(block.getPowerUp().equals("slowdown")){ // if the powerup is "fastforward"
                        gInfo.setSpeedMultiplier((int)gInfo.getSpeedMultiplier()/2); // sets the speedMultipler to half its original value
                    }
                }
            }
    
            if (slider.ballAtSlider(this)) { // if the ball is at the slider
                arkPanel.playSoundFX("bounce.wav"); // plays the "bounce" sound effect
                recalculateMoveDirection(); // recalculate the direction of the ball
            }
    
            if (py > screen_y) { // if the ball falls below the screen
                gInfo.removeBall(this); // removes the ball
                if(gInfo.getBalls().size() == 0){ // if there's no balls remaining after that ball was removed
                    gInfo.setNextStage();
                }
            }
        }
    }

    public int getXSpeed(){ // returns the current x_speed of the ball
        return x_speed;
    }

    public int getYSpeed(){ // returns the current y_speed of the ball
        return y_speed;
    }
    
    public int getDiameter() { // returns the diameter of the ball
        return diameter;
    }

    public int getY() { // returns the "y" position of the ball
        return py;
    }

    public int getX() { // returns the "x" position of the ball
        return px;
    }
    
    public void fadeOut(){ // lowers the current transparency of the ball each time its called
        if(!isFading){ // if its not fading
            isFading = true; // sets isFading to true
        }
        currTransparency -= 15; // subtracts 15 from the currTransparency
        if(currTransparency <= 0){ // if its smaller than 0
            currTransparency = 0; // sets it to 0
            gInfo.removeBall(this); // removes the current ball
        }
    }

    public void draw(Graphics g) { // draws the ball onto the screen / window
        if(!ballDestroyed && !isFading){ // if the ball isnt destroyed and isnt fading
            g.setColor(new Color(255,255,255,currTransparency)); // sets the color to white with the currTransparency value as the transparency value
            g.fillOval(px, py, diameter,diameter); // fills in an oval with px and py being its position, and diameter being its size
            gInfo.setBallTrail(this,g); // sets the ball trail
            prevX = px; // sets the previous x position to the current x position
            prevY = py; // sets the previous y position to the current y position
        }else if(isFading){ // if the ball is fading
            fadeOut(); // calls on the fadeOut() method
            g.setColor(new Color(255,255,255,currTransparency)); // sets the color to white with the currTransparency value as the transparency value
            g.fillOval(px, py, diameter,diameter); // fills in an oval with px and py being its position, and diameter being its size
        }
    }
}

class Slider { // the slider class, that contains all the important stuff for it to function properly
    private int px, py = 550; // location of the slider (top left corner)
    private int width = 150, height = 10; // size of the slider
    private int speed = 10; // speed of how fast the slider moves
    private ArrayList<Ball> allAttachedBalls = new ArrayList<>(); // arraylist that contains all the balls that are attached to the slider
    private ArkanoidUtil gInfo; // stores the "ArkanoidUtil" class that contains all the important game methods
    private boolean movingLeft = false,movingRight = false; // booleans that determine whether the slider is moving left or right
    public int prevX,prevY; // previous co-ordinates (top left corner) of the slider
    private boolean fadingOut = true; // determines if the slider is fading out or not (starts at true by default)
    private int currTransparency = 0; // the transparency of the slider (starts at 0 by default) |  255 = opaque, 0 = transparent
    private int targetWidth = width; // what the "targetWidth" is for the slider
    private int originalWidth = width; // what the original width is for the slider
    private ArkanoidPanel arkPanel; // stores the ArkanodPanel object that holds certain components (like playing sounds, loading images, etc) for this game

    public Slider(int x,ArkanoidUtil gameInfo,ArkanoidPanel arkanoidPanel) { // constructor
        px = x-width/2; // sets px to x - 1/2 of the slider's width
        gInfo = gameInfo; // sets gInfo to gameInfo (which was passed in the parameters within this constructor)
        arkPanel = arkanoidPanel;
        prevX = px; // sets prevX to px
        prevY = py; // sets prevY to py
        Timer moveTimer = new Timer(1,new ActionListener(){ // move timer (which allows for smooth instead of choppy movement)
            @Override // overrides the actionPerformed method
            public void actionPerformed(ActionEvent e){ // action performed method that gets called on if an action gets performed
                if(!fadingOut){ // if the slider isnt fading out
                    move(); // runs the move method
                }
            }
        });
        moveTimer.start(); // starts the moveTimer
    }

    public void resetSliderPos(){ // method that resets the slider's position
        if(currTransparency > 0){ // if the current transparency of the slider is greater than 0
            targetWidth = originalWidth; // sets the targetWidth to the original width of the slider
            fadeOut(); // runs the fadeOut() method
            fadingOut = true; // sets fadingOut to true
        }
    }

    public int getY() { // method that returns the y-position of the top left corner (py) of the slider
        return py;
    }

    public int getX() { // method that returns the x-position of the top left corner (px) of the slider
        return px;
    }

    public int getWidth() { // method that returns the width of the slider
        return width;
    }

    public int getHeight(){ // method that returns the height of the slider
        return height;
    }

    public void modifyTargetWidth(int newWidth){ // method that sets the targetWidth to the "newWidth" that's passed in the method's parameters
        targetWidth = newWidth;
    }

    public void keyPressed(KeyEvent k) { // key pressed method that helps determine whether the slider should move left or right
        if (k.getKeyCode() == KeyEvent.VK_LEFT || k.getKeyCode() == KeyEvent.VK_A) { // if the 'A' or left arrow key is pressed; set movingLeft to true and movingRight to false
            movingLeft = true;
            movingRight = false;
        } else if (k.getKeyCode() == KeyEvent.VK_RIGHT || k.getKeyCode() == KeyEvent.VK_D) { // else if the 'D' or right arrow key is pressed; set movingRight to true and movingLeft to false
            movingRight = true;
            movingLeft = false;
        }
    }

    public void keyReleased(KeyEvent k) { // key released method that helps determine if the slider should continue moving left/right (where if a key is released, it would stop moving assuming the key released is 'A','D',left arrow or right arrow)
        if (k.getKeyCode() == KeyEvent.VK_LEFT || k.getKeyCode() == KeyEvent.VK_A) { // if the key is 'A' or left arrow, then movingLeft = false (basically means stop moving left)
            movingLeft = false;
        } else if (k.getKeyCode() == KeyEvent.VK_RIGHT || k.getKeyCode() == KeyEvent.VK_D) { // if the key is 'D' or right arrow, then movingRight = false (basically means stop moving right)
            movingRight = false;
        }
    }


    public void move(){ // move method that actually moves the slider
        int changeInPos = 0; // determines the amount of pixels the slider has changed in the x-direction
        int[] dimensions = gInfo.getScreenSize(); // gets the dimensions of the screen, where width = index 0, and height = index 1
        if (px < dimensions[0]-width && movingRight) { // if px is smaller than the width of the screen minus the width of the slider, and movingRight is true
            changeInPos += speed; // increases changeInPos by the speed
            if(px+changeInPos > dimensions[0]-width){ // if px+changeInPos is greater than the width of the screen minus the width of the slider
                changeInPos = px+changeInPos - (dimensions[0]-width); // set changeInPos to px+changeInPos minus the width of the screen minus the width of the slider
            }
        } else if (px>0 && movingLeft) { // else if px is greater than 0 and movingLeft is true
            changeInPos -= speed; // decreases changeInPos by the speed
            if(px+changeInPos < 0){ // if px+changeInPos is smaller than 0
                changeInPos = 0-px; // set changeInPos to 0-px
            }
        }
    
        px += changeInPos; // increase px by changeInPos

        for(int i=0;i<allAttachedBalls.size();i++){ // loops through all of the "allAttachedBalls.size()" arraylist
            allAttachedBalls.get(i).manualMove(changeInPos); // manually moves each attached ball by changeInPos
        }

    }


    public boolean ballAtSlider(Ball ball) { // method that returns whether the ball is at the slider or not
        return (ball.getY() + ball.getDiameter() >= py) && // if the ball's y position + the ball's diameter is greater or equal to the slider's y position
               (ball.getY() + ball.getDiameter() <= py+height) && // if the ball's y position + the ball's diameter is smaller or equal to the slider's y position plus the slider's height
               (ball.getX() + ball.getDiameter() >= px) && // if the ball's x position + the ball's diameter is greater or equal to the x position of the slider
               (ball.getX() <= px + width); // and if the ball's x position is smaller than the slider's x position + the width of the slider
    }

    public void attachBall(Ball ball) { // method that attaches the ball to the slider
        if (!allAttachedBalls.contains(ball)) { // if the ball isnt already attached
            allAttachedBalls.add(ball); // adds the ball to the "allAttachedBalls" arraylist
            ball.setAttached(true); // sets the ball's attached value to true
        }
    }

    public void detachBall(Ball ball) { // method that deattaches the ball from the slider
        if (allAttachedBalls.contains(ball)) { // if the ball is attached to the slider
            allAttachedBalls.remove(ball); // removes the ball from the "allAttachedBalls" arraylist
            ball.setAttached(false); // sets the ball's attached value to false
        }
    }

    public ArrayList<Ball> getAttachedBalls() { // method tha returns all of the balls that are attached to the slider (aka it returns the "allAttachedBalls" arraylist)
        return allAttachedBalls;
    }

    private void fadeOut(){ // fades out the slider 
        if(fadingOut){ // if the slider is fading out
            if(currTransparency-15 >=0){ // if the current slider's transpareny minus 15 is greater or equal to 0, the subtract the currTransparency value by 15
                currTransparency -= 15;
            }else{ // otherwise....
                currTransparency = 255; // sets currTransparency to 255
                px = (gInfo.getScreenSize()[0]-width)/2; // sets px to the middle of the screen
                fadingOut = false; // sets fadingOut to false
                Ball starterBall = new Ball(px+width / 2 - 8, py-15,gInfo.getScreenSize(),gInfo,arkPanel);// creates a new ball at the slider's position
                attachBall(starterBall); // attaches it to the slider
                gInfo.addBall(starterBall); // adds the ball into the balls arraylist
            }
        }
    }

    private void changeWidth(){ // method that changes the with of the slider by +- 1 depending on the conditions
        if(width < targetWidth){ // if the width is smaller than the targetWidth
            width+=1; // adds 1 to width
            if((targetWidth-width)%2==0 && px > 0 && !movingRight){ // if the targetWidth-width is divisible by 2, px is greater than 0, and the slider isnt moving right
                px -= 1; // subtracts px by 1
            }
        }else if(width > targetWidth){ // if the width is greater than the targetWidth
            width -= 1; // subtracts 1 from width
            if((width-targetWidth)%2==0 && px < gInfo.getScreenSize()[0] && !movingLeft){ // if the width-targetWidth is divisble by 2, and px is smaller than the width of the screen, and the slider isnt moving left 
                px += 1; // adds 1 to px
            }
        }
    }

    public void draw(Graphics g) { // method that draws the slider onto the screen
        if(targetWidth != width){ // if the targetWidth does not equal to the width of the slider
            changeWidth(); // runs the changeWith() method
        }
        if(!fadingOut && gInfo.getGameStarted()){ // if the slider isnt fading out and the game has started
            g.setColor(Color.WHITE); // sets the color to white
            g.fillRoundRect(px, py, width, height,10,10); // fills a rounded rectangle with the proper parameters (position, size, etc)
            int dir = 0; // direction of the slider (-1 = left, 1 = right)
            if(prevX > px){ // if the previous x position is greater than px
                dir = -1; // sets dir to -1
            }else if(prevX < px){ // if the previous x position is smaller than px
                dir = 1; // sets dir to 1
            }
            if(prevX != px && (movingLeft || movingRight)){ // if the previous x position doesnt equal to px, and the slider is either moving left or right
                gInfo.setSliderTrail(g,dir); // calls on the "setSliderTrail" method
            }
            
        }else{ // otherwise....
            if(fadingOut){ // if the slider is fading out
                fadeOut(); // runs the fadeOut() method
            }
            g.setColor(new Color(255,255,255,currTransparency)); // sets the color to white, with a transparency value of "currTransparency"
            g.fillRoundRect(px,py,width,height,10,10); // fills a rounded rectangle with the proper parameters (position, size, etc)
        }
        prevX = px; // sets the previous x position to px
        prevY = py; // sets the previous y position to py
    }
}
