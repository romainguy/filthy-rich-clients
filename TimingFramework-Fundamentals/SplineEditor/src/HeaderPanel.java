/**
 * Copyright (c) 2006, Sun Microsystems, Inc
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
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Rectangle;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

class HeaderPanel extends JPanel {

    private ImageIcon icon;

    HeaderPanel(ImageIcon icon,
                String title,
                String help1,
                String help2) {
        super(new BorderLayout());

        this.icon = icon;

        JPanel titlesPanel = new JPanel(new GridLayout(3, 1));
        titlesPanel.setOpaque(false);
        titlesPanel.setBorder(new EmptyBorder(12, 0, 12, 0));

        JLabel headerTitle = new JLabel(title);
        Font police = headerTitle.getFont().deriveFont(Font.BOLD);
        headerTitle.setFont(police);
        headerTitle.setBorder(new EmptyBorder(0, 12, 0, 0));
        titlesPanel.add(headerTitle);

        JLabel message;

        titlesPanel.add(message = new JLabel(help1));
        police = headerTitle.getFont().deriveFont(Font.PLAIN);
        message.setFont(police);
        message.setBorder(new EmptyBorder(0, 24, 0, 0));

        titlesPanel.add(message = new JLabel(help2));
        police = headerTitle.getFont().deriveFont(Font.PLAIN);
        message.setFont(police);
        message.setBorder(new EmptyBorder(0, 24, 0, 0));

        message = new JLabel(this.icon);
        message.setBorder(new EmptyBorder(0, 0, 0, 12));

        add(BorderLayout.WEST, titlesPanel);
        add(BorderLayout.EAST, message);
        add(BorderLayout.SOUTH, new JSeparator(JSeparator.HORIZONTAL));

        setPreferredSize(new Dimension(500, this.icon.getIconHeight() + 24));
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!isOpaque()) {
            return;
        }
        
        Rectangle bounds = g.getClipBounds();

        Color control = UIManager.getColor("control");
        int width = getWidth();

        Graphics2D g2 = (Graphics2D) g;
        Paint storedPaint = g2.getPaint();
        g2.setPaint(new GradientPaint(this.icon.getIconWidth(), 0, Color.white, width, 0, control));
        g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        g2.setPaint(storedPaint);
    }
}
