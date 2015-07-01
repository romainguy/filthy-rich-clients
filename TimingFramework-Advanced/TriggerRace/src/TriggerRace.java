import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.jdesktop.animation.timing.triggers.ActionTrigger;
/*
 * TriggerRace.java
 *
 * Created on May 3, 2007, 1:43 PM
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
 * Exactly like SetterRace, only this version uses Triggers to
 * start/stop the animation automatically based on the user
 * clicking the Go/Stop buttons (no need for an ActionListener here)
 *
 * @author Chet
 */
public class TriggerRace extends NonLinearRace {
    
    /** Creates a new instance of TriggerRace */
    public TriggerRace(String appName) {
        super(appName);
        // Clicks on the Go button will atuomatically start the animator
        JButton goButton = controlPanel.getGoButton();
        ActionTrigger trigger = ActionTrigger.addTrigger(goButton, animator);
    }
    
    /**
     * Handle clicks on the Stop button. Clicks on Go are handled through
     * the ActionTrigger above.
     */
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("Stop")) {
            animator.stop();
        }
    }
        
    
    public static void main(String args[]) {
        Runnable doCreateAndShowGUI = new Runnable() {
            public void run() {
                TriggerRace race = new TriggerRace("Trigger Race");
            }
        };
        SwingUtilities.invokeLater(doCreateAndShowGUI);
    }
}
