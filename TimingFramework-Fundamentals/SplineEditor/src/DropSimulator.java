/**
 * Copyright (c) 2006, Sun Microsystems, Inc
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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;


public class DropSimulator extends AbstractSimulator {
    private static final Color COLOR_BACKGROUND = Color.WHITE;

    private BufferedImage image;
    private BufferedImage shadow;
    
    private float angle = 90;
    private int distance = 20;

    // cached values for fast painting
    private int distance_x = 0;
    private int distance_y = 0;
    
    public DropSimulator() {
        try {
            image = ImageIO.read(BouncerSimulator.class.getResource("images/icon.png"));
            ShadowFactory factory = new ShadowFactory(5, 0.5f, Color.BLACK);
            shadow = factory.createShadow(image);
        } catch (Exception e) { }
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!isVisible()) {
            return;
        }
        
        Graphics2D g2 = (Graphics2D) g;

        setupGraphics(g2);
        drawBackground(g2);
        drawItem(g2);
    }

    private void drawItem(Graphics2D g2) {
        double position = time;

        int width = (int) (shadow.getWidth() / 2 * (1.0 + position));
        int height = (int) (shadow.getHeight() / 2 * (1.0 + position));
        int x = (getWidth() - width) / 2;
        int y = (getHeight() - height) / 2;

        Composite composite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                                                   1.0f - (0.5f * (float) position)));

        computeShadowPosition((position * distance) + 1.0);
        g2.drawImage(shadow, x + distance_x, y + distance_y, width, height, null);
        
        g2.setComposite(composite);
        
        width = (int) (image.getWidth() / 2 * (1.0 + position));
        height = (int) (image.getHeight() / 2 * (1.0 + position));
        x = (getWidth() - width) / 2;
        y = (getHeight() - height) / 2;

        g2.drawImage(image, x, y, width, height, null);
    }

    private void setupGraphics(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }

    private void drawBackground(Graphics2D g2) {
        g2.setColor(COLOR_BACKGROUND);
        g2.fill(g2.getClipBounds());
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(150, 100);
    }

    private void computeShadowPosition(double distance) {
        double angleRadians = Math.toRadians(angle);
        distance_x = (int) (Math.cos(angleRadians) * distance);
        distance_y = (int) (Math.sin(angleRadians) * distance);
    }
}
