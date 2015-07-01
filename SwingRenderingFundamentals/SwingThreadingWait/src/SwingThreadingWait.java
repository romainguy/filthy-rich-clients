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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import javax.swing.*;

/**
 * @author Romain Guy
 */
public class SwingThreadingWait extends JFrame implements ActionListener {
    private JLabel counter;
    private long start = 0;

    public SwingThreadingWait() {
        super("Invoke & Wait");

        JButton freezer = new JButton("Open File");
        freezer.addActionListener(this);

        counter = new JLabel("Time elapsed: 0s");

        add(freezer, BorderLayout.CENTER);
        add(counter,  BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void actionPerformed(ActionEvent e) {
        start = System.currentTimeMillis();
        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }

                    final int elapsed = (int) ((System.currentTimeMillis() - start) / 1000);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            counter.setText("Time elapsed: " + elapsed + "s");
                        }
                    });

                    if (elapsed == 4) {
                        try {
                            final int[] answer = new int[1];
                            SwingUtilities.invokeAndWait(new Runnable() {
                                public void run() {
                                    answer[0] = JOptionPane.showConfirmDialog(SwingThreadingWait.this,
                                                                              "Abort long operation?",
                                                                              "Abort?",
                                                                              JOptionPane.YES_NO_OPTION);
                                }
                            });
                            if (answer[0] == JOptionPane.YES_OPTION) {
                                return;
                            }
                        } catch (InterruptedException e1) {
                        } catch (InvocationTargetException e1) {
                        }
                    }
                }
            }
        }).start();
    }

    public static void main(String... args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                SwingThreadingWait edt = new SwingThreadingWait();
                edt.setVisible(true);
            }
        });
    }
}
