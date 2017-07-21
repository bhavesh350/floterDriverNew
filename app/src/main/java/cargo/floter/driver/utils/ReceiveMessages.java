package cargo.floter.driver.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by SONI on 5/10/2017.
 */

public class ReceiveMessages extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
//        if (action.equalsIgnoreCase(TheService.DOWNLOADED)) {
//            // send message to activity
//        }
    }
}
