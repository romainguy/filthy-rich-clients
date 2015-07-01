/*
 * SpherePanel.java
 *
 * Created on February 19, 2007, 9:09 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.interpolation.PropertySetter;

/**
 * This class encapsulates both the rendering of a sphere, at a location
 * that may be animating, and the animation that drives the sphere
 * movement.
 *
 * @author Chet
 */
public class SpherePanel extends JPanel {
    
    BufferedImage sphereImage = null;
    private static final int PADDING = 5;
    private static final int PANEL_HEIGHT = 300;
    private int sphereX = PADDING, sphereY = 0;
    Animator bouncer;
    
    /**
     * The animation changes the location of the sphere over time through
     * this property setter. We force a repaint to display the sphere in
     * its new location.
     */
    public void setSphereY(int sphereY) {
        this.sphereY = sphereY;
        repaint();
    }
    
    /**
     * Load the named image and create the animator that will bounce the 
     * image down and back up in this panel.
     */
    SpherePanel(String filename) {
        try {
            URL url = getClass().getResource("images/" + filename);
            sphereImage = ImageIO.read(url);
        } catch (Exception e) {
            System.out.println("Problem loading image " + filename + ": " + e);
            return;
        }
        setPreferredSize(new Dimension(sphereImage.getWidth() + 2 * PADDING, 
                PANEL_HEIGHT));
        bouncer = PropertySetter.createAnimator(2000, this, "sphereY",
                0, (PANEL_HEIGHT - sphereImage.getHeight()), 0);
        bouncer.setAcceleration(.5f);
        bouncer.setDeceleration(.5f);
    }
    
    Animator getAnimator() {
        return bouncer;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(Color.white);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.drawImage(sphereImage, sphereX, sphereY, null);
    }
    
}
