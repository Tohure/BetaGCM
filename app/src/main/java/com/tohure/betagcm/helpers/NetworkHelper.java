package com.tohure.betagcm.helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkHelper {

    public static boolean haveNetworkConnection(final Context context) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            final NetworkInfo[] netInfo = cm.getAllNetworkInfo();
            for (final NetworkInfo netInfoCheck : netInfo) {
                if (netInfoCheck.getTypeName().equalsIgnoreCase("WIFI")) { if (netInfoCheck.isConnected()) { haveConnectedWifi = true;}}
                if (netInfoCheck.getTypeName().equalsIgnoreCase("MOBILE")) { if (netInfoCheck.isConnected()) { haveConnectedMobile = true;}}
            }
        }
        return haveConnectedWifi || haveConnectedMobile;
    }
}