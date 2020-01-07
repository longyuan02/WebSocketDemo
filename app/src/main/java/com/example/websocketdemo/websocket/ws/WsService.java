package com.example.websocketdemo.websocket.ws;

import android.util.Log;

import com.example.websocketdemo.bean.MsgModel;
import com.example.websocketdemo.websocket.IWsLfvMsgRecvListener;
import com.example.websocketdemo.websocket.IWsMsgRecvListener;
import com.example.websocketdemo.websocket.ObjCacheManager;
import com.example.websocketdemo.websocket.WebSocketClient;
import com.example.websocketdemo.websocket.WsMsgRecvListenerManager;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by tangyb on 2017/9/26.
 */

public class WsService {
    public interface WsConnectHandler{
        void onStatusChanged(WebSocketClient.LINK_STATUS status);
    }

    //要取消同步请求等待的key
    private static String willCancelReqKey=null;
    private static boolean isTimeout=false;


    /**
     * 初始化连接
     * @param wsUrl
     */
    public static void open(String wsUrl, final WsConnectHandler handler){
        WebSocketClient.open(wsUrl, new WebSocketClient.WsHandler() {
            @Override
            public void onOpen() {
                handler.onStatusChanged(WebSocketClient.LINK_STATUS.OPEN);
            }

            @Override
            public void onMessage(String text) {
                MsgModel msg=new MsgModel();
                msg.parse(text.getBytes());
                WsMsgRecvListenerManager.getInstance().triggerWsMsgRecvListener(msg);
            }

            @Override
            public void onMessage(byte[] bytes) {
                MsgModel msg=new MsgModel();
                msg.parse(bytes);
                WsMsgRecvListenerManager.getInstance().triggerWsMsgRecvListener(msg);
            }

            @Override
            public void onClosing() {
                handler.onStatusChanged(WebSocketClient.LINK_STATUS.CLOSING);
            }

            @Override
            public void onClosed() {
                handler.onStatusChanged(WebSocketClient.LINK_STATUS.CLOSED);
            }

            @Override
            public void onFailure() {
                handler.onStatusChanged(WebSocketClient.LINK_STATUS.CLOSED);
            }
        });
    }

    /**
     * 同步发送
     * @param reqMsg
     * @return
     */
    public static MsgModel syncSend(MsgModel reqMsg){
        return syncSend(reqMsg,20);
    }

    /**
     * 同步发送
     * @param reqMsg
     * @param timeout
     * @return
     */
    public static MsgModel syncSend(final MsgModel reqMsg,int timeout){
        willCancelReqKey=null;
        isTimeout=false;
        asyncSend(reqMsg, new IWsMsgRecvListener() {
            @Override
            public boolean onReceivedMsg(MsgModel rspMsg) {
                ObjCacheManager.getInstance().add(rspMsg);
                return false;
            }
        },0);
        try {
            FutureTask<MsgModel> task = new FutureTask<MsgModel>(new Callable<MsgModel>() {
                @Override
                public MsgModel call() throws Exception {
                    while (true){
                        if(isTimeout){
                            isTimeout=false;
                            ObjCacheManager.getInstance().add(reqMsg.getCacheKey(),null);
                            MsgModel timeoutMsg=new MsgModel();
                            timeoutMsg.optionCode=2;
                            return timeoutMsg;
                        }
                        if(reqMsg.getCacheKey().equals(willCancelReqKey)) {//取消请求
                            willCancelReqKey = null;
                            isTimeout=false;
                            ObjCacheManager.getInstance().add(reqMsg.getCacheKey(), null);
                            MsgModel timeoutMsg = new MsgModel();
                            timeoutMsg.optionCode = -1;
                            return timeoutMsg;
                        }
                        Object obj=ObjCacheManager.getInstance().get(reqMsg.getCacheKey());
                        if(obj!=null) {
                            ObjCacheManager.getInstance().remove(reqMsg.getCacheKey());
                            MsgModel rspMsg = (MsgModel) obj;
                            return rspMsg;
                        }
                    }
                }
            });
            new Thread(task).start();
            return task.get(timeout, TimeUnit.SECONDS);
        }catch (TimeoutException te){
            Log.i("WS","request time out!!!");
            isTimeout=true;
            ObjCacheManager.getInstance().add(reqMsg.getCacheKey(),null);
            MsgModel timeoutMsg=new MsgModel();
            timeoutMsg.optionCode=2;
            return timeoutMsg;
        }catch (Exception e){
            e.printStackTrace();
            ObjCacheManager.getInstance().add(reqMsg.getCacheKey(),null);
            MsgModel timeoutMsg=new MsgModel();
            timeoutMsg.optionCode=3;
            return timeoutMsg;
        }

        /*for(int i=0;i<timeout;i++){
            if(reqMsg.getCacheKey().equals(willCancelReqKey)){//取消请求
                willCancelReqKey=null;
                ObjCacheManager.getInstance().add(reqMsg.getCacheKey(),null);
                MsgModel timeoutMsg=new MsgModel();
                timeoutMsg.optionCode=-1;
                return timeoutMsg;
            }
            Object obj=ObjCacheManager.getInstance().get(reqMsg.getCacheKey());
            if(obj!=null){
                ObjCacheManager.getInstance().remove(reqMsg.getCacheKey());
                MsgModel rspMsg=(MsgModel)obj;
                return rspMsg;
            }else{
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        ObjCacheManager.getInstance().add(reqMsg.getCacheKey(),null);
        MsgModel timeoutMsg=new MsgModel();
        timeoutMsg.optionCode=2;
        return timeoutMsg;*/
    }

    /**
     * 异步发送
     * @param reqMsg
     * @param recvListener
     * @param reqType 请求类型  0:iq消息，1:message消息 // iq消息只回调一次
     */
    public static void asyncSend(final MsgModel reqMsg, final IWsMsgRecvListener recvListener, final int reqType){
        if(WebSocketClient.getConnectState()!= WebSocketClient.LINK_STATUS.OPEN){
            MsgModel rspMsg=new MsgModel();
            rspMsg.sBusiType=reqMsg.sBusiType;
            rspMsg.sExchCode=reqMsg.sExchCode;
            rspMsg.sSerialNo=reqMsg.sSerialNo;
            rspMsg.optionCode=1;
            recvListener.onReceivedMsg(rspMsg);
            return;
        }
        WsMsgRecvListenerManager.getInstance().addWsMsgRecvListener(reqMsg.sBusiType, reqMsg.sExchCode, reqMsg.sSerialNo, new IWsMsgRecvListener() {
            @Override
            public boolean onReceivedMsg(MsgModel rspMsg) {
                if(reqType == 0) {
                    WsMsgRecvListenerManager.getInstance().removeWsMsgRecvListener(reqMsg.sBusiType, reqMsg.sExchCode, reqMsg.sSerialNo);
                }
                recvListener.onReceivedMsg(rspMsg);
                return false;
            }
        });
        WebSocketClient.send(reqMsg.toString());
    }

    /**
     * 添加行情监听
     * @param sExchCode
     * @param recvListener
     */
    public static void registerPushMsg(String sExchCode, IWsLfvMsgRecvListener recvListener){
        WsMsgRecvListenerManager.getInstance().addPushLfvMsgRecvListener(sExchCode,recvListener);
    }

    /**
     * 移除行情监听
     * @param sExchCode
     */
    public static void unRegisterPushMsg(String sExchCode){
        WsMsgRecvListenerManager.getInstance().removePushLfvMsgRecvListener(sExchCode);
    }

    /**
     * 移除行情监听
     * @param sExchCode
     * @param recvListener
     */
    public static void unRegisterPushMsg(String sExchCode, IWsLfvMsgRecvListener recvListener){
        WsMsgRecvListenerManager.getInstance().removePushLfvMsgRecvListener(sExchCode,recvListener);
    }

    public static void cancelRequest(String reqkey){
        willCancelReqKey=reqkey;
    }

    public static void close(){
        WebSocketClient.close();
    }

    public static void destory(){
        WebSocketClient.destory();
    }
}
