/*
 * File: QRScannerActivity.java
 * Purpose: Operator QR scanning screen (uses ZXing)
 */
package com.example.evcharging.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.journeyapps.barcodescanner.CaptureActivity;

public class QRScannerActivity extends CaptureActivity {
    // For ZXing embedded we can use CaptureActivity as-is or extend to handle callbacks via Intent
    // See ZXing docs for wiring scanning result via startActivityForResult or registerForActivityResult
}
