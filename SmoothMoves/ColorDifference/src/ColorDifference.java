import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
/*
 * ColorDifference.java
 *
 * Created on May 3, 2007, 7:12 AM
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
public class ColorDifference extends JComponent implements ActionListener {
    
    private Color largeRectColors[] = {
        Color.BLACK, new Color(3, 3, 3)
    };
    private Color smallRectColors[] = {
        Color.BLACK, Color.WHITE
    };
    private int colorIndex = 0;
    private static int FADE_X = 0;
    private static int BLANK_X = 0;
    
    /** Creates a new instance of ColorDifference */
    public ColorDifference() {
        Timer timer = new Timer(1000, this);
        timer.start();
        setPreferredSize(new Dimension(300, 200));
    }

    /**
     * Displays our component with the animating colors of the areas on the 
     * left and right, separated by an area of white in the middle
     */
    @Override
    protected void paintComponent(Graphics g) {
        int fadeX = 0;
        int blankX = getWidth()/3;
        int bigRectW = getWidth()/3;
        
        // Fill left-side rectangle with current animating color
        g.setColor(largeRectColors[colorIndex]);
        g.fillRect(0, 0, bigRectW, getHeight());
        
        // Fill middle area with white
        g.setColor(Color.WHITE);
        g.fillRect(bigRectW, 0, bigRectW, getHeight());
        
        // Fill right-side rectangle with black, with a white square 
        // in the middle
        int bigRectX = 2 * bigRectW;
        int smallRectW = 4;
        int smallRectH = 4;
        int smallRectX = bigRectX + (bigRectW / 2) - (smallRectW / 2);
        int smallRectY = (getHeight() / 2) - (smallRectH / 2);
        g.setColor(Color.BLACK);
        g.fillRect(2*getWidth()/3, 0, getWidth()/3, getHeight());
        g.setColor(smallRectColors[colorIndex]);
        g.fillRect(smallRectX, smallRectY, smallRectW, smallRectH);
    }
    
    /**
     * Handles Timer events by toggling the colorIndex used to fill the
     * left-side rectangle
     */
    public void actionPerformed(ActionEvent ae) {
        colorIndex++;
        colorIndex = colorIndex % 2;
        repaint();
    }
    
    private static void createAndShowGUI() {
	JFrame f = new JFrame("Color Difference");
	f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	f.setSize(300, 200);
	ColorDifference component = new ColorDifference();
	f.add(component);
        f.pack();
	f.setVisible(true);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
	Runnable doCreateAndShowGUI = new Runnable() {
	    public void run() {
		createAndShowGUI();
	    }
	};
	SwingUtilities.invokeLater(doCreateAndShowGUI);
    }
}
