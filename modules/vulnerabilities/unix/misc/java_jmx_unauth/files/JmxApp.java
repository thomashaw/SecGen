package com.secgen;

// Minimal long-running Java process. It does nothing but stay alive so that the
// JMX connector server (enabled by system properties on the command line) keeps
// listening for the lifetime of the JVM.
public class JmxApp {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("SecGen JMX demo app started; JMX management agent is up.");
        final Object lock = new Object();
        synchronized (lock) {
            lock.wait();
        }
    }
}
