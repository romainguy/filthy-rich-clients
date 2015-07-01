import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SwingUtilities;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.interpolation.PropertySetter;
/*
 * SetterRace.java
 *
 * Created on May 3, 2007, 2:37 PM
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
 * Like BasicRace, only this version uses the property setter capabilities
 * of the framework.  Instead of manually calculating the position of the
 * car, we have the framework do it for us, based on how we set up the
 * PropertySetter object.  All of this is done at
 * construction time and we merely start or stop the animation based on
 * the Go/Stop buttons at runtime.
 *
 * @author Chet
 */
public class SetterRace implements ActionListener {
    
    protected Animator timer;
    public static final int RACE_TIME = 2000;
    
    
    /** Creates a new instance of BasicRace */
    public SetterRace(String appName) {
        RaceGUI basicGUI = new RaceGUI(appName);
        
        // Now set up an animation that will automatically
        // run itself with PropertySetter
        timer = PropertySetter.createAnimator(RACE_TIME, basicGUI.getTrack(), 
                "carPosition", TrackView.START_POS, TrackView.FIRST_TURN_START);
        basicGUI.getControlPanel().addListener(this);
    }
    
    public static void main(String args[]) {
        Runnable doCreateAndShowGUI = new Runnable() {
            public void run() {
                SetterRace race = new SetterRace("Property Setter Race");
            }
        };
        SwingUtilities.invokeLater(doCreateAndShowGUI);
    }
    
    /**
     * Handles clicks on Go/Stop buttons to start/stop the animation
     */
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("Go")) {
            if (timer != null) {
                timer.stop();
                timer.start();
            } else {
                timer.start();
            }
        } else if (ae.getActionCommand().equals("Stop")) {
            if (timer != null) {
                timer.stop();
            }
        }
    }
}
