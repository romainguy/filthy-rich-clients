import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
/*
 * AnimatedGraphics.java
 *
 * Created on May 2, 2007, 4:02 PM
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
public class AnimatedGraphics extends JComponent implements ActionListener {

    Color startColor = Color.GRAY;	// where we start
    Color endColor = Color.BLACK;         // where we end
    Color currentColor = startColor;
    int animationDuration = 2000; 	// each animation will take 2 seconds
    long animStartTime;			// start time for each animation
    
    /**
     * Set up and start the timer
     */
    public AnimatedGraphics() {
        Timer timer = new Timer(30, this);
        // initial delay while window gets set up
        timer.setInitialDelay(1000);
        animStartTime = 1000 + System.nanoTime() / 1000000;
        timer.start();
    }
    
    /**
     * Erase to the background color and fill an oval with the current
     * color (which is being animated elsewhere)
     */
    public void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(currentColor);
        g.fillOval(0, 0, getWidth(), getHeight());
    }
    
    /**
     * Callback from the Swing Timer. Calculate the fraction elapsed of
     * our desired animation duration and interpolate between our start and
     * end colors accordingly.
     */
    public void actionPerformed(ActionEvent ae) {
        // calculate elapsed fraction of animation
        long currentTime = System.nanoTime() / 1000000;
        long totalTime = currentTime - animStartTime;
        if (totalTime > animationDuration) {
            animStartTime = currentTime;
        }
        float fraction = (float)totalTime / animationDuration;
        fraction = Math.min(1.0f, fraction);
        // interpolate between start and end colors with current fraction
        int red = (int)(fraction * endColor.getRed() + 
                (1 - fraction) * startColor.getRed());
        int green = (int)(fraction * endColor.getGreen() + 
                (1 - fraction) * startColor.getGreen());
        int blue = (int)(fraction * endColor.getBlue() + 
                (1 - fraction) * startColor.getBlue());
        // set our new color appropriately
        currentColor = new Color(red, green, blue);
        // force a repaint to display our oval with its new color
        repaint();
    }
    
    private static void createAndShowGUI() {    
        JFrame f = new JFrame("Animated Graphics");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(200, 200);
        f.add(new AnimatedGraphics());
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
}
