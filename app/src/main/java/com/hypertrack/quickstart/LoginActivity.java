package com.hypertrack.quickstart;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.GsonBuilder;
import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.callbacks.HyperTrackCallback;
import com.hypertrack.lib.internal.common.util.HTTextUtils;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.SuccessResponse;
import com.hypertrack.lib.models.User;
import com.hypertrack.lib.models.UserParams;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Aman on 24/10/17.
 */

public class LoginActivity extends BaseActivity {
    private Context context;
    private EditText nameText, phoneNumberText, lookupIdText;
    private LinearLayout loginBtnLoader;
    public static final String HT_QUICK_START_SHARED_PREFS_KEY = "com.hypertrack.quickstart:SharedPreference";
    String name = "LOGGING";
    String[] listViewArray = {};
    private String[] readFromFile() {
        String pathoffile;
        String contents="";
        String[] toReturn = {};
        File mFolder = new File(getFilesDir() + "/sample");
        File myFile = new File(mFolder.getAbsolutePath() + "/myData.txt");
        if(!myFile.exists()) {
            Log.e(name, "file not");
            return toReturn;
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(myFile));
            int c;
            while ((c = br.read()) != -1) {
                contents=contents+(char)c;
            }
            toReturn = contents.split("-");

        }
        catch (IOException e) {
            //You'll need to add proper error handling here
            Log.e(name, e.toString());
            return toReturn;
        }
        return toReturn;
    }

    private void writeToFile(String[] toWrite) {
        String pathoffile;
        String contents="";
        for (int i =0;i<toWrite.length;i++) {
            contents += (toWrite[i] + '-');
        }
        Log.e(name, "Start Write");
//        File myFoo = new File(android.os.Environment.getExternalStorageDirectory(), "/myData.txt");
        File mFolder = new File(getFilesDir() + "/sample");
        Log.e(name, mFolder.getAbsolutePath());
        File myFoo = new File(mFolder.getAbsolutePath() + "/myData.txt");
        if (!mFolder.exists()) {
            mFolder.mkdir();
        }
        // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder

//        File myFoo = new File (root.getAbsolutePath() + "/myData.txt");
        if(!myFoo.exists()) {
            try {
                myFoo.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileOutputStream fooStream;
        try {
            FileWriter fooWriter = new FileWriter(myFoo, false); // true to append
            fooWriter.write(contents);
            fooWriter.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
            Log.e(name, e.toString());
            e.printStackTrace();
            return;
        }
        Log.e(name, "END WRITE");
    }
    private void createNotification(String text, String link){

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.ic_battery_icon)
                        .setContentTitle(text);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // pending implicit intent to view url
        Intent resultIntent = new Intent(Intent.ACTION_VIEW);
        resultIntent.setData(Uri.parse(link));

        PendingIntent pending = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(pending);

        // using the same tag and Id causes the new notification to replace an existing one
        mNotificationManager.notify(1, notificationBuilder.build());
    }

    private void checkNearby () {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    String url = "http://10.67.13.83:5000/peace/user_notice?user_id=" +Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID) + "_ne";//user_id=tempid&is_add=1&task=temp%20task
                    File mFolder = new File(getFilesDir() + "/sample");
                    File output = new File(mFolder.getAbsolutePath() + "/output.txt");
                    HttpRequest.get(url).receive(output);
                    File myFile = output;
                    String contents = "";
                    if(!myFile.exists()) {
                        Log.e(name, "file not");
                        return;
                    }
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(myFile));
                        int c;
                        while ((c = br.read()) != -1) {
                            contents=contents+(char)c;
                        }

                    if (contents.length() != 0) {
                        JSONObject jObject = new JSONObject(contents);
                        String lat = jObject.getString("lat");
                        String lng = jObject.getString("lng");

                        String url_to_s = "https://maps.google.com/maps?daddr="+lat+","+lng;
                        Log.e("checkkk", url_to_s);
                        createNotification("Hey Peace here! One of your task is nearby!", url_to_s);
                    }

                    }
                    catch (IOException e) {
                        //You'll need to add proper error handling here
                        Log.e(name, e.toString());

                    }
                Log.e("checkNearby", contents);

                    //Your code goes here
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private View.OnClickListener addTask = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            Intent mainActivityIntent = new Intent(LoginActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(mainActivityIntent);
//            finish();
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setTitle("Add new Task");

// Set up the input
//            this.context = context;
            final EditText input = new EditText(LoginActivity.this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

// Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final String m_Text = input.getText().toString();
                    listViewArray = increaseArray(listViewArray, 1);
                    listViewArray[listViewArray.length-1] = m_Text;

                    Thread thread = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try  {
                                String url = "http://10.67.13.83:5000/peace/user_add_del";//user_id=tempid&is_add=1&task=temp%20task

                                int response = HttpRequest.post(url).send("user_id="+Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID) + "_ne&is_add=1&task="+m_Text).code();
                                //Your code goes here
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    thread.start();


                    ArrayAdapter adapter = new ArrayAdapter<String>(LoginActivity.this,
                            R.layout.activity_listview, listViewArray);

                    ListView listView = (ListView) findViewById(R.id.mobile_list);
                    listView.setBackgroundColor(Color.CYAN);
                    listView.setAdapter(adapter);

                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();

        }
    };

    public String[] increaseArray(String[] theArray, int increaseBy)
    {
        int i = theArray.length;
        int n = ++i;
        String[] newArray = new String[n];
        for(int cnt=0;cnt<theArray.length;cnt++)
        {
            newArray[cnt] = theArray[cnt];
        }
        return newArray;
    }
    public String[] delFromArray(final String[] theArray, final int ind)
    {
        int i = theArray.length;
        int n = --i;
//        Thread thread = new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                try  {
//                    String url = "http://192.168.88.105:5000/peace/user_add_del";//user_id=tempid&is_add=1&task=temp%20task
//
//                    int response = HttpRequest.post(url).send("user_id="+Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID) + "_ne&is_add=0&task="+theArray[ind]).code();
//                    //Your code goes here
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        thread.start();
        String[] newArray = new String[n];
        Log.e("delfromarr",Integer.toString(ind));
        for(int cnt=0;cnt<theArray.length;cnt++)
        {
            if (cnt < ind)
            newArray[cnt] = theArray[cnt];
            else if (ind!=cnt)
                newArray[cnt-1] = theArray[cnt];
        }

        return newArray;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        context = this.context;
        setContentView(R.layout.activity_login);
        checkForLocationSettings();
        final Button trackingToggle = (Button) findViewById(R.id.tracking_toggle);
        trackingToggle.setOnClickListener(addTask);
        // Check if user is logged in
//        if (getUser() != null) {
//            Intent mainActivityIntent = new Intent(this, MainActivity.class);
//            startActivity(mainActivityIntent);
//            finish();
//            return;
//        }

        // Initialize Toolbar
        initToolbar("Tasks");

//        String[] listViewArray = readFromFile();
//        writeToFile(listViewArray);
        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.activity_listview, listViewArray);

        ListView listView = (ListView) findViewById(R.id.mobile_list);
        listView.setBackgroundColor(Color.CYAN);
        listView.setAdapter(adapter);
        listView.setLongClickable(true);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View v, final int position, long id) {
                //Do your tasks here


                AlertDialog.Builder alert = new AlertDialog.Builder(
                        LoginActivity.this);
                alert.setTitle("Delete a Task!");
                alert.setMessage("Are you sure to delete record");
                alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do your work here

                        listViewArray = delFromArray(listViewArray, position);

                        ArrayAdapter adapter = new ArrayAdapter<String>(LoginActivity.this,
                                R.layout.activity_listview, listViewArray);

                        ListView listView = (ListView) findViewById(R.id.mobile_list);
                        listView.setBackgroundColor(Color.CYAN);
                        listView.setAdapter(adapter);

                        dialog.dismiss();

                    }
                });
                alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });

                alert.show();

                return true;
            }
        });

        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.e("inside", "hello");
                            checkNearby();
                        }
                        catch (Exception e) {

                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0,10000);

        // Initialize UI Views

    }

    /**
     * Call this method to initialize UI views and handle listeners for these
     * views
     */
//    private void initUIViews() {
//        // Initialize UserName Views
//        ListView simpleList;
//        String countryList[] = {"India", "China", "australia", "Portugle", "America", "NewZealand"};
//        simpleList = (ListView)findViewById(R.id.simpleListView);
//        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.activity_listview, R.id.textView, countryList);
//        simpleList.setAdapter(arrayAdapter);
//
//    }

    /**
     * Call this method when User Login button has been clicked.
     * Note that this method is linked with the layout file (content_login.xml)
     * using this button's layout's onClick attribute. So no need to invoke this
     * method or handle login button's click listener explicitly.
     *
     * @param view
     */
//    public void onLoginButtonClick(View view) {
//        // Check if Location Settings are enabled, if yes then attempt
//        // DriverLogin
//        checkForLocationSettings();
//    }

    /**
     * Call this method to check Location Settings before proceeding for User
     * Login
     */
    private void checkForLocationSettings() {
        // Check for Location permission
        // Refer here for more info https://docs.hypertrack.com/sdks/android/reference/hypertrack.html#boolean-checklocationpermission
        if (!HyperTrack.checkLocationPermission(this)) {
            HyperTrack.requestPermissions(this);
            return;
        }

        // Check for Location settings
        // // Refer here for more info https://docs.hypertrack.com/sdks/android/reference/hypertrack.html#boolean-checklocationservices
        if (!HyperTrack.checkLocationServices(this)) {
            HyperTrack.requestLocationServices(this);
        }

        // Location Permissions and Settings have been enabled
        // Proceed with your app logic here i.e User Login in this case
        attemptUserLogin();
    }

    /**
     * Call this method to attempt user login. This method will create a User
     * on HyperTrack Server and configure the SDK using this generated UserId.
     */
    private void attemptUserLogin() {

        // Show Login Button loader
//        loginBtnLoader.setVisibility(View.VISIBLE);

        // Get User details, if specified
        final String name = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        final String phoneNumber = "+919933979842";
        final String lookupId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID) + "_ne";

        UserParams userParams = new UserParams().setName(name).setPhone(phoneNumber).setLookupId(lookupId);
        /**
         * Get or Create a User for given lookupId on HyperTrack Server here to
         * login your user & configure HyperTrack SDK with this generated
         * HyperTrack UserId.
         * OR
         * Implement your API call for User Login and get back a HyperTrack
         * UserId from your API Server to be configured in the HyperTrack SDK.
         *
         * Refer here for more detail https://docs.hypertrack.com/sdks/android/reference/user.html#getorcreate-user
         *
         */
        HyperTrack.getOrCreateUser(userParams, new HyperTrackCallback() {
            @Override
            public void onSuccess(@NonNull SuccessResponse successResponse) {

                User user = (User) successResponse.getResponseObject();
                String userId = user.getId();
                // Handle createUser success here, if required
                // HyperTrack SDK auto-configures UserId on createUser API call,
                // so no need to call HyperTrack.setUserId() API

                // On UserLogin success
                onUserLoginSuccess();
            }

            @Override
            public void onError(@NonNull ErrorResponse errorResponse) {
                // Hide Login Button loader
//                loginBtnLoader.setVisibility(View.GONE);

                Toast.makeText(LoginActivity.this, R.string.login_error_msg + " " + errorResponse.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Call this method when user has successfully logged in
     */
    private void onUserLoginSuccess() {

        /**
         * See more method of user's mock tracking session https://docs.hypertrack.com/sdks/android/reference/hypertrack.html#void-startmocktracking
         * */
        if(1==1)
        return;
        else {
            HyperTrack.startMockTracking(new HyperTrackCallback() {
                @Override
                public void onSuccess(@NonNull SuccessResponse successResponse) {
                    // Hide Login Button loader
//                loginBtnLoader.setVisibility(View.GONE);

                    Toast.makeText(LoginActivity.this, R.string.login_success_msg, Toast.LENGTH_SHORT).show();

                    // Start User Session by starting MainActivity
                    Intent mainActivityIntent = new Intent(LoginActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainActivityIntent);
                    finish();
                }

                @Override
                public void onError(@NonNull ErrorResponse errorResponse) {
                    // Hide Login Button loader
//                loginBtnLoader.setVisibility(View.GONE);

                    Toast.makeText(LoginActivity.this, R.string.login_error_msg + " " + errorResponse.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Handle on Grant Location Permissions request accepted/denied result
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == HyperTrack.REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Check if Location Settings are enabled to proceed
                checkForLocationSettings();

            } else {
                // Handle Location Permission denied error
                Toast.makeText(this, "Location Permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Handle on Enable Location Services request accepted/denied result
     *
     * @param requestCode
     * @param resultCode
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == HyperTrack.REQUEST_CODE_LOCATION_SERVICES) {
            if (resultCode == Activity.RESULT_OK) {
                // Check if Location Settings are enabled to proceed
                checkForLocationSettings();

            } else {
                // Handle Enable Location Services request denied error
                Toast.makeText(this, R.string.enable_location_settings, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveUser(User user) {
        SharedPreferences sharedPreferences = getSharedPreferences(HT_QUICK_START_SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user", new GsonBuilder().create().toJson(user));
        editor.apply();
    }

    private User getUser() {
        SharedPreferences sharedPreferences = getSharedPreferences(HT_QUICK_START_SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        String jsonString = sharedPreferences.getString("user", "null");
        if (HTTextUtils.isEmpty(jsonString)) {
            return null;
        }
        User user = null;
        try {

            user = new GsonBuilder().create().fromJson(jsonString, User.class);
        } catch (Exception e) {
            return null;
        }

        return user;
    }
}
