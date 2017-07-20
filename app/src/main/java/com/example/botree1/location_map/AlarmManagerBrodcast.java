package com.example.botree1.location_map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by botree1 on 20/1/17.
 */

public class AlarmManagerBrodcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

       String msg=intent.getStringExtra("messageAddress");
        Toast.makeText(context,msg,Toast.LENGTH_LONG).show();

    }
}
