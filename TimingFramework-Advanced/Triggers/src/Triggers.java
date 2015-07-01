import java.awt.BorderLayout;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.jdesktop.animation.timing.triggers.ActionTrigger;
import org.jdesktop.animation.timing.triggers.FocusTrigger;
import org.jdesktop.animation.timing.triggers.FocusTriggerEvent;
import org.jdesktop.animation.timing.triggers.MouseTrigger;
import org.jdesktop.animation.timing.triggers.MouseTriggerEvent;
import org.jdesktop.animation.timing.triggers.TimingTrigger;
import org.jdesktop.animation.timing.triggers.TimingTriggerEvent;
/*
 * Triggers.java
 *
 * Created on May 3, 2007, 1:24 PM
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
public class Triggers extends JComponent {
    
    SpherePanel armed, over, action, focus, timing;
    static JButton triggerButton;
    
    /** Creates a new instance of Triggers */
    public Triggers() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        action = new SpherePanel("yellow-sphere.png");
        focus = new SpherePanel("blue-sphere.png");
        armed = new SpherePanel("red-sphere.png");
        over = new SpherePanel("green-sphere.png");
        timing = new SpherePanel("gray-sphere.png");
        
        add(action);
        add(focus);
        add(armed);
        add(over);
        add(timing);
        
        // Add triggers for each sphere, depending on what we want to 
        // trigger them
        ActionTrigger.addTrigger(triggerButton, action.getAnimator());
        FocusTrigger.addTrigger(triggerButton,
                focus.getAnimator(), FocusTriggerEvent.IN);
        MouseTrigger.addTrigger(triggerButton, 
                armed.getAnimator(), MouseTriggerEvent.PRESS);
        MouseTrigger.addTrigger(triggerButton, 
                over.getAnimator(), MouseTriggerEvent.ENTER);
        TimingTrigger.addTrigger(action.getAnimator(),
                timing.getAnimator(), TimingTriggerEvent.STOP);
    }
    
    private static void createAndShowGUI() {
        JFrame f = new JFrame("Triggers");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLayout(new BorderLayout());
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        // Note: "Other Button" exists only to provide another component to
        // move focus from/to, in order to show how FocusTrigger works
        buttonPanel.add(new JButton("Other Button"), BorderLayout.NORTH);
        triggerButton = new JButton("Trigger");
        buttonPanel.add(triggerButton, BorderLayout.SOUTH);
        f.add(buttonPanel, BorderLayout.NORTH);
        f.add(new Triggers(), BorderLayout.CENTER);
        f.pack();
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
