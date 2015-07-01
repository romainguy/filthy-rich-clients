import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SwingUtilities;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTargetAdapter;
/*
 * BasicRace.java
 *
 * Created on May 3, 2007, 7:37 AM
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
 * The simplest version of the animation; set up a Animator to
 * move the car from one position to another over a given time period.
 * 
 * 
 * @author Chet
 */
public class BasicRace extends TimingTargetAdapter implements ActionListener {
    
    public static final int RACE_TIME = 2000;    
    Point start = TrackView.START_POS;
    Point end = TrackView.FIRST_TURN_START;
    Point current = new Point();
    protected Animator animator;
    TrackView track;
    RaceControlPanel controlPanel;
    
    /** Creates a new instance of BasicRace */
    public BasicRace(String appName) {
        RaceGUI basicGUI = new RaceGUI(appName);
        controlPanel = basicGUI.getControlPanel();
        controlPanel.addListener(this);
        track = basicGUI.getTrack();
        animator = new Animator(RACE_TIME, this);
    }
    
    //
    // Events
    //
    
    /**
     * This receives the Go/Stop events that start/stop the animation
     */
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("Go")) {
            animator.stop();
            animator.start();
        } else if (ae.getActionCommand().equals("Stop")) {
            animator.stop();
        }
    }
    
    /**
     * TimingTarget implementation: calculate and set the current
     * car position based on the animation fraction
     */
    public void timingEvent(float fraction) {
        // Simple linear interpolation to find current position
        current.x = (int)(start.x + (end.x - start.x) * fraction);
        current.y = (int)(start.y + (end.y - start.y) * fraction);
        
        // set the new position; this will force a repaint in TrackView
        // and will display the car in the new position
        track.setCarPosition(current);
    }

    public static void main(String args[]) {
        Runnable doCreateAndShowGUI = new Runnable() {
            public void run() {
                BasicRace race = new BasicRace("BasicRace");
            }
        };
        SwingUtilities.invokeLater(doCreateAndShowGUI);
    }
    
}
