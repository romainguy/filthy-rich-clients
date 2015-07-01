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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;
import java.awt.image.RescaleOp;
import java.awt.image.ShortLookupTable;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * <p>Demos of a custom buffered image operation.</p>
 * 
 * @author Romain Guy <romain.guy@mac.com>
 */
public class ApplicationFrame extends JFrame {
    private BufferedImage sourceImage;
    private ImagePanel imagePanel;
    
    private JSlider redSlider;
    private JSlider greenSlider;
    private JSlider blueSlider;
    private JSlider alphaSlider;
    
    public ApplicationFrame() {
        super("Custom Image Op Demo");
        
        loadSourceImage();
        buildContent();
        
        pack();
        
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
    
    public static void main(String... args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ApplicationFrame().setVisible(true);
            }
        });
    }

    private void buildContent() {
        buildImagePanel();
        buildControlsPanel();
    }

    private void loadSourceImage() {
        try {
            sourceImage = GraphicsUtilities.loadCompatibleImage(
                    getClass().getResource("./images/chess.jpg"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void buildImagePanel() {
        add(imagePanel = new ImagePanel());
    }
    
    private void buildControlsPanel() {
        JPanel controls = new JPanel(new GridBagLayout());
        
        // red component
        controls.add(new JLabel("Red: 0"), new GridBagConstraints(
                0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.LINE_END,
                GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        controls.add(redSlider = new JSlider(0, 255, 255), new GridBagConstraints(
                1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        controls.add(new JLabel("255"), new GridBagConstraints(
                2, 0, 1, 1, 1.0, 1.0, GridBagConstraints.LINE_START,
                GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        
        // green component
        controls.add(new JLabel("Green: 0"), new GridBagConstraints(
                0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.LINE_END,
                GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        controls.add(greenSlider = new JSlider(0, 255, 255), new GridBagConstraints(
                1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        controls.add(new JLabel("255"), new GridBagConstraints(
                2, 1, 1, 1, 1.0, 1.0, GridBagConstraints.LINE_START,
                GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        
        // blue component
        controls.add(new JLabel("Blue: 0"), new GridBagConstraints(
                0, 2, 1, 1, 1.0, 1.0, GridBagConstraints.LINE_END,
                GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        controls.add(blueSlider = new JSlider(0, 255, 255), new GridBagConstraints(
                1, 2, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        controls.add(new JLabel("255"), new GridBagConstraints(
                2, 2, 1, 1, 1.0, 1.0, GridBagConstraints.LINE_START,
                GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        
        // mix value
        controls.add(new JLabel("Mix: 0%"), new GridBagConstraints(
                0, 3, 1, 1, 1.0, 1.0, GridBagConstraints.LINE_END,
                GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        controls.add(alphaSlider = new JSlider(0, 100, 50), new GridBagConstraints(
                1, 3, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        controls.add(new JLabel("100%"), new GridBagConstraints(
                2, 3, 1, 1, 1.0, 1.0, GridBagConstraints.LINE_START,
                GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        
        // change listener
        ChangeListener colorChange = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                imagePanel.setColor(new Color(redSlider.getValue(),
                        greenSlider.getValue(), blueSlider.getValue()));
            }
        };
        redSlider.addChangeListener(colorChange);
        greenSlider.addChangeListener(colorChange);
        blueSlider.addChangeListener(colorChange);
        
        // alpha listener
        alphaSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                imagePanel.setMix((float) alphaSlider.getValue() / 100.0f);
            }
        });
        
        add(controls, BorderLayout.SOUTH);
    }
    
    private class ImagePanel extends JComponent {
        private ColorTintFilter op = new ColorTintFilter(Color.WHITE, 0.5f);
        private BufferedImage cache;
        private boolean damaged;
        
        private ImagePanel() {
            cache = GraphicsUtilities.createCompatibleImage(sourceImage);
            damaged = true;
        }
        
        public void setColor(Color color) {
            op = new ColorTintFilter(color, op.getMixValue());
            damaged = true;
            repaint();
        }
        
        public void setMix(float mix) {
            op = new ColorTintFilter(op.getMixColor(), mix);
            damaged = true;
            repaint();
        }
        
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(sourceImage.getWidth(), sourceImage.getHeight());
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            
            if (damaged) {
                op.filter(sourceImage, cache);
            }
            
            int x = (getWidth() - cache.getWidth()) / 2;
            int y = (getHeight() - cache.getHeight()) / 2;
            g2.drawImage(cache, x, y, null);
        }
    }
}
