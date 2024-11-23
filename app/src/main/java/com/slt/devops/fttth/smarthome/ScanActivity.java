package com.slt.devops.fttth.smarthome;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanActivity extends AppCompatActivity  {

    public ZXingScannerView scannerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);
        scannerView.setResultHandler(new ZXingScannerView.ResultHandler() {
            @Override
            public void handleResult(Result result) {
                final String myResult = result.getText();
                Log.d("QRCodeScanner", result.getText());
                Log.d("QRCodeScanner", result.getBarcodeFormat().toString());
                Intent returnIntent = new Intent();
                returnIntent.putExtra("scanvalue", result.getText());
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
                return;

            }
        });
        scannerView.startCamera();
    }


}
