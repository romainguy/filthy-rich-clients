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
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.interpolation.PropertySetter;
import org.jdesktop.animation.timing.triggers.FocusTrigger;
import org.jdesktop.animation.timing.triggers.FocusTriggerEvent;

/**
 *
 * @author Romain Guy <romain.guy@mac.com>
 */
public class PulseFieldDemo extends JFrame {
    
    public PulseFieldDemo() {
        super("PulseField Demo");
        
        add(buildPulsatingField());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        pack();
        setLocationRelativeTo(null);
    }
    
    private JComponent buildPulsatingField() {
        JTextField field = new JTextField(20);
        
        PulsatingBorder border = new PulsatingBorder(field);
        field.setBorder(new CompoundBorder(field.getBorder(), border));
        
        PropertySetter setter = new PropertySetter(
                border, "thickness", 0.0f, 1.0f);
        Animator animator = new Animator(900, Animator.INFINITE,
                Animator.RepeatBehavior.REVERSE, setter);
        animator.start();
        
        JPanel panel = new JPanel(new FlowLayout());
        panel.add(field);
        panel.add(new JButton("OK"));
        panel.add(new JButton("Cancel"));
        return panel;
    }
    
    public static class PulsatingBorder implements Border {
        private float thickness = 0.0f;
        private JComponent c;
        
        public PulsatingBorder(JComponent c) {
            this.c = c;
        }
        
        public void paintBorder(Component c, Graphics g,
                int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            
            Rectangle2D r = new Rectangle2D.Double(x, y, width - 1, height - 1);
            g2.setStroke(new BasicStroke(2.0f * getThickness()));
            g2.setComposite(AlphaComposite.SrcOver.derive(getThickness()));
            g2.setColor(new Color(0x54A4DE));
            g2.draw(r);
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(2, 2, 2, 2);
        }

        public boolean isBorderOpaque() {
            return false;
        }

        public float getThickness() {
            return thickness;
        }

        public void setThickness(float thickness) {
            this.thickness = thickness;
            c.repaint();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new PulseFieldDemo().setVisible(true);
            }
        });
    }
}
