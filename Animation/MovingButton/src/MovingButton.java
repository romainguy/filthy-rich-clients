import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
/*
 * MovingButton.java
 *
 * Created on May 2, 2007, 4:17 PM
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
public class MovingButton extends JButton implements ActionListener {

    Timer timer;                        // for later start/stop actions
    int animationDuration = 2000; 	// each animation will take 2 seconds
    long animStartTime;			// start time for each animation
    int translateY = 0;                 // y location of the button
    static final int MAX_Y = 100;
    
    /** Creates a new instance of TranslucentButton */
    public MovingButton(String label) {
        super(label);
        setOpaque(false);
        timer = new Timer(30, this);
        addActionListener(this);
    }
    
    /**
     * Displays our component in the location (0, translateY). Note that
     * this changes only the rendering location of the button, not the
     * physical location of it. Note, also, that rendering into g will
     * be clipped to the physical location of the button, so the button will
     * disappear as it moves away from that location.
     */
    public void paint(Graphics g) {
        g.translate(0, translateY);
        super.paint(g);
    }
    
    /**
     * This method handles both button clicks, which start/stop the animation,
     * and Swing Timer events.
     */
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource().equals(this)) {
            // button click
            if (!timer.isRunning()) {
                animStartTime = System.nanoTime() / 1000000;
                this.setText("Stop Animation");
                timer.start();
            } else {
                timer.stop();
                this.setText("Start Animation");
                // reset translation to 0
                translateY = 0;
            }
        } else {
            // Timer event
            // calculate the elapsed fraction
            long currentTime = System.nanoTime() / 1000000;
            long totalTime = currentTime - animStartTime;
            if (totalTime > animationDuration) {
                animStartTime = currentTime;
            }
            float fraction = (float)totalTime / animationDuration;
            fraction = Math.min(1.0f, fraction);
            // This calculation will cause translateY to go from 0 to MAX_Y
            // as the fraction goes from 0 to 1
            if (fraction < .5f) {
                translateY = (int)(MAX_Y * (2 * fraction));
            } else {
                translateY = (int)(MAX_Y * (2 * (1 - fraction)));
            }
            // redisplay our component with the new location
            repaint();
        }
    }

    private static void createAndShowGUI() {
        JFrame f = new JFrame("Moving Button");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(300, 300);
        JPanel checkerboard = new Checkerboard();
        checkerboard.add(new MovingButton("Start Animation"));
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

    /**
     * Paints a checkerboard background
     */
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
