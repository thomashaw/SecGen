/*
 * Copyright (c) 2003-2004, Simon Brown
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution.
 *
 *   - Neither the name of Pebble nor the names of its contributors may
 *     be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package pebble.swing;

import java.awt.*;

import javax.swing.*;

/**
 * An implementation of a splash screen that can either be timed, or
 * closed manually.
 * <br><br>
 * To use, simply create an instance of SplashScreen and then,
 * call setVisible(true) on that instance.
 * <br><br>
 * Here are some examples of how SplashScreen can be used (imagine that you've
 * already create a panel with some text and a logo on called "panel").
 * <pre>
 * // this version stays up until you call setVisible(false)
 * SplashScreen splash = new SplashScreen(panel, 400, 200, 5000);
 * splash.setVisible(true);
 * <br><br>
 * // this version stays up for approximately 5 seconds
 * SplashScreen splash = new SplashScreen(panel, 400, 200, 5000);
 * splash.setVisible(true);
 * </pre>
 *
 * @author      Simon Brown
 */
public class SplashScreen extends JWindow implements LookAndFeelGuidelines {

    protected JComponent content;

    private boolean timed = false;
    private int delay = 1000;

    // internal thread
    private TimerThread thread;

    /**
     * Default, no argument constructor so that this object can be used
     * as a javabean.
     */
    public SplashScreen() {
        // initialise this splashscreen with some default values
        this(null);
    }

    /**
     * Timed constructor.
     *
     * @param   component       the component (usually a panel) to be shown
     *                          in the splash screen
     * @param   delay           the time that this splash screen should stay
     *                          on-screen for (in milliseconds)
     */
    public SplashScreen(JComponent component, int delay) {
        this(component);

        this.setTimed(true);
        this.setDelay(delay);
    }

    /**
     * Not timed constructor.
     *
     * @param   component       the component (usually a panel) to be shown
     *                          in the splash screen
     */
    public SplashScreen(JComponent component) {
        this.setTimed(false);
        this.setContent(component);
    }

    /**
     * Setter for the component to be displayed on this splash screen.
     *
     * @param   content       the component to be displayed
     */
     public void setContent(JComponent content) {
        this.content = content;

        // add component
        if (content != null) {
            content.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK, 1), BorderFactory.createEmptyBorder(SPLASH_OUTSIDE_BORDER_TOP, SPLASH_OUTSIDE_BORDER_LEFT, SPLASH_OUTSIDE_BORDER_BOTTOM, SPLASH_OUTSIDE_BORDER_RIGHT)));
            getContentPane().add(content, BorderLayout.CENTER);
        }

        pack();

	    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    	setLocation((d.width - getSize().width) / 2, (d.height - getSize().height) / 2);
     }

    /**
     * Extension of the superclass setVisible(boolean) that also sets
     * the cursor on this window.
     *
     * @param   b       true if we are show the window, false otherwise
     */
    public void setVisible(boolean b) {
        super.setVisible(b);

        if (b) {
            if (timed) {

                // may need to start a new thread to sleep!
                if (thread != null) {
                    thread.interrupt();
                }

                thread = new TimerThread();
                thread.start();
            }

            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    /**
     * Getter for checking whether this splash screen should be timed.
     *
     * @return      true if this splash screen is timed, false otherwise
     */
     public boolean isTimed() {
        return this.timed;
     }

    /**
     * Setter for whether this splash screen should be timed.
     *
     * @param   timed       true if this splash screen is timed,
     *                      false otherwise
     */
     public void setTimed(boolean timed) {
        this.timed = timed;
     }

    /**
     * Getter for the delay that this splash screen should be shown for
     * (in milliseconds).
     *
     * @return  the delay that this splash screen is displayed for
     */
    public int getDelay() {
        return this.delay;
    }

    /**
     * Setter for the delay that this splash screen should be shown for.
     * (in milliseconds).
     *
     * @param   delay   the delay that this splash screen is to be displayed for
     */
    public void setDelay(int delay) {
        this.delay = delay;
    }

    /**
     * Inner class thread that just waits for a set period of time and
     * then disposes of the window.
     *
     * @author      Simon Brown
     */
    class TimerThread extends Thread {

        /**
         * Constructor specifying the number of milliseconds to wait.
         */
        public TimerThread() {
            super();
        }

        /**
         * Implementation of the standard run() method in this thread.
         */
        public void run() {
            // ... wait for a while ...
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ie) {
                // oh well!
            } finally {
                // ... and then dispose of it
                setVisible(false);
                dispose();
            }
        }
    }

    /**
     * Example code and test for this SplashScreen.
     */
    public static void main(String args[]) {

        /*
         * The simplest use of the splash screen.
         *
        SplashScreen splash = new SplashScreen();
        splash.setVisible(true);
         *
         */

        /*
         * An example, specifying a JComponent, width and height - again
         * not timed.
         *
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(new JLabel("Put whatever you like here ...", SwingConstants.CENTER));
        panel.add(new JLabel("... on this splash screen!", SwingConstants.CENTER));
        panel.setBorder(BorderFactory.createEtchedBorder());

        SplashScreen splash = new SplashScreen(panel, 400, 200);
        splash.setVisible(true);
         *
         */

        /*
         * An example, specifying a JComponent, width and height - this is timed.
         *
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(new JLabel("Put whatever you like here ...", SwingConstants.CENTER));
        panel.add(new JLabel("... on this timed splash screen!", SwingConstants.CENTER));
        panel.setBorder(BorderFactory.createEtchedBorder());

        SplashScreen splash = new SplashScreen(panel, 400, 200, 5000);
        splash.setVisible(true);
         *
         */
    }
}