package com.example.websocketdemo.websocket;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketClient {
    //连接状态
    public enum LINK_STATUS {
        NOT_CONNECT, CONNECTING, OPEN, CLOSING, CLOSED
    }

    private static final String TAG = "WebSocketClient";
    private static final int NORMAL_CLOSURE_STATUS = 1000;
    private static OkHttpClient sClient;
    private static WebSocket sWebSocket;
    private static WebSocketClient.LINK_STATUS linkStatus = WebSocketClient.LINK_STATUS.NOT_CONNECT;

    private static WebSocketClient.WsHandler wsHandler = null;

    public static synchronized void open(String wsUrl, WebSocketClient.WsHandler callback) {
        if (wsHandler == null) {
            wsHandler = callback;
        }
        if (sClient == null) {
            //sClient=new OkHttpClient();
            sClient = new OkHttpClient.Builder()
                    .pingInterval(5, TimeUnit.SECONDS)
                    .build();
        }
        if (sWebSocket == null) {
            Request request = new Request.Builder().url(wsUrl).build();
            WebSocketClient.WsListener listener = new WebSocketClient.WsListener();
            sWebSocket = sClient.newWebSocket(request, listener);
        }
    }

    public static boolean send(String msg) {
        WebSocket ws;
        synchronized (WebSocketClient.class) {
            ws = sWebSocket;
        }
        if (ws == null) {
            Log.i(TAG, "send,but ws is null!");
            return false;
        }
        if (linkStatus != WebSocketClient.LINK_STATUS.OPEN) {
            Log.i(TAG, "send,but ws is not open!");
            return false;
        }
        ws.send(msg);
        Log.i(TAG, "--->SEND:" + msg);
        return true;
    }

    public static boolean send(byte[] msgBytes) {
        WebSocket ws;
        synchronized (WebSocketClient.class) {
            ws = sWebSocket;
        }
        if (ws == null) {
            Log.i(TAG, "send,but ws is null!");
            return false;
        }
        if (linkStatus != WebSocketClient.LINK_STATUS.OPEN) {
            Log.i(TAG, "send,but ws is not open!");
            return false;
        }
        ws.send(ByteString.of(msgBytes));
        Log.i(TAG, "--->SEND:" + new String(msgBytes));
        return true;
    }

    public static synchronized void close() {
        if (sWebSocket != null) {
            sWebSocket.close(NORMAL_CLOSURE_STATUS, "GoodBye!");
            sWebSocket = null;
            linkStatus = WebSocketClient.LINK_STATUS.CLOSED;
        }
    }

    public static synchronized void destory() {
        if (sClient != null) {
            sClient.dispatcher().executorService().shutdown();
            sClient = null;
            linkStatus = WebSocketClient.LINK_STATUS.CLOSED;
        }
    }

    public static void reset() {
        synchronized (WebSocketClient.class) {
            sWebSocket = null;
            linkStatus = WebSocketClient.LINK_STATUS.NOT_CONNECT;
        }
    }

    public static WebSocketClient.LINK_STATUS getConnectState() {
        return linkStatus;
    }


    public static class WsListener extends WebSocketListener {
        private static final String TAG = "WS";

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            //super.onOpen(webSocket, response);
            Log.i(TAG, "WS---onOpen---");
            linkStatus = WebSocketClient.LINK_STATUS.OPEN;
            wsHandler.onOpen();
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            //super.onMessage(webSocket, text);
            Log.i(TAG, "WS---onMessage---text:" + text);
            wsHandler.onMessage(text);
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            //super.onMessage(webSocket, bytes);
            Log.i(TAG, "WS---onMessage---bytes length:" + bytes.size());
            wsHandler.onMessage(bytes.toByteArray());
            //webSocket.send(bytes.toString());
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            //super.onClosing(webSocket, code, reason);
            Log.i(TAG, "WS---onClosing---");
            linkStatus = WebSocketClient.LINK_STATUS.CLOSING;
            wsHandler.onClosing();
            reset();
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            //super.onClosed(webSocket, code, reason);
            Log.i(TAG, "WS---onClosed---");
            linkStatus = WebSocketClient.LINK_STATUS.CLOSED;
            wsHandler.onClosed();
            reset();
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            //super.onFailure(webSocket, t, response);
            Log.i(TAG, "WS---onFailure---");
            t.printStackTrace();
            linkStatus = WebSocketClient.LINK_STATUS.CLOSED;
            wsHandler.onFailure();
            reset();
        }
    }

    public interface WsHandler {
        void onOpen();

        void onMessage(String text);

        void onMessage(byte[] bytes);

        void onClosing();

        void onClosed();

        void onFailure();
    }
}