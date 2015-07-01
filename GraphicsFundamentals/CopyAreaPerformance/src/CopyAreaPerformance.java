import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
/*
 * CopyAreaPerformance.java
 *
 * Created on May 1, 2007, 4:24 PM
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
public class CopyAreaPerformance extends JComponent implements KeyListener {
    
    private static final int SMILEY_SIZE = 10;
    private static final int PADDING = 2;
    private static final int MIN_COLOR = 0;
    private static final int CANVAS_W = SMILEY_SIZE * (256 - MIN_COLOR);
    private static final int CANVAS_H = SMILEY_SIZE * (256 - MIN_COLOR);
    private static final int SCROLL_SIZE = 100;
    private int viewX = CANVAS_W / 2;
    private int viewY = CANVAS_H / 2;
    private boolean useCopyArea = false;
    private boolean useClip = false;
    int prevVX;
    int prevVY;
    
    /** Creates a new instance of CopyAreaPerformance */
    public CopyAreaPerformance() {
        setOpaque(true);
    }
    
    private void drawSmiley(Graphics g, Color faceColor, int x, int y) {
        // fill face color
        g.setColor(faceColor);
        g.fillOval(x, y, SMILEY_SIZE, SMILEY_SIZE);
        g.setColor(Color.BLACK);
        // draw head
        g.drawOval(x, y, SMILEY_SIZE, SMILEY_SIZE);
        // draw smile
        g.drawArc(x + (int)((SMILEY_SIZE * .2)), (int)(y + (SMILEY_SIZE * .2)),
                (int)(SMILEY_SIZE * .6), (int)(SMILEY_SIZE * .6), 200, 140);
        // draw eyes
        int eyeSize = Math.max(2, (int)(SMILEY_SIZE * .1));
        g.fillOval(x + (int)((SMILEY_SIZE * .5) - (SMILEY_SIZE * .1) - eyeSize),
                y + (int)(SMILEY_SIZE * .3),
                eyeSize, eyeSize);
        g.fillOval(x + (int)((SMILEY_SIZE * .5) + (SMILEY_SIZE * .1)),
                y + (int)(SMILEY_SIZE * .3),
                eyeSize, eyeSize);
    }
    
    protected void paintComponent(Graphics g) {
        long startTime = System.nanoTime();
        // prevVX is set to -10000 when first enabled
        if (useCopyArea && prevVX > -9999) {
            // Most of this code determines the proper areas to copy and clip
            int scrollX = viewX - prevVX;
            int scrollY = viewY - prevVY;
            int copyFromY, copyFromX;
            int clipFromY, clipFromX;
            if (scrollX == 0) {
                // vertical scroll
                if (scrollY < 0) {
                    copyFromY = 0;
                    clipFromY = 0;
                } else {
                    copyFromY = scrollY;
                    clipFromY = getHeight() - scrollY;
                }
                // copy the old content, set the clip to the new area
                g.copyArea(0, copyFromY, getWidth(), getHeight() - Math.abs(scrollY),
                        0, -scrollY);
                g.setClip(0, clipFromY, getWidth(), Math.abs(scrollY));
            } else {
                // horizontal scroll
                if (scrollX < 0) {
                    copyFromX = 0;
                    clipFromX = 0;
                } else {
                    copyFromX = scrollX;
                    clipFromX = getWidth() - scrollX;
                }
                // copy the old content, set the clip to the new area
                g.copyArea(copyFromX, 0, getWidth() - Math.abs(scrollX), 
                        getHeight(), -scrollX, 0);
                g.setClip(clipFromX, 0, Math.abs(scrollX), getHeight());
            }
        }
        // Track previous view position for next scrolling operation
        prevVX = viewX;
        prevVY = viewY;
        // Get the clip in case we need it later
        Rectangle clipRect = g.getClip().getBounds();
        int clipL = (int)(clipRect.getX());
        int clipT = (int)(clipRect.getY());
        int clipR = (int)(clipRect.getMaxX());
        int clipB = (int)(clipRect.getMaxY());
        g.setColor(Color.WHITE);
        g.fillRect(clipL, clipT, (int)clipRect.getWidth(), (int)clipRect.getHeight());
        for (int column = 0; column < 256; ++column) {
            int x = column * (SMILEY_SIZE + PADDING) - viewX;
            if (useClip) {
                if (x > clipR || (x + (SMILEY_SIZE + PADDING)) < clipL) {
                    // trivial reject; outside to the left or right
                    continue;
                }
            }
            for (int row = 0; row < 256; ++row) {
                int y = row * (SMILEY_SIZE + PADDING) - viewY;
                if (useClip) {
                    if (y > clipB || (y + (SMILEY_SIZE + PADDING)) < clipT) {
                        // trivial reject; outside to the top or bottom
                        continue;
                    }
                }
                Color faceColor = new Color(column, row, 0);
                drawSmiley(g, faceColor, x, y);
            }
        }
        long stopTime = System.nanoTime();
        System.out.println("Painted in " + 
                ((stopTime - startTime) / 1000000) + " ms");
    }
    
    private void scroll(int scrollX, int scrollY) {
        viewX += scrollX;
        viewY += scrollY;
        viewX = Math.max(viewX, 0);
        viewX = Math.min(viewX, CANVAS_W - viewX);
        viewY = Math.max(viewY, 0);
        viewY = Math.min(viewY, CANVAS_H - viewY);
        repaint();
    }
    
    // KeyListener methods
    
    /**
     * Arrow keys scroll the view around. The 'c' key toggles clip area
     * optimization. The 'a' key toggles copyArea optimization.
     */
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            scroll(SCROLL_SIZE, 0);
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            scroll(-SCROLL_SIZE, 0);
        } else if (e.getKeyCode() == KeyEvent.VK_UP) {
            scroll(0, -SCROLL_SIZE);
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            scroll(0, SCROLL_SIZE);
        } else if (e.getKeyCode() == KeyEvent.VK_C) {
            useClip = !useClip;
            System.out.println("useClip = " + useClip);
        } else if (e.getKeyCode() == KeyEvent.VK_A) {
            useCopyArea = !useCopyArea;
            prevVX = -10000;
            System.out.println("useCopyArea = " + useCopyArea);
        }
    }
    public void keyReleased(KeyEvent e) {
    }
    public void keyTyped(KeyEvent e) {
    }

    private static void createAndShowGUI() {
        JFrame f = new JFrame("CopyAreaPerformance");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(600, 600);
        CopyAreaPerformance component = new CopyAreaPerformance();
        f.add(component);
        f.addKeyListener(component);
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
