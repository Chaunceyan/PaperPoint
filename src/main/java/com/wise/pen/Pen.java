package com.wise.pen;

import org.hid4java.*;
import org.hid4java.jna.HidApi;
import org.hid4java.jna.HidDeviceStructure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Pen {

    private static int queue_size = 1000;
    public boolean connected;
    public String path;
    public int vendor_id;
    public int product_id;
    public String serial_number;
    public int battery;
    public int memory;
    private Thread readLoopThread;
    private Thread consumerThread;
    private BlockingQueue msg_queue;
    private HidDeviceStructure hid_handle;
    private ArrayList<PenEventListener> listeners;

    // private constructor means you can not instantiate this class manually
    // use constructor Pen.fromHidDevice()
    private Pen() {
        this.connected = false;
        this.path = "";
        this.vendor_id = 0;
        this.product_id = 0;
        this.serial_number = "";
        this.hid_handle = null;
        listeners = new ArrayList<PenEventListener>();
        msg_queue = new ArrayBlockingQueue(queue_size);
    }

    static Pen fromHidDevice(HidDevice dev) {
        Pen p = new Pen();
        p.path = dev.getPath();
        p.product_id = dev.getProductId();
        p.vendor_id = dev.getVendorId();
        p.serial_number = dev.getSerialNumber();
        return p;
    }

    private void consumeBuffer(byte[] data) {

        switch (data[0]) {

            // Device Event Report (pg 11)
            case 0x05:
                switch (data[1]) {
                    case 0x01:
                        System.out.println("Device Started");
                        break;
                    case 0x05:
                        //clear to send
                        break;
                    case 0x06:
                        break;
                    case 0x07:
                        System.out.println("Battery Almost Empty");
                        break;
                    case 0x0F:
                        this.battery = data[2];
                        System.out.println("Battery: " + this.battery + "%");
                        break;
                    case 0x10:
                        this.memory = data[2];
                        System.out.println("Memory: " + this.memory + "%");
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


            // Device Request Report (pg 12)
            case 0x0D:
                if (data[1] == 0x0) { // Received Request To Send
                    write(new byte[]{0x14, 0x01, 0x0});
                } else if (data[1] == 0x01) { // Waiting for Acknowledgement
                    write(new byte[]{0x14, 0x02, data[3]});
                }
                break;

            // Device Position Report (pg 9)
            case 0x0E:
                double x = Tools.calculateCoordinate(Arrays.copyOfRange(data, 1, 5));
                double y = Tools.calculateCoordinate(Arrays.copyOfRange(data, 5, 9));
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

    public void open() {
        hid_handle = HidApi.open(vendor_id, product_id, serial_number);
        readLoopThread = new Thread(new ReadLoop(msg_queue, hid_handle));
        consumerThread = new Thread(new ConsumerLoop(msg_queue));
        readLoopThread.start();
        consumerThread.start();
    }

    public void open(PenEventListener listener) {
        addListener(listener);
        open();
    }

    public void close() {
        HidApi.close(hid_handle);
        readLoopThread.interrupt();
    }

    public void addListener(PenEventListener listener) {
        listeners.add(listener);
    }

    public void removeListener(PenEventListener listener) {
        listeners.remove(listener);
    }

    public void requestBatteryStatus() {
        write(new byte[]{0x14, 0x04, 0x0});
    }

    private int write(byte[] data) {
        return HidApi.write(hid_handle, data);
    }

    /*private void notifyListeners(int eventNumber) {
        // Send the event to event listeners.
        PenEvent event = new PenEvent(eventNumber);

        for (PenEventListener listener : listeners) {
            listener.penEventReceive(event);
        }
    }*/

    // this runnable does a blocking read to retrieve device reports
    private static class ReadLoop implements Runnable {

        private final BlockingQueue<byte[]> queue;
        private HidDeviceStructure hid_handle;

        ReadLoop(BlockingQueue _queue, HidDeviceStructure _hid_handle) {
            this.hid_handle = _hid_handle;
            this.queue = _queue;
        }

        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    byte[] data = HidApi.hid_read(hid_handle);
                    queue.put(data);
                }
            } catch (Exception e) {
                System.out.println("Readloop interupted!");
            }
        }
    }

    // this runnable takes devices reports, parses them and invokes the listeners if relevant
    private class ConsumerLoop implements Runnable {
        private final BlockingQueue<byte[]> queue;

        ConsumerLoop(BlockingQueue _queue) {
            this.queue = _queue;
        }

        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    while (true) {
                        consumeBuffer(queue.take());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Consumerloop interupted!");
            }
        }
    }

}
