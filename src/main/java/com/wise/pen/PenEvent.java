package com.wise.pen;

import java.util.Arrays;
import java.util.EventObject;

/**
 * Created by Chauncey on 10/3/15.
 */
public class PenEvent extends EventObject {
    private int eventNumber;
    private double x;
    private double y;

    public PenEvent(Object source, byte[] data) {
        super(source);
        switch (data[0]) {

            // Device Event Report (pg 11)
            case 0x05:
                switch (data[1]) {
                    case 0x01:
                        eventNumber = 0;
                        System.out.println("Device Started");
                        break;
                    case 0x05:
                        //clear to send
                        break;
                    case 0x06:
                        break;
                    case 0x07:
                        eventNumber = 1;
                        System.out.println("Battery Almost Empty");
                        break;
                    case 0x13:
                        break;
                    default:
                        System.out.println("Undefined Event Report");
                }


                // Device Multiple Report (pg 10)
            case 0x0B:
                while (data[1]-- > 0) {
                    System.out.println("Multiple Report data[1]: " + data[1]);
                    System.out.println("Multiple Report data[2]: " + data[2]);
                }
                //if(data[1] == 0x0){ // Received Request To Send
                //    write(new byte[]{0x14,0x01,0x0});
                //}else if(data[1] == 0x01) { // Waiting for Acknowledgement
                //    write(new byte[]{0x14, 0x02, data[3]});
                //}
                break;

            // Device Position Report (pg 9)
            case 0x0E:
                eventNumber = 2;
                x = Tools.calculateCoordinate(Arrays.copyOfRange(data, 1, 5));
                y = Tools.calculateCoordinate(Arrays.copyOfRange(data, 5, 9));
                int force = data[9];
                System.out.printf("x: %.0f, y: %.0f, force: %d\n", x, y, force);
                break;

            // Device No-Position Report (pg 9)
            case 0x0F:
                switch (data[1]) {
                    // i
                    case 0:
                        System.out.println("Decode Failed");
                        break;
                    case 1:
                        System.out.println("Locked Segment");
                        break;
                    case 2:
                        System.out.println("Not An ANOTO Paper");
                        break;
                    case 3:
                        System.out.println("Frame Skipped");
                        break;
                    case 4:
                        System.out.println("Camera Restarted");
                        break;
                    case 5:
                        System.out.println("Pen Down");
                        break;
                    case 6:
                        System.out.println("Pen Up");
                        break;
                    default:
                        System.out.println("Code Undefined");
                        break;
                }
                break;

            // Ignore the rest
            default:
                System.out.println("Undefined Element");
                break;
        }

    }

    // Get event from the Event object
    public int getEventNumber() {
        return eventNumber;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}