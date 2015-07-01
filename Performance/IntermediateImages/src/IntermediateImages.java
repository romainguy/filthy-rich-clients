import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
/*
 * IntermediateImages.java
 *
 * Created on May 2, 2007, 10:58 AM
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
public class IntermediateImages extends JComponent {

    private static final int SCALE_X = 20;
    private static final int SMILEY_X = 200;
    private static final int DIRECT_Y = 10;
    private static final int INTERMEDIATE_Y = 260;
    private static final int SMILEY_SIZE = 100;
    private static BufferedImage picture = null;
    private BufferedImage scaledImage = null;
    private BufferedImage smileyImage = null;
    private static final double SCALE_FACTOR = .1;
    private int scaleW, scaleH;
    
    /** Creates a new instance of IntermediateImages */
    public IntermediateImages() {
        try {
            URL url = getClass().getResource("images/BB.jpg");
            picture = ImageIO.read(url);
            scaleW = (int)(SCALE_FACTOR * picture.getWidth());
            scaleH = (int)(SCALE_FACTOR * picture.getHeight());
        } catch (Exception e) {
            System.out.println("Problem reading image file: " + e);
            System.exit(0);
        }
    }
    
    /**
     * Draws both the direct and intermediate-image versions of a 
     * scaled-image, timing both variations.
     */
    private void drawScaled(Graphics g) {
        long startTime, endTime, totalTime;
        
        // Scaled image
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        startTime = System.nanoTime();
        for (int i = 0; i < 100; ++i) {
            g.drawImage(picture, SCALE_X, DIRECT_Y, scaleW, scaleH, null);
        }
        endTime = System.nanoTime();
        totalTime = (endTime - startTime) / 1000000;
        g.setColor(Color.BLACK);
        g.drawString("Direct: " + ((float)totalTime/100) + " ms", 
                SCALE_X, DIRECT_Y + scaleH + 20);
        System.out.println("scaled: " + totalTime);
        
        // Intermediate Scaled
        // First, create the intermediate image
        if (scaledImage == null ||
            scaledImage.getWidth() != scaleW ||
            scaledImage.getHeight() != scaleH)
        {
            GraphicsConfiguration gc = getGraphicsConfiguration();
            scaledImage = gc.createCompatibleImage(scaleW, scaleH);
            Graphics gImg = scaledImage.getGraphics();
            ((Graphics2D)gImg).setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            gImg.drawImage(picture, 0, 0, scaleW, scaleH, null);
        }
        // Now, copy the intermediate image into place
        startTime = System.nanoTime();
        for (int i = 0; i < 100; ++i) {
            g.drawImage(scaledImage, SCALE_X, INTERMEDIATE_Y, null);
        }
        endTime = System.nanoTime();
        totalTime = (endTime - startTime) / 1000000;
        g.drawString("Intermediate: " + ((float)totalTime/100) + " ms", 
                SCALE_X, INTERMEDIATE_Y + scaleH + 20);
        System.out.println("Intermediate scaled: " + totalTime);
    }
    
    private void renderSmiley(Graphics g, int x, int y) {
	Graphics2D g2d = (Graphics2D)g.create();
        
	// Yellow face
	g2d.setColor(Color.yellow);
	g2d.fillOval(x, y, SMILEY_SIZE, SMILEY_SIZE);
        
	// Black eyes
	g2d.setColor(Color.black);
	g2d.fillOval(x + 30, y + 30, 8, 8);
	g2d.fillOval(x + 62, y + 30, 8, 8);
        
	// Black outline
	g2d.drawOval(x, y, SMILEY_SIZE, SMILEY_SIZE);
        
	// Black smile
	g2d.setStroke(new BasicStroke(3.0f));
	g2d.drawArc(x + 20, y + 20, 60, 60, 190, 160);
        
        g2d.dispose();
    }
    
    /**
     * Draws both the direct and intermediate-image versions of a 
     * smiley face, timing both variations.
     */
    private void drawSmiley(Graphics g) {
        long startTime, endTime, totalTime;

        // Draw smiley directly
        startTime = System.nanoTime();
        for (int i = 0; i < 100; ++i) {
            renderSmiley(g, SMILEY_X, DIRECT_Y);
        }
        endTime = System.nanoTime();
        totalTime = (endTime - startTime) / 1000000;
        g.setColor(Color.BLACK);
        g.drawString("Direct: " + ((float)totalTime/100) + " ms", 
                SMILEY_X, DIRECT_Y + SMILEY_SIZE + 20);
        System.out.println("Direct: " + totalTime);
        
        // Intermediate Smiley
        // First, create the intermediate image if necessary
	if (smileyImage == null) {
	  GraphicsConfiguration gc = getGraphicsConfiguration();
	  smileyImage = gc.createCompatibleImage(
                  SMILEY_SIZE + 1, SMILEY_SIZE + 1, Transparency.BITMASK);
	  Graphics2D gImg = (Graphics2D)smileyImage.getGraphics();
	  renderSmiley(gImg, 0, 0);
	  gImg.dispose();
	}
        // Now, copy the intermediate image
        startTime = System.nanoTime();
        for (int i = 0; i < 100; ++i) {
            g.drawImage(smileyImage, SMILEY_X, INTERMEDIATE_Y, null);
        }
        endTime = System.nanoTime();
        totalTime = (endTime - startTime) / 1000000;
        g.drawString("Intermediate: " + ((float)totalTime/100) + " ms", 
                SMILEY_X, INTERMEDIATE_Y + SMILEY_SIZE + 20);
        System.out.println("intermediate smiley: " + totalTime);
    }
    
    
    protected void paintComponent(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        drawScaled(g);
        drawSmiley(g);
    }
    
    private static void createAndShowGUI() {    
        JFrame f = new JFrame("IntermediateImages");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(360, 540);
        f.add(new IntermediateImages());
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
