import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
/*
 * PictureScaler.java
 *
 * Created on May 1, 2007, 5:03 PM
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
public class PictureScaler extends JComponent {

    private static BufferedImage picture = null;
    private static final int PADDING = 10;
    private static final double SCALE_FACTOR = .05;
    private int scaleW, scaleH;
    
    /** Creates a new instance of PictureScaler */
    public PictureScaler() {
        try {
            URL url = getClass().getResource("images/BB.jpg");
            picture = ImageIO.read(url);
            scaleW = (int)(SCALE_FACTOR * picture.getWidth());
            scaleH = (int)(SCALE_FACTOR * picture.getHeight());
            System.out.println("w, h = " + picture.getWidth() + ", " + picture.getHeight());
            setPreferredSize(new Dimension(PADDING + (5 * (scaleW + PADDING)), 
                    scaleH + (4 * PADDING)));
        } catch (Exception e) {
            System.out.println("Problem reading image file: " + e);
            System.exit(0);
        }
    }

    /**
     * Convenience method that returns a scaled instance of the
     * provided BufferedImage.
     * 
     * 
     * @param img the original image to be scaled
     * @param targetWidth the desired width of the scaled instance,
     *    in pixels
     * @param targetHeight the desired height of the scaled instance,
     *    in pixels
     * @param hint one of the rendering hints that corresponds to
     *    RenderingHints.KEY_INTERPOLATION (e.g.
     *    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR,
     *    RenderingHints.VALUE_INTERPOLATION_BILINEAR,
     *    RenderingHints.VALUE_INTERPOLATION_BICUBIC)
     * @param progressiveBilinear if true, this method will use a multi-step
     *    scaling technique that provides higher quality than the usual
     *    one-step technique (only useful in down-scaling cases, where
     *    targetWidth or targetHeight is
     *    smaller than the original dimensions)
     * @return a scaled version of the original BufferedImage
     */
    public BufferedImage getFasterScaledInstance(BufferedImage img,
            int targetWidth, int targetHeight, Object hint,
            boolean progressiveBilinear)
    {
        int type = (img.getTransparency() == Transparency.OPAQUE) ?
            BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = img;
        BufferedImage scratchImage = null;
        Graphics2D g2 = null;
        int w, h;
        int prevW = ret.getWidth();
        int prevH = ret.getHeight();
        boolean isTranslucent = img.getTransparency() !=  Transparency.OPAQUE; 

        if (progressiveBilinear) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = img.getWidth();
            h = img.getHeight();
        } else {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;
        }
        
        do {
            if (progressiveBilinear && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }

            if (progressiveBilinear && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }

            if (scratchImage == null || isTranslucent) {
                // Use a single scratch buffer for all iterations
                // and then copy to the final, correctly-sized image
                // before returning
                scratchImage = new BufferedImage(w, h, type);
                g2 = scratchImage.createGraphics();
            }
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, 0, 0, prevW, prevH, null);
            prevW = w;
            prevH = h;

            ret = scratchImage;
        } while (w != targetWidth || h != targetHeight);
        
        if (g2 != null) {
            g2.dispose();
        }

        // If we used a scratch buffer that is larger than our target size,
        // create an image of the right size and copy the results into it
        if (targetWidth != ret.getWidth() || targetHeight != ret.getHeight()) {
            scratchImage = new BufferedImage(targetWidth, targetHeight, type);
            g2 = scratchImage.createGraphics();
            g2.drawImage(ret, 0, 0, null);
            g2.dispose();
            ret = scratchImage;
        }
        
        return ret;
    }
    
    /**
     * Render all scaled versions 10 times, timing each version and 
     * reporting the results below the appropriate scaled image.
     */
    protected void paintComponent(Graphics g) {
        // Scale with NEAREST_NEIGHBOR
        int xLoc = PADDING, yLoc = PADDING;
        long startTime, endTime;
        float totalTime;
        int iterations = 10;
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        startTime = System.nanoTime();
        for (int i = 0; i < iterations; ++i) {
            g.drawImage(picture, xLoc, yLoc, scaleW, scaleH, null);
        }
        endTime = System.nanoTime();
        totalTime = (float)((endTime - startTime) / 1000000) / iterations;
        g.drawString("NEAREST ", xLoc, yLoc + scaleH + PADDING);
        g.drawString(Float.toString(totalTime) + " ms", 
                xLoc, yLoc + scaleH + PADDING + 10);
        System.out.println("NEAREST: " + ((endTime - startTime) / 1000000));
        
        // Scale with BILINEAR
        xLoc += scaleW + PADDING;
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        startTime = System.nanoTime();
        for (int i = 0; i < iterations; ++i) {
            g.drawImage(picture, xLoc, yLoc, scaleW, scaleH, null);
        }
        endTime = System.nanoTime();
        totalTime = (float)((endTime - startTime) / 1000000) / iterations;
        g.drawString("BILINEAR", xLoc, yLoc + scaleH + PADDING);
        g.drawString(Float.toString(totalTime) + " ms", 
                xLoc, yLoc + scaleH + PADDING + 10);
        System.out.println("BILINEAR: " + ((endTime - startTime) / 1000000));

        // Scale with BICUBIC
        xLoc += scaleW + PADDING;
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        startTime = System.nanoTime();
        for (int i = 0; i < iterations; ++i) {
            g.drawImage(picture, xLoc, yLoc, scaleW, scaleH, null);
        }
        endTime = System.nanoTime();
        totalTime = (float)((endTime - startTime) / 1000000) / iterations;
        g.drawString("BICUBIC", xLoc, yLoc + scaleH + PADDING);
        g.drawString(Float.toString(totalTime) + " ms", 
                xLoc, yLoc + scaleH + PADDING + 10);
        System.out.println("BICUBIC: " + ((endTime - startTime) / 1000000));

        // Scale with getScaledInstance
        xLoc += scaleW + PADDING;
        startTime = System.nanoTime();
        for (int i = 0; i < iterations; ++i) {
            Image scaledPicture = picture.getScaledInstance(scaleW, scaleH, 
                    Image.SCALE_AREA_AVERAGING);
            g.drawImage(scaledPicture, xLoc, yLoc, null);
        }
        endTime = System.nanoTime();
        totalTime = (float)((endTime - startTime) / 1000000) / iterations;
        g.drawString("getScaled", xLoc, yLoc + scaleH + PADDING);
        g.drawString(Float.toString(totalTime) + " ms", 
                xLoc, yLoc + scaleH + PADDING + 10);
        System.out.println("getScaled: " + ((endTime - startTime) / 1000000));
        
        // Scale with Progressive Bilinear
        xLoc += scaleW + PADDING;
        startTime = System.nanoTime();
        for (int i = 0; i < iterations; ++i) {
            Image scaledPicture = getFasterScaledInstance(picture, scaleW, scaleH, 
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
            g.drawImage(scaledPicture, xLoc, yLoc, null);
        }
        endTime = System.nanoTime();
        totalTime = (float)((endTime - startTime) / 1000000) / iterations;
        g.drawString("Progressive", xLoc, yLoc + scaleH + PADDING);
        g.drawString(Float.toString(totalTime) + " ms", 
                xLoc, yLoc + scaleH + PADDING + 10);
        System.out.println("Progressive: " + ((endTime - startTime) / 1000000));
    }
    
    private static void createAndShowGUI() {
        JFrame f = new JFrame();
        f.setLayout(new BorderLayout());
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        PictureScaler test = new PictureScaler();
        //f.setSize(scaleW + (4 * PADDING), scaleH + (4 * PADDING));
        f.add(test);        
        f.validate();
        f.pack();
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
