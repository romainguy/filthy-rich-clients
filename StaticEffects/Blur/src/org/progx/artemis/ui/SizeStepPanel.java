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

package org.progx.artemis.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.imageio.ImageIO;

import org.progx.artemis.Application;
import org.progx.artemis.graphics.GraphicsUtilities;
import org.progx.artemis.graphics.Reflection;

class SizeStepPanel extends JPanel {
    private BufferedImage small;
    private BufferedImage medium;
    private BufferedImage large;
    private boolean isLoaded;
    private int smallHeight;
    private int mediumHeight;
    private int largeHeight;
    private Rectangle largeImageBounds;
    private Rectangle mediumImageBounds;
    private Rectangle smallImageBounds;

    public SizeStepPanel() {
        setBackground(Color.BLACK);
        setOpaque(false);
        createThumbnails();
        addMouseMotionListener(new CursorChanger());
        addMouseListener(new SizeSelector());
    }

    private void createThumbnails() {
        Thread loader = new Thread(new Runnable() {
            public void run() {
                final MainFrame mainFrame = Application.getMainFrame();
                BufferedImage image = mainFrame.getImage();

                small = GraphicsUtilities.createThumbnail(image, 90);
                smallHeight = small.getHeight();
                small = Reflection.createReflection(small);

                medium = GraphicsUtilities.createThumbnail(image, 160);
                mediumHeight = medium.getHeight();
                medium = Reflection.createReflection(medium);

                large = GraphicsUtilities.createThumbnail(image, 240);
                largeHeight = large.getHeight();
                large = Reflection.createReflection(large);

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        mainFrame.hideWaitGlassPane();
                    }
                });

                isLoaded = true;
                repaint();
            }
        });
        loader.start();
    }
    
    public void dispose() {
        if (large != null) {
            large.flush();
            large = null;
        }
        if (medium != null) {
            medium.flush();
            medium = null;
        }
        if (small != null) {
            small.flush();
            small = null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (!isLoaded) {
            return;
        }

        int totalWidth = 24 * 2;
        totalWidth += large.getWidth() + medium.getWidth() + small.getWidth();

        int x = (getWidth() - totalWidth) / 2;
        int y = (getHeight() - large.getHeight()) / 2;
        y += 42;

        g.drawImage(large, x, y, null);
        if (largeImageBounds == null) {
            largeImageBounds = new Rectangle(x, y, large.getWidth(), largeHeight);
        }
        x += large.getWidth() + 24;
        y += largeHeight - mediumHeight;

        g.drawImage(medium, x, y, null);
        if (mediumImageBounds == null) {
            mediumImageBounds = new Rectangle(x, y, medium.getWidth(), mediumHeight);
        }
        x += medium.getWidth() + 24;
        y += mediumHeight - smallHeight;

        g.drawImage(small, x, y, null);
        if (smallImageBounds == null) {
            smallImageBounds = new Rectangle(x, y, small.getWidth(), smallHeight);
        }
    }

    private static void saveImage(final BufferedImage image, final File file) {
        Thread writer = new Thread(new Runnable() {
            public void run() {
                try {
                    ImageIO.write(image, "JPEG", file); // NON-NLS
                } catch (IOException e) {
                    e.printStackTrace();
                }
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        Application.getMainFrame().showDoneStep();
                    }
                });
            }
        });
        writer.start();
    }

    private class SizeSelector extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            Point point = e.getPoint();
            for (final Rectangle r : new Rectangle[] { largeImageBounds, mediumImageBounds, smallImageBounds }) {
                if (r != null && r.contains(point)) {
                    Application.getMainFrame().showWaitGlassPane();
                    Thread sizer = new Thread(new Runnable() {
                        public void run() {
                            int width;
                            if (r == largeImageBounds) {
                                width = 1024;
                            } else if (r == mediumImageBounds) {
                                width = 800;
                            } else {
                                width = 640;
                            }
                            final BufferedImage toSave =
                                    GraphicsUtilities.createThumbnail(
                                            Application.getMainFrame().getImage(), width);
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    JFileChooser chooser = new JFileChooser();
                                    chooser.setSelectedFile(new File(
                                            MessageFormat.format(Application
                                                    .getResourceBundle().getString(
                                                    "file.save.prefix"),
                                                                 Application
                                                                         .getMainFrame().getFileName())));
                                    dispose();
                                    if (chooser.showSaveDialog(Application.getMainFrame()) == JFileChooser.APPROVE_OPTION) {
                                        File selectedFile = chooser.getSelectedFile();
                                        if (!selectedFile.getPath().toLowerCase().endsWith(".jpg")) { // NON-NLS
                                            selectedFile = new File(selectedFile.getPath() + ".jpg"); // NON-NLS
                                        }
                                        saveImage(toSave, selectedFile);
                                    } else {
                                        Application.getMainFrame().showDragAndDropStep();
                                    }
                                }
                            });
                        }
                    });
                    sizer.start();
                }
            }
        }
    }

    private class CursorChanger extends MouseAdapter {
        @Override
        public void mouseMoved(MouseEvent e) {
            Point point = e.getPoint();
            if ((largeImageBounds != null && largeImageBounds.contains(point)) ||
                (mediumImageBounds != null && mediumImageBounds.contains(point)) ||
                (smallImageBounds != null && smallImageBounds.contains(point))) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else if (!getCursor().equals(Cursor.getDefaultCursor())) {
                setCursor(Cursor.getDefaultCursor());
            }
        }
    }
}
