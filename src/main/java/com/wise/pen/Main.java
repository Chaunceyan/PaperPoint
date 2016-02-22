package com.wise.pen;

import org.hid4java.*;

public class Main {

    public static void main(String[] args) {

        Pen p = PenManager.findPen("00-07-cf-63-b1-e4");
        if (p == null) {
            System.out.println("Could not find pen with serial 00-07-cf-63-b1-e4");
            return;
        }
        p.open();

        p.requestBatteryStatus();

        HIDAPI.hid_exit();

    }

}
