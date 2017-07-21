package cargo.floter.driver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public  class IncomingSms extends BroadcastReceiver {
   static SmsListener listener;

    public void setIncomingSms(SmsListener listener ) {
        this.listener = listener;
    }
    public IncomingSms() {
    }
    public void onReceive(Context context, Intent intent) {

        final Bundle bundle = intent.getExtras();

        try {

            if (bundle != null) {

                final Object[] pdusObj = (Object[]) bundle.get("pdus");

                for (int i = 0; i < pdusObj.length; i++) {

                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();
                    String body = currentMessage.getMessageBody().toString();
                    String address = currentMessage.getOriginatingAddress();
                    String senderNum = phoneNumber;
                    String message = currentMessage.getDisplayMessageBody();
                    if(message.contains("is your OTP")){
                        String[] splited = message.split(" is");
                        if(listener!=null)
                            listener.onNewOTP(splited[0]);
                    }
                }
            }

        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" +e);

        }
    }

    public interface SmsListener{
        void onNewOTP(String otp);
    }

    public static void setListener(SmsListener listener){
        IncomingSms.listener = listener;
    }
}