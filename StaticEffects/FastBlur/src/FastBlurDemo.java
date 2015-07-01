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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * See {@link org.jdesktop.swingx.image.FastBlurFilter}.
 *
 * @author Romain Guy <romain.guy@mac.com>
 */
public class FastBlurDemo extends JFrame {
    private BlurTestPanel blurTestPanel;
    private JSlider radiusSlider;
    private JSlider iterationsSlider;

    public FastBlurDemo() {
        super("Fast Blur/Stack Blur");

        blurTestPanel = new BlurTestPanel();
        add(blurTestPanel);

        radiusSlider = new JSlider(0, 100, 0);
        radiusSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                blurTestPanel.setRadius(radiusSlider.getValue());
            }
        });

        iterationsSlider = new JSlider(1, 15, 1);
        iterationsSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                blurTestPanel.setIterations(iterationsSlider.getValue());
            }
        });

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(new JLabel("Radius: 0px"));
        controls.add(radiusSlider);
        controls.add(new JLabel("100px"));

        controls.add(new JLabel("Iterations: 1"));
        controls.add(iterationsSlider);
        controls.add(new JLabel("15"));

        add(controls, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static class BlurTestPanel extends JPanel {
        private BufferedImage image = null;
        private BufferedImage imageA;
        private int radius = 0;
        private StackBlurFilter blurFilter;
        private int iterations = 1;
        private boolean repaint = false;

        public BlurTestPanel() {
            try {
                imageA = GraphicsUtilities.loadCompatibleImage(getClass().getResource("images/A.jpg"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            blurFilter = new StackBlurFilter(radius, iterations);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(imageA.getWidth(), imageA.getHeight());
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (image == null) {
                image = new BufferedImage(imageA.getWidth(),
                                          imageA.getHeight(),
                                          BufferedImage.TYPE_INT_ARGB);
                repaint = true;
            }
            
            if (repaint) {
                Graphics2D g2 = image.createGraphics();
                g2.setComposite(AlphaComposite.Clear);
                g2.fillRect(0, 0, image.getWidth(), image.getHeight());
                
                g2.setComposite(AlphaComposite.Src);
                
                if (radius > 0) {
                    long start = System.nanoTime();
                    
                    g2.drawImage(imageA, blurFilter, 0, 0);
                    
                    long delay = System.nanoTime() - start;
                    System.out.println("time = " + (delay / 1000.0f / 1000.0f) + "ms");
                } else {
                    g2.drawImage(imageA, null, 0, 0);
                }
                g2.dispose();
                
                repaint = false;
            }

            int x = (getWidth() - image.getWidth()) / 2;
            int y = (getHeight() - image.getHeight()) / 2;
            g.drawImage(image, x, y, null);
        }

        public void setRadius(int radius) {
            this.radius = radius;
            this.blurFilter = new StackBlurFilter(radius, iterations);
            repaint = true;
            repaint();
        }

        public void setIterations(int iterations) {
            this.iterations = iterations;
            this.blurFilter = new StackBlurFilter(radius, iterations);
            repaint = true;
            repaint();
        }
    }

    public static void main(String... args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new FastBlurDemo().setVisible(true);
            }
        });
    }
}
