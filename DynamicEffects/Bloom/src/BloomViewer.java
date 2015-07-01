/*
 * Copyright (c) 2007, Romain Guy
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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.swing.JComponent;

/**
 * @author Romain Guy <romain.guy@mac.com>
 */
public class BloomViewer extends JComponent {
    private BrightPassFilter brightPassFilter = new BrightPassFilter();
    private BufferedImage image = null;
    private float smoothness = 4.0f;

    private BufferedImage bloom = null;

    public BloomViewer(String fileName) {
        try {
            image = GraphicsUtilities.loadCompatibleImage(getClass().getResource(fileName));
        }  catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(image.getWidth(), image.getHeight());
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (bloom == null) {
            BufferedImage result = image;

            if (smoothness > 1.0f) {
                result = GraphicsUtilities.createThumbnailFast(image, 
                            (int) (image.getWidth() / smoothness));
            }

            BufferedImage brightPass = brightPassFilter.filter(result, null);
            GaussianBlurFilter gaussianBlurFilter = new GaussianBlurFilter(5);

            bloom = GraphicsUtilities.createCompatibleImage(image);
            Graphics2D g2 = bloom.createGraphics();
            
            g2.drawImage(image, 0, 0, null);
            g2.setComposite(BlendComposite.Add);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            g2.drawImage(gaussianBlurFilter.filter(brightPass, null),
                    0, 0, image.getWidth(), image.getHeight(), null);
            
            for (int i = 0; i < 3; i++) {
                brightPass = GraphicsUtilities.createThumbnailFast(brightPass,
                            brightPass.getWidth() / 2);
                g2.drawImage(gaussianBlurFilter.filter(brightPass, null),
                        0, 0, image.getWidth(), image.getHeight(), null);
            }

            g2.dispose();
        }

        int x = (getWidth() - bloom.getWidth()) / 2;
        int y = (getHeight() - bloom.getHeight()) / 2;
        g.drawImage(bloom, x, y, null);
    }

    public void setThreshold(float threshold) {
        brightPassFilter = new BrightPassFilter(threshold);
        bloom = null;
        repaint();
    }

    public void setSmoothness(float smoothness) {
        this.smoothness = smoothness;
        bloom = null;
        repaint();
    }

    public void loadImage(File file) {
        try {
            this.image = GraphicsUtilities.loadCompatibleImage(file.toURI().toURL());
            bloom = null;
            repaint();
        }  catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
