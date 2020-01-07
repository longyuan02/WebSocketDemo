package com.Multigold.CunJinBao.websocket.trade.ws.client;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * Created by tangyb on 2017/10/24.
 */

public class WsClient {
    private final static int RECONNECT_INTERVAL = 3 * 1000;    //重连自增步长
    private final static long RECONNECT_MAX_TIME = 120 * 1000;   //最大重连间隔
    private Context mContext;
    private String wsUrl;
    private boolean isNeedReconnect = true;
    private boolean isManualClose = false;         //是否为手动关闭websocket连接
    private int reconnectCount = 0;   //重连次数
    private boolean isCancelReconnect = false;
    private OkHttpClient mOkHttpClient;
    private static WebSocket mWebSocket;
    private Lock mLock;
    private WsHandler wsHandler = null;

    private int mCurrentStatus = WsStatus.DISCONNECTED;//websocket连接状态

    public synchronized void setCurrentStatus(int currentStatus) {
        this.mCurrentStatus = currentStatus;
    }

    public synchronized int getCurrentStatus() {
        return mCurrentStatus;
    }

    public WsClient(Builder builder) {
        mContext = builder.mContext;
        wsUrl = builder.wsUrl;
        isNeedReconnect = builder.needReconnect;
        mOkHttpClient = builder.mOkHttpClient;
        mLock = new ReentrantLock();
    }

    public void setWsListener(WsHandler handler) {
        this.wsHandler = handler;
    }

    public void startConnect() {

        isManualClose = false;
        isCancelReconnect = false;
        buildConnect();
    }

    public void stopConnect() {
        isManualClose = true;
        disconnect();
    }

    private void tryReconnect() {
        if (!isNeedReconnect || isManualClose) {
            return;
        }

        if (!isNetworkConnected(mContext)) {
            setCurrentStatus(WsStatus.DISCONNECTED);
        }

        setCurrentStatus(WsStatus.RECONNECT);

        //try
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    long delay = reconnectCount * RECONNECT_INTERVAL;
                    Thread.sleep(delay > RECONNECT_MAX_TIME ? RECONNECT_MAX_TIME : delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                reconnectCount++;
                //do
                if (!isCancelReconnect) {
                    buildConnect();
                }
            }
        }).start();
    }

    private void cancelReconnect() {
        isCancelReconnect = true;
        reconnectCount = 0;
    }

    private void disconnect() {
        if (mCurrentStatus == WsStatus.DISCONNECTED) {
            return;
        }

        cancelReconnect();
        if (mOkHttpClient != null) {
            mOkHttpClient.dispatcher().cancelAll();
        }
        if (mWebSocket != null) {
            boolean isClosed = mWebSocket.close(WsStatus.CODE.NORMAL_CLOSE, "GoodBye!");
            //非正常关闭连接
            if (!isClosed) {
                if (wsHandler != null) {
                    wsHandler.onClosed(WsStatus.CODE.ABNORMAL_CLOSE, WsStatus.TIP.ABNORMAL_CLOSE);
                }
            }
        }
        setCurrentStatus(WsStatus.DISCONNECTED);
    }

    private synchronized void buildConnect() {
        if (!isNetworkConnected(mContext)) {
            setCurrentStatus(WsStatus.DISCONNECTED);
            if (wsHandler != null) {
                wsHandler.onNetworkDisable(WsStatus.NetworkDisabled);
            }
            return;
        }
        switch (getCurrentStatus()) {
            case WsStatus.CONNECTED:
            case WsStatus.CONNECTING:
                break;
            default:
                setCurrentStatus(WsStatus.CONNECTING);
                initWebSocket();
        }
    }

    private void initWebSocket() {
        if (mOkHttpClient == null) {
            mOkHttpClient = new OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    //.pingInterval(5, TimeUnit.SECONDS)
                    .build();
        }
        mOkHttpClient.dispatcher().cancelAll();
//        if(mWebSocket==null){
        Request request = new Request.Builder().url(wsUrl).build();
        try {
            mLock.lockInterruptibly();
                WebSocketListener mWsListener = new WebSocketListener() {
                    @Override
                    public void onOpen(WebSocket webSocket, Response response) {
                        Log.i("WS","onOpen");
                        mWebSocket = webSocket;
                        setCurrentStatus(WsStatus.CONNECTED);
                        reconnectCount = 0;
                        if (wsHandler != null) {
                            wsHandler.onOpen();
                        }
                    }

                    @Override
                    public void onMessage(WebSocket webSocket, String text) {
//                        Log.i("WS","WS---onMessage---text:"+text);
                        if (wsHandler != null) {
                            wsHandler.onMessage(text);
                        }
                    }

                    @Override
                    public void onMessage(WebSocket webSocket, ByteString bytes) {
//                        Log.i("WS","WS---onMessage---bytes length:"+bytes.size());
//                        Log.i("WS","WS---onMessage---bytes string:"+ new String(bytes.toByteArray()));
                        if (wsHandler != null) {
                            wsHandler.onMessage(bytes.toByteArray());
                        }
                    }

                    @Override
                    public void onClosing(WebSocket webSocket, int code, String reason) {
                        Log.i("WS","onClosing");
                        setCurrentStatus(WsStatus.DISCONNECTED);
                        if (wsHandler != null) {
                            wsHandler.onClosing(code, reason);
                        }
                    }

                    @Override
                    public void onClosed(WebSocket webSocket, int code, String reason) {
                        Log.i("WS","onClosed");
                        setCurrentStatus(WsStatus.DISCONNECTED);
                        if (wsHandler != null) {
                            wsHandler.onClosed(code, reason);
                        }
                    }

                    @Override
                    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                        Log.i("WS","onFailure"+t.getMessage());
                        //t.printStackTrace();
                        if(t instanceof SocketTimeoutException){
                            //wsHandler.onConnectTimeout();
                            setCurrentStatus(WsStatus.CONNECT_TIMEOUT);
                        }else if(t instanceof UnknownHostException){
                            //wsHandler.onConnectTimeout();
                            setCurrentStatus(WsStatus.CONNECT_TIMEOUT);
                        }else{
                            setCurrentStatus(WsStatus.DISCONNECTED);
                        }
                        //tryReconnect();
                        //setCurrentStatus(WsStatus.DISCONNECTED);
                        if (wsHandler != null) {
                            wsHandler.onFailure();
                        }
                        tryReconnect();
                    }
                };
            mWebSocket = mOkHttpClient.newWebSocket(request, mWsListener);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mLock.unlock();
        }
//        }
    }

    //检查网络是否连接
    private synchronized boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null) {
                return networkInfo.isAvailable();
            }
        }
        return false;
    }
    
    //发送消息
    public boolean sendMessage(String msg) {
        return send(msg);
    }

    public boolean sendMessage(ByteString byteString) {
        return send(byteString);
    }

    private boolean send(Object msg) {

        boolean isSend = false;
        if (mWebSocket != null && mCurrentStatus == WsStatus.CONNECTED) {
            if (msg instanceof String) {
                isSend = mWebSocket.send((String) msg);
            } else if (msg instanceof ByteString) {
                isSend = mWebSocket.send((ByteString) msg);
            }
            //发送消息失败，尝试重连
            if (!isSend) {
                tryReconnect();
            }
        }
        return isSend;
    }

//    public static WebSocketListener mWsListener;


    public static final class Builder {
        private Context mContext;
        private String wsUrl;
        private boolean needReconnect = true;
        private OkHttpClient mOkHttpClient;

        public Builder(Context ctx) {
            mContext = ctx;
        }

        public Builder client(OkHttpClient client) {
            mOkHttpClient = client;
            return this;
        }

        public Builder wsUrl(String url) {
            wsUrl = url;
            return this;
        }

        public Builder needReconnect(boolean val) {
            needReconnect = val;
            return this;
        }

        public WsClient build() {
            return new WsClient(this);
        }
    }

    public interface WsHandler {
        void onOpen();

        void onMessage(String text);

        void onMessage(byte[] bytes);

        void onClosing(int code, String reason);

        void onClosed(int code, String reason);

        void onFailure();

        void onNetworkDisable(int code);

        //void onConnectTimeout();
    }

    public class WsStatus {
        public final static int CONNECTING = 0;
        public final static int CONNECTED = 1;
        public final static int RECONNECT = 2;
        public final static int DISCONNECTED = -1;
        public final static int NetworkDisabled = -2;
        public final static int CONNECT_TIMEOUT = -3;//连接超时

        class CODE {

            public final static int NORMAL_CLOSE = 1000;
            public final static int ABNORMAL_CLOSE = 1001;
        }

        class TIP {

            public final static String NORMAL_CLOSE = "normal close";
            public final static String ABNORMAL_CLOSE = "abnormal close";
        }
    }

    public class WsReconnection{
        private int oneConnectCount = 0;//每个url的重连次数
        private List<String> urlList = new ArrayList<>();
        private int lastIndex = -1;

        public int getOneConnectCount() {
            return oneConnectCount;
        }

        public void setOneConnectCount(int oneConnectCount) {
            this.oneConnectCount = oneConnectCount;
        }

        public List<String> getUrlList() {
            return urlList;
        }

        public void setUrlList(List<String> urlList) {
            this.urlList = urlList;
        }

        public WsReconnection(){
            if(getUrlList().size()>0){
                Random random=new Random();
                lastIndex = random.nextInt(getUrlList().size());
            }
        }

        public String getOneUrl(){
            if(lastIndex > 0){
                return getUrlList().get(lastIndex);
            }else{
                return "";
            }
        }
    }
}
