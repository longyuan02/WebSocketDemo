package com.example.websocketdemo.websocket.ws;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.Multigold.CunJinBao.websocket.trade.ws.client.WsClient;
import com.example.websocketdemo.bean.MsgModel;
import com.example.websocketdemo.websocket.IWsLfvMsgRecvListener;
import com.example.websocketdemo.websocket.IWsMsgRecvListener;
import com.example.websocketdemo.websocket.ObjCacheManager;
import com.example.websocketdemo.websocket.WsMsgRecvListenerManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import okhttp3.OkHttpClient;

/**
 * Created by tangyb on 2017/9/26.
 */

public class WsManager {
    public interface WsStatusChangedHandler {
        void onStatusChanged(int status);
    }
    private static WsManager instance=null;

    public static WsManager getInstance(){
        if(instance==null){
            instance=new WsManager();
        }
        return instance;
    }

    //要取消同步请求等待的key
    private static String willCancelReqKey = null;
    private static Map<String,Boolean> willCancelReqKeyMap = new HashMap<>();
    //已超时的key List
    private static Map<String,Boolean> timeoutReqKeyMap = new HashMap<>();
    private static boolean isTimeout = false;
    private static int mConnectTimeout = 0; //连接超时 0 使用默认
    private static int mRequestTimeout = 20; //请求超时
    private static boolean mNeedReconnect = true;  //是否需要重连

    private static WsClient wsClient;

    private static Handler wsMainHandler = new Handler(Looper.getMainLooper());

    private static WsStatusChangedHandler mStatusHandler;

    public static void setStatusHandler(WsStatusChangedHandler handler) {
        mStatusHandler = handler;
    }

    private static NetworkConnectChangedReceiver mNetworkChangeListener;

    private static Map<String, Long> optionTimeRecordsMap = new HashMap<>();

    private static void statusChanged(final int status) {
        if (mStatusHandler != null) {
            wsMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mStatusHandler.onStatusChanged(status);
                }
            });
        }
    }

    /**
     * 初始化连接
     *
     * @param wsUrl
     */
    public static void startConnect(final Context context, final String wsUrl,
                                    boolean needReconnect, int connectTimeout, int requestTimeout) {
        mNeedReconnect = needReconnect;
        if(connectTimeout > 0) {
            mConnectTimeout = connectTimeout;
        }
        if(requestTimeout > 0) {
            mRequestTimeout = requestTimeout;
        }
        if (mNetworkChangeListener == null) {
            mNetworkChangeListener = new NetworkConnectChangedReceiver();
            mNetworkChangeListener.setListener(new NetworkConnectChangedReceiver.NetworkChangedHandler() {
                @Override
                public void onConnectChanged(boolean isConnected) {
                    if (isConnected) {
                        connect(context, wsUrl);
                    }
                }
            });
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
            filter.addAction("android.net.wifi.STATE_CHANGE");
            context.registerReceiver(mNetworkChangeListener, filter);
        }
        connect(context, wsUrl);
    }

    /**
     * 初始化连接
     *
     * @param wsUrl
     */
    private static synchronized void connect(Context context, String wsUrl) {
        Log.i("WS","connect");
        if (wsClient != null) {
            wsClient.stopConnect();
            //wsClient=null;
        }
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .retryOnConnectionFailure(mNeedReconnect);
        if(mConnectTimeout > 0){
            builder = builder.connectTimeout(mConnectTimeout,TimeUnit.SECONDS);
        }
        OkHttpClient okHttpClient = builder.build();
//        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                //.pingInterval(10,TimeUnit.SECONDS)
//                .retryOnConnectionFailure(true)
//                .build();
        Log.i("okHttpClient timeout",okHttpClient.connectTimeoutMillis()+"");
        Log.i("WS Connect Url",wsUrl);
        if (wsClient == null) {
            wsClient = new WsClient.Builder(context)
                    .client(okHttpClient)
                    .needReconnect(mNeedReconnect)
                    .wsUrl(wsUrl)
                    .build();
            WsClient.WsHandler handler = new WsClient.WsHandler() {
                @Override
                public void onOpen() {
                    statusChanged(wsClient.getCurrentStatus());
                }

                @Override
                public void onMessage(String text) {
                    MsgModel msg = new MsgModel();
                    msg.parse(text.getBytes());
                    WsMsgRecvListenerManager.getInstance().triggerWsMsgRecvListener(msg);
                }

                @Override
                public void onMessage(byte[] bytes) {
                    MsgModel msg = new MsgModel();
                    msg.parse(bytes);
                    WsMsgRecvListenerManager.getInstance().triggerWsMsgRecvListener(msg);
                }

                @Override
                public void onClosing(int code, String reason) {
                    statusChanged(wsClient.getCurrentStatus());
                    MsgModel msg = new MsgModel();
                    msg.optionCode = wsClient.getCurrentStatus();
                    WsMsgRecvListenerManager.getInstance().triggerWsMsgRecvListenerForError(msg);
                }

                @Override
                public void onClosed(int code, String reason) {
                    statusChanged(wsClient.getCurrentStatus());
                    MsgModel msg = new MsgModel();
                    msg.optionCode = wsClient.getCurrentStatus();
                    WsMsgRecvListenerManager.getInstance().triggerWsMsgRecvListenerForError(msg);
                }

                @Override
                public void onFailure() {
                    statusChanged(wsClient.getCurrentStatus());
                    MsgModel msg = new MsgModel();
                    msg.optionCode = wsClient.getCurrentStatus();
                    WsMsgRecvListenerManager.getInstance().triggerWsMsgRecvListenerForError(msg);
                }

                @Override
                public void onNetworkDisable(int code) {

                    statusChanged(WsClient.WsStatus.NetworkDisabled);
                }

                /*@Override
                public void onConnectTimeout() {
                    statusChanged(WsClient.WsStatus.CONNECT_TIMEOUT);
                }*/
            };
            wsClient.setWsListener(handler);
        }
        wsClient.startConnect();
    }

    public static void stopConnect(Context context) {
        if(wsClient == null){
            return;
        }
        wsClient.stopConnect();
        if (mNetworkChangeListener != null) {
            context.unregisterReceiver(mNetworkChangeListener);
            mNetworkChangeListener = null;
        }
    }

    /**
     * 同步发送
     *
     * @param reqMsg
     * @return
     */
    public static MsgModel syncSend(MsgModel reqMsg) {
        return syncSend(reqMsg, 20);
    }

    /**
     * 同步发送
     *
     * @param reqMsg
     * @param timeout
     * @return
     */
    public static MsgModel syncSend(final MsgModel reqMsg, int timeout) {
        if (!checkOptionTime(reqMsg)) {
            return null;
        }
        willCancelReqKey = null;
        isTimeout = false;
        final String reqKey = reqMsg.getCacheKey();
        asyncSend(reqMsg, new IWsMsgRecvListener() {
            @Override
            public boolean onReceivedMsg(MsgModel rspMsg) {
                ObjCacheManager.getInstance().add(rspMsg);
                return false;
            }
        }, 1);
        try {
            FutureTask<MsgModel> task = new FutureTask<MsgModel>(new Callable<MsgModel>() {
                @Override
                public MsgModel call() throws Exception {
                    while (true) {
                        if (isTimeout) {
                            isTimeout = false;
                            ObjCacheManager.getInstance().add(reqMsg.getCacheKey(), "null");
                            MsgModel timeoutMsg = new MsgModel();
                            timeoutMsg.optionCode = 2;
                            return timeoutMsg;
                        }
                        if (reqMsg.getCacheKey().equals(willCancelReqKey)) {//取消请求
                            willCancelReqKey = null;
                            isTimeout = false;
                            ObjCacheManager.getInstance().add(reqMsg.getCacheKey(), "null");
                            MsgModel timeoutMsg = new MsgModel();
                            timeoutMsg.optionCode = -1;
                            return timeoutMsg;
                        }
                        Object obj = ObjCacheManager.getInstance().get(reqMsg.getCacheKey());
                        if (obj != null) {
                            ObjCacheManager.getInstance().remove(reqMsg.getCacheKey());
                            MsgModel rspMsg = (MsgModel) obj;
                            return rspMsg;
                        }
                    }
                }
            });
            new Thread(task).start();
            return task.get(timeout, TimeUnit.SECONDS);
        } catch (TimeoutException te) {
            Log.i("WS", "request time out!!! reqKey = " + reqKey);
            isTimeout = true;
            ObjCacheManager.getInstance().add(reqKey, "null");
            MsgModel timeoutMsg = new MsgModel();
            timeoutMsg.optionCode = 2;
            return timeoutMsg;
        } catch (Exception e) {
            e.printStackTrace();
            ObjCacheManager.getInstance().add(reqMsg.getCacheKey(), "null");
            MsgModel timeoutMsg = new MsgModel();
            timeoutMsg.optionCode = 3;
            return timeoutMsg;
        }
    }

    /**
     * 异步发送
     *
     * @param reqMsg
     * @param recvListener
     */
    public static void asyncSend(final MsgModel reqMsg, final IWsMsgRecvListener recvListener) {
        asyncSend(reqMsg, recvListener, 0);
    }

    /**
     * 发送
     *
     * @param reqMsg
     * @param recvListener
     * @param reqType      请求类型  0:异步，1:同步
     */
    private static void asyncSend(final MsgModel reqMsg, final IWsMsgRecvListener recvListener, final int reqType) {
        if (!checkOptionTime(reqMsg)) {
            return;
        }

        if (wsClient == null) {
            MsgModel rspMsg = new MsgModel();
            rspMsg.sBusiType = reqMsg.sBusiType;
            rspMsg.sExchCode = reqMsg.sExchCode;
            rspMsg.sSerialNo = reqMsg.sSerialNo;
            rspMsg.optionCode = 4;
            recvListener.onReceivedMsg(rspMsg);
            return;

        }
        if (wsClient.getCurrentStatus() != WsClient.WsStatus.CONNECTED) {
            MsgModel rspMsg = new MsgModel();
            rspMsg.sBusiType = reqMsg.sBusiType;
            rspMsg.sExchCode = reqMsg.sExchCode;
            rspMsg.sSerialNo = reqMsg.sSerialNo;
            rspMsg.optionCode = 1;
            recvListener.onReceivedMsg(rspMsg);
            return;
        }

        //取消或超时检测
        final Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            int i = 1;

            public void run() {
                if (willCancelReqKeyMap.containsKey(reqMsg.getCacheKey())) {//取消请求
                    willCancelReqKeyMap.remove(reqMsg.getCacheKey());
                    timeoutReqKeyMap.remove(reqMsg.getCacheKey());
                    WsMsgRecvListenerManager.getInstance().removeWsMsgRecvListener(reqMsg.sBusiType, reqMsg.sExchCode, reqMsg.sSerialNo);
                    final MsgModel cancelMsg = new MsgModel();
                    cancelMsg.optionCode = -1;
                    wsMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            recvListener.onReceivedMsg(cancelMsg);
                        }
                    });
                    timer.cancel();
                    return;
                }
                if (i == mRequestTimeout) {
                    timeoutReqKeyMap.put(reqMsg.getCacheKey(), true);
                    WsMsgRecvListenerManager.getInstance().removeWsMsgRecvListener(reqMsg.sBusiType, reqMsg.sExchCode, reqMsg.sSerialNo);
                    final MsgModel timeoutMsg = new MsgModel();
                    timeoutMsg.optionCode = 2;
                    wsMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            recvListener.onReceivedMsg(timeoutMsg);
                        }
                    });
                    timer.cancel();
                }
                i++;
            }
        }, 0, 1 * 1000);

        WsMsgRecvListenerManager.getInstance().addWsMsgRecvListener(reqMsg.sBusiType, reqMsg.sExchCode, reqMsg.sSerialNo, new IWsMsgRecvListener() {
            @Override
            public boolean onReceivedMsg(final MsgModel rspMsg) {
                if (willCancelReqKeyMap.containsKey(reqMsg.getCacheKey())) {
                    return false;
                }
                if(timeoutReqKeyMap.containsKey(reqMsg.getCacheKey())){
                    return false;
                }else{
                    timeoutReqKeyMap.clear();
                }
                WsMsgRecvListenerManager.getInstance().removeWsMsgRecvListener(reqMsg.sBusiType, reqMsg.sExchCode, reqMsg.sSerialNo);
                if (reqType == 0) {
                    wsMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            recvListener.onReceivedMsg(rspMsg);
                            timer.cancel();
                        }
                    });
                } else {
                    recvListener.onReceivedMsg(rspMsg);
                    timer.cancel();
                }
                return false;
            }
        });

        wsClient.sendMessage(reqMsg.toString());
    }

    private static boolean checkOptionTime(MsgModel msg) {
        String sKey = msg.sBusiType + ":" + msg.sExchCode + ":" + msg.sSerialNo;
        if (optionTimeRecordsMap.containsKey(sKey)) {
            long last = optionTimeRecordsMap.get(sKey);
            long interval = System.currentTimeMillis() - last;
            if (interval > 1000) {
                optionTimeRecordsMap.remove(sKey);
                return true;
            } else {
                return false;
            }
        } else {
            optionTimeRecordsMap.put(sKey, System.currentTimeMillis());
            return true;
        }
    }

    /**
     * 添加行情监听
     *
     * @param sExchCode
     * @param recvListener
     */
    public static void registerPushMsg(String sExchCode, final IWsLfvMsgRecvListener recvListener) {
        //WsMsgRecvListenerManager.getInstance().addPushLfvMsgRecvListener(sExchCode,recvListener);
        WsMsgRecvListenerManager.getInstance().addPushLfvMsgRecvListener(sExchCode, new IWsLfvMsgRecvListener() {
            @Override
            public void onReceivedLfv(final String sExchCode, final Map<String, String> lfvMsg, final MsgModel msg) {
                wsMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        recvListener.onReceivedLfv(sExchCode, lfvMsg, msg);
                    }
                });
            }
        });
    }

    /**
     * 移除行情监听
     *
     * @param sExchCode
     */
    public static void unRegisterPushMsg(String sExchCode) {
        WsMsgRecvListenerManager.getInstance().removePushLfvMsgRecvListener(sExchCode);
    }

    /**
     * 移除行情监听
     *
     * @param sExchCode
     * @param recvListener
     */
    public static void unRegisterPushMsg(String sExchCode, IWsLfvMsgRecvListener recvListener) {
        WsMsgRecvListenerManager.getInstance().removePushLfvMsgRecvListener(sExchCode, recvListener);
    }

    public static void cancelRequest(MsgModel reqMsg) {
        willCancelReqKey = reqMsg.getCacheKey();
        willCancelReqKeyMap.put(reqMsg.getCacheKey(),false);
    }

//    public static void destory(){
//        WebSocketClient.destory();
//    }
}
