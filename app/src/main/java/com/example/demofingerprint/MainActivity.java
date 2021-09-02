package com.example.demofingerprint;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.integratedbiometrics.ibscanultimate.IBScan;
import com.integratedbiometrics.ibscanultimate.IBScanDevice;
import com.integratedbiometrics.ibscanultimate.IBScanDeviceListener;
import com.integratedbiometrics.ibscanultimate.IBScanException;
import com.integratedbiometrics.ibscanultimate.IBScanListener;

import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity implements IBScanListener, IBScanDeviceListener {

    private IBScan m_ibScan;
    private IBScanDevice m_ibScanDevice;
    private TextView mobileLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mobileLog = findViewById(R.id.log);
        mobileLog.setText("");
        m_ibScan = IBScan.getInstance(this.getApplicationContext());
        m_ibScan.setScanListener(this);
        final UsbManager manager = (UsbManager) this.getApplicationContext().getSystemService(Context.USB_SERVICE);
        final HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
       logOnUIThread("searching for connected device while looping");
        if (deviceList.isEmpty()) {
           logOnUIThread("\ndevice list is empty");
        } else {
            for (UsbDevice device : deviceList.values()) {
                final boolean isScanDevice = IBScan.isScanDevice(device);
               logOnUIThread("\nfound a device but validate if it is a scan device");
                if (isScanDevice) {
                   logOnUIThread("\nfound an IbScan device and check for permission");
                    final boolean hasPermission = manager.hasPermission(device);
                    if (!hasPermission) {
                       logOnUIThread("\nRequest an IbScan device permission");
                        this.m_ibScan.requestPermission(device.getDeviceId());
                    }
                }
            }
        }
    }


    @Override
    public void deviceCommunicationBroken(IBScanDevice ibScanDevice) {
       logOnUIThread("\ndevice Communication Broken");
    }

    @Override
    public void deviceImagePreviewAvailable(IBScanDevice ibScanDevice, IBScanDevice.ImageData imageData) {
       logOnUIThread("\ndevice Image Preview Available");
    }

    @Override
    public void deviceFingerCountChanged(IBScanDevice ibScanDevice, IBScanDevice.FingerCountState fingerCountState) {
       logOnUIThread("\ndevice Finger Count Changed");
    }

    @Override
    public void deviceFingerQualityChanged(IBScanDevice ibScanDevice, IBScanDevice.FingerQualityState[] fingerQualityStates) {
       logOnUIThread("\ndevice Finger Quality Changed");
    }

    @Override
    public void deviceAcquisitionBegun(IBScanDevice ibScanDevice, IBScanDevice.ImageType imageType) {
       logOnUIThread("\ndevice Acquisition Begun");
    }

    @Override
    public void deviceAcquisitionCompleted(IBScanDevice ibScanDevice, IBScanDevice.ImageType imageType) {
        logOnUIThread("\ndevice Acquisition Completed");
        m_ibScanDevice = ibScanDevice;
    }

    @Override
    public void deviceImageResultAvailable(IBScanDevice ibScanDevice, IBScanDevice.ImageData imageData, IBScanDevice.ImageType imageType, IBScanDevice.ImageData[] imageData1) {
       logOnUIThread("\nIbScan device image now available with original size " + imageType);
    }

    @Override
    public void deviceImageResultExtendedAvailable(IBScanDevice ibScanDevice, IBScanException e, IBScanDevice.ImageData imageData, IBScanDevice.ImageType imageType, int i, IBScanDevice.ImageData[] imageData1, IBScanDevice.SegmentPosition[] segmentPositions) {
       logOnUIThread("\nIbScan device image now available with Extended size " + imageType);
    }

    @Override
    public void devicePlatenStateChanged(IBScanDevice ibScanDevice, IBScanDevice.PlatenState platenState) {
       logOnUIThread("\ndevice Platen State Changed");
    }

    @Override
    public void deviceWarningReceived(IBScanDevice ibScanDevice, IBScanException warning) {
       logOnUIThread("\nIbScan device warning " + warning.getType().toString() + " " + warning.getMessage());
    }

    @Override
    public void devicePressedKeyButtons(IBScanDevice ibScanDevice, int i) {
       logOnUIThread("\ndevice Pressed Key Buttons");
    }

    @Override
    public void scanDeviceAttached(int deviceId) {
       logOnUIThread("\nIbScan device attached " + deviceId);

        final boolean hasPermission = m_ibScan.hasPermission(deviceId);
        if (!hasPermission) {
            m_ibScan.requestPermission(deviceId);
        } else {
           logOnUIThread("\nopening device at index 0");
            new Thread(() -> {
                try {
                    m_ibScanDevice = m_ibScan.openDevice(0);
                } catch (IBScanException e) {
                   logOnUIThread(e.getType().name());
                }
            }).start();
        }
    }

    @Override
    public void scanDeviceDetached(int deviceId) {
       logOnUIThread("\nIbScan device Detached " + deviceId);
    }

    @Override
    public void scanDevicePermissionGranted(int deviceId, boolean granted) {
        if (granted) {

           logOnUIThread("\nIbScan device permission Request granted");

            // showToastOnUiThread("Permission granted to device " + deviceId, Toast.LENGTH_SHORT);
        } else {
           logOnUIThread("\nIbScan device permission Request denied");
            //  showToastOnUiThread("Permission denied to device " + deviceId, Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void scanDeviceCountChanged(int deviceId) {
       logOnUIThread("\nscan Device Count Changed " + deviceId);
    }

    @Override
    public void scanDeviceInitProgress(int deviceIndex, int progressValue) {
       logOnUIThread("\nscanDeviceInitProgress " + progressValue + " %");
    }


    @Override
    public void scanDeviceOpenComplete(int deviceIndex, IBScanDevice ibScanDevice, IBScanException e) {
       runOnUiThread(() -> {
           if (ibScanDevice == null) {
               logOnUIThread("\nCan't open the scan device because " + e.getMessage());
           } else {
               logOnUIThread("\nscan Device Open Complete " + deviceIndex);
               m_ibScanDevice = ibScanDevice;
               try {
                   ibScanDevice.beginCaptureImage(IBScanDevice.ImageType.FLAT_SINGLE_FINGER, IBScanDevice.ImageResolution.RESOLUTION_500, IBScanDevice.OPTION_AUTO_CAPTURE);

               } catch (IBScanException ibScanException) {
                   logOnUIThread("\nscan Device Open Complete " + e.getType().name());
                   //  Log.e(Constants.ERROR, "Error Capturing Image because " + e.getMessage());
               }
           }
       });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        for (int i = 0; i < 10; i++) {
            try {
                _ReleaseDevice();
                break;
            } catch (IBScanException ibse) {
                if (ibse.getType().equals(IBScanException.Type.RESOURCE_LOCKED)) {
                } else {
                    break;
                }
            }
        }
    }

    protected void _ReleaseDevice() throws IBScanException {
        if (getIBScanDevice() != null) {
            if (getIBScanDevice().isOpened() == true) {
                getIBScanDevice().close();
                setIBScanDevice(null);
            }
        }

    }

    protected IBScanDevice getIBScanDevice() {
        return (this.m_ibScanDevice);
    }

    protected void setIBScanDevice(IBScanDevice ibScanDevice) {
        m_ibScanDevice = ibScanDevice;
        if (ibScanDevice != null) {
            ibScanDevice.setScanDeviceListener(this);
        }
    }

    public void openDevice(View view) {
        logOnUIThread("open Device clicked");
        new Thread(() -> {
            try {
                m_ibScanDevice = m_ibScan.openDevice(0);
            } catch (IBScanException e) {
               logOnUIThread(e.getType().name());
            }
        }).start();
    }
    private void logOnUIThread(String message){
      runOnUiThread(() ->mobileLog.append("\n"+message));
    }
}