package com.example.websocketdemo.websocket.ws;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Created by tangyb on 2017/10/25.
 */

public class NetworkConnectChangedReceiver extends BroadcastReceiver {
    private static final String TAG = "NET Changed";
    private NetworkChangedHandler mNetworkChangedHandler;

    public void setListener(NetworkChangedHandler handler){
        mNetworkChangedHandler=handler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //监听wifi的打开与关闭
        if(WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())){
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
            Log.e(TAG, "wifiState:" + wifiState);
            switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLED:
                    //APP.getInstance().setEnablaWifi(false);
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    //APP.getInstance().setEnablaWifi(true);
                    break;
                case WifiManager.WIFI_STATE_UNKNOWN:
                    break;
                default:
                    break;
            }
        }
        if(ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())){
            ConnectivityManager manager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
            if (activeNetwork != null) { // connected to the internet
                if (activeNetwork.isConnected()) {
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                        // connected to wifi
//                        APP.getInstance().setWifi(true);
                        Log.e(TAG, "当前WiFi连接可用 ");
                        notify(true);
                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                        // connected to the mobile provider's data plan
                        //APP.getInstance().setMobile(true);
                        Log.e(TAG, "当前移动网络连接可用 ");
                        notify(true);
                    }
                } else {
                    Log.e(TAG, "当前没有网络连接，请确保你已经打开网络 ");
                    notify(false);
                }
//                Log.e(TAG1, "info.getTypeName()" + activeNetwork.getTypeName());
//                Log.e(TAG1, "getSubtypeName()" + activeNetwork.getSubtypeName());
//                Log.e(TAG1, "getState()" + activeNetwork.getState());
//                Log.e(TAG1, "getDetailedState()"
//                        + activeNetwork.getDetailedState().name());
//                Log.e(TAG1, "getDetailedState()" + activeNetwork.getExtraInfo());
//                Log.e(TAG1, "getType()" + activeNetwork.getType());
            } else {   // not connected to the internet
                Log.e(TAG, "当前没有网络连接，请确保你已经打开网络 ");
                notify(false);
//                APP.getInstance().setWifi(false);
//                APP.getInstance().setMobile(false);
//                APP.getInstance().setConnected(false);
            }

        }
    }

    private void notify(boolean connect){
        if(mNetworkChangedHandler!=null){
            mNetworkChangedHandler.onConnectChanged(connect);
        }
    }


    public interface NetworkChangedHandler {
        void onConnectChanged(boolean isConnected);
    }
}
