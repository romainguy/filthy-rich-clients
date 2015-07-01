import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
/*
 * SmoothMoves.java
 *
 * Created on May 2, 2007, 4:49 PM
 *
 * Copyright (c) 2007, Sun Microsystems, Inc
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution.
 *   * Neither the name of the TimingFramework project nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 *
 * @author Chet
 */
public class SmoothMoves extends JComponent implements ActionListener, KeyListener {
        
    /** image holds the graphics we render for each animating object */
    BufferedImage image = null;
    static int imageW = 100;
    static int imageH = 150;
    
    /** Location of fading animation */
    int fadeX = 50;
    int fadeY = 50;
    
    /** X values that moving animation will move between */
    static int moveMinX = 150;
    static int moveMaxX = 350;
    
    /** Current x/y location of moving animation */
    int moveX = moveMinX;
    int moveY = 50;
    
    /** Current opacity of fading animation */
    float opacity = 0.0f;
    
    /** Toggles for various demo options (key to toggle in parentheses) */
    boolean useImage = false;   // (i) image instead of rectangle
    boolean useAA = false;      // (a) anti-aliased edges (rectangle only)
    boolean motionBlur = false; // (b) ghost images behind moving animation
    boolean alterColor = false; // (c) light-gray instead of black rectangle
    boolean linear = true;      // (l) linear vs. non-linear motion

    /** Used for motion blur rendering; holds information for ghost trail */
    int blurSize = 5;
    int prevMoveX[];
    int prevMoveY[];
    float trailOpacity[];
    
    /** Basic Timer animation info */
    final static int CYCLE_TIME = 2000;     // One cycle takes 2 seconds
    int currentResolution = 50;             // current Timer resolution
    Timer timer = null;                     // animation Timer
    long cycleStart;                        // track start time for each cycle
    
    /** Creates a new instance of SmoothAnimation */
    public SmoothMoves() {
        //createAnimationImage();
        cycleStart = System.nanoTime() / 1000000;
        startTimer(currentResolution);
    }
    
    /**
     * Create the image that will be animated. This image may be an actual
     * image (duke.gif), or some graphics (a variation on a black filled
     * rectangle) that are rendered into an image. The contents
     * of this image are dependent upon the runtime toggles that have been
     * set when this method is called.
     */
    void createAnimationImage() {
        GraphicsConfiguration gc = getGraphicsConfiguration();
        image = gc.createCompatibleImage(imageW, imageH, Transparency.TRANSLUCENT);
        Graphics2D gImg = image.createGraphics();
        if (useImage) {
            try {
	        URL url = getClass().getResource("images/duke.gif");
                Image originalImage = ImageIO.read(url);
                gImg.drawImage(originalImage, 0, 0, imageW, imageH, null);
            } catch (Exception e) {}
        } else {
            // use graphics
            Color graphicsColor;
            if (alterColor) {
                graphicsColor = Color.LIGHT_GRAY;
            } else {
                graphicsColor = Color.BLACK;
            }
            gImg.setColor(graphicsColor);
            gImg.fillRect(0, 0, imageW, imageH);
            if (useAA) {
                // Antialiasing hack - just draw a fading-out border around the
                // rectangle
                gImg.setComposite(AlphaComposite.Src);
                int red = graphicsColor.getRed();
                int green = graphicsColor.getRed();
                int blue = graphicsColor.getRed();
                gImg.setColor(new Color(red, green, blue, 50));
                gImg.drawRect(0, 0, imageW - 1, imageH - 1);
                gImg.setColor(new Color(red, green, blue, 100));
                gImg.drawRect(1, 1, imageW - 3, imageH - 3);
                gImg.setColor(new Color(red, green, blue, 150));
                gImg.drawRect(2, 2, imageW - 5, imageH - 5);
                gImg.setColor(new Color(red, green, blue, 200));
                gImg.drawRect(3, 3, imageW - 7, imageH - 7);
                gImg.setColor(new Color(red, green, blue, 225));
                gImg.drawRect(4, 4, imageW - 9, imageH - 9);
            }
        }
        gImg.dispose();
    }
    
    public void paintComponent(Graphics g) {
        if (image == null) {
            createAnimationImage();
        }
        
        // Erase the background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        // Draw the fading image
        Graphics2D gFade = (Graphics2D)g.create();
        gFade.setComposite(AlphaComposite.SrcOver.derive(opacity));
        gFade.drawImage(image, fadeX, fadeY, null);
        gFade.dispose();
        
        // Draw the moving image
        if (motionBlur) {
            // Draw previous locations of the image as a trail of 
            // ghost images
            if (prevMoveX == null) {
                // blur location array not yet created; create it now
                prevMoveX = new int[blurSize];
                prevMoveY = new int[blurSize];
                trailOpacity = new float[blurSize];
                float incrementalFactor = .2f / (blurSize + 1);
                for (int i = 0; i < blurSize; ++i) {
                    // default values, act as flag to not render these
                    // until they have real values
                    prevMoveX[i] = -1;
                    prevMoveY[i] = -1;
                    // vary the translucency by the number of the ghost
                    // image; the further away it is from the current one,
                    // the more faded it will be
                    trailOpacity[i] = (.2f - incrementalFactor) - 
                            i * incrementalFactor;
                }
            } else {
                Graphics2D gTrail = (Graphics2D)g.create();
                for (int i = 0; i < blurSize; ++i) {
                    if (prevMoveX[i] >= 0) {
                        // Render each blur image with the appropriate
                        // amount of translucency
                        gTrail.setComposite(AlphaComposite.SrcOver.derive(trailOpacity[i]));
                        gTrail.drawImage(image, prevMoveX[i], prevMoveY[i], null);
                    }
                }
                gTrail.dispose();
            }
        }
        g.drawImage(image, moveX, moveY, null);
        if (motionBlur) {
            // shift the ghost positions to add the current position and
            // drop the oldest one
            for (int i = blurSize - 1; i > 0; --i) {
                prevMoveX[i] = prevMoveX[i - 1];
                prevMoveY[i] = prevMoveY[i - 1];
            }
            prevMoveX[0] = moveX;
            prevMoveY[0] = moveY;
        }
    }
    
    /**
     * This method handles the events from the Swing Timer
     */
    public void actionPerformed(ActionEvent ae) {
        // calculate the fraction elapsed of the animation and call animate()
        // to alter the values accordingly
        long currentTime = System.nanoTime() / 1000000;
        long totalTime = currentTime - cycleStart;
        if (totalTime > CYCLE_TIME) {
            cycleStart = currentTime;
        }
        float fraction = (float)totalTime / CYCLE_TIME;
        fraction = Math.min(1.0f, fraction);
        fraction = 1 - Math.abs(1 - (2 * fraction));
        animate(fraction);
    }

    /**
     * Animate the opacity and location factors, according to the current
     * fraction.
     */
    public void animate(float fraction) {
        float animationFactor;
        if (linear) {
            animationFactor = fraction;
        } else {
            // Our "nonlinear" motion just uses a sin function to get a 
            // simple bounce behavior
            animationFactor = (float)Math.sin(fraction * (float)Math.PI/2);
        }
        // Clamp the value to make sure it does not exceed the bounds
        animationFactor = Math.min(animationFactor, 1.0f);
        animationFactor = Math.max(animationFactor, 0.0f);
        // The opacity, used by the fading animation, will just use the 
        // animation fraction directly
        opacity = animationFactor;
        // The move animation will calculate a location based on a linear
        // interpolation between its start and end points using the fraction
        moveX = moveMinX + (int)(.5f + animationFactor * 
                (float)(moveMaxX - moveMinX));
        // redisplay our component with the new animated values
        repaint();
    }
    
    /**
     * Moves the frame rate up or down by changing the Timer resolution
     */
    private void changeResolution(boolean faster) {
        if (faster) {
            currentResolution -= 5;
        } else {
            currentResolution += 5;
        }
        currentResolution = Math.max(currentResolution, 0);
        currentResolution = Math.min(currentResolution, 500);
        startTimer(currentResolution);
    }
    
    /**
     * Starts the animation
     */
    private void startTimer(int resolution) {
        if (timer != null) {
            timer.stop();
            timer.setDelay(resolution);
        } else {
            timer = new Timer(resolution, this);
        }
        timer.start();
    }

    /**
     * Toggles various rendering flags
     */
    public void keyPressed(KeyEvent ke) {
        int keyCode = ke.getKeyCode();
        if (keyCode == KeyEvent.VK_B) {
            // B: Motion blur - displays trail of ghost images
            motionBlur = !motionBlur;
        } else if (keyCode == KeyEvent.VK_A) {
            // A: Antialiasing - Displays soft edges around graphics
            useAA = !useAA;
            createAnimationImage();
        } else if (keyCode == KeyEvent.VK_C) {
            // C: Color - Toggles rectangle color between dark and light colors
            alterColor = !alterColor;
            createAnimationImage();
        } else if (keyCode == KeyEvent.VK_I) {
            // I: Image - Toggles use of image or filled rectangle to show how 
            // straight edges affect animation perception
            useImage = !useImage;
            createAnimationImage();
        } else if (keyCode == KeyEvent.VK_UP) {
            // Up Arrow: Speed - Speeds up frame rate
            changeResolution(true);
        } else if (keyCode == KeyEvent.VK_DOWN) {
            // Down Arrow: Speed - Slows down frame rate
            changeResolution(false);
        } else if (keyCode == KeyEvent.VK_L) {
            // L: Linearity: Toggles linear/nonlinear motion
            linear = !linear;
        } else if (keyCode >= KeyEvent.VK_1 && keyCode <= KeyEvent.VK_9) {
            // 0-9: Blur size: Toggles size of ghost trail for motion blur
            blurSize = keyCode - KeyEvent.VK_0;
            prevMoveX = prevMoveY = null;
        }
    }

    // Unused KeyListener implementations
    public void keyReleased(KeyEvent ke) {}
    public void keyTyped(KeyEvent ke) {}
    
    private static void createAndShowGUI() {
	JFrame f = new JFrame("Smooth Moves");
	f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	f.setSize(moveMaxX + imageW + 50, 300);
	SmoothMoves component = new SmoothMoves();
	f.add(component);
	f.setVisible(true);
        f.addKeyListener(component);
    }

    public static void main(String[] args) {
	Runnable doCreateAndShowGUI = new Runnable() {
	    public void run() {
		createAndShowGUI();
	    }
	};
	SwingUtilities.invokeLater(doCreateAndShowGUI);
    }
    
}
