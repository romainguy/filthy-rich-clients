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
import java.awt.Component;
import java.awt.Composite;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Romain Guy
 */
public class AlphaCompositesApplication extends JFrame {
    private CompositePainter painter;
    private JSlider opacity;
    private JComboBox composites;
    
    public AlphaCompositesApplication() {
        super("Alpha Composites");
        
        add(painter = new CompositePainter(), BorderLayout.CENTER);
        
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        panel.add(buildCompositeSelector());
        panel.add(buildOpacitySelector());
        add(panel, BorderLayout.SOUTH);
        
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    private Component buildOpacitySelector() {
        opacity = new JSlider(0, 100, 50);
        opacity.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                changeComposite();
            }
        });
        JPanel panel = new JPanel();
        panel.add(new JLabel("0%"));
        panel.add(opacity);
        panel.add(new JLabel("100%"));
        return panel;
    }
    
    private Component buildCompositeSelector() {
        composites = new JComboBox(new String[] {
            "CLEAR",
            "DST", "DST_ATOP", "DST_IN", "DST_OUT", "DST_OVER",
            "SRC", "SRC_ATOP", "SRC_IN", "SRC_OUT", "SRC_OVER",
            "XOR"
        });
        composites.setSelectedItem("SRC");
        composites.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                changeComposite();
            }
        });
        return composites;
    }
    
    private void changeComposite() {
        String rule = composites.getSelectedItem().toString();
        try {
            Field ruleField = AlphaComposite.class.getDeclaredField(rule);
            AlphaComposite composite = AlphaComposite.getInstance(ruleField.getInt(null),
                (float) opacity.getValue() / 100.0f);
            painter.setComposite(composite);
        } catch (SecurityException ex) {
            ex.printStackTrace();
        } catch (NoSuchFieldException ex) {
            ex.printStackTrace();
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }
    
    private final class CompositePainter extends JComponent {
        private AlphaComposite composite = AlphaComposite.getInstance(
            AlphaComposite.SRC, 0.5f);

        @Override
        protected void paintComponent(Graphics g) {
            BufferedImage image = new BufferedImage(getWidth(), getHeight(),
                BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = image.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2.setColor(Color.BLUE);
            g2.fillRect(4 + (getWidth() / 4), 4, getWidth() / 2, getHeight() - 8);
            g2.setColor(Color.RED);
            g2.setComposite(composite);
            g2.fillOval(40, 40, getWidth() - 80, getHeight() - 80);
            g2.dispose();
            
            g.drawImage(image, 0, 0, null);
        }

        private void setComposite(AlphaComposite composite) {
            this.composite = composite;
            repaint();
        }
    }
    
    public static void main(String... args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new AlphaCompositesApplication().setVisible(true);
            }
        });
    }
}
