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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.interpolation.PropertySetter;

/**
 *
 * @author Romain Guy <romain.guy@mac.com>
 */
public class SpringDemo extends JFrame {
    private JList list;
    private SpringGlassPane glassPane;

    public SpringDemo() {
        super("Spring Demo");
        
        setupGlassPane();
        
        add(Box.createVerticalStrut(16), BorderLayout.NORTH);
        add(Box.createHorizontalStrut(16), BorderLayout.WEST);
        add(buildList());
        add(Box.createHorizontalStrut(16), BorderLayout.EAST);
        add(Box.createVerticalStrut(16), BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
    
    private void setupGlassPane() {
        glassPane = new SpringGlassPane();
        setGlassPane(glassPane);
        glassPane.setVisible(true);
    }
    
    private JComponent buildList() {
        Application[] elements = new Application[] {
            new Application("Address Book", "x-office-address-book.png"),
            new Application("Calendar",     "x-office-calendar.png"),
            new Application("Presentation", "x-office-presentation.png"),
            new Application("Spreadsheet",  "x-office-spreadsheet.png"),
        };
        
        list = new JList(elements);
        list.setCellRenderer(new ApplicationListCellRenderer());
        list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        list.setVisibleRowCount(2);
        list.setBorder(BorderFactory.createEtchedBorder());
        list.addMouseListener(new MouseAdapter() {
             public void mouseClicked(MouseEvent e) {
                 if (e.getClickCount() == 2) {
                     int index = list.getSelectedIndex();
                     
                     Rectangle bounds = list.getCellBounds(index, index);
                     Point location = new Point(bounds.x, bounds.y);
                     location = SwingUtilities.convertPoint(list, location, glassPane);
                     location.y -= 13;
                     bounds.setLocation(location);
                     
                     glassPane.showSpring(bounds,
                             ((Application) list.getSelectedValue()).icon.getImage());
                 }
             }
         });
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Launcher"),
                new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
                    GridBagConstraints.LINE_START, GridBagConstraints.NONE,
                    new Insets(0, 0, 0, 0), 0, 0));
        panel.add(list, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        panel.add(new JLabel("Double-click an icon to launch the program"),
                new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0,
                    GridBagConstraints.LINE_START, GridBagConstraints.NONE,
                    new Insets(0, 0, 0, 0), 0, 0));
        
        return panel;
    }
    
    public static class SpringGlassPane extends JComponent {
        private static final float MAGNIFY_FACTOR = 1.5f;
        
        private Rectangle bounds;
        private Image image;
        
        private float zoom = 0.0f;

        @Override
        protected void paintComponent(Graphics g) {
            if (image != null && bounds != null) {
                int width = image.getWidth(this);
                width += (int) (image.getWidth(this) * MAGNIFY_FACTOR * getZoom());
                
                int height = image.getHeight(this);
                height += (int) (image.getHeight(this) * MAGNIFY_FACTOR * getZoom());
                
                int x = (bounds.width - width) / 2;
                int y = (bounds.height - height) / 2;

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                
                g2.setComposite(AlphaComposite.SrcOver.derive(1.0f - getZoom()));
                g2.drawImage(image, x + bounds.x, y + bounds.y,
                        width, height, null);
            }
        }

        public void showSpring(Rectangle bounds, Image image) {
            this.bounds = bounds;
            this.image = image;
            
            Animator animator = PropertySetter.createAnimator(250, this,
                    "zoom", 0.0f, 1.0f);
            animator.setAcceleration(0.2f);
            animator.setDeceleration(0.4f);
            animator.start();
            
            repaint();
        }

        public float getZoom() {
            return zoom;
        }

        public void setZoom(float zoom) {
            this.zoom = zoom;
            repaint();
        }
    }
    
    private static class ApplicationListCellRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            JLabel c;
            c = (JLabel) super.getListCellRendererComponent(list, value,
                    index, isSelected, cellHasFocus);
            
            Application element = (Application) value;
            c.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));
            c.setFont(c.getFont().deriveFont(18.0f).deriveFont(Font.BOLD));
            c.setText(element.label);
            c.setIcon(element.icon);
            c.setHorizontalTextPosition(JLabel.CENTER);
            c.setVerticalTextPosition(JLabel.BOTTOM);
            if (isSelected) {
                c.setBackground(new Color(0, 0, 200, 20));
            }
            
            return c;
        }   
    }
    
    private static class Application {
        public ImageIcon icon;
        public String label;
        
        public Application(String label, String icon) {
            this.icon = new ImageIcon(getClass().getResource("images/" + icon));
            this.label = label;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new SpringDemo().setVisible(true);
            }
        });
    }
}
