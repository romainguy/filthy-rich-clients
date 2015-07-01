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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

/**
 * @author Romain Guy <romain.guy@mac.com>
 */
public class BloomDemo extends JFrame {
    private BloomViewer viewer;
    private JScrollPane scroller;

    public BloomDemo() {
        super("Bloom Demo");

        add(buildBloomViewer());
        add(buildControls(), BorderLayout.SOUTH);

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        pack();
        setSize(640, 480);
        setLocationRelativeTo(null);
    }
    
    private JComponent buildControls() {
        JPanel controls = new JPanel(new GridLayout(3, 1));
        
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        panel.add(new JLabel("Bloom: 0.0"));
        JSlider slider;
        panel.add(slider = new JSlider(0, 300, 70));
        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                float threshold = ((JSlider) e.getSource()).getValue() / 100.0f;
                viewer.setThreshold(threshold);
            }
        });
        panel.add(new JLabel("3.0"));
        controls.add(panel);
        
        panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        panel.add(new JLabel("Smooth: 1"));
        panel.add(slider = new JSlider(10, 100, 40));
        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                float smoothness = ((JSlider) e.getSource()).getValue() / 10.0f;
                viewer.setSmoothness(smoothness);
            }
        });
        panel.add(new JLabel("10"));
        controls.add(panel);
        
        panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        JButton button;
        panel.add(button = new JButton("Open Image..."));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                if (chooser.showOpenDialog(BloomDemo.this) == JFileChooser.APPROVE_OPTION) {
                    viewer.loadImage(chooser.getSelectedFile());
                    scroller.revalidate();
                }
            }
        });
        controls.add(panel);
        
        return controls;
    }

    private JComponent buildBloomViewer() {
        viewer = new BloomViewer("/images/screen.png");
        scroller = new JScrollPane(viewer);
        scroller.setBorder(null);
        scroller.getViewport().setBackground(Color.BLACK);
        return scroller;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new BloomDemo().setVisible(true);
            }
        });
    }
}
