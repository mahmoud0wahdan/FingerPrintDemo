package com.example.demofingerprint;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
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
        mobileLog.append("searching for connected device while looping");
        if (deviceList.isEmpty()) {
            mobileLog.append("\ndevice list is empty");
        } else {
            for (UsbDevice device : deviceList.values()) {
                final boolean isScanDevice = IBScan.isScanDevice(device);
                mobileLog.append("\nfound a device but validate if it is a scan device");
                if (isScanDevice) {
                    mobileLog.append("\nfound an IbScan device and check for permission");
                    final boolean hasPermission = manager.hasPermission(device);
                    if (!hasPermission) {
                        mobileLog.append("\nRequest an IbScan device permission");
                        this.m_ibScan.requestPermission(device.getDeviceId());
                    }
                }
            }
        }
    }


    @Override
    public void deviceCommunicationBroken(IBScanDevice ibScanDevice) {
        mobileLog.append("\ndevice Communication Broken");
    }

    @Override
    public void deviceImagePreviewAvailable(IBScanDevice ibScanDevice, IBScanDevice.ImageData imageData) {
        mobileLog.append("\ndevice Image Preview Available");
    }

    @Override
    public void deviceFingerCountChanged(IBScanDevice ibScanDevice, IBScanDevice.FingerCountState fingerCountState) {
        mobileLog.append("\ndevice Finger Count Changed");
    }

    @Override
    public void deviceFingerQualityChanged(IBScanDevice ibScanDevice, IBScanDevice.FingerQualityState[] fingerQualityStates) {
        mobileLog.append("\ndevice Finger Quality Changed");
    }

    @Override
    public void deviceAcquisitionBegun(IBScanDevice ibScanDevice, IBScanDevice.ImageType imageType) {
        mobileLog.append("\ndevice Acquisition Begun");
    }

    @Override
    public void deviceAcquisitionCompleted(IBScanDevice ibScanDevice, IBScanDevice.ImageType imageType) {
        mobileLog.append("\ndevice Acquisition Completed");
        m_ibScanDevice = ibScanDevice;
    }

    @Override
    public void deviceImageResultAvailable(IBScanDevice ibScanDevice, IBScanDevice.ImageData imageData, IBScanDevice.ImageType imageType, IBScanDevice.ImageData[] imageData1) {
        mobileLog.append("\nIbScan device image now available with original size " + imageType);
    }

    @Override
    public void deviceImageResultExtendedAvailable(IBScanDevice ibScanDevice, IBScanException e, IBScanDevice.ImageData imageData, IBScanDevice.ImageType imageType, int i, IBScanDevice.ImageData[] imageData1, IBScanDevice.SegmentPosition[] segmentPositions) {
        mobileLog.append("\nIbScan device image now available with Extended size " + imageType);
    }

    @Override
    public void devicePlatenStateChanged(IBScanDevice ibScanDevice, IBScanDevice.PlatenState platenState) {
        mobileLog.append("\ndevice Platen State Changed");
    }

    @Override
    public void deviceWarningReceived(IBScanDevice ibScanDevice, IBScanException warning) {
        mobileLog.append("\nIbScan device warning " + warning.getType().toString() + " " + warning.getMessage());
    }

    @Override
    public void devicePressedKeyButtons(IBScanDevice ibScanDevice, int i) {
        mobileLog.append("\ndevice Pressed Key Buttons");
    }

    @Override
    public void scanDeviceAttached(int deviceId) {
        mobileLog.append("\nIbScan device attached " + deviceId);

        final boolean hasPermission = m_ibScan.hasPermission(deviceId);
        if (!hasPermission) {
            m_ibScan.requestPermission(deviceId);
        } else {
            mobileLog.append("\nopening device at index 0");
            new Thread(() -> {
                try {
                    m_ibScanDevice = m_ibScan.openDevice(0);
                } catch (IBScanException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @Override
    public void scanDeviceDetached(int deviceId) {
        mobileLog.append("\nIbScan device Detached " + deviceId);
    }

    @Override
    public void scanDevicePermissionGranted(int deviceId, boolean granted) {
        if (granted) {

            new Thread(() -> {
                try {
                    mobileLog.append("\nIbScan device permission Request granted " + deviceId);
                    m_ibScanDevice = m_ibScan.openDevice(0);
                } catch (IBScanException e) {
                    e.printStackTrace();
                }
            }).start();

            // showToastOnUiThread("Permission granted to device " + deviceId, Toast.LENGTH_SHORT);
        } else {
            mobileLog.append("\nIbScan device permission Request denied");
            //  showToastOnUiThread("Permission denied to device " + deviceId, Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void scanDeviceCountChanged(int deviceId) {
        mobileLog.append("\nscan Device Count Changed " + deviceId);
    }

    @Override
    public void scanDeviceInitProgress(int deviceIndex, int progressValue) {
        mobileLog.append("\nscanDeviceInitProgress " + progressValue + " %");
    }

    @Override
    public void scanDeviceOpenComplete(int deviceIndex, IBScanDevice ibScanDevice, IBScanException e) {
        if (ibScanDevice == null) {
            mobileLog.append("\nCan't open the scan device because " + e.getMessage());
        } else {
            mobileLog.append("\nscan Device Open Complete " + deviceIndex);
            m_ibScanDevice = ibScanDevice;
            try {
                ibScanDevice.beginCaptureImage(IBScanDevice.ImageType.FLAT_SINGLE_FINGER, IBScanDevice.ImageResolution.RESOLUTION_500, IBScanDevice.OPTION_AUTO_CAPTURE);

            } catch (IBScanException ibScanException) {
                mobileLog.append("\nscan Device Open Complete " + e.getMessage());
                //  Log.e(Constants.ERROR, "Error Capturing Image because " + e.getMessage());
            }
        }
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

}