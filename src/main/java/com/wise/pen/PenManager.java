package com.wise.pen;

import com.wise.hidapi.HIDAPI;
import com.wise.hidapi.HIDDevice;
import org.hid4java.HidDevice;
import org.hid4java.HidServices;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class PenManager {

    private static int vendorID = 0x1E61;
    private static HidServices hidServices = new HidServices();

    static {
        hidServices.start(500);
    }

    public static List<HidDevice> listPens() {
        List<HidDevice> hid_devices = hidServices.getAttachedHidDevices();
        List<HidDevice> hid_pens = new ArrayList<HidDevice>();
        for (HidDevice dev : hid_devices) {
            if (dev.getVendorId() == vendorID) {
                hid_pens.add(dev);
            }
        }
        return hid_pens;
    }

    public static Pen findPen(int vendorId, int productId, String serialNumber) {
        List<HidDevice> hid_devices = hidServices.getAttachedHidDevices();
        HidDevice pen_device = hidServices.getHidDevice(vendorId, productId, serialNumber);
        if (pen_device != null) {
            return Pen.fromHidDevice(pen_device);
        } else {
            return null;
        }
    }
}
