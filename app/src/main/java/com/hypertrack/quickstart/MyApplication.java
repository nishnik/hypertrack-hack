package com.hypertrack.quickstart;

import android.app.Application;
import android.util.Log;

import com.hypertrack.lib.HyperTrack;

/**
 * Created by Aman on 24/10/17.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {

        super.onCreate();

        // Initialize HyperTrack SDK with your Publishable Key here
        // Refer to documentation at
        // https://docs.hypertrack.com/gettingstarted/authentication.html
        // @NOTE: Add **YOUR_PUBLISHABLE_KEY_HERE** here for SDK to be
        // authenticated with HyperTrack Server
        HyperTrack.initialize(this, "pk_ffd22e986188486533e47e187d084f76e0ec6f30");
        HyperTrack.enableDebugLogging(Log.VERBOSE);
    }
}