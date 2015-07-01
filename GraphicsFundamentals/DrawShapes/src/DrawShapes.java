import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
/*
 * DrawShapes.java
 *
 * Created on May 1, 2007, 4:16 PM
 *
 * Copyright (c) 2007, Sun Microsystems, Inc
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

/**
 *
 * @author Chet
 */
public class DrawShapes extends JComponent {

    private List<Shape> shapes = new ArrayList();
    private boolean getStar = true;
    
    /**
     * Generates a star Shape from the given location, radii, and points
     * parameters. The Shape is created by constructing a GeneralPath
     * that moves between the inner and outer rings.
     */
    private static Shape generateStar(double x, double y,
                                      double innerRadius, double outerRadius,
                                      int pointsCount) {
        GeneralPath path = new GeneralPath();

        double outerAngleIncrement = 2 * Math.PI / pointsCount;

        double outerAngle = 0.0;
        double innerAngle = outerAngleIncrement / 2.0;

        x += outerRadius;
        y += outerRadius;

        float x1 = (float) (Math.cos(outerAngle) * outerRadius + x);
        float y1 = (float) (Math.sin(outerAngle) * outerRadius + y);

        float x2 = (float) (Math.cos(innerAngle) * innerRadius + x);
        float y2 = (float) (Math.sin(innerAngle) * innerRadius + y);

        path.moveTo(x1, y1);
        path.lineTo(x2, y2);

        outerAngle += outerAngleIncrement;
        innerAngle += outerAngleIncrement;

        for (int i = 1; i < pointsCount; i++) {
            x1 = (float) (Math.cos(outerAngle) * outerRadius + x);
            y1 = (float) (Math.sin(outerAngle) * outerRadius + y);

            path.lineTo(x1, y1);

            x2 = (float) (Math.cos(innerAngle) * innerRadius + x);
            y2 = (float) (Math.sin(innerAngle) * innerRadius + y);

            path.lineTo(x2, y2);

            outerAngle += outerAngleIncrement;
            innerAngle += outerAngleIncrement;
        }

        path.closePath();
        return path;
    }
    
    /**
     * Generates a donut shape from the given location and radii by subtracting
     * an inner circular Area from an outer one.
     */
    private static Shape generateDonut(double x, double y,
            double innerRadius, double outerRadius) {
        Area a1 = new Area(new Ellipse2D.Double(x, y, outerRadius, outerRadius));
        double innerOffset = (outerRadius - innerRadius)/2;
        Area a2 = new Area(new Ellipse2D.Double(x + innerOffset, y + innerOffset, 
                innerRadius, innerRadius));
        a1.subtract(a2);
        return a1;
    }

    /** 
     * This class processes mouse clicks and generates stars and donuts,
     * alternately, in the click location. The new shape is added to the
     * List of current shapes, then the scene is repainted.
     */
    private class ClickReceiver extends MouseAdapter {
        public void mouseClicked(MouseEvent me) {
            int centerX = me.getX();
            int centerY = me.getY();
            double innerSize = 1 + (25 * Math.random());
            double outerSize = innerSize + 10 + (15 * Math.random());
            Shape newShape;
            if (getStar) {
                int numPoints = (int)(8 * Math.random() + 5);
                newShape = generateStar(centerX - outerSize, 
                        centerY - outerSize,
                        innerSize, outerSize, numPoints);
            } else {
                newShape = generateDonut(centerX - outerSize/2, 
                        centerY - outerSize/2,
                        innerSize, outerSize);
            }
            getStar = !getStar;
            shapes.add(newShape);
            repaint();
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        // Paint a gradient for the sky
        GradientPaint background = new GradientPaint(0f, 0f, Color.GRAY.darker(),
                0f, (float)getHeight(), Color.GRAY.brighter());
        g2d.setPaint(background);
        g2d.fillRect(0, 0, getWidth(), 4*getHeight()/5);
        
        // Paint a gradient for the ground
        background = new GradientPaint(0f, (float)4*getHeight()/5, 
                Color.BLACK,
                0f, (float)getHeight(), Color.GRAY.darker());
        g2d.setPaint(background);
        g2d.fillRect(0, 4*getHeight()/5, getWidth(), getHeight()/5);
        
        // Enable anti-aliasing to get smooth outlines
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Iterate through all of the current shapes
        for (Shape shape : shapes) {
            // Get the bounds to compute the RadialGradient properties
            Rectangle rect = shape.getBounds();
            Point2D center = new Point2D.Float(
                    rect.x + (float)rect.width / 2.0f,
                    rect.y + (float)rect.height / 2.0f);
            float radius = (float)rect.width / 2.0f;
            float[] dist = {0.1f, 0.9f};
            Color[] colors = {Color.WHITE, Color.BLACK};
            
            // Create and set a RadialGradient centered on the object,
            // going from white at the center to black at the edges
            RadialGradientPaint paint = new RadialGradientPaint(center, radius,
                    dist, colors);
            g2d.setPaint(paint);
            
            // Finally, render our shape
            g2d.fill(shape);
        }
    }
    
    /**
     * Creates a new instance of DrawShapes
     */
    public DrawShapes() {
        setBackground(Color.WHITE);
        addMouseListener(new ClickReceiver());
    }
    
    private static void createAndShowGUI() {    
        JFrame f = new JFrame("Draw Shapes");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(500, 500);
        f.add(new DrawShapes());
        f.setVisible(true);
    }
    
    public static void main(String args[]) {
        Runnable doCreateAndShowGUI = new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        };
        SwingUtilities.invokeLater(doCreateAndShowGUI);
    }

}
