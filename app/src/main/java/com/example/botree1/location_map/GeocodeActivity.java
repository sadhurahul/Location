package com.example.botree1.location_map;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

/**
 * Created by botree1 on 19/1/17.
 */

public class GeocodeActivity extends Activity implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    Location mCurrentLocation;
    String mLastUpdateTime;
    AddressResultReceiver addressResultReceiver;
    AlarmManagerBrodcast alarmManagerBrodcast;
    Context mContext;
    String messageAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.geocode_activity_screen);
        mContext=this;

        addressResultReceiver=new AddressResultReceiver(null);

        Button btnsetAlarm=(Button)findViewById(R.id.btnsetAlarm);
        btnsetAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Date currentDate=new Date();
                Calendar calendar=Calendar.getInstance();
                DateTimeZone timeZone=DateTimeZone.getDefault();
                DateTime dateTime=new DateTime(calendar,timeZone);
                /*DateTime setDate=dateTime.plusDays(3);*/
                Calendar setAsDate=dateTime.toCalendar(Locale.getDefault());
                setAsDate.set(Calendar.MINUTE,46);



                String dateString=setAsDate.toString();
                Log.v("@@@@",dateString);





                AlarmManager alarmManager=(AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
                Intent i=new Intent(mContext,AlarmManagerBrodcast.class);
                i.putExtra("messageAddress",messageAddress);
                PendingIntent pi=PendingIntent.getBroadcast(mContext,0,i,0);
                alarmManager.set(AlarmManager.RTC_WAKEUP,setAsDate.getTimeInMillis(),pi);


            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermissionDialog();

    }

    private void checkPermissionDialog() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.v("###", "start code");
            setLocation();
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            ///////////////request Permission Go to onRequestPermissionRequest
        }


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 100:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults.length > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    //////Permission is Granted
                    Log.v("###", "Permission_Granted");


                } else {
                    //////Permission Cancle....
                    Toast.makeText(this, "please Location Setting Make Enable..", Toast.LENGTH_LONG).show();
                }
        }


    }

    private void setLocation() {


        if (mGoogleApiClient == null) {

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            mGoogleApiClient.connect();
            Log.v("###", "setLocation()");
        }


        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(PRIORITY_HIGH_ACCURACY);


        ////////    Requested Location Dialog

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);

        final PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.v("###", "All location settings are satisfied.");

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i("###", "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(GeocodeActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.v("###", "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i("###", "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.v("###", "User agreed to make required location settings changes.");
                        getLocation();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.v("###", "User chose not to make required location settings changes.");
                        break;
                }

                break;
        }
    }

    private void getLocation() {
        if (null != mCurrentLocation) {
            String lat = String.valueOf(mCurrentLocation.getLatitude());
            String lng = String.valueOf(mCurrentLocation.getLongitude());
            Log.d("###", "At Time: " + mLastUpdateTime + "\n" +
                    "Latitude: " + lat + "\n" +
                    "Longitude: " + lng + "\n" +
                    "Accuracy: " + mCurrentLocation.getAccuracy() + "\n" +
                    "Provider: " + mCurrentLocation.getProvider());


                Intent i=new Intent(this,GeocodeService.class);
                 i.putExtra("addressResultReceiver",addressResultReceiver);
                 i.putExtra("latitude",Double.parseDouble(lat));
                 i.putExtra("longitude",Double.parseDouble(lng));
                 startService(i);





        } else {
            Log.d("####", "location is null ...............");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.v("###","onConnected()");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        Log.d("###", "Location update started ..............: ");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.v("###","OnconnectionFailed() :"+connectionResult);

    }

    @Override
    public void onLocationChanged(Location location) {
              Log.v("###","locationChanged");
        mCurrentLocation=location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        getLocation();

    }

    class AddressResultReceiver extends ResultReceiver{

        /**
         * Create a new ResultReceive to receive results.  Your
         * {@link #onReceiveResult} method will be called from the thread running
         * <var>handler</var> if given, or from an arbitrary thread if null.
         *
         * @param handler
         */
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }


        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if(resultCode==500){
                Address address=resultData.getParcelable("RESULT_ADDRESS");
                String message=resultData.getString("RESULT_MESSAGE");
                messageAddress=message;

                Log.v("###","result :"+""+"address :"+address+"  message : "+message);

            }
        }
    }

}

