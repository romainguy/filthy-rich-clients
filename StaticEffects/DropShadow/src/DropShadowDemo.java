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
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.IOException;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author Romain Guy <romain.guy@mac.com>
 */
public class DropShadowDemo extends JFrame {
    private BlurTestPanel blurTestPanel;
    private JSlider shadowSizeSlider;
    private JSlider shadowOpacitySlider;
    private JCheckBox fastRenderingCheck;

    public DropShadowDemo() {
        super("Drop Shadow");

        blurTestPanel = new BlurTestPanel();
        add(blurTestPanel);

        shadowSizeSlider = new JSlider(1, 20, 5);
        shadowSizeSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                blurTestPanel.setShadowSize(shadowSizeSlider.getValue());
            }
        });
        
        shadowOpacitySlider = new JSlider(0, 100, 50);
        shadowOpacitySlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                blurTestPanel.setShadowOpacity((float) shadowOpacitySlider.getValue() / 100.0f);
            }
        });
        
        fastRenderingCheck = new JCheckBox("Fast rendering");
        fastRenderingCheck.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                blurTestPanel.setFastRendering(fastRenderingCheck.isSelected());
            }
        });
        
        JPanel metaControls = new JPanel(new GridLayout(3, 1));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(new JLabel("Size: 1px"));
        controls.add(shadowSizeSlider);
        controls.add(new JLabel("20px"));
        metaControls.add(controls);
        
        controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(new JLabel("Opacity: 0%"));
        controls.add(shadowOpacitySlider);
        controls.add(new JLabel("100%"));
        metaControls.add(controls);
        
        controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(fastRenderingCheck);
        metaControls.add(controls);

        add(metaControls, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static class BlurTestPanel extends JPanel {
        private BufferedImage image = null;
        private BufferedImage imageA;
        private int shadowSize = 5;
        private boolean fastRendering = false;
        private float shadowOpacity = 0.5f;

        public BlurTestPanel() {
            try {
                imageA = GraphicsUtilities.loadCompatibleImage(getClass().getResource("subject.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(imageA.getWidth(), imageA.getHeight());
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (image == null) {
                long start = System.nanoTime();

                if (!fastRendering) {
                    image = createDropShadow(imageA, shadowSize);
                } else {
                    ShadowRenderer renderer = new ShadowRenderer(shadowSize / 2, 1.0f, Color.BLACK);
                    image = renderer.createShadow(imageA);
                }
                
                long delay = System.nanoTime() - start;
                System.out.println("time = " + (delay / 1000.0f / 1000.0f) + "ms");
            }

            int x = (getWidth() - imageA.getWidth()) / 2;
            int y = (getHeight() - imageA.getHeight()) / 2;
            
            Graphics2D g2 = (Graphics2D) g;
            Composite c = g2.getComposite();
            g2.setComposite(AlphaComposite.SrcOver.derive(shadowOpacity));
            
            if (!fastRendering) {
                g.drawImage(image, x - shadowSize * 2 + 5, y - shadowSize * 2 + 5, null);
            } else {
                g.drawImage(image, x - shadowSize / 2 + 5, y - shadowSize / 2 + 5, null);
            }
            
            g2.setComposite(c);
            
            g.drawImage(imageA, x, y, null);
        }

        public void setShadowSize(int radius) {
            this.shadowSize = radius;
            image = null;
            repaint();
        }

        private void setFastRendering(boolean fastRendering) {
            this.fastRendering = fastRendering;
            image = null;
            repaint();
        }

        private void setShadowOpacity(float shadowOpacity) {
            this.shadowOpacity = shadowOpacity;
            image = null;
            repaint();
        }
    }
    
    public static BufferedImage createDropShadow(BufferedImage image,
            int size) {
        BufferedImage shadow = new BufferedImage(
            image.getWidth() + 4 * size,
            image.getHeight() + 4 * size,
            BufferedImage.TYPE_INT_ARGB);
        
        Graphics2D g2 = shadow.createGraphics();
        g2.drawImage(image, size * 2, size * 2, null);
        
        g2.setComposite(AlphaComposite.SrcIn);
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, shadow.getWidth(), shadow.getHeight());       
        
        g2.dispose();
        
        shadow = getGaussianBlurFilter(size, true).filter(shadow, null);
        shadow = getGaussianBlurFilter(size, false).filter(shadow, null);
        
        return shadow;
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
    
    public static void main(String... args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new DropShadowDemo().setVisible(true);
            }
        });
    }
}
