package com.example.evcharging.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.evcharging.R;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class CustomScannerActivity extends AppCompatActivity {

    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;

    /**
     * Called when the activity is first created.
     * Initializes the barcode scanner view and sets up the back button functionality.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_scanner);

        barcodeScannerView = findViewById(R.id.zxing_barcode_scanner);

        // --- THIS IS THE FIX FOR THE BACK BUTTON ---
        ImageButton backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> onBackPressed());
        // --- END OF FIX ---

        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.decode();
    }

    /**
     * Called when the activity is resumed.
     * Resumes the barcode scanner capture process.
     */
    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
    }

    /**
     * Called when the activity is paused.
     * Pauses the barcode scanner capture process.
     */
    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }


    /**
     * Called when the activity is destroyed.
     * Releases resources used by the CaptureManager.
     */

    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

    /**
     * Called to save the instance state of the activity.
     * Ensures the CaptureManager state is preserved during configuration changes.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }
}
