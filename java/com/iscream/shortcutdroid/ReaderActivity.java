package com.iscream.shortcutdroid;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

//https://code.tutsplus.com/tutorials/reading-qr-codes-using-the-mobile-vision-api--cms-24680

public class ReaderActivity extends AppCompatActivity {

    String codeRead=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);

        init();
    }

    void init()
    {
        final SurfaceView camView=(SurfaceView)findViewById(R.id.camera_view);
        final TextView barcodeInfo = (TextView)findViewById(R.id.code_info);
        Button acceptBtn=(Button)findViewById(R.id.acceptBtn);
        BarcodeDetector detector=new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.QR_CODE).build();



        int camWidth=1280; //camView.getWidth();
        final CameraSource camSource=new CameraSource.Builder(this, detector).setRequestedPreviewSize(camWidth, camWidth*9/16).build();

        //Log.d("CAMWIDTH", ""+camWidth);


        camView.getHolder().addCallback(new SurfaceHolder.Callback(){
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                try {
                    camSource.start(camView.getHolder());
                } catch (IOException ie) {
                    Log.e("CAMERA SOURCE", ie.getMessage());
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {}

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                camSource.stop();
            }
        });

        detector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() != 0) {
                    /*new AsyncTask<Void, Void, Void>()
                    {
                        String code;
                        @Override
                        protected Void doInBackground(Void... voids) {
                            code=barcodes.valueAt(0).displayValue;
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            barcodeInfo.setText(code);
                            codeRead=code;
                            super.onPostExecute(aVoid);
                        }
                    }*/
                    barcodeInfo.post(new Runnable() {    // Use the post method of the TextView
                        String code;
                        public void run() {
                            code=barcodes.valueAt(0).displayValue;
                            barcodeInfo.setText(    // Update the TextView
                                    code
                            );
                            setCodeRead(code);
                        }
                    });
                }
            }
        });

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] array=codeRead.split("\n");
                setResult(123, getIntent().putExtra("code", array[0]));
                finish();
            }
        });
    }

    private void setCodeRead(String code)
    {
        codeRead=code;
    }
}
