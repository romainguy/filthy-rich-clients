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

import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

/**
 *
 * @author Romain Guy <romain.guy@mac.com>
 */
public class Validator extends JComponent {
    private Set<JComponent> invalidFields = new HashSet<JComponent>();
    private BufferedImage warningIcon;
    
    /** Creates a new instance of Validator */
    public Validator() {
        loadImages();
    }

    public void addWarning(JComponent field) {
        if (invalidFields.contains(field)) {
            invalidFields.remove(field);
            repaintBadge(field);
        }
    }

    public void removeWarning(JComponent field) {
        invalidFields.add(field);
        repaintBadge(field);
    }

    private void repaintBadge(JComponent field) {
        Point p = field.getLocationOnScreen();
        SwingUtilities.convertPointFromScreen(p, this);
        
        int x = p.x - warningIcon.getWidth() / 2;
        int y = (int) (p.y + field.getHeight() - warningIcon.getHeight() / 1.5);
        
        repaint(x, y, warningIcon.getWidth(), warningIcon.getHeight());
    }
    
    private void loadImages() {
        try {
            warningIcon = ImageIO.read(getClass().getResource("images/dialog-warning.png"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        for (JComponent invalid : invalidFields) {
            if (invalid.getParent() instanceof JViewport) {
                JViewport viewport = (JViewport) invalid.getParent();
                // the parent of the viewport is a JScrollPane
                invalid = (JComponent) viewport.getParent();
            }
            
            Point p = invalid.getLocationOnScreen();
            SwingUtilities.convertPointFromScreen(p, this);
            
            int x = p.x - warningIcon.getWidth() / 2;
            int y = (int) (p.y + invalid.getHeight() - warningIcon.getHeight() / 1.5);
            
            if (g.getClipBounds().intersects(x, y,
                    warningIcon.getWidth(), warningIcon.getHeight())) {
                g.drawImage(warningIcon, x, y, null);
            }
        }
    }
}
