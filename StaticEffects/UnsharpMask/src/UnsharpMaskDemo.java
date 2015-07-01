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
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
/**
 *
 * @author Romain Guy <romain.guy@mac.com>
 */
public class UnsharpMaskDemo extends JFrame {
    private BufferedImage image;
    private JLabel viewer;
    
    private float amount;
    private int radius;
    private int threshold;

    private JPanel viewerPanel;
    private CardLayout carder;

    private JLabel amountLabel;
    private JLabel radiusLabel;
    private JLabel thresholdLabel;
    
    public UnsharpMaskDemo() {
        super("Unsharp Mask Demo");
        
        UnsharpMaskFilter filter = new UnsharpMaskFilter();
        this.amount = filter.getAmount();
        this.radius = filter.getRadius();
        this.threshold = filter.getThreshold();
        
        loadImage();
        add(buildInstructions(), BorderLayout.NORTH);
        add(buildControls(), BorderLayout.SOUTH);
        add(buildViewer());
        
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }
    
    private JComponent buildInstructions() {
        JLabel label = new JLabel("Click the image to see the original.");
        label.setBorder(new EmptyBorder(3, 3, 3, 3));
        return label;
    }
    
    private JComponent buildViewer() {
        viewerPanel = new JPanel(carder = new CardLayout());
        viewer = new JLabel(new ImageIcon(image));
        viewer.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                carder.show(viewerPanel, "sharp");
            }

            public void mousePressed(MouseEvent e) {
                carder.show(viewerPanel, "original");
            }
        });
        updateViewer();
        
        viewerPanel.add(viewer, "sharp");
        viewerPanel.add(new JLabel(new ImageIcon(image)), "original");
        
        return viewerPanel;
    }
    
    private JComponent buildControls() {
        JPanel panel = new JPanel(new GridBagLayout());
        JSlider slider;
        
        panel.add(amountLabel = new JLabel("Amount: "),
                new GridBagConstraints(0, 0, 1, 1,
                    1.0, 1.0, GridBagConstraints.LINE_START,
                    GridBagConstraints.NONE, new Insets(3, 3, 0, 3), 0, 0));
        panel.add(slider = new JSlider(0, 500, (int) (amount * 100.0f)),
                new GridBagConstraints(0, 1, 1, 1,
                    1.0, 1.0, GridBagConstraints.LINE_START,
                    GridBagConstraints.NONE, new Insets(0, 3, 0, 3), 0, 0));
        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider slider = (JSlider) e.getSource();
                float amount = slider.getValue() / 100.0f;
                setAmount(amount);
            }
        });
        
        panel.add(radiusLabel = new JLabel("Radius: "),
                new GridBagConstraints(0, 2, 1, 1,
                    1.0, 1.0, GridBagConstraints.LINE_START,
                    GridBagConstraints.NONE, new Insets(3, 3, 0, 3), 0, 0));
        panel.add(slider = new JSlider(1, 50, radius),
                new GridBagConstraints(0, 3, 1, 1,
                    1.0, 1.0, GridBagConstraints.LINE_START,
                    GridBagConstraints.NONE, new Insets(0, 3, 0, 3), 0, 0));
        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider slider = (JSlider) e.getSource();
                setRadius(slider.getValue());
            }
        });
        
        panel.add(thresholdLabel = new JLabel("Threshold: "),
                new GridBagConstraints(0, 4, 1, 1,
                    1.0, 1.0, GridBagConstraints.LINE_START,
                    GridBagConstraints.NONE, new Insets(3, 3, 0, 3), 0, 0));
        panel.add(slider = new JSlider(0, 255, threshold),
                new GridBagConstraints(0, 5, 1, 1,
                    1.0, 1.0, GridBagConstraints.LINE_START,
                    GridBagConstraints.NONE, new Insets(0, 3, 3, 3), 0, 0));
         slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider slider = (JSlider) e.getSource();
                setThreshold(slider.getValue());
            }
        });
        
        return panel;
    }
    
    private void setAmount(float amount) {
        this.amount = amount;
        updateViewer();
    }
    
    private void setRadius(int radius) {
        this.radius = radius;
        updateViewer();
    }
    
    private void setThreshold(int threshold) {
        this.threshold = threshold;
        updateViewer();
    }
    
    private void updateViewer() {
        amountLabel.setText("Amount: " + (int) (amount * 100.0f) + "%");
        radiusLabel.setText("Radius: " + radius  + " pixels");
        thresholdLabel.setText("Threshold: " + threshold + " levels");
        
        UnsharpMaskFilter filter = new UnsharpMaskFilter(amount, radius, threshold);
        viewer.setIcon(new ImageIcon(filter.filter(image, null)));
    }
    
    private void loadImage() {
        try {
            this.image = GraphicsUtilities.loadCompatibleImage(getClass().
                    getResource("/images/scene.jpg"));
            this.image = GraphicsUtilities.createThumbnail(this.image, 300);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new UnsharpMaskDemo().setVisible(true);
            } 
        });
    }
}
