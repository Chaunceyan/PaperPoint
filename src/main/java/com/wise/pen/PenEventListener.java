package com.wise.pen;

import java.util.EventListener;

import static com.sun.activation.registries.LogSupport.log;

public class PenEventListener implements EventListener {
    /**
     * Trying to implement simple Listener
     * which can be accessed by other components easily.
     * Authored by Chauncey Yan.
     */
    public void penEventReceive(PenEvent event) {
        log("An event has been captured");
    }
}
