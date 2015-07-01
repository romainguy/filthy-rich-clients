import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
/*
 * TimeResolution.java
 *
 * Created on May 2, 2007, 3:38 PM
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
public class TimeResolution implements ActionListener {
    
    private static int INCREMENT = 5;
    private static int MAX = 50;
    
    /**
     * Measures how much time has elapsed according to both currentTimeMillis()
     * and nanoTime() at each interval. Note that the time reported for 
     * sleep() may not be accurate since the internal sleep timer may not
     * have the appropriate resolution to sleep for the requested time.
     * The main utility of this function is to compare the two timing
     * functions, although it is also interesting to see how the measured
     * time varies from the sleep() time.
     */
    private void measureTimeFunctions(int increment, int max) {
        long startTime = System.currentTimeMillis();
        long startNanos = System.nanoTime();
        long elapsedTimeActual = 0;
        long elapsedTimeMeasured = 0;
        long elapsedNanosMeasured = 0;
        System.out.printf("sleep   currentTimeMillis   nanoTime\n");
        while (elapsedTimeActual < max) {
            try {
                Thread.sleep(increment);
            } catch (Exception e) {}
            long currentTime = System.currentTimeMillis();
            long currentNanos = System.nanoTime();
            elapsedTimeActual += increment;
            elapsedTimeMeasured = currentTime - startTime;
            elapsedNanosMeasured = (currentNanos - startNanos) / 1000000;
            System.out.printf(" %3d           %4d          %4d\n",
                    elapsedTimeActual, elapsedTimeMeasured, elapsedNanosMeasured);
        }
    }

    /**
     * This method measures the actual time slept, compared to the requested
     * sleep() time. We run many iterations for each value of sleep() to
     * get more accurate timing values; this accounts for possible 
     * inaccuracies of our nanoTime() method for small time differences.
     */
    private void measureSleep() {
        System.out.printf("                                 measured\n");
        System.out.printf("sleep time   iterations   total time   per-sleep\n");
        for (int sleepTime = 0; sleepTime <= 20; ++sleepTime) {
            int iterations = (sleepTime == 0) ? 10000 : (1000 / sleepTime);
            long startTime = System.nanoTime();
            for (int i = 0; i < iterations; ++i) {
                try {
                    Thread.sleep(sleepTime);
                } catch (Exception e) {
                }
            }
            long endTime = System.nanoTime();
            long totalTime = (endTime - startTime) / 1000000;
            float calculatedSleepTime = totalTime / (float)iterations;
            System.out.printf("   %2d          %5d         %4d       %5.2f\n", 
                    sleepTime, iterations, totalTime, calculatedSleepTime);
        }
    }

    /**
     * This method is like the measureSleep() method above, only for the
     * wait() method instead of sleep().
     */
    private synchronized void measureWait() {
        System.out.printf("                                measured\n");
        System.out.printf("wait time   iterations   total time   per-wait\n");
        for (int sleepTime = 1; sleepTime <= 20; ++sleepTime) {
            int iterations = (sleepTime == 0) ? 10000 : (1000 / sleepTime);
            long startTime = System.nanoTime();
            for (int i = 0; i < iterations; ++i) {
                try {
                    wait(sleepTime);
                } catch (Exception e) {
                    System.out.println("Exception: " + e);
                    Thread.dumpStack();
                }
            }
            long endTime = System.nanoTime();
            long totalTime = (endTime - startTime) / 1000000;
            float calculatedSleepTime = totalTime / (float)iterations;
            System.out.printf("  %2d          %5d         %4d       %5.2f\n", 
                    sleepTime, iterations, totalTime, calculatedSleepTime);
        }
    }
    
    // Variables used in measurement of Swing timer
    int timerIteration = 0;
    int iterations = 0;
    Timer timer;
    long startTime, endTime;
    int sleepTime;
    
    /**
     * This method is called during the execution of the Swing timer.
     */
    public void actionPerformed(ActionEvent ae) {
        if (++timerIteration > iterations) {
            timer.stop();
            timerIteration = 0;
            endTime = System.nanoTime();
            long totalTime = (endTime - startTime) / 1000000;
            float calculatedDelayTime = totalTime / (float)iterations;
            System.out.printf("  %2d          %5d         %5d        %5.2f\n", 
                    sleepTime, iterations, totalTime, calculatedDelayTime);
        }
    }
       
    /**
     * This method measures the accuracy of the Swing timer, which is 
     * internally dependent upon both the internal timing mechanisms
     * (either currentTimeMillis() or nanoTime()) and the wait() method.
     * So the results we see here should be predictable from the results
     * we see in the other measurement methods.
     */
    public void measureTimer() {
        System.out.printf("                                  measured\n");
        System.out.printf("timer delay   iterations   total time   per-delay\n");
        for (sleepTime = 0; sleepTime <= 20; ++sleepTime) {
            iterations = (sleepTime == 0) ? 1000 : (1000 / sleepTime);
            timerIteration = 1;
            timer = new Timer(sleepTime, this);
            startTime = System.nanoTime();
            timer.start();
            while (timerIteration > 0) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {}
            }
        }
    }

    /**
     * Execute the various timer resolution tests.
     */
    public static void main(String args[]) {
        TimeResolution timeResolution = new TimeResolution();
        timeResolution.measureTimer();
        timeResolution.measureTimeFunctions(INCREMENT, MAX);
        timeResolution.measureSleep();
        timeResolution.measureWait();
    }
    
}
