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

import equation.EquationDisplay;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Area;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

public class SplineDisplay extends EquationDisplay {
    private static final double CONTROL_POINT_SIZE = 12.0;

    private Point2D control1 = new Point2D.Double(0.25, 0.75);
    private Point2D control2 = new Point2D.Double(0.75, 0.25);
    
    private Point2D selected = null;
    private Point dragStart = null;
    
    private boolean isSaving = false;
    
    private PropertyChangeSupport support;
    
    SplineDisplay() {
        super(0.0, 0.0,
              -0.1, 1.1, -0.1, 1.1,
              0.2, 6,
              0.2, 6);
        
        setEnabled(false);
        
        addMouseMotionListener(new ControlPointsHandler());
        addMouseListener(new SelectionHandler());
        
        support = new PropertyChangeSupport(this);
    }
    
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        support.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        support.removePropertyChangeListener(propertyName, listener);
    }

    public Point2D getControl1() {
        return (Point2D) control1.clone();
    }

    public Point2D getControl2() {
        return (Point2D) control2.clone();
    }
    
    public void setControl1(Point2D control1) {
        support.firePropertyChange("control1",
                                   (Point2D) this.control1.clone(),
                                   (Point2D) control1.clone());
        this.control1 = (Point2D) control1.clone();
        repaint();
    }

    public void setControl2(Point2D control2) {
        support.firePropertyChange("control2",
                                   (Point2D) this.control2.clone(),
                                   (Point2D) control2.clone());
        this.control2 = (Point2D) control2.clone();
        repaint();
    }
    
    synchronized void saveAsTemplate(OutputStream out) {
        BufferedImage image = Java2dHelper.createCompatibleImage(getWidth(), getHeight());
        Graphics g = image.getGraphics();
        isSaving = true;
        setDrawText(false);
        paint(g);
        setDrawText(true);
        isSaving = false;
        g.dispose();
        
        BufferedImage subImage = image.getSubimage((int) xPositionToPixel(0.0),
                                                   (int) yPositionToPixel(1.0),
                                                   (int) (xPositionToPixel(1.0) - xPositionToPixel(0.0)) + 1,
                                                   (int) (yPositionToPixel(0.0) - yPositionToPixel(1.0)) + 1);
        
        try {
            ImageIO.write(subImage, "PNG", out);
        } catch (IOException e) {
        }
        
        image.flush();
        subImage = null;
        image = null;
    }

    @Override
    protected void paintInformation(Graphics2D g2) {
        if (!isSaving) {
            paintControlPoints(g2);
        }
        paintSpline(g2);
    }

    private void paintControlPoints(Graphics2D g2) {
        paintControlPoint(g2, control1);
        paintControlPoint(g2, control2);
    }
        
    private void paintControlPoint(Graphics2D g2, Point2D control) {
        double origin_x = xPositionToPixel(control.getX());
        double origin_y = yPositionToPixel(control.getY());
        double pos = control == control1 ? 0.0 : 1.0;
        
        Ellipse2D outer = getDraggableArea(control);
        Ellipse2D inner = new Ellipse2D.Double(origin_x + 2.0 - CONTROL_POINT_SIZE / 2.0,
                                               origin_y + 2.0 - CONTROL_POINT_SIZE / 2.0,
                                               8.0, 8.0);
        
        Area circle = new Area(outer);
        circle.subtract(new Area(inner));
        
        Stroke stroke = g2.getStroke();
        g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                                     5, new float[] { 5, 5 }, 0));
        g2.setColor(new Color(1.0f, 0.0f, 0.0f, 0.4f));
        g2.drawLine(0, (int) origin_y, (int) origin_x, (int) origin_y);
        g2.drawLine((int) origin_x, (int) origin_y, (int) origin_x, getHeight());
        g2.setStroke(stroke);
        
        if (selected == control) {
            g2.setColor(new Color(1.0f, 1.0f, 1.0f, 1.0f));
        } else {
            g2.setColor(new Color(0.8f, 0.8f, 0.8f, 0.6f));
        }
        g2.fill(inner);
        
        g2.setColor(new Color(0.0f, 0.0f, 0.5f, 0.5f));
        g2.fill(circle);
        
        g2.drawLine((int) origin_x, (int) origin_y,
                    (int) xPositionToPixel(pos), (int) yPositionToPixel(pos));
    }

    private Ellipse2D getDraggableArea(Point2D control) {
        Ellipse2D outer = new Ellipse2D.Double(xPositionToPixel(control.getX()) - CONTROL_POINT_SIZE / 2.0,
                                               yPositionToPixel(control.getY()) - CONTROL_POINT_SIZE / 2.0,
                                               CONTROL_POINT_SIZE, CONTROL_POINT_SIZE);
        return outer;
    }

    private void paintSpline(Graphics2D g2) {
        CubicCurve2D spline = new CubicCurve2D.Double(xPositionToPixel(0.0), yPositionToPixel(0.0),
                                                      xPositionToPixel(control1.getX()),
                                                      yPositionToPixel(control1.getY()),
                                                      xPositionToPixel(control2.getX()),
                                                      yPositionToPixel(control2.getY()),
                                                      xPositionToPixel(1.0), yPositionToPixel(1.0));
        g2.setColor(new Color(0.0f, 0.3f, 0.0f, 1.0f));
        g2.draw(spline);
    }
    
    private void resetSelection() {
        Point2D oldSelected = selected;
        selected = null;
        
        if (oldSelected != null) {
            Rectangle bounds = getDraggableArea(oldSelected).getBounds();
            repaint(bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }
    
    private class ControlPointsHandler extends MouseMotionAdapter {
        @Override
        public void mouseMoved(MouseEvent e) {
            Ellipse2D area1 = getDraggableArea(control1);
            Ellipse2D area2 = getDraggableArea(control2);
            
            if (area1.contains(e.getPoint()) || area2.contains(e.getPoint())) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else {
                setCursor(Cursor.getDefaultCursor());
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (selected == null) {
                return;
            }
            
            Point dragEnd = e.getPoint();

            double distance = xPixelToPosition(dragEnd.getX()) -
                              xPixelToPosition(dragStart.getX());
            double x = selected.getX() + distance;
            if (x < 0.0) {
                x = 0.0;
            } else if (x > 1.0) {
                x = 1.0;
            }
            
            distance = yPixelToPosition(dragEnd.getY()) -
                       yPixelToPosition(dragStart.getY());
            double y = selected.getY() + distance;
            if (y < 0.0) {
                y = 0.0;
            } else if (y > 1.0) {
                y = 1.0;
            }

            Point2D selectedCopy = (Point2D) selected.clone();
            selected.setLocation(x, y);
            support.firePropertyChange("control" + (selected == control1 ? "1" : "2"),
                                       selectedCopy, (Point2D) selected.clone());
            
            repaint();

            double xPos = xPixelToPosition(dragEnd.getX());
            double yPos = -yPixelToPosition(dragEnd.getY());
            
            if (xPos >= 0.0 && xPos <= 1.0) {
                dragStart.setLocation(dragEnd.getX(), dragStart.getY());
            }
            if (yPos >= 0.0 && yPos <= 1.0) {
                dragStart.setLocation(dragStart.getX(), dragEnd.getY());
            }
        }
    }
    
    private class SelectionHandler extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            Ellipse2D area1 = getDraggableArea(control1);
            Ellipse2D area2 = getDraggableArea(control2);
            
            if (area1.contains(e.getPoint())) {
                selected = control1;
                dragStart = e.getPoint();
                
                Rectangle bounds = area1.getBounds();
                repaint(bounds.x, bounds.y, bounds.width, bounds.height);
            } else if (area2.contains(e.getPoint())) {
                selected = control2;
                dragStart = e.getPoint();
                
                Rectangle bounds = area2.getBounds();
                repaint(bounds.x, bounds.y, bounds.width, bounds.height);
            } else {
                resetSelection();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            resetSelection();
        }
    }
}
