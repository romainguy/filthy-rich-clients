import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Line2D;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
/*
 * OptimalPrimitives.java
 *
 * Created on May 2, 2007, 11:06 AM
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
public class OptimalPrimitives extends JComponent {
    
    private static final int LINE_X = 100;
    private static final int RECT_X = 200;
    private static final int TEXT_X = 250;
    private static final int BAD_Y = 60;
    private static final int GOOD_Y = 160;
    private static final int ITERATIONS = 1000;
    
    /** Creates a new instance of OptimalPrimitives */
    public OptimalPrimitives() {
    }
    
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        long startTime, endTime, totalTime;
        
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.BLACK);
        
        g.drawString("Bad vs. Good Primitive Rendering", 50, 20);
        g.drawString("(" + ITERATIONS + " iterations)", 100, 35);
        g.drawString("Bad: ", 10, BAD_Y + 30);
        g.drawString("Good: ", 10, GOOD_Y + 30);
        
        // Bad line
        Shape line = new Line2D.Double(LINE_X, BAD_Y, LINE_X + 50, 
                BAD_Y + 50);
        startTime = System.nanoTime();
        for (int i = 0; i < ITERATIONS; ++i) {
            g2d.draw(line);
        }
        endTime = System.nanoTime();
        totalTime = (endTime - startTime) / 1000000;
        System.out.println("bad line = " + totalTime);
        g.drawString(totalTime + " ms", LINE_X, BAD_Y + 70);
        
        // Good line
        startTime = System.nanoTime();
        for (int i = 0; i < ITERATIONS; ++i) {
            g.drawLine(LINE_X, GOOD_Y, LINE_X + 50, GOOD_Y + 50);
        }
        endTime = System.nanoTime();
        totalTime = (endTime - startTime) / 1000000;
        System.out.println("good line = " + totalTime);
        g.drawString(totalTime + " ms", LINE_X, GOOD_Y + 70);
        
        
        // Bad rect
        Shape rect = new Rectangle(RECT_X, BAD_Y, 50, 50);
        startTime = System.nanoTime();
        for (int i = 0; i < ITERATIONS; ++i) {
            g2d.fill(rect);
        }
        endTime = System.nanoTime();
        totalTime = (endTime - startTime) / 1000000;
        System.out.println("bad rect = " + totalTime);
        g.drawString(totalTime + " ms", RECT_X, BAD_Y + 70);
        
        // Good rect
        startTime = System.nanoTime();
        for (int i = 0; i < ITERATIONS; ++i) {
            g.fillRect(RECT_X, GOOD_Y, 50, 50);
        }
        endTime = System.nanoTime();
        totalTime = (endTime - startTime) / 1000000;
        System.out.println("good rect = " + totalTime);
        g.drawString(totalTime + " ms", RECT_X, GOOD_Y + 70);
    }
    
    private static void createAndShowGUI() {    
        JFrame f = new JFrame("OptimalPrimitives");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(320, 300);
        f.add(new OptimalPrimitives());
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
