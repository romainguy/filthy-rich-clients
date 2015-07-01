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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author Romain Guy <romain.guy@mac.com>
 */
public class ReflectionDemo extends JFrame {
    private ReflectionPanel reflectionPanel;
    private JSlider opacitySlider;
    private JSlider lengthSlider;
    private JSlider radiusSlider;

    public ReflectionDemo() {
        super("Reflections");

        setContentPane(new GradientPanel());

        reflectionPanel = new ReflectionPanel();
        add(reflectionPanel);

        opacitySlider = new JSlider(0, 100, 35);
        opacitySlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                reflectionPanel.setOpacity(opacitySlider.getValue() / 100.0f);
            }
        });

        lengthSlider = new JSlider(0, 100, 40);
        lengthSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                reflectionPanel.setLength(lengthSlider.getValue() / 100.0f);
            }
        });

        radiusSlider = new JSlider(1, 20, 1);
        radiusSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                reflectionPanel.setRadius(radiusSlider.getValue());
            }
        });

        JPanel controls = new JPanel(new GridBagLayout());
        JLabel label;
        controls.setOpaque(false);
        controls.add(label = new JLabel("Opacity: 0%"),
                     new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0,
                                            GridBagConstraints.LINE_START,
                                            GridBagConstraints.NONE,
                                            new Insets(0, 6, 0, 0),
                                            0, 0));
        label.setForeground(Color.WHITE);
        controls.add(opacitySlider,
                     new GridBagConstraints(1, 0, 1, 1, 0.0, 1.0,
                                            GridBagConstraints.LINE_START,
                                            GridBagConstraints.NONE,
                                            new Insets(0, 0, 0, 0),
                                            0, 0));
        controls.add(label = new JLabel("100%"),
                     new GridBagConstraints(2, 0, 1, 1, 0.0, 1.0,
                                            GridBagConstraints.LINE_START,
                                            GridBagConstraints.NONE,
                                            new Insets(0, 0, 0, 0),
                                            0, 0));
        label.setForeground(Color.WHITE);

        controls.add(label = new JLabel("Length: 0%"),
                     new GridBagConstraints(0, 1, 1, 1, 0.0, 1.0,
                                            GridBagConstraints.LINE_START,
                                            GridBagConstraints.NONE,
                                            new Insets(0, 6, 0, 0),
                                            0, 0));
        label.setForeground(Color.WHITE);
        controls.add(lengthSlider,
                     new GridBagConstraints(1, 1, 1, 1, 0.0, 1.0,
                                            GridBagConstraints.LINE_START,
                                            GridBagConstraints.NONE,
                                            new Insets(0, 0, 0, 0),
                                            0, 0));
        controls.add(label = new JLabel("100%"),
                     new GridBagConstraints(2, 1, 1, 1, 0.0, 1.0,
                                            GridBagConstraints.LINE_START,
                                            GridBagConstraints.NONE,
                                            new Insets(0, 0, 0, 0),
                                            0, 0));
        label.setForeground(Color.WHITE);

        controls.add(label = new JLabel("Blur Radius: 1px"),
                     new GridBagConstraints(0, 2, 1, 1, 0.0, 1.0,
                                            GridBagConstraints.LINE_START,
                                            GridBagConstraints.NONE,
                                            new Insets(0, 6, 0, 0),
                                            0, 0));
        label.setForeground(Color.WHITE);
        controls.add(radiusSlider,
                     new GridBagConstraints(1, 2, 1, 1, 0.0, 1.0,
                                            GridBagConstraints.LINE_START,
                                            GridBagConstraints.NONE,
                                            new Insets(0, 0, 0, 0),
                                            0, 0));
        controls.add(label = new JLabel("20px"),
                     new GridBagConstraints(2, 2, 1, 1, 0.0, 1.0,
                                            GridBagConstraints.LINE_START,
                                            GridBagConstraints.NONE,
                                            new Insets(0, 0, 0, 0),
                                            0, 0));
        label.setForeground(Color.WHITE);

        JCheckBox blurCheckBox = new JCheckBox("Blur Enabled");
        blurCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                reflectionPanel.setBlurEnabled(
                        ((JCheckBox) changeEvent.getSource()).isSelected());
            }
        });
        blurCheckBox.setOpaque(false);
        blurCheckBox.setForeground(Color.WHITE);
        controls.add(blurCheckBox,
                     new GridBagConstraints(0, 3, 1, 1, 0.0, 1.0,
                                            GridBagConstraints.LINE_START,
                                            GridBagConstraints.NONE,
                                            new Insets(0, 0, 6, 0),
                                            0, 0));

        //add(controls, BorderLayout.SOUTH);
        reflectionPanel.setLayout(new BorderLayout());
        reflectionPanel.add(controls, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static class ReflectionPanel extends JPanel {
        private BufferedImage image = null;
        private BufferedImage imageA;
        private ReflectionRenderer renderer = new ReflectionRenderer();

        public ReflectionPanel() {
            try {
                imageA = GraphicsUtilities.loadCompatibleImage(getClass().getResource("images/deathvalley.jpg"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            image = renderer.createReflection(imageA);
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(640, 520);
        }

        @Override
        protected void paintComponent(Graphics g) {
            int x = (getWidth() - imageA.getWidth()) / 2;
            int y = 24;
            if (renderer.isBlurEnabled()) {
                x -= renderer.getEffectiveBlurRadius();
                y -= renderer.getEffectiveBlurRadius() + 1;
            }
            g.drawImage(image, x, y + imageA.getHeight(), null);
            if (renderer.isBlurEnabled()) {
                x += renderer.getEffectiveBlurRadius();
                y += renderer.getEffectiveBlurRadius() + 1;
            }
            g.drawImage(imageA, x, y, null);
        }

        public void setOpacity(float opacity) {
            renderer.setOpacity(opacity);
            image = renderer.createReflection(imageA);
            repaint();
        }

        public void setLength(float length) {
            renderer.setLength(length);
            image = renderer.createReflection(imageA);
            repaint();
        }

        public void setBlurEnabled(boolean selected) {
            renderer.setBlurEnabled(selected);
            image = renderer.createReflection(imageA);
            repaint();
        }

        public void setRadius(int radius) {
            renderer.setBlurRadius(radius);
            image = renderer.createReflection(imageA);
            repaint();
        }
    }

    private static class GradientPanel extends JPanel {
        GradientPanel() {
            super(new BorderLayout());
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            Rectangle clip = g2.getClipBounds();
            Paint paint = g2.getPaint();
            g2.setPaint(new GradientPaint(0.0f, getHeight() * 0.22f,
                                          new Color(0x202737),
                                          0.0f, getHeight() * 0.7f,
                                          Color.BLACK));
            g2.fillRect(clip.x, clip.y, clip.width, clip.height);
            g2.setPaint(paint);
        }
    }

    public static void main(String... args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ReflectionDemo().setVisible(true);
            }
        });
    }
}
