package com.ursic.nikola.movieticketingsystem.employee;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission_group.CAMERA;

public class ScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{


    private static final int REQUEST_CAMERA = 1;
    private ZXingScannerView scannerView;
    private String idEmployee;
    private String nextFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        idEmployee = bundle.getString("idEmployee");
        nextFragment = bundle.getString("nextFragment");
        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);
    }

    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA);
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted) {
                        // Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access camera", Toast.LENGTH_LONG).show();
                    } else {
                        //  Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access and camera", Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(CAMERA)) {
                                if (shouldShowRequestPermissionRationale(CAMERA)) {
                                    showMessageOKCancel("Potrebno je dopustiti rad sa kamerom",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                        requestPermissions(new String[]{CAMERA},
                                                                REQUEST_CAMERA);
                                                    }
                                                }
                                            });
                                    return;
                                }
                            }
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Odustani", null)
                .create()
                .show();
    }


    @Override
    public void onResume() {
        super.onResume();
            int currentapiVersion = android.os.Build.VERSION.SDK_INT;
            if (currentapiVersion >= android.os.Build.VERSION_CODES.M) {
                if (checkPermission()) {
                    if (scannerView == null) {
                        scannerView = new ZXingScannerView(this);
                        setContentView(scannerView);
                    }
                    scannerView.setResultHandler(this);
                    scannerView.startCamera();
                } else {
                    requestPermission();
                }
            }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        scannerView.stopCamera();

    }

    @Override
    public void handleResult(Result result) {
        //Toast.makeText(this, result.getText(), Toast.LENGTH_SHORT).show();
        String idTicket = result.getText();
        finish();
        Intent intent = new Intent(ScanActivity.this, EmployeeHomeActivity.class);
        intent.putExtra("idEmployee", idEmployee);
        intent.putExtra("nextFragment", nextFragment);
        intent.putExtra("idTicket", idTicket);
        startActivity(intent);

    }


    @Override
    public void onBackPressed() {
       // scannerView.stopCamera();
        finish();
        Intent intent = new Intent(ScanActivity.this, EmployeeHomeActivity.class);
        intent.putExtra("idEmployee", idEmployee);
        startActivity(intent);
    }
}
