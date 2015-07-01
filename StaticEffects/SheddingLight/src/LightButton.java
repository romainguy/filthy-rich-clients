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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicButtonUI;

public class LightButton extends JButton {

    private float shadowOffsetX;
    private float shadowOffsetY;
    
    private Color shadowColor = Color.BLACK;
    private int shadowDirection = 60;
    
    private Image normalButton, normalButtonPressed, buttonHighlight;
    private int shadowDistance = 1;
    private Insets sourceInsets = new Insets(6, 7, 6, 8);
    private Dimension buttonDimension = new Dimension(116, 35);
    private Color buttonForeground = Color.WHITE;
    private Font buttonFont = new Font("Arial", Font.BOLD, 22);
    
    private float ghostValue;
    
    private boolean main = false;

    public LightButton(String text) {
        this();
        setText(text);
    }
    
    public LightButton(Action a) {
        this();
        setAction(a);
    }
    
    public LightButton() {
        computeShadow();

        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setForeground(buttonForeground);
        setFont(buttonFont);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusable(false);
        
        normalButton = loadImage("./button-normal.png");
        normalButtonPressed = loadImage("./button-normal-pressed.png");
        buttonHighlight = loadImage("./header-halo.png");
        
        // Hacky? Hacky!
        setUI(new BasicButtonUI() {
            @Override
            public Dimension getMinimumSize(JComponent c) {
                return getPreferredSize(c);
            }
            
            @Override
            public Dimension getMaximumSize(JComponent c) {
                return getPreferredSize(c);
            }
            
            @Override
            public Dimension getPreferredSize(JComponent c) {
                Insets insets = c.getInsets();
                Dimension d = new Dimension(buttonDimension);
                d.width += insets.left + insets.right;
                d.height += insets.top + insets.bottom;
                return d;
            }
        });
    }
    
    private static Image loadImage(String fileName) {
        try {
            return ImageIO.read(LightButton.class.getResource(fileName));
        } catch (IOException ex) {
            return null;
        }
    }
    
    private void computeShadow() {
        double rads = Math.toRadians(shadowDirection);
        shadowOffsetX = (float) Math.cos(rads) * shadowDistance;
        shadowOffsetY = (float) Math.sin(rads) * shadowDistance;
    }
    
    private Image getImage(boolean armed) {
        return armed ? normalButtonPressed : normalButton;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        
        ButtonModel m = getModel();
        Insets insets = getInsets();
        
        int width = getWidth() - insets.left - insets.right;
        int height = getHeight() - insets.top - insets.bottom;
        
        tileStretchPaint(g2,this,(BufferedImage) getImage(m.isArmed()), sourceInsets);
        
        if (getModel().isRollover()) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(buttonHighlight,
                    insets.left + 2, insets.top + 2,
                    width - 4, height - 4, null);
        }
        
        FontMetrics fm = getFontMetrics(getFont());
        TextLayout layout = new TextLayout(getText(),
                getFont(),
                g2.getFontRenderContext());
        Rectangle2D bounds = layout.getBounds();
        
        int x = (int) (getWidth() - insets.left - insets.right -
                bounds.getWidth()) / 2;
        //x -= 2;
        int y = (getHeight() - insets.top - insets.bottom -
                 fm.getMaxAscent() - fm.getMaxDescent()) / 2;
        y += fm.getAscent() - 1;
        
        if (m.isArmed()) {
            x += 1;
            y += 1;
        }
        
        g2.setColor(shadowColor);
        layout.draw(g2,
                x + (int) Math.ceil(shadowOffsetX),
                y + (int) Math.ceil(shadowOffsetY));
        g2.setColor(getForeground());
        layout.draw(g2, x, y);
    }
    
    /**
     * Draws an image on top of a component by doing a 3x3 grid stretch of the image
     * using the specified insets.
     *
     * From SwingLabs, licensed under LGPL
     */
    public static void tileStretchPaint(Graphics g, 
                JComponent comp,
                BufferedImage img,
                Insets ins) {
        
        int left = ins.left;
        int right = ins.right;
        int top = ins.top;
        int bottom = ins.bottom;
        
        // top
        g.drawImage(img,
                    0,0,left,top,
                    0,0,left,top,
                    null);
        g.drawImage(img,
                    left,                 0, 
                    comp.getWidth() - right, top, 
                    left,                 0, 
                    img.getWidth()  - right, top, 
                    null);
        g.drawImage(img,
                    comp.getWidth() - right, 0, 
                    comp.getWidth(),         top, 
                    img.getWidth()  - right, 0, 
                    img.getWidth(),          top, 
                    null);

        // middle
        g.drawImage(img,
                    0,    top, 
                    left, comp.getHeight()-bottom,
                    0,    top,   
                    left, img.getHeight()-bottom,
                    null);
        
        g.drawImage(img,
                    left,                  top, 
                    comp.getWidth()-right,      comp.getHeight()-bottom,
                    left,                  top,   
                    img.getWidth()-right,  img.getHeight()-bottom,
                    null);
         
        g.drawImage(img,
                    comp.getWidth()-right,     top, 
                    comp.getWidth(),           comp.getHeight()-bottom,
                    img.getWidth()-right, top,   
                    img.getWidth(),       img.getHeight()-bottom,
                    null);
        
        // bottom
        g.drawImage(img,
                    0,comp.getHeight()-bottom, 
                    left, comp.getHeight(),
                    0,img.getHeight()-bottom,   
                    left,img.getHeight(),
                    null);
        g.drawImage(img,
                    left,                    comp.getHeight()-bottom, 
                    comp.getWidth()-right,        comp.getHeight(),
                    left,                    img.getHeight()-bottom,   
                    img.getWidth()-right,    img.getHeight(),
                    null);
        g.drawImage(img,
                    comp.getWidth()-right,     comp.getHeight()-bottom, 
                    comp.getWidth(),           comp.getHeight(),
                    img.getWidth()-right, img.getHeight()-bottom,   
                    img.getWidth(),       img.getHeight(),
                    null);
    }

}
