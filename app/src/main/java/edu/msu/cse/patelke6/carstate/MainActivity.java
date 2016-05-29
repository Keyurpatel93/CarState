package edu.msu.cse.patelke6.carstate;
import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.openxc.VehicleManager;
import com.openxc.measurements.EngineSpeed;
import com.openxc.measurements.FuelConsumed;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.VehicleSpeed;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;


public class MainActivity extends AppCompatActivity  implements ActivityCompat.OnRequestPermissionsResultCallback {


    private VehicleManager mVehicleManager;
    private TextView mEngineSpeedView;
    private TextView mFuelConsumptionView;
    private TextView mVehicleSpeedView;
    private TextView mAlertView;
    private ImageView mMainImageView;
    private CheckBox mEnfroceSeatBeltView;
    private CarData carData = null;
    private Button mIncreaseSeatBtn;
    private Button mDecreaseSeatBtn;
    private TextView mLowerSeatPosition;
    private TextView mMaxSpeedView;

    private TextView mRadio1View;
    private TextView mRadio2View;
    private TextView mRadio3View;


    private static final int MY_PERMISSIONS_REQUEST = 0;


    private static String[] PERMISSIONS_REQUEST = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};


    /**
     * Id to identify a contacts permission request.
     */
    private static final int REQUEST_CONTACTS = 1;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Bundle extras = getIntent().getExtras();
        setContentView(R.layout.activity_main);


        // Here, thisActivity is the current activity
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // todo Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                ActivityCompat.requestPermissions(this, PERMISSIONS_REQUEST,
                        MY_PERMISSIONS_REQUEST);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, PERMISSIONS_REQUEST,
                        MY_PERMISSIONS_REQUEST);
            }
        }

    }

    //Only Init the private variables if permission is granted for Read/Write to External Memory
    private void init() {


        String rootSDPath = Environment.getExternalStorageDirectory().getAbsolutePath();

        File file = new File(rootSDPath + "/cardata.xml");
        if (file.exists()){
            Log.i("File Exist", "Yes");
        } else{
            Log.i("File Exist", "No");
            try {
                InputStream in = getResources().openRawResource(R.raw.cardata);
                FileOutputStream out = new FileOutputStream(rootSDPath + "/cardata.xml");
                byte[] buff = new byte[1024];
                int read = 0;

                try {
                    while ((read = in.read(buff)) > 0) {
                        out.write(buff, 0, read);
                    }
                } finally {
                    in.close();
                    out.close();
                }
            } catch (Exception ex) {
                Log.i("Copy File Failed", ex.toString());
            }

        }

        carData = new CarData(rootSDPath + "/cardata.xml", getApplicationContext());
        carData.setMaxSpeed(1000);
        mEngineSpeedView = (TextView) findViewById(R.id.engine_speed);
        mFuelConsumptionView = (TextView) findViewById(R.id.fuel_consumption);
        mVehicleSpeedView = (TextView) findViewById(R.id.vehicle_speed);
        mAlertView = (TextView) findViewById(R.id.alertView);
        mMainImageView = (ImageView) findViewById(R.id.carImageView);
        mRadio1View = (TextView) findViewById(R.id.radio1View);
        mRadio2View = (TextView) findViewById(R.id.radio2View);
        mRadio3View = (TextView) findViewById(R.id.radio3View);
        mEnfroceSeatBeltView = (CheckBox) findViewById(R.id.enforceSeatBeltCheckBox);
        mMainImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateView();
            }
        });
        mIncreaseSeatBtn = (Button) findViewById(R.id.seatIncrease);
        mIncreaseSeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adjustSeatPosition(5);
            }
        });
        mDecreaseSeatBtn = (Button) findViewById(R.id.seatDecrease);
        mDecreaseSeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adjustSeatPosition(-5);
            }
        });
        mLowerSeatPosition = (TextView) findViewById(R.id.lowerSeatPositionView);
        mMaxSpeedView = (TextView) findViewById(R.id.maxSpeedView);

        updateView();
    }

    @Override
    public void onPause() {
        super.onPause();
        // When the activity goes into the background or exits, we want to make
        // sure to unbind from the service to avoid leaking memory
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {

            if (mVehicleManager != null) {
                Log.i("OpenXC", "Unbinding from Vehicle Manager");
                // Remember to remove your listeners, in typical Android
                // fashion.
                mVehicleManager.removeListener(EngineSpeed.class,
                        mSpeedListener);
                mVehicleManager.removeListener(FuelConsumed.class, mFuelConsumed);
                mVehicleManager.removeListener(VehicleSpeed.class, mVehicleSpeed);
                unbindService(mConnection);
                mVehicleManager = null;
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        // When the activity starts up or returns from the background,
        // re-connect to the VehicleManager so we can receive updates.;


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i("Permission", "In second");
            init();

            if (mVehicleManager == null) {
                Intent intent = new Intent(this, VehicleManager.class);
                bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            }

            android.os.Handler handler = new android.os.Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    updateView();
                    updateView();
                }
            }, 2000);
        }
    }


    EngineSpeed.Listener mSpeedListener = new EngineSpeed.Listener() {
        @Override
        public void receive(Measurement measurement) {
            // When we receive a new EngineSpeed value from the car, we want to
            // update the UI to display the new value. First we cast the generic
            // Measurement back to the type we know it to be, an EngineSpeed.
            final EngineSpeed speed = (EngineSpeed) measurement;
            // In order to modify the UI, we have to make sure the code is
            // running on the "UI thread" - Google around for this, it's an
            // important concept in Android.
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    // Finally, we've got a new value and we're running on the
                    // UI thread - we set the text of the EngineSpeed view to
                    // the latest value
                    //todo call static string values from strings.xml
                    mEngineSpeedView.setText("Engine speed (RPM): "
                            + speed.getValue().doubleValue());
                }
            });
        }
    };

    FuelConsumed.Listener mFuelConsumed = new FuelConsumed.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final FuelConsumed fuelConsumed = (FuelConsumed) measurement;
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mFuelConsumptionView.setText("Fuel Consumed: " + fuelConsumed.getValue().doubleValue());
                }
            });
        }
    };

    VehicleSpeed.Listener mVehicleSpeed = new VehicleSpeed.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final VehicleSpeed vehicleSpeed = (VehicleSpeed) measurement;
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mVehicleSpeedView.setText("Vehicle Speed: " + vehicleSpeed.getValue().doubleValue());
                    if (vehicleSpeed.getValue().doubleValue() > carData.getMaxSpeed()) {
                        mAlertView.setText("Please SLOW Down!!!");
                    } else {
                        mAlertView.setText("");
                    }
                }
            });
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the VehicleManager service is
        // established, i.e. bound.
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.i("OpenXC", "Bound to VehicleManager");
            // When the VehicleManager starts up, we store a reference to it
            // here in "mVehicleManager" so we can call functions on it
            // elsewhere in our code.
            mVehicleManager = ((VehicleManager.VehicleBinder) service)
                    .getService();

            // We want to receive updates whenever the EngineSpeed changes. We
            // have an EngineSpeed.Listener (see above, mSpeedListener) and here
            // we request that the VehicleManager call its receive() method
            // whenever the EngineSpeed changes
            mVehicleManager.addListener(EngineSpeed.class, mSpeedListener);
            mVehicleManager.addListener(FuelConsumed.class, mFuelConsumed);
            mVehicleManager.addListener(VehicleSpeed.class, mVehicleSpeed);
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Log.w("OpenXC", "VehicleManager Service  disconnected unexpectedly");
            mVehicleManager = null;
        }
    };


    /*
    Updates the UI of the application from the values in the CARData.xml file file
     */
    public void updateView() {

        //Personalize Car Settings based on the Auth User if user was authenticated
        carData.personalizeSettings();

        mEnfroceSeatBeltView.setChecked(carData.getEnforceSeatBelt());
        int temp = carData.getMaxSpeed();
        mLowerSeatPosition.setText("Position  " + carData.getLowerSeatPosition());
        if (carData.getMaxSpeed() == 1000)
            mMaxSpeedView.setText("Max Speed Not Set");
        else
            mMaxSpeedView.setText("Max Speed " + carData.getMaxSpeed());

        String[] radioStations = carData.getRadioStations().split(",");
        if (radioStations.length != 0) {
            mRadio1View.setText("Radio Station1: " + radioStations[0]);
            mRadio2View.setText("Radio Station2: " + radioStations[1]);
            mRadio3View.setText("Radio Station3: " + radioStations[2]);
        }


    }

    //Gets called when tapping the button that moves the seat up or back.
    public void adjustSeatPosition(int value) {
        int currentLowerSeatPosition = carData.getLowerSeatPosition();
        int newPosition = currentLowerSeatPosition + value;
        if (newPosition > 100)
            newPosition = 100;
        else if (newPosition < 0)
            newPosition = 0;
        mLowerSeatPosition.setText("Position  " + newPosition);
        carData.setLowerSeatPosition(newPosition);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == MY_PERMISSIONS_REQUEST) {

            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                init();
                Log.i("Permission", "Write and Read permission has now been granted.");

            } else {
                Log.i("Permission", "Write permission was NOT granted.");
                Toast.makeText(this, "Permission required to operate app", Toast.LENGTH_LONG).show();

                //Close App
                android.os.Handler handler = new android.os.Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        System.exit(0);
                    }
                }, 2000);
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}