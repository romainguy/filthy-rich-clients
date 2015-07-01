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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.interpolation.PropertySetter;
import org.jdesktop.animation.timing.triggers.MouseTrigger;
import org.jdesktop.animation.timing.triggers.MouseTriggerEvent;
/**
 *
 * @author Romain Guy <romain.guy@mac.com>
 */
public class MorphingDemo extends JFrame {
    private ImageViewer imageViewer;

    public MorphingDemo() {
        super("Morphing Demo");
        
        add(buildImageViewer());
        add(buildControls(), BorderLayout.SOUTH);
        
        pack();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }
        
    private JComponent buildImageViewer() {
        return imageViewer = new ImageViewer();
    }
    
    private JComponent buildControls() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        
        JButton button;
        panel.add(button = new DirectionButton("Backward",
                DirectionButton.Direction.LEFT));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                imageViewer.previous();
            }
        });
        
        panel.add(button = new DirectionButton("Forward",
                DirectionButton.Direction.RIGHT));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                imageViewer.next();
            }
        });
        
        return panel;
    }
    
    public static class DirectionButton extends JButton {
        public enum Direction {
            LEFT,
            RIGHT
        };
        private DirectionButton.Direction direction;
        private Map desktopHints;
        private float morphing = 0.0f;
        
        private DirectionButton(String text, Direction direction) {
            super(text);
            this.direction = direction;
            
            setupTriggers();
            setFont(getFont().deriveFont(Font.BOLD));
            setOpaque(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
        }
        
        private void setupTriggers() {
            Animator animator = PropertySetter.createAnimator(
                    150, this, "morphing", 0.0f, 1.0f);
            animator.setAcceleration(0.2f);
            animator.setDeceleration(0.3f);
            MouseTrigger.addTrigger(this, animator, MouseTriggerEvent.ENTER, true);
        }
        
        private Morphing2D createMorph() {
            Shape sourceShape = new RoundRectangle2D.Double(2.0, 2.0,
                    getWidth() - 4.0, getHeight() - 4.0, 12.0, 12.0);
            
            GeneralPath.Double destinationShape = new GeneralPath.Double();
            destinationShape.moveTo(2.0, getHeight() / 2.0);
            destinationShape.lineTo(22.0, 0.0);
            destinationShape.lineTo(22.0, 5.0);
            destinationShape.lineTo(getWidth() - 2.0, 5.0);
            destinationShape.lineTo(getWidth() - 2.0, getHeight() - 5.0);
            destinationShape.lineTo(22.0, getHeight() - 5.0);
            destinationShape.lineTo(22.0, getHeight());
            destinationShape.closePath();
            
            return new Morphing2D(sourceShape, destinationShape);
        }
        
        public float getMorphing() {
            return morphing;
        }

        public void setMorphing(float morphing) {
            this.morphing = morphing;
            repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (desktopHints == null) {
                Toolkit tk = Toolkit.getDefaultToolkit();
                desktopHints = (Map) (tk.getDesktopProperty("awt.font.desktophints"));
            }

            if (desktopHints != null) {
                g2.addRenderingHints(desktopHints);
            }
            
            LinearGradientPaint p;
            Color[] colors;
            if (!getModel().isArmed()) {
                colors = new Color[] {
                    new Color(0x63a5f7),
                    new Color(0x3799f4),
                    new Color(0x2d7eeb),
                    new Color(0x30a5f9) };
            } else {
                colors = new Color[] {
                    new Color(0x63a5f7).darker(),
                    new Color(0x3799f4).darker(),
                    new Color(0x2d7eeb).darker(),
                    new Color(0x30a5f9).darker() };
            }
            
            p = new LinearGradientPaint(0.0f, 0.0f, 0.0f, getHeight(),
                new float[] { 0.0f, 0.5f, 0.501f, 1.0f },
                colors);
            
            g2.setPaint(p);
            
            Morphing2D morph = createMorph();
            morph.setMorphing(getMorphing());
            if (direction == Direction.RIGHT) {
                g2.translate(getWidth(), 0.0);
                g2.scale(-1.0, 1.0);
            }
            g2.fill(morph);
            if (direction == Direction.RIGHT) {
                g2.scale(-1.0, 1.0);
                g2.translate(-getWidth(), 0.0);
            }
            
            int width = g2.getFontMetrics().stringWidth(getText());
            
            int x = (getWidth() - width) / 2;
            int y = getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 1;

            g2.setColor(Color.BLACK);
            g2.drawString(getText(), x, y + 1);
            g2.setColor(Color.WHITE);
            g2.drawString(getText(), x, y);
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
            Animator animator = new Animator(500);
            animator.addTarget(new PropertySetter(this, "alpha", 1.0f));
            animator.setAcceleration(0.2f);
            animator.setDeceleration(0.4f);
            animator.start();
        }
        
        public void previous() {
            Animator animator = new Animator(500);
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
                new MorphingDemo().setVisible(true);
            }
        });
    }
}
