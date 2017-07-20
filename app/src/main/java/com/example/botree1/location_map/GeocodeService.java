package com.example.botree1.location_map;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by botree1 on 19/1/17.
 */

public class GeocodeService extends IntentService {

    protected ResultReceiver receiver;


    public GeocodeService() {
        super("GeocodeService");
        Log.v("###","start GeocodeService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String errorMessage = " ";
        List<Address> addressArrayList = null;
        double latitude = intent.getDoubleExtra("latitude", 0);
        double longitude = intent.getDoubleExtra("longitude", 0);
        try {
            addressArrayList = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            errorMessage = "Service is not available";
            Log.v("###", errorMessage);
            e.printStackTrace();
        }


        receiver = intent.getParcelableExtra("addressResultReceiver");
        if (addressArrayList == null || addressArrayList.size() == 0) {

            if(errorMessage.isEmpty()) {
                errorMessage = "Not Found";
                Log.v("###", errorMessage);
            }
            deliverResultToReceiver(400,errorMessage,null);
        }else {
            for(Address address : addressArrayList){
                String outputAddress="";
                for(int i=0;i<address.getMaxAddressLineIndex();i++){
                    outputAddress+="_________"+address.getAddressLine(i);
                }

                Log.v("###",outputAddress);

            }

            Address address=addressArrayList.get(0);
            ArrayList<String> addressFragments=new ArrayList<>();

            for(int i=0;i<address.getMaxAddressLineIndex();i++){
                addressFragments.add(address.getAddressLine(i));
            }

            Log.v("###","Address Found");
            deliverResultToReceiver(500, TextUtils.join(System.getProperty("line.separator"), addressFragments),address);
        }






    }


    public void deliverResultToReceiver(int resultcode,String message,Address address){
        Bundle bundle=new Bundle();
        bundle.putParcelable("RESULT_ADDRESS",address);
        bundle.putString("RESULT_MESSAGE",message);
        receiver.send(resultcode,bundle);

    }



}
