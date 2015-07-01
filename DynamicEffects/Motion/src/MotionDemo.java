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
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.interpolation.PropertySetter;
import org.jdesktop.animation.timing.triggers.ActionTrigger;
/**
 *
 * @author Romain Guy <romain.guy@mac.com>
 */
public class MotionDemo extends JFrame {
    private JButton rightLayoutButton;
    private JButton leftLayoutButton;
    
    private JButton saveButton;
    private JButton openButton;

    private JTextArea textArea;
    
    public MotionDemo() {
        super("Motion Demo");
        
        add(buildToolbar(), BorderLayout.NORTH);
        add(buildContentPane());
        
        configureAnimations();
        
        pack();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }
    
    private void configureAnimations() {
        Animator leftAnimator = new Animator(200);
        leftAnimator.setAcceleration(0.3f);
        leftAnimator.setDeceleration(0.2f);
        leftAnimator.addTarget(new PropertySetter(
                saveButton, "location", new Point(16, 16)));
        leftAnimator.addTarget(new PropertySetter(
                openButton, "location", new Point(16, openButton.getY())));
        leftAnimator.addTarget(new PropertySetter(
                textArea, "location",
                new Point(16 + saveButton.getWidth() + 6, 16)));
        
        ActionTrigger.addTrigger(leftLayoutButton, leftAnimator);
        
        Animator rightAnimator = new Animator(200);
        rightAnimator.setAcceleration(0.3f);
        rightAnimator.setDeceleration(0.2f);
        rightAnimator.addTarget(new PropertySetter(
                saveButton, "location", saveButton.getLocation()));
        rightAnimator.addTarget(new PropertySetter(
                openButton, "location", openButton.getLocation()));
        rightAnimator.addTarget(new PropertySetter(
                textArea, "location", textArea.getLocation()));
        
        ActionTrigger.addTrigger(rightLayoutButton, rightAnimator);
    }
    
    private JComponent buildContentPane() {
        JPanel panel = new JPanel(null);
        
        int x = 16;
        int y = 16;
        Dimension size;
        
        textArea = new JTextArea("Type your document here.", 12, 25);
        textArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        size = textArea.getPreferredSize();
        textArea.setBounds(x, y, size.width, size.height);
        panel.add(textArea);
        
        x += size.width + 6;

        saveButton = new JButton("Save...");
        size = saveButton.getPreferredSize();
        saveButton.setBounds(x, y, size.width, size.height);
        panel.add(saveButton);
        
        y += size.height + 4;
        
        openButton = new JButton("Open...");
        size = openButton.getPreferredSize();
        openButton.setBounds(x, y, size.width, size.height);
        panel.add(openButton);
        
        x += size.width + 16;
        y = textArea.getPreferredSize().height + 16 + 16;
        
        panel.setPreferredSize(new Dimension(x, y));
        
        return panel;
    }
    
    private JComponent buildToolbar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint paint =new GradientPaint(0.0f, 0.0f, new Color(0xF2F2F2),
                        0.0f, getHeight(), new Color(0xD7D7D7));
                g2.setPaint(paint);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        leftLayoutButton = new JButton("Left Layout");
        leftLayoutButton.setIcon(new ImageIcon(
                getClass().getResource("images/left-layout.png")));
        leftLayoutButton.setVerticalTextPosition(JButton.BOTTOM);
        leftLayoutButton.setHorizontalTextPosition(JButton.CENTER);
        leftLayoutButton.setOpaque(false);
        leftLayoutButton.setContentAreaFilled(false);
        leftLayoutButton.setBorderPainted(false);
        panel.add(leftLayoutButton);
        
        rightLayoutButton = new JButton("Right Layout");
        rightLayoutButton.setIcon(new ImageIcon(
                getClass().getResource("images/right-layout.png")));
        rightLayoutButton.setVerticalTextPosition(JButton.BOTTOM);
        rightLayoutButton.setHorizontalTextPosition(JButton.CENTER);
        rightLayoutButton.setOpaque(false);
        rightLayoutButton.setContentAreaFilled(false);
        rightLayoutButton.setBorderPainted(false);
        panel.add(rightLayoutButton);
        
        JPanel wrapper = new JPanel(new BorderLayout());
        JPanel blackPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        blackPanel.add(Box.createVerticalStrut(1));
        wrapper.add(blackPanel, BorderLayout.SOUTH);
        wrapper.add(panel);
        
        return wrapper;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MotionDemo().setVisible(true);
            }
        });
    }
}
