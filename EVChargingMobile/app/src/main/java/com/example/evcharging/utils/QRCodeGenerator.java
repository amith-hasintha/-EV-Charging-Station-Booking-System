/*
 * File: QRCodeGenerator.java
 * Purpose: Helper to generate QR bitmap using ZXing
 */
package com.example.evcharging.utils;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class QRCodeGenerator {
    /**
     * Generate a QR code bitmap for given text
     * @param text content to encode
     * @param size pixel size
     * @return Bitmap
     */
    public static Bitmap generate(String text, int size) throws WriterException {
        BarcodeEncoder encoder = new BarcodeEncoder();
        return encoder.encodeBitmap(text, BarcodeFormat.QR_CODE, size, size);
    }
}
