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

import java.awt.AWTEvent;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 *
 * @author Romain Guy
 */
public class WatermarkGlassPane extends JComponent {
    private BufferedImage image = null;
    
    /** Creates a new instance of WatermarkGlassPane */
    public WatermarkGlassPane() {
    }
    
    @Override
    public boolean contains(int x, int y) {
        if (getMouseListeners().length == 0 &&
            getMouseMotionListeners().length == 0 &&
            getMouseWheelListeners().length == 0 &&
            getCursor() == Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) {
            if (image == null) {
                return false;
            } else {
                int imageX = getWidth() - image.getWidth();
                int imageY = getHeight() - image.getHeight();
                
                // if the mouse cursor is on a non-opaque pixel, mouse events
                // are allowed
                int inImageX = x - imageX;
                int inImageY = y - imageY;
                
                if (inImageX >= 0 && inImageY >= 0 &&
                    inImageX < image.getWidth() && inImageY < image.getHeight()) {
                    int color = image.getRGB(inImageX, inImageY);
                    return (color >> 24 & 0xFF) > 0;
                }
                
                return x > imageX && x < getWidth() &&
                       y > imageY && y < getHeight();
            }
        }
        return super.contains(x, y);
    }

    
    @Override
    protected void paintComponent(Graphics g) {
        if (image == null) {
            try {
                image = ImageIO.read(getClass().getResource("watermark.png"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        g.drawImage(image, getWidth() - image.getWidth(),
                getHeight() - image.getHeight(), null);
    }
}
