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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import quicktime.QTSession;
import quicktime.app.view.MoviePlayer;
import quicktime.app.view.QTFactory;
import quicktime.std.StdQTConstants;
import quicktime.std.movies.Movie;
import quicktime.std.movies.media.DataRef;
/**
 *
 * @author Romain Guy <romain.guy@mac.com>
 */
public class RepaintManagerDemo extends JFrame {
    private ReflectionPanel reflectionPanel;
    
    public RepaintManagerDemo() {
        super("Repaint Manager Demo");
        
        setContentPane(new GradientPanel());
        getContentPane().setLayout(new GridBagLayout());
        
        add(buildReflectionPanel(), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(96, 96, 96, 96), 0, 0));

        pack();
        setLocationRelativeTo(null);
        setResizable(false);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    @Override
    public void dispose() {
        super.dispose();
        QTSession.close();
    }
    
    private JComponent buildReflectedComponent() {
        try {
            Class.forName("quicktime.QTSession");
        } catch (ClassNotFoundException ex) {
            return new DummyPanel();
        }
        
        try {
            QTSession.open();
            String url = "http://images.apple.com/movies/sony_pictures/spider-man_3/spider-man_3-tlr1_h.480.mov";
            DataRef dRef = new DataRef(url);
            Movie mov = Movie.fromDataRef (dRef, StdQTConstants.newMovieActive);
            MoviePlayer player = new MoviePlayer(mov);
            mov.start();
            JComponent qtPlayer = QTFactory.makeQTJComponent(player).asJComponent();
            
            return qtPlayer;
        } catch (Exception e) {
            return new DummyPanel();
        }
    }
    
    private JComponent buildReflectionPanel() {
        reflectionPanel = new ReflectionPanel();
        reflectionPanel.add(buildReflectedComponent());

        return reflectionPanel;
    }

    private static class GradientPanel extends JPanel {
        GradientPanel() {
            super(new BorderLayout());
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(new GradientPaint(0.0f, getHeight() * 0.22f,
                                          new Color(0x202737),
                                          0.0f, getHeight() * 0.7f,
                                          Color.BLACK, true));
            Rectangle clip = g.getClipBounds();
            g2.fillRect(clip.x, clip.y, clip.width, clip.height);
            g2.dispose();
        }
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
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
                new RepaintManagerDemo().setVisible(true);
            }
        });
    }
}
