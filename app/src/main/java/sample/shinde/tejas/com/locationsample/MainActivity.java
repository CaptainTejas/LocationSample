package sample.shinde.tejas.com.locationsample;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LocationListener {

    final String TAG = "GPS";

    private final static int ALL_PERMISSION_RESULT = 101;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BW_UPDATES = 1000*60*1;

    TextView tvLatitude, tvLongitude,tvTime;

    LocationManager locationManager;
    Location loc;

    ArrayList<String> permission = new ArrayList<>();
    ArrayList<String> permissionToRequest;
    ArrayList<String> permissionRejected = new ArrayList();

    boolean isGPS = false;
    boolean isNetwork = false;
    boolean canGetLocation = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLatitude = (TextView)findViewById(R.id.tvLatitude);
        tvLongitude = (TextView)findViewById(R.id.tvLongitude);
        tvTime = (TextView)findViewById(R.id.tvTime);

        locationManager = (LocationManager)getSystemService(Service.LOCATION_SERVICE);
        isGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        permission.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permission.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionToRequest = findUnAskedPermissions(permission);

        if(!isGPS&&!isNetwork){
            Log.d(TAG,"Connection off");
            showSettingAlert();
            getLastLocation();

        }else{
            Log.d(TAG,"Connection on");

            if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
                if(permissionToRequest.size()>0){
                    requestPermissions(permissionToRequest.toArray(new String[permissionToRequest.size()]),ALL_PERMISSION_RESULT);
                    Log.d(TAG,"Permission requests");
                    canGetLocation =false;

                }
            }

            getLocation();
        }


    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "OnLocationChanged");
        updateUI(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {}

    @Override
    public void onProviderEnabled(String s) {
        getLocation();
    }

    @Override
    public void onProviderDisabled(String s) {
        if(locationManager != null){
            locationManager.removeUpdates(this);
        }
    }

    private void getLocation(){
        try{
            if(canGetLocation){
                Log.d(TAG,"Can get location");
                if(isGPS){
                    Log.d(TAG,"GPS ON");
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,this
                    );

                    if(locationManager != null){
                        loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if(loc != null) updateUI(loc);
                    }

                }else if (isNetwork){
                    Log.d(TAG, "NETWORK PROVIDER on");
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            this
                    );

                    if (locationManager != null){
                        loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if(loc != null) updateUI(loc);
                    }
                }else{
                    loc.setLatitude(0);
                    loc.setLongitude(0);
                    updateUI(loc);
                }
            }else{
                Log.d(TAG,"Cant get Location");
            }

        }catch (SecurityException e){
            e.printStackTrace();
        }
    }

    private void getLastLocation(){
        try{
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria,false);
            Location location = locationManager.getLastKnownLocation(provider);
            Log.d(TAG,provider);
            Log.d(TAG,location == null ? "No lastLocation ":location.toString());



        }catch (SecurityException e){
            e.printStackTrace();
        }
    }

    private ArrayList findUnAskedPermissions(ArrayList<String> wanted){
        ArrayList result = new ArrayList();
        for (String perm : wanted){
            if(!hasPermission(perm)){
                result.add(perm);
            }
        }
        return  result;
    }

    private boolean hasPermission(String permission ){
        if(canAskPermission()){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                return (checkSelfPermission(permission)== PackageManager.PERMISSION_GRANTED);

            }
        }

        return true;
    }

    private  boolean canAskPermission(){
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case ALL_PERMISSION_RESULT:
                Log.d(TAG,"onRequestPermissionResult");
                for(String perms : permissionToRequest){
                    if(!hasPermission(perms)){
                        permissionRejected.add(perms);
                    }

                }

                if(permissionRejected.size() > 0){
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        if(shouldShowRequestPermissionRationale(permissionRejected.get(0))){
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access ",
                            new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                                        requestPermissions(permissionRejected.toArray( new String[permissionRejected.size()]),
                                                ALL_PERMISSION_RESULT
                                                );
                                    }

                                }
                            });
                            return;
                        }
                    }
                }else{
                    Log.d(TAG,"No rejected permissions");
                    canGetLocation = true;
                    getLocation();
                }
                break;
        }
    }

    public void showSettingAlert(){
        AlertDialog.Builder alertdialog =new AlertDialog.Builder(this);
        alertdialog.setTitle("GPS is not Enabled");
        alertdialog.setMessage("Do you want to turn on GPS?");
        alertdialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        alertdialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        alertdialog.show();
    }

    private  void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener){
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK",okListener)
                .setNegativeButton("Cancel",null)
                .create()
                .show();

    }

    private  void updateUI(Location loc){
        Log.d(TAG,"updateUI");
        tvLatitude.setText(Double.toString(loc.getLatitude()));
        tvLongitude.setText(Double.toString(loc.getLongitude()));
        tvTime.setText(DateFormat.getTimeInstance().format(loc.getTime()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(locationManager != null){
            locationManager.removeUpdates(this);
        }
    }
}
