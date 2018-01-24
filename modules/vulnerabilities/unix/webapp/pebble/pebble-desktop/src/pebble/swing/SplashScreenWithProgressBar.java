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
import java.util.MissingResourceException;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JProgressBar;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * A dialog with a progress bar that can be used when long
 * tasks are being performed.  For example, loading an
 * application.
 * <br><br>
 * Here's an example of how to use it.
 * <br><br>
 * <pre>
 * // this application has 10 steps to loading it all
 * SplashScreenWithProgressBar progress = new SplashScreenWithProgressBar(null, 10, "Loading...");
 * progress.setComponent(p);
 * progress.setSize(500, 200);
 * progress.show();
 * <br><br>
 * progress.incrementProgress("Loading item #1...");
 * progress.incrementProgress("Loading item #2...");
 * progress.incrementProgress("Loading item #3...");
 * ...
 * </pre>
 *
 * @author      Simon Brown
 */
public class SplashScreenWithProgressBar extends SplashScreen implements LookAndFeelGuidelines {

    private JProgressBar progressBar;
    private JLabel progressBarLabel;
    private int currentProgress = 0;
    private int maximumProgress = 0;

    /**
     * A constructor that opens a fully configued dialog.
     *
     * @param   maximumProgress     the max count for the progress bar
     * @param   message             a starting message for the progress bar
     */
    public SplashScreenWithProgressBar(int maximumProgress, String message, JComponent component) {
        super();

        progressBar = new JProgressBar();
        progressBarLabel = new JLabel(message);
        progressBarLabel.setLabelFor(progressBar);
        progressBar.setMinimum(0);
        this.maximumProgress = maximumProgress;
        progressBar.setMaximum(maximumProgress);
        progressBar.setStringPainted(true);

        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(progressBarLabel, BorderLayout.NORTH);
        p.add(progressBar, BorderLayout.SOUTH);

        JPanel content = new JPanel(new BorderLayout());
        content.add(p, BorderLayout.SOUTH);
        content.add(component, BorderLayout.CENTER);

        setContent(content);

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    /**
     * Allows an external class to increment the count of the progress
     * bar and set a new message.
     *
     * @param   message     the message to display above the progress bar
     */
    public synchronized void incrementProgress(String message) {
        progressBarLabel.setText(message);
        incrementProgress();
    }

    /**
     * Allows an external class to increment the count of the progress
     * bar.  This version of the method retains the current progress bar
     * message.
     */
    public synchronized void incrementProgress() {
        progressBar.setValue(++currentProgress);
    }

    public static void main(String args[]) {
        SplashScreenWithProgressBar splash;
        
        // show the application splash screen
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel("My application", SwingConstants.CENTER), BorderLayout.CENTER);

        try {
            p.add(new JLabel("Image"), BorderLayout.NORTH);
        } catch (MissingResourceException mre) {
        }

        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        splash = new SplashScreenWithProgressBar(4, "Loading stuff 1...", p);
        splash.setVisible(true);

        try {
            Thread.sleep(500);
            splash.incrementProgress("Loading stuff 2...");
            Thread.sleep(500);
            splash.incrementProgress("Loading stuff 3...");
            Thread.sleep(500);
            splash.incrementProgress("Loading stuff 4...");
            Thread.sleep(500);
            splash.incrementProgress();
            Thread.sleep(500);
        } catch (Exception e) {
        }
    }
}