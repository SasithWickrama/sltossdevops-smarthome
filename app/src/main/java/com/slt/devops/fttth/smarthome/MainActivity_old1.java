package com.slt.devops.fttth.smarthome;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.zxing.Result;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class MainActivity_old1 extends AppCompatActivity {

    WebView detailwebview;
    private final static int FILECHOOSER_RESULTCODE = 1;
    private final static int CAMERA_REQUEST_CODE = 2;
    private ValueCallback<Uri[]> mUploadMessage;

    private static final int REQUEST_EXTERNAL_STORAGE = 5;
    private final String[] permissions = {"Camera", "Media & Storage"};
    private AlertDialog.Builder builder;
    private SharedPreferences.Editor editor;

    public String currentPhotoPath, lat, lon;
    public ZXingScannerView scannerView;
    private static int camId = Camera.CameraInfo.CAMERA_FACING_BACK;
    public String scanid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        detailwebview = findViewById(R.id.web);

        detailwebview.clearCache(true);
        detailwebview.clearHistory();
        detailwebview.getSettings().setJavaScriptEnabled(true);
        detailwebview.getSettings().setAllowFileAccess(true);
        detailwebview.getSettings().setAllowContentAccess(true);
        detailwebview.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        detailwebview.setWebViewClient(new MyWebViewClient());
        detailwebview.setWebChromeClient(new MyWebChromeClient());
        detailwebview.setBackgroundColor(0);
        detailwebview.addJavascriptInterface(new JSScanInterface(this), "smarthome");

        requestPermission();

        checkNet();
    }

   /* @Override
    public void handleResult(Result result) {

        final String myResult = result.getText();
        Log.d("QRCodeScanner", result.getText());
        Log.d("QRCodeScanner", result.getBarcodeFormat().toString());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Scan Result");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                scannerView.resumeCameraPreview(MainActivity.this);
            }
        });
        builder.setNeutralButton("Visit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(myResult));
                startActivity(browserIntent);
            }
        });
        builder.setMessage(result.getText());
        AlertDialog alert1 = builder.create();
        alert1.show();

    }*/


    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String urlString) {
            view.loadUrl("javascript:checkversion('" + getResources().getString(R.string.app_version) + "')");


        }
    }

    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {

            if (mUploadMessage != null) {
                mUploadMessage.onReceiveValue(null);
            }

            mUploadMessage = filePathCallback;
            dispatchTakePictureIntent();
            return true;
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
            AlertDialog dialog = new AlertDialog.Builder(view.getContext()).
                    setTitle("Alert !").
                    setMessage(message).
                    setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //do nothing
                        }
                    }).create();
            dialog.show();
            result.confirm();
            return true;
        }

    }


    public class JSScanInterface {
        Context mContext;

        JSScanInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void scanbarcode(final String buttonid) {
            Log.println(Log.WARN, "MYAPP", "button id " + buttonid);
            scanid = buttonid.replace("btn","");
            if (scannerView == null) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        scannerView = new ZXingScannerView(mContext);
                        setContentView(scannerView);
                        scannerView.setResultHandler(new ZXingScannerView.ResultHandler() {
                            @Override
                            public void handleResult(Result result) {
                                final String myResult = result.getText();
                                Log.d("QRCodeScanner", result.getText());
                                Log.d("QRCodeScanner", result.getBarcodeFormat().toString());

                               /* AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                builder.setTitle("Scan Result");
                                builder.setMessage(result.getText());
                                AlertDialog alert1 = builder.create();
                                alert1.show();*/
                                scannerView.stopCamera();

                                detailwebview.loadUrl("javascript:setscanvalue('" + scanid + "','" + result.getText().toString() + "')");
                                onBackPressed();

                            }
                        });
                        scannerView.startCamera();
                    }
                });

            }
          /*  scannerView.setResultHandler(new ZXingScannerView.ResultHandler() {
                @Override
                public void handleResult(Result result) {
                    final String myResult = result.getText();
                    Log.d("QRCodeScanner", result.getText());
                    Log.d("QRCodeScanner", result.getBarcodeFormat().toString());

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("Scan Result");
                    builder.setMessage(result.getText());
                    AlertDialog alert1 = builder.create();
                    alert1.show();
                }
            });*/

        }
    }


    @Override
    public void onBackPressed() {
        if (detailwebview.canGoBack()) {
            String Url = detailwebview.copyBackForwardList().getItemAtIndex(detailwebview.copyBackForwardList().getCurrentIndex() - 1).getUrl();
            if (Url.contains("auth.php")) {
                detailwebview.loadUrl("https://serviceportal.slt.lk/smarthome/login.php");
            }
        } else {
            // Let the system handle the back button
            super.onBackPressed();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
       /* if (scannerView == null) {
            scannerView = new ZXingScannerView(this);
            setContentView(scannerView);
        }
        scannerView.setResultHandler(this);
        scannerView.startCamera();*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scannerView.stopCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        super.onActivityResult(requestCode, resultCode, intent);
        detailwebview.loadUrl("javascript:myJavaScriptFunc('" + lon + "','" + lat + "')"); //if passing in an object. Mapping may need to take place
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage || intent == null || resultCode != RESULT_OK) {
                return;
            }

            Uri[] result = null;
            String dataString = intent.getDataString();

            if (dataString != null) {
                result = new Uri[]{Uri.parse(dataString)};
            }

            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (null == mUploadMessage || resultCode != RESULT_OK) {
                return;
            }

            Uri[] result = null;
            Log.i("myapp", currentPhotoPath);


            File file = new File(currentPhotoPath);
            Log.i("myapp", String.valueOf(file.length()));
            int compressionRatio = (50000 / (Integer.parseInt(String.valueOf(file.length())) / 1024));
            ; //1 == originalImage, 2 = 50% compression, 4=25% compress
            Log.i("myapp", String.valueOf(compressionRatio));
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, compressionRatio, fos);
                fos.close();
            } catch (Throwable t) {
                Log.e("ERROR", "Error compressing file." + t.toString());
                t.printStackTrace();
            }


            if (currentPhotoPath != null) {
                result = new Uri[]{Uri.parse("file:" + currentPhotoPath)};
            }

            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
    }


    public void checkNet() {
        if (haveNetworkConnection()) {
            detailwebview.loadUrl("https://serviceportal.slt.lk/smarthome/login.php");
        } else {
            Toast.makeText(getApplicationContext(), "No internet connection was found!", Toast.LENGTH_LONG);

            new android.app.AlertDialog.Builder(this)
                    .setCancelable(true)
                    .setMessage("Plese connect to internet to proceed")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            checkNet();
                        }
                    })
                    .show();
        }
    }

    public boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        android.net.ConnectivityManager cm = (android.net.ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED /* ||
                   ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED*/) {
                ActivityCompat.requestPermissions(MainActivity_old1.this,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
                // Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
            }
        }
    }


    public void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.slt.devops.smarthome.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }


    public File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //  File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );


        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();

        return image;
    }

}