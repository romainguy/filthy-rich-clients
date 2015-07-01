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
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTargetAdapter;
import org.jdesktop.animation.timing.interpolation.KeyFrames;
import org.jdesktop.animation.timing.interpolation.KeyValues;
import org.jdesktop.animation.timing.interpolation.PropertySetter;

/**
 *
 * @author Romain Guy <romain.guy@mac.com>
 */
public class FadingDemo extends JFrame {
    private ImageViewer imageViewer;

    private JButton nextButton;
    private JButton previousButton;

    private HelpGlassPane glass;

    private JTextField titleField;

    public FadingDemo() {
        super("Fading Demo");
        
        add(buildTitle(), BorderLayout.NORTH);
        add(buildImageViewer(), BorderLayout.CENTER);
        add(buildControls(), BorderLayout.SOUTH);
        
        pack();
        
        setupGlassPane();
        
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
    }
    
    private JComponent buildTitle() {
        titleField = new JTextField("Suzhou");
        return titleField;
    }
    
    private void setupGlassPane() {
        glass = new HelpGlassPane();
        setGlassPane(glass);
        glass.setVisible(true);
    }
    
    public static void setTextAndAnimate(final JTextComponent textComponent,
            final String text) {
       Color c = textComponent.getForeground();

       KeyFrames keyFrames = new KeyFrames(KeyValues.create(
                   new Color(c.getRed(), c.getGreen(), c.getBlue(), 255),
                   new Color(c.getRed(), c.getGreen(), c.getBlue(), 0),
                   new Color(c.getRed(), c.getGreen(), c.getBlue(), 255)
               ));
       PropertySetter setter = new PropertySetter(textComponent, "foreground",
               keyFrames);

       Animator animator = new Animator(200, setter);
       animator.addTarget(new TimingTargetAdapter() {
           private boolean textSet = false;

           public void timingEvent(float fraction) {
               if (fraction >= 0.5f && !textSet) {
                   textComponent.setText(text);
                   textSet = true;
               }
           } 
       });
       animator.start();
    }
    
    private JComponent buildControls() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        
        panel.add(previousButton = new JButton("Previous"));
        previousButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                imageViewer.previous();
                setTextAndAnimate(titleField, "Suzhou");
            }
        });
        panel.add(nextButton = new JButton("Next"));
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                imageViewer.next();
                setTextAndAnimate(titleField, "Shanghai");
                
                if (glass.isVisible()) {
                    Animator animator = new Animator(200);
                    animator.addTarget(new PropertySetter(glass, "alpha", 0.0f));
                    animator.setAcceleration(0.2f);
                    animator.setDeceleration(0.4f);
                    animator.start();
                }
            }
        });
        
        return panel;
    }
    
    private JComponent buildImageViewer() {
        return imageViewer = new ImageViewer();
    }
    
    public class HelpGlassPane extends JComponent {
        private BufferedImage helpImage;
        private float alpha = 1.0f;
        
        private HelpGlassPane() {
            try {
                helpImage = GraphicsUtilities.loadCompatibleImage(
                        getClass().getResource("images/help.png"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Animator animator = new Animator(200);
                    animator.addTarget(new PropertySetter(
                            HelpGlassPane.this, "alpha", 0.0f));
                    animator.setAcceleration(0.2f);
                    animator.setDeceleration(0.4f);
                    animator.start();
                }
            });
        }
        
        public void setAlpha(float alpha) {
            this.alpha = alpha;
            if (alpha <= 0.01f) {
                setVisible(false);
            }
            repaint();
        }
        
        public float getAlpha() {
            return this.alpha;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            
            Point p = nextButton.getLocationOnScreen();
            
            p.x += nextButton.getWidth() / 2 - 16;
            p.y += nextButton.getHeight() / 2 - helpImage.getHeight() + 10;
            
            SwingUtilities.convertPointFromScreen(p, this);
            
            g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
            g2.drawImage(helpImage, p.x, p.y, null);
        }
    }
    
    public static class ImageViewer extends JComponent {
        private BufferedImage firstImage;
        private BufferedImage secondImage;
        
        private float alpha = 0.0f;
        
        private ImageViewer() {
            try {
                firstImage = GraphicsUtilities.loadCompatibleImage(
                    getClass().getResource("images/suzhou.jpg"));
                secondImage = GraphicsUtilities.loadCompatibleImage(
                    getClass().getResource("images/shanghai.jpg"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(firstImage.getWidth(), firstImage.getHeight());
        }
        
        public void next() {
            Animator animator = new Animator(1000);
            animator.addTarget(new PropertySetter(this, "alpha", 1.0f));
            animator.setAcceleration(0.2f);
            animator.setDeceleration(0.4f);
            animator.start();
        }
        
        public void previous() {
            Animator animator = new Animator(1000);
            animator.addTarget(new PropertySetter(this, "alpha", 0.0f));
            animator.setAcceleration(0.2f);
            animator.setDeceleration(0.4f);
            animator.start();
        }
        
        public void setAlpha(float alpha) {
            this.alpha = alpha;
            repaint();
        }
        
        public float getAlpha() {
            return this.alpha;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            
            g2.setComposite(AlphaComposite.SrcOver.derive(1.0f - alpha));
            g2.drawImage(firstImage, 0, 0, null);
            g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
            g2.drawImage(secondImage, 0, 0, null);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new FadingDemo().setVisible(true);
            }
        });
    }
}
