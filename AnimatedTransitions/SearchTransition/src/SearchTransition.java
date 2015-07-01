import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.interpolation.PropertySetter;
import org.jdesktop.animation.transitions.Effect;
import org.jdesktop.animation.transitions.EffectsManager;
import org.jdesktop.animation.transitions.ScreenTransition;
import org.jdesktop.animation.transitions.TransitionTarget;
import org.jdesktop.animation.transitions.effects.CompositeEffect;
import org.jdesktop.animation.transitions.effects.FadeIn;
/*
 * SearchTransition.java
 *
 * Created on May 3, 2007, 3:05 PM
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
public class SearchTransition extends JComponent implements TransitionTarget, 
        ActionListener {
    //
    // GUI components used in the application screens
    //
    JLabel instructions = new JLabel("Search and ye shall find...");
    JLabel searchLabel = new JLabel("Search:");
    JTextField searchField = new JTextField("");
    JEditorPane results = new JEditorPane("text/html",
            "<html><body><b>Dung Beetles</b>: An Ode<br/>" +
            "My Life with <b>Dung Beetles</b><br/>" +
            "<b>Beetle</b> Bailey Gets Latrine Duty<br/>" +
            "Evolution's Oddities<br/>" +
            "Society's Parasites<br/>" +
            "You <b>Dung</b> Me Wrong: A Country Music History<br/>" +
            "Ding, <b>Dung</b>, The Witch is Dead<br/>" +
            "'To be or not to <b>beetle</b>'<br/>" +
            "Gross Insects of the World<br/>" +
            "Nature's Sanitation Engineers<br/>" +
            "Why are they here?<br/>" +
            "</body></html>");
    JScrollPane scroller = new JScrollPane(results);
    private static final int LABEL_W = 50;
    private static final int LABEL_H = 20;
    private static final int FIELD_W = 100;
    private static final int FIELD_H = 20;
    private static final int INSTRUCTIONS_W = 170;
    private static final int INSTRUCTIONS_H = 20;
    private static final int RESULTS_X = 30;
    
    //
    // Animation variables
    //
    Animator animator = new Animator(500);    // Animate for half-second
    // Setup transition with:
    //      "this" as the transition container
    //      "this" as the TransitionTarget callback object
    //      animator as the animator that drives the transition
    ScreenTransition transition = new ScreenTransition(this,
            this, animator);
    private CompositeEffect moverFader = null;
    
    // 
    // Misc other instance variables
    //
    private int currentScreen = 0;      // Which screen are we on?
    private int prevHeight = -1;
    Paint bgGradient = null;
    int prevW, prevH;
    

    /** Creates a new instance of SearchTransition */
    public SearchTransition() {
        results.setEditable(false);
        
        // Setup the animation parameters
        animator.setAcceleration(.2f);  // Accelerate for first 20%
        animator.setDeceleration(.4f);  // Decelerate for last 40%
        
        // Set this as the listener for entries in the search field
        searchField.addActionListener(this);
        
        instructions.setFont(instructions.getFont().deriveFont(15f));
    }
    
    @Override
    public void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, y, w, h);

        if (w != prevW || h != prevH) {
            // Setup GUI for current screen given new size of our container
            setupNextScreen();
            prevW = w;
            prevH = h;
        }
    }
    
    /**
     * Arrange the GUI for the initial search screen.
     */
    private void setupSearchScreen() {
        int instructionsX =  (getWidth() - INSTRUCTIONS_W) / 2;
        int instructionsY = getHeight() / 4;
        int searchX = (getWidth() - LABEL_W - FIELD_W - 10) / 2;
        int searchY = instructionsY + INSTRUCTIONS_H + 20;
        int fieldX = searchX + LABEL_W + 10;
        int fieldY = searchY;
        add(instructions);
        add(searchLabel);
        add(searchField);
        instructions.setBounds(instructionsX, instructionsY, 
                INSTRUCTIONS_W, INSTRUCTIONS_H);
        searchLabel.setBounds(searchX, searchY, LABEL_W, LABEL_H);
        searchField.setBounds(fieldX, fieldY, FIELD_W, FIELD_H);
    }
    
    /**
     * Arrange the GUI for the results screen
     */
    public void setupResultsScreen() {
        int searchX = getWidth() - LABEL_W - FIELD_W - RESULTS_X - 10;
        int searchY = 10;
        int fieldX = searchX + LABEL_W + 10;
        int fieldY = searchY;
        int resultsX = RESULTS_X;
        int resultsY = searchY + LABEL_H + 20;
        add(searchLabel);
        add(searchField);
        add(scroller);
        searchLabel.setBounds(searchX, 10, LABEL_W, LABEL_H);
        searchField.setBounds(fieldX, fieldY, FIELD_W, FIELD_H);
        scroller.setBounds(resultsX, resultsY, 
                getWidth() - (2 * resultsX), getHeight() - resultsY - 20);
    }
    
    /**
     * Change the gradient and effect according to the new window size
     */
    private void setupBackgroundAndEffect() {
        // init the background gradient according to current height
        bgGradient = new GradientPaint(0, 0, Color.LIGHT_GRAY.brighter(), 
                0, getHeight(), Color.DARK_GRAY.brighter());
        
        // Init resultsEffect with current component size info
        MoveIn mover = new MoveIn(RESULTS_X, getHeight());
        FadeIn fader = new FadeIn();
        moverFader = new CompositeEffect(mover);
        moverFader.addEffect(fader);
        EffectsManager.setEffect(scroller, moverFader, EffectsManager.TransitionType.APPEARING);
        prevHeight = getHeight();
    }
    
    /**
     * Override of paintComponent() to draw the gradient background
     */
    @Override
    protected void paintComponent(Graphics g) {
        if (bgGradient == null || getHeight() != prevHeight) {
            setupBackgroundAndEffect();
        }
        ((Graphics2D)g).setPaint(bgGradient);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    /**
     * TransitionTarget callback; clear current state and set up
     * state for next screen
     */
    public void setupNextScreen() {
        // Clear out current GUI state
	removeAll();
        switch (currentScreen) {
            case 0:
                setupSearchScreen();
                break;
            case 1:
                setupResultsScreen();
                break;
            default:
                break;
        }
    }
    
    // Handle user hitting Enter in the search field
    public void actionPerformed(ActionEvent ae) {
        if (moverFader == null || prevHeight != getHeight()) {
            setupBackgroundAndEffect();
        }
        // Change currentScreen, used later in setupNextScreen() callback
        currentScreen = (currentScreen == 0) ? 1 : 0;
        transition.start();
    }
    
    private static void createAndShowGUI() {
	JFrame f = new JFrame();
	f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	f.setSize(400, 300);
	SearchTransition component = new SearchTransition();
	f.add(component);
	f.setVisible(true);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
	Runnable doCreateAndShowGUI = new Runnable() {
	    public void run() {
		createAndShowGUI();
	    }
	};
	SwingUtilities.invokeLater(doCreateAndShowGUI);
    }
}

/**
 * Custom effect: moves a component in to its end location
 * from a specified starting point
 */
class MoveIn extends Effect {
    
    private Point startLocation = new Point();
    
    public MoveIn(int x, int y) {
        startLocation.x = x;
        startLocation.y = y;
    }

    /**
     * Handles setup of animation that will vary the location during the
     * transition
     */
    @Override
    public void init(Animator animator, Effect parentEffect) {
        Effect targetEffect = (parentEffect == null) ? this : parentEffect;
        PropertySetter ps;
        ps = new PropertySetter(targetEffect, "location", 
                startLocation, new Point(getEnd().getX(), getEnd().getY()));
        animator.addTarget(ps);
        super.init(animator, parentEffect);
    }
}
