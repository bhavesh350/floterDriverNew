package cargo.floter.driver.application;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Display;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

//import com.crashlytics.android.Crashlytics;
import cargo.floter.driver.R;
import cargo.floter.driver.model.Driver;
import cargo.floter.driver.model.RateCard;
import cargo.floter.driver.model.Trip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import io.fabric.sdk.android.Fabric;


public class MyApp extends Application {

    private static ProgressDialog dialog;
    public static String SHARED_PREF_NAME = "APP_PREF";
    private static Context ctx;
    private static MyApp myApplication = null;

    @Override
    public void onLowMemory() {
        Runtime.getRuntime().gc();
        super.onLowMemory();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Fabric.with(this, new Crashlytics());
        ctx = getApplicationContext();
        myApplication = this;
    }

    public static MyApp getApplication() {
        return myApplication;
    }

    public static void spinnerStart(Context context, String text) {
        String pleaseWait = text;
        dialog = ProgressDialog.show(context, pleaseWait, "", true);

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
    }

    public static void spinnerStop() {
        if (dialog != null) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }

    }

    public static boolean isConnectingToInternet(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
        }
        return false;
    }


    public static void popMessage(String titleMsg, String errorMsg,
                                  Context context) {
        // pop error message
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titleMsg).setMessage(errorMsg)
                .setPositiveButton("OK", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void popMessageAndFinish(String titleMsg, String errorMsg,
                                           final Activity context) {
        // pop error message
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle(titleMsg).setMessage(errorMsg)
                .setPositiveButton("OK", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        context.finish();
                        dialog.dismiss();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showMassage(Context ctx, String msg) {
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
    }

    public static final String DISPLAY_MESSAGE_ACTION = "pushnotifications.DISPLAY_MESSAGE";
    public static final String EXTRA_MESSAGE = "message";

    public static void displayMessage(Context context, String message) {
        Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
        intent.putExtra(EXTRA_MESSAGE, message);
        context.sendBroadcast(intent);
    }

    public static long getSharedPrefLong(String preffConstant) {
        long longValue = 0;
        SharedPreferences sp = myApplication.getSharedPreferences(
                SHARED_PREF_NAME, 0);
        longValue = sp.getLong(preffConstant, 0);
        return longValue;
    }

    public static void setSharedPrefLong(String preffConstant, long longValue) {
        SharedPreferences sp = myApplication.getSharedPreferences(
                SHARED_PREF_NAME, 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(preffConstant, longValue);
        editor.commit();
    }

    public static String getSharedPrefString(String preffConstant) {
        String stringValue = "";
        SharedPreferences sp = myApplication.getSharedPreferences(
                SHARED_PREF_NAME, 0);
        stringValue = sp.getString(preffConstant, "");
        return stringValue;
    }

    public static void setSharedPrefString(String preffConstant,
                                           String stringValue) {
        SharedPreferences sp = myApplication.getSharedPreferences(
                SHARED_PREF_NAME, 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(preffConstant, stringValue);
        editor.commit();
    }

    public static int getSharedPrefInteger(String preffConstant) {
        int intValue = 0;
        SharedPreferences sp = myApplication.getSharedPreferences(
                SHARED_PREF_NAME, 0);
        intValue = sp.getInt(preffConstant, 0);
        return intValue;
    }

    public static void setSharedPrefInteger(String preffConstant, int value) {
        SharedPreferences sp = myApplication.getSharedPreferences(
                SHARED_PREF_NAME, 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(preffConstant, value);
        editor.commit();
    }

    public static float getSharedPrefFloat(String preffConstant) {
        float floatValue = 0;
        SharedPreferences sp = myApplication.getSharedPreferences(
                preffConstant, 0);
        floatValue = sp.getFloat(preffConstant, 0);
        return floatValue;
    }

    public static void setSharedPrefFloat(String preffConstant, float floatValue) {
        SharedPreferences sp = myApplication.getSharedPreferences(
                preffConstant, 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putFloat(preffConstant, floatValue);
        editor.commit();
    }

    public static void setSharedPrefArray(String preffConstant, float floatValue) {
        SharedPreferences sp = myApplication.getSharedPreferences(
                preffConstant, 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putFloat(preffConstant, floatValue);
        editor.commit();
    }

    public static boolean getStatus(String name) {
        boolean status;
        SharedPreferences sp = myApplication.getSharedPreferences(
                SHARED_PREF_NAME, 0);
        status = sp.getBoolean(name, false);
        return status;
    }

    public static void setStatus(String name, boolean istrue) {
        SharedPreferences sp = myApplication.getSharedPreferences(
                SHARED_PREF_NAME, 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(name, istrue);
        editor.commit();
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity
                .getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus()
                .getWindowToken(), 0);
    }

    public static Bitmap getimagebitmap(String imagepath) {
        Bitmap bitmap = decodeFile(new File(imagepath));

        // rotate bitmap
        Matrix matrix = new Matrix();
        // matrix.postRotate(MyApplication.getExifOrientation(imagepath));
        // create new rotated bitmap
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);

        return bitmap;
    }

    private static Bitmap decodeFile(File F) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(F), null, o);

            // The new size we want to scale to
            final int REQUIRED_SIZE = 204;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE
                    && o.outHeight / scale / 2 >= REQUIRED_SIZE)
                scale *= 2;

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(F), null, o2);
        } catch (FileNotFoundException e) {
        }
        return null;
    }

    public static double roundDouble(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    static double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }


    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }

    public static String getDeviceId() {

        String android_id = "";
        final TelephonyManager tm = (TelephonyManager) ctx
                .getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = ""
                + Settings.Secure.getString(
                ctx.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(),
                ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
        android_id = deviceUuid.toString();
        return android_id;

    }

    public static BigDecimal roundFloat(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd;
    }

    public static int getDisplayWidth() {
        WindowManager wm = (WindowManager) ctx
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        return width;
    }

    public static int getDisplayHeight() {
        WindowManager wm = (WindowManager) ctx
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        return height;
    }

    public static boolean isEmailValid(String email) {
        String regExpn = "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        if (matcher.matches())
            return true;
        else
            return false;
    }

    public static String convertTime(String date) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            cal.setTime(sdf.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }

//Here you say to java the initial timezone. This is the secret
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
//Will print in UTC
//        System.out.println(sdf.format(cal.getTime()));

//Here you set to your timezone
        sdf.setTimeZone(TimeZone.getDefault());
//Will print on your default Timezone
//        System.out.println(sdf.format(cal.getTime()));
        return sdf.format(cal.getTime());
    }

    public static String millsToDate(long mills) {

        Date d = new Date(mills);
        String format = "MM/dd/yyyy";
        return new SimpleDateFormat(format).format(d);
    }

    public static String millsToDate2(long mills) {

        return new SimpleDateFormat("yyyy-MM-dd").format(new Date(mills));
    }

    public static String millsToTime(long mills) {

        Date d = new Date(mills);
        String format = "hh:mm a";
        return new SimpleDateFormat(format).format(d);
    }

    public static double millsToDayTime(long mills) {

        Date d = new Date(mills);
        String format = "kk:mm";
        String dateString = new SimpleDateFormat(format).format(d);
        double hr = Double.parseDouble(dateString.split(":")[0]);
        double min = Double.parseDouble(dateString.split(":")[1]);
        return hr + (min / 100d);
    }

    public static String parseDateToddMMyyyy(String time) {
        String inputPattern = "dd-MM-yyyy";
        String outputPattern = "dd-MMM-yyyy";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static String getDateOrTimeFromMillis(String x) {
        DateFormat formatter = new SimpleDateFormat("dd/MM/yy - hh:mm a");

        long milliSeconds;
        try {
            milliSeconds = Long.parseLong(x);
        } catch (Exception e) {
            milliSeconds = Long.parseLong(x.replace(".", ""));
        }
        System.out.println(milliSeconds);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);

        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTimeInMillis(System.currentTimeMillis());

        String s = formatter.format(calendar.getTime()).split("-")[0];
        String s1 = formatter.format(calendar2.getTime()).split("-")[0];

        if (s.equals(s1)) {
            return formatter.format(calendar.getTime()).split("-")[1];
        } else {
            return formatter.format(calendar.getTime()).split("-")[0];
        }
    }

    private static final int _A_SECOND = 1000;
    private static final int _A_MINUTE = 60 * _A_SECOND;
    /**
     * One hour (in milliseconds)
     */
    private static final int _AN_HOUR = 60 * _A_MINUTE;
    /**
     * One day (in milliseconds)
     */
    private static final int _A_DAY = 24 * _AN_HOUR;

    public static String getDateOrTimeFromMillis(Long x) {
        DateFormat formatter = new SimpleDateFormat("EEEE, dd MMMM yy");

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(x);

        return formatter.format(calendar.getTime());

    }


    public static void openFile(Context context, File url) throws IOException {
        // Create URI
        File file = url;
        Uri uri = Uri.fromFile(file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        // Check what kind of file you are trying to open, by comparing the url with extensions.
        // When the if condition is matched, plugin sets the correct intent (mime) type,
        // so Android knew what application to use to open the file
        if (url.toString().contains(".doc") || url.toString().contains(".docx")) {
            // Word document
            intent.setDataAndType(uri, "application/msword");
        } else if (url.toString().contains(".pdf")) {
            // PDF file
            intent.setDataAndType(uri, "application/pdf");
        } else if (url.toString().contains(".ppt") || url.toString().contains(".pptx")) {
            // Powerpoint file
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        } else if (url.toString().contains(".xls") || url.toString().contains(".xlsx")) {
            // Excel file
            intent.setDataAndType(uri, "application/vnd.ms-excel");
        } else if (url.toString().contains(".zip") || url.toString().contains(".rar")) {
            // WAV audio file
            intent.setDataAndType(uri, "application/x-wav");
        } else if (url.toString().contains(".rtf")) {
            // RTF file
            intent.setDataAndType(uri, "application/rtf");
        } else if (url.toString().contains(".wav") || url.toString().contains(".mp3")) {
            // WAV audio file
            intent.setDataAndType(uri, "audio/x-wav");
        } else if (url.toString().contains(".gif")) {
            // GIF file
            intent.setDataAndType(uri, "image/gif");
        } else if (url.toString().contains(".jpg") || url.toString().contains(".jpeg") || url.toString().contains(".png")) {
            // JPG file
            intent.setDataAndType(uri, "image/jpeg");
        } else if (url.toString().contains(".txt")) {
            // Text file
            intent.setDataAndType(uri, "text/plain");
        } else if (url.toString().contains(".3gp") || url.toString().contains(".mpg") || url.toString().contains(".mpeg") || url.toString().contains(".mpe") || url.toString().contains(".mp4") || url.toString().contains(".avi")) {
            // Video files
            intent.setDataAndType(uri, "video/*");
        } else {
            intent.setDataAndType(uri, "*/*");
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void cancelNotification(Context ctx, int notifyId) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
        nMgr.cancel(notifyId);
    }


    public void writeDriver(Driver user) {
        try {
            String path = "/data/data/" + ctx.getPackageName()
                    + "/Driver.ser";
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            }
            FileOutputStream fileOut = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(user);
            out.close();
            fileOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Driver readDriver() {
        String path = "/data/data/" + ctx.getPackageName() + "/Driver.ser";
        File f = new File(path);
        Driver user = null;
        if (f.exists()) {
            try {
                System.gc();
                FileInputStream fileIn = new FileInputStream(path);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                user = (Driver) in.readObject();
                in.close();
                fileIn.close();
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (OptionalDataException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return user;
    }

    public void writeTrip(Trip user) {
        try {
            String path = "/data/data/" + ctx.getPackageName()
                    + "/Trip.ser";
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            }
            FileOutputStream fileOut = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(user);
            out.close();
            fileOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Trip readTrip() {
        String path = "/data/data/" + ctx.getPackageName() + "/Trip.ser";
        File f = new File(path);
        Trip user = null;
        if (f.exists()) {
            try {
                System.gc();
                FileInputStream fileIn = new FileInputStream(path);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                user = (Trip) in.readObject();
                in.close();
                fileIn.close();
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (OptionalDataException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return user;
    }

    public void writeRateCard(RateCard user) {
        try {
            String path = "/data/data/" + ctx.getPackageName() + "/RateCard.ser";
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            }
            FileOutputStream fileOut = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(user);
            out.close();
            fileOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    public RateCard readRateCard() {
        String path = "/data/data/" + ctx.getPackageName() + "/RateCard.ser";
        RateCard user = null;
        if (!new File(path).exists()) {
            return null;
        }
        try {
            System.gc();
            FileInputStream fileIn = new FileInputStream(path);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            user = (RateCard) in.readObject();
            in.close();
            fileIn.close();
            return user;
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
            return user;
        } catch (OptionalDataException e2) {
            e2.printStackTrace();
            return user;
        } catch (FileNotFoundException e3) {
            e3.printStackTrace();
            return user;
        } catch (ClassNotFoundException e4) {
            e4.printStackTrace();
            return user;
        } catch (IOException e5) {
            e5.printStackTrace();
            return user;
        }
    }
}
