package com.wise.pen;

/**
 * Created by rroels on 30/03/15.
 */
public class Tools {

    static public String bufferToString(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("0x%02X ", b));
        }
        return sb.toString();
    }

    static long calculateCoordinate(byte[] data) {
        long value = 0;
        for (int i = 0; i < data.length; i++) {
            value += ((long) data[i] & 0xffL) << (8 * i);
        }
        return value;
    }
/*
    double calculateCoordinate(unsigned char *bytes) {
        //cached fraction values for 3 bits fraction
        static float fractions[8] = {0.0, 0.125, 0.25, 0.375, 0.5, 0.625, 0.75, 0.875};
        //calculate relative integer coordinate value
        int temp = (bytes[1] << 5) | (bytes[0] >> 3);
        //add the fraction
        double result = temp + fractions[(bytes[0] & 0x7)];
        //calculate offset
        temp = ((bytes[3] << 8) | bytes[2]);
        temp = temp * 0x2000;
        //add offset to relative coordinate and return
        return temp + result;
    }
*/
}
