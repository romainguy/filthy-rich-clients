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
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 *
 * @author Romain Guy
 */
public class PictureGlassPane extends JComponent {
    private BufferedImage image;
    private Point location;
    private BufferedImage shadow;
    
    public PictureGlassPane() {
    }
    
    public void moveIt(Point location) {
        Point oldLocation = this.location;
        SwingUtilities.convertPointFromScreen(location, this);
        this.location = location;
        
        Rectangle newClip = new Rectangle(location.x - image.getWidth() / 2, location.y - image.getHeight() / 2,
                image.getWidth(), image.getHeight());
        newClip.add(new Rectangle(oldLocation.x - image.getWidth() / 2, oldLocation.y - image.getHeight() / 2,
                image.getWidth(), image.getHeight()));
        newClip.add(new Rectangle(oldLocation.x - image.getWidth() / 2, oldLocation.y - image.getHeight() / 2,
                shadow.getWidth(), shadow.getHeight()));
        newClip.add(new Rectangle(location.x - image.getWidth() / 2, location.y - image.getHeight() / 2,
                shadow.getWidth(), shadow.getHeight()));

        repaint(newClip);
    }
    
    public void hideIt() {
        setVisible(false);
    }
    
    public void showIt(BufferedImage image, Point location) {
        this.image = image;
        this.shadow = new ShadowRenderer(5, 0.3f, Color.BLACK).createShadow(image);

        SwingUtilities.convertPointFromScreen(location, this);
        this.location = location;
        
        setVisible(true);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        if (image != null && location != null) {
            int x = location.x - image.getWidth() / 2;
            int y = location.y - image.getHeight() / 2;
            
            g.drawImage(shadow, x, y, null);
            g.drawImage(image, x, y, null);
        }
    }
}
