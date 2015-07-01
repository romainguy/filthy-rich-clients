import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
/*
 * ScaleTest.java
 *
 * Created on May 1, 2007, 4:42 PM
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
public class ScaleTest extends JComponent {
    
    private static final int FULL_SIZE = 190;
    private static final int PADDING = 5;
    private static final int QUAD_SIZE = FULL_SIZE / 2;
    private static final double SCALE_FACTOR = .17;
    private static BufferedImage originalImage = 
            new BufferedImage(FULL_SIZE, FULL_SIZE, BufferedImage.TYPE_INT_RGB);
    boolean originalImagePainted = false;
    
    /**
     * Paints the test image that will be downscaled and timed by the various
     * scaling methods. A different image is rendered into each of the four
     * quadrants of this image: RGB stripes, a picture, vector art, and 
     * a black and white grid.
     */
    private void paintOriginalImage() {
        Graphics g = originalImage.getGraphics();
        // Erase to black
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, FULL_SIZE, FULL_SIZE);
        
        // RGB quadrant
        for (int i = 0; i < QUAD_SIZE; i += 3) {
            int x = i;
            g.setColor(Color.RED);
            g.drawLine(x, 0, x, QUAD_SIZE);
            x++;
            g.setColor(Color.GREEN);
            g.drawLine(x, 0, x, QUAD_SIZE);
            x++;
            g.setColor(Color.BLUE);
            g.drawLine(x, 0, x, QUAD_SIZE);
        }
        
        // Picture quadrant
        try {
            URL url = getClass().getResource("images/BBGrayscale.png");
            BufferedImage picture = ImageIO.read(url);
            // Center picture in quadrant area
            int xDiff = QUAD_SIZE - picture.getWidth();
            int yDiff = QUAD_SIZE - picture.getHeight();
            g.drawImage(picture, QUAD_SIZE + xDiff/2, yDiff/2, null);
        } catch (Exception e) {
            System.out.println("Problem reading image file: " + e);
        }
        
        // Vector drawing quadrant
        g.setColor(Color.WHITE);
        g.fillRect(0, QUAD_SIZE, QUAD_SIZE, QUAD_SIZE);
        g.setColor(Color.BLACK);
        g.drawOval(2, QUAD_SIZE + 2, QUAD_SIZE-4, QUAD_SIZE-4);
        g.drawArc(20, QUAD_SIZE + 20, (QUAD_SIZE - 40), QUAD_SIZE - 40, 
                190, 160);
        int eyeSize = 7;
        int eyePos = 30 - (eyeSize / 2);
        g.fillOval(eyePos, QUAD_SIZE + eyePos, eyeSize, eyeSize);
        g.fillOval(QUAD_SIZE - eyePos - eyeSize, QUAD_SIZE + eyePos, 
                eyeSize, eyeSize);
        
        // B&W grid
        g.setColor(Color.WHITE);
        g.fillRect(QUAD_SIZE + 1, QUAD_SIZE + 1, QUAD_SIZE, QUAD_SIZE);
        g.setColor(Color.BLACK);
        for (int i = 0; i < QUAD_SIZE; i += 4) {
            int pos = QUAD_SIZE + i;
            g.drawLine(pos, QUAD_SIZE + 1, pos, FULL_SIZE);
            g.drawLine(QUAD_SIZE + 1, pos, FULL_SIZE, pos);
        }
        
        originalImagePainted = true;
    }
    

    /**
     * Progressive bilinear scaling: for any downscale size, scale
     * iteratively by halves using BILINEAR filtering until the proper 
     * size is reached.
     */
    private BufferedImage getOptimalScalingImage(BufferedImage inputImage,
            int startSize, int endSize) {
        int currentSize = startSize;
        BufferedImage currentImage = inputImage;
        int delta = currentSize - endSize;
        int nextPow2 = currentSize >> 1;
        while (currentSize > 1) {
            if (delta <= nextPow2) {
                if (currentSize != endSize) {
                    BufferedImage tmpImage = new BufferedImage(endSize,
                            endSize, BufferedImage.TYPE_INT_RGB);
                    Graphics g = tmpImage.getGraphics();
                    ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g.drawImage(currentImage, 0, 0, tmpImage.getWidth(), 
                            tmpImage.getHeight(), null);
                    currentImage = tmpImage;
                }
                return currentImage;
            } else {
                BufferedImage tmpImage = new BufferedImage(currentSize >> 1,
                        currentSize >> 1, BufferedImage.TYPE_INT_RGB);
                Graphics g = tmpImage.getGraphics();
                ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.drawImage(currentImage, 0, 0, tmpImage.getWidth(), 
                        tmpImage.getHeight(), null);
                currentImage = tmpImage;
                currentSize = currentImage.getWidth();
                delta = currentSize - endSize;
                nextPow2 = currentSize >> 1;
            }
        }
        return currentImage;
    }
    
    /**
     * Progressive Bilinear approach: this method gets each scaled version from
     * the getOptimalScalingImage method and copies it into place.
     */
    private void drawBetterImage(Graphics g, int yLoc) {
        int xLoc = 100;
        int delta = (int)(SCALE_FACTOR * FULL_SIZE);
        for (int scaledSize = FULL_SIZE; scaledSize > 0; scaledSize -= delta) {
            Image scaledImage = getOptimalScalingImage(originalImage, FULL_SIZE, scaledSize);
            g.drawImage(scaledImage, xLoc, yLoc + (FULL_SIZE - scaledSize)/2, 
                    null);
            xLoc += scaledSize + 20;
        }
    }
    
    /**
     * This approach uses either the getScaledInstance() approach to get
     * each new size or it scales on the fly using drawImage().
     */
    private void drawImage(Graphics g, int yLoc, boolean getScaled) {
        int xLoc = 100;
        int delta = (int)(SCALE_FACTOR * FULL_SIZE);
        if (getScaled) {
            for (int scaledSize = FULL_SIZE; scaledSize > 0; scaledSize -= delta) {
                Image scaledImage = originalImage.getScaledInstance(scaledSize,
                        scaledSize, Image.SCALE_AREA_AVERAGING);
                g.drawImage(scaledImage, xLoc, yLoc + (FULL_SIZE - scaledSize)/2, 
                        null);
                xLoc += scaledSize + 20;
            }
        } else {
            for (int scaledSize = FULL_SIZE; scaledSize > 0; scaledSize -= delta) {
                g.drawImage(originalImage, xLoc, yLoc + (FULL_SIZE - scaledSize)/2, 
                        scaledSize, scaledSize, null);
                xLoc += scaledSize + 20;
            }
        }
    }
    
    /**
     * Scale the image to several smaller sizes using each of the approaches
     * and time each series of operations. The times are output into the
     * application window for each row that they represent.
     */
    protected void paintComponent(Graphics g) {
        if (!originalImagePainted) {
            paintOriginalImage();
        }
        long startTime, endTime, totalTime;
        int xLoc, yLoc;
        
        // Draw scaled versions with nearest neighbor
        xLoc = 5;
        yLoc = 20;
        startTime = System.nanoTime();
        drawImage(g, yLoc, false);
        endTime = System.nanoTime();
        totalTime = (endTime - startTime) / 1000000;
        g.drawString("NEAREST ", xLoc, yLoc + (FULL_SIZE / 2));
        g.drawString(Long.toString(totalTime) + " ms", 
                xLoc, yLoc + (FULL_SIZE / 2) + 15);
        System.out.println("NEAREST: " + (endTime - startTime) / 1000000);

        // BILINEAR
        yLoc += FULL_SIZE + PADDING;
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        startTime = System.nanoTime();
        drawImage(g, yLoc, false);
        endTime = System.nanoTime();
        totalTime = (endTime - startTime) / 1000000;
        g.drawString("BILINEAR ", xLoc, yLoc + (FULL_SIZE / 2));
        g.drawString(Long.toString(totalTime) + " ms", 
                xLoc, yLoc + (FULL_SIZE / 2) + 15);
        System.out.println("BILINEAR: " + (endTime - startTime) / 1000000);

        // BIDUBIC
        yLoc += FULL_SIZE + PADDING;
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        startTime = System.nanoTime();
        drawImage(g, yLoc, false);
        endTime = System.nanoTime();
        totalTime = (endTime - startTime) / 1000000;
        g.drawString("BICUBIC ", xLoc, yLoc + (FULL_SIZE / 2));
        g.drawString(Long.toString(totalTime) + " ms", 
                xLoc, yLoc + (FULL_SIZE / 2) + 15);
        System.out.println("BICUBIC: " + (endTime - startTime) / 1000000);

        // getScaledInstance()
        yLoc += FULL_SIZE + PADDING;
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        startTime = System.nanoTime();
        drawImage(g, yLoc, true);
        endTime = System.nanoTime();
        totalTime = (endTime - startTime) / 1000000;
        g.drawString("getScaled ", xLoc, yLoc + (FULL_SIZE / 2));
        g.drawString(Long.toString(totalTime) + " ms", 
                xLoc, yLoc + (FULL_SIZE / 2) + 15);
        System.out.println("getScaled: " + (endTime - startTime) / 1000000);

        // Progressive Bilinear
        yLoc += FULL_SIZE + PADDING;
        startTime = System.nanoTime();
        drawBetterImage(g, yLoc);
        endTime = System.nanoTime();
        totalTime = (endTime - startTime) / 1000000;
        g.drawString("Progressive ", xLoc, yLoc + (FULL_SIZE / 2));
        g.drawString(Long.toString(totalTime) + " ms", 
                xLoc, yLoc + (FULL_SIZE / 2) + 15);
        System.out.println("faster: " + (endTime - startTime) / 1000000);

        // Draw image sizes
        xLoc = 100;
        int delta = (int)(SCALE_FACTOR * FULL_SIZE);
        for (int scaledSize = FULL_SIZE; scaledSize > 0; scaledSize -= delta) {
            g.drawString(scaledSize + " x " + scaledSize, 
                    xLoc + Math.max(0, scaledSize/2 - 20), 15);
            xLoc += scaledSize + 20;
        }
    }

    private static void createAndShowGUI() {
        JFrame f = new JFrame();
        f.setLayout(new BorderLayout());
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(900, 50 + (5 * FULL_SIZE) + (6 * PADDING));
        ScaleTest test = new ScaleTest();
        f.add(test);        
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
