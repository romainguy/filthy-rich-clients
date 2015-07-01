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

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.interpolation.PropertySetter;

/**
 *
 * @author Romain Guy <romain.guy@mac.com>
 */
public class PulseDemo extends JFrame {
    
    public PulseDemo() {
        super("Pulse Demo");
        
        setContentPane(buildBlackPanel());
        add(buildPulsatingText());
        
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        setSize(320, 280);
        setLocationRelativeTo(null);
    }
    
    private JComponent buildBlackPanel() {
        return new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                
                Rectangle clip = g2.getClipBounds();
                g2.setPaint(new GradientPaint(0.0f, 0.0f, new Color(0x666f7f).darker(),
                        0.0f, getHeight(), new Color(0x262d3d).darker()));
                
                g2.fillRect(clip.x, clip.y, clip.width, clip.height);
            }
        };
    }
    
    private JComponent buildPulsatingText() {
        return new PulsatingLogo("images/network-wireless.png");
    }
    
    public static class PulsatingLogo extends JComponent {
        private BufferedImage image;
        private BufferedImage glow;
        private float alpha = 0.0f;
        
        public PulsatingLogo(String imageName) {
            try {
                image = GraphicsUtilities.loadCompatibleImage(
                        getClass().getResource(imageName));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(image.getWidth(), image.getHeight());
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2;
            
            if (glow == null) {
                glow = GraphicsUtilities.createCompatibleImage(image);
                g2 = glow.createGraphics();
                g2.drawImage(image, 0, 0, null);
                g2.dispose();
                
                BufferedImageOp filter = getGaussianBlurFilter(24, true);
                glow = filter.filter(glow, null);
                filter = getGaussianBlurFilter(24, false);
                glow = filter.filter(glow, null);
                filter = new ColorTintFilter(Color.WHITE, 1.0f);
                glow = filter.filter(glow, null);
                
                startAnimator();
            }
            
            int x = (getWidth() - image.getWidth()) / 2;
            int y = (getHeight() - image.getHeight()) / 2;
            
            g2 = (Graphics2D) g.create();

            g2.setComposite(AlphaComposite.SrcOver.derive(getAlpha()));
            g2.drawImage(glow, x, y, null);
            g2.setComposite(AlphaComposite.SrcOver);
            g2.drawImage(image, x, y, null);
        }
        
        private void startAnimator() {
            PropertySetter setter = new PropertySetter(this, "alpha", 0.0f, 1.0f);
            Animator animator = new Animator(600, Animator.INFINITE,
                    Animator.RepeatBehavior.REVERSE, setter);
            animator.start();
        }
        
        public float getAlpha() {
            return alpha;
        }

        public void setAlpha(float alpha) {
            this.alpha = alpha;
            repaint();
        }
        
        public static ConvolveOp getGaussianBlurFilter(int radius,
                boolean horizontal) {
            if (radius < 1) {
                throw new IllegalArgumentException("Radius must be >= 1");
            }

            int size = radius * 2 + 1;
            float[] data = new float[size];

            float sigma = radius / 3.0f;
            float twoSigmaSquare = 2.0f * sigma * sigma;
            float sigmaRoot = (float) Math.sqrt(twoSigmaSquare * Math.PI);
            float total = 0.0f;

            for (int i = -radius; i <= radius; i++) {
                float distance = i * i;
                int index = i + radius;
                data[index] = (float) Math.exp(-distance / twoSigmaSquare) / sigmaRoot;
                total += data[index];
            }

            for (int i = 0; i < data.length; i++) {
                data[i] /= total;
            }        

            Kernel kernel = null;
            if (horizontal) {
                kernel = new Kernel(size, 1, data);
            } else {
                kernel = new Kernel(1, size, data);
            }
            return new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new PulseDemo().setVisible(true);
            }
        });
    }
}
