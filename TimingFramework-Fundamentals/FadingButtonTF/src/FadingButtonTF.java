import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.Animator.Direction;
import org.jdesktop.animation.timing.Animator.RepeatBehavior;
import org.jdesktop.animation.timing.TimingTarget;
/*
 * FadingButtonTF.java
 *
 * Created on May 3, 2007, 7:20 AM
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
public class FadingButtonTF extends JButton 
        implements ActionListener, TimingTarget {

    float alpha = 1.0f;                 // current opacity of button
    Animator animator;                  // for later start/stop actions
    int animationDuration = 2000; 	// each cycle will take 2 seconds
    BufferedImage buttonImage = null;
    
    /** Creates a new instance of FadingButtonTF */
    public FadingButtonTF(String label) {
        super(label);
        setOpaque(false);
        animator = new Animator(animationDuration/2, Animator.INFINITE, 
                RepeatBehavior.REVERSE, this);
        animator.setStartFraction(1.0f);
        animator.setStartDirection(Direction.BACKWARD);
        addActionListener(this);
    }
    
    public void paint(Graphics g) {
        // Create an image for the button graphics if necessary
        if (buttonImage == null || buttonImage.getWidth() != getWidth() ||
                buttonImage.getHeight() != getHeight()) {
            buttonImage = getGraphicsConfiguration().
                    createCompatibleImage(getWidth(), getHeight());
        }
        Graphics gButton = buttonImage.getGraphics();
        gButton.setClip(g.getClip());
        
        //  Have the superclass render the button for us
        super.paint(gButton);
        
        // Make the graphics object sent to this paint() method translucent
	Graphics2D g2d  = (Graphics2D)g;
	AlphaComposite newComposite = 
	    AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
	g2d.setComposite(newComposite);
        
        // Copy the button's image to the destination graphics, translucently
        g2d.drawImage(buttonImage, 0, 0, null);
    }
    
    /**
     * This method receives click events, which start and stop the animation
     */
    public void actionPerformed(ActionEvent ae) {
        if (!animator.isRunning()) {
            this.setText("Stop Animation");
            animator.start();
        } else {
            animator.stop();
            this.setText("Start Animation");
            // reset alpha to opaque
            alpha = 1.0f;
        }
    }
    // Ununsed MouseListener implementations
    public void begin() {}
    public void end() {}
    public void repeat() {}
    
    /**
     * TimingTarget implementation: this method sets the alpha of our button
     * to be equal to the current elapsed fraction of the animation
     */
    public void timingEvent(float fraction) {
        alpha = fraction;
        // redisplay our cbutton
        repaint();
    }

    private static void createAndShowGUI() {
        JFrame f = new JFrame("Fading Button TF");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(300, 300);
        JPanel checkerboard = new Checkerboard();
        checkerboard.add(new FadingButtonTF("Start Animation"));
        f.add(checkerboard);
        f.setVisible(true);
    }
    
    public static void main(String args[]) {
        Runnable doCreateAndShowGUI = new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        };
        SwingUtilities.invokeLater(doCreateAndShowGUI);
    }

    private static class Checkerboard extends JPanel {
        private static final int DIVISIONS = 10;
        static final int CHECKER_SIZE = 60;
        public void paintComponent(Graphics g) {
            g.setColor(Color.white);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.BLACK);
            for (int stripeX = 0; stripeX < getWidth(); stripeX += CHECKER_SIZE) {
                for (int y = 0, row = 0; y < getHeight(); y += CHECKER_SIZE/2, ++row) {
                    int x = (row % 2 == 0) ? stripeX : (stripeX + CHECKER_SIZE/2);
                    g.fillRect(x, y, CHECKER_SIZE/2, CHECKER_SIZE/2);
                }
            }
        }
    }
}
