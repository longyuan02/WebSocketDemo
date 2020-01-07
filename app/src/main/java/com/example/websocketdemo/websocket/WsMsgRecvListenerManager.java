package com.example.websocketdemo.websocket;


import com.example.websocketdemo.bean.MsgModel;
import com.example.websocketdemo.util.MsgUtil;

import java.util.ArrayList;
import java.util.Map;

/**
 * WebSocket 消息事件管理器
 * Created by tangyb on 2017/9/26.
 */

public class WsMsgRecvListenerManager extends ListenerManager {

    private static WsMsgRecvListenerManager instance=null;
    public static WsMsgRecvListenerManager getInstance(){
        if(instance==null){
            instance=new WsMsgRecvListenerManager();
        }
        return instance;
    }

    private WsMsgRecvListenerManager(){

    }

    /**
     * 触发报文到达事件
     * @param msg
     */
    public void triggerWsMsgRecvListener(MsgModel msg){
        if(msg.optionCode == -1 || msg.optionCode == -3){
            this.triggerWsMsgRecvListenerForError(msg);
            return;
        }
        if (msg.cMsgType == MsgModel.MSG_TYPE_PUSH && msg.sExchCode.startsWith("10611")) {
            String key= MsgUtil.getLfvListenerKey(msg.sExchCode);
            this.triggerPushLfvMsgRecvListener(key,msg);
        }else {
            String key = "";
            boolean isContinue = true;

            //根据流水号触发，一般是响应报文的处理
            key = msg.sBusiType + ":" + msg.sExchCode + ":" + msg.sSerialNo;
            isContinue = this.triggerWsMsgRecvListener(key, msg);

            //具体交易代码的触发
            if (isContinue == true) {
                key = msg.sBusiType + ":" + msg.sExchCode + ":*";
                isContinue = this.triggerWsMsgRecvListener(key, msg);
            }

            //具体业务类型的触发
            if (isContinue == true) {
                key = msg.sBusiType + ":*:*";
                isContinue = this.triggerWsMsgRecvListener(key, msg);
            }

            //所有业务的触发
            if (isContinue == true) {
                key = "*:*:*";
                isContinue = this.triggerWsMsgRecvListener(key, msg);
            }
        }
    }

    /**
     * 根据订阅的Key，触发报文到达事件
     * @param key
     * @param msg
     * @return
     */
    public boolean triggerWsMsgRecvListener(String key, MsgModel msg){
        ArrayList<IListener> al=this._map_listener.get(key);
        for(int i=0;al!=null && i<al.size();i++){
            try {
                IWsMsgRecvListener listener=(IWsMsgRecvListener)al.get(i);
                if(listener.onReceivedMsg(msg)==false){
                    return false;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return true;
    }

    public boolean triggerWsMsgRecvListenerForError(MsgModel msg) {
        for (int pos = 0; pos < this._map_listener.size(); pos++) {
            ArrayList<IListener> al = this._map_listener.get(this._map_listener.keySet().toArray()[pos]);
            for (int i = 0; al != null && i < al.size(); i++) {
                try {
//                    IWsMsgRecvListener listener = (IWsMsgRecvListener) al.get(i);
//                    if (listener.onReceivedMsg(msg) == false) {
//                        break;
//                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }


    /**
     * 触发通知消息回调
     * @param key
     * @param msg
     */
    public void triggerPushLfvMsgRecvListener(String key,MsgModel msg){
        ArrayList<IListener> al=this._map_listener.get(key);
        if(al!=null && al.size()>0){
            Map<String,String> lfvMap=msg.decodingLFVMsg();
            for(int i=0;al!=null && i<al.size();i++){
                try{
                    IWsLfvMsgRecvListener listener=(IWsLfvMsgRecvListener)al.get(i);
                    listener.onReceivedLfv(msg.sExchCode,lfvMap, msg);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

    }

    /****************************************添加监听*********************************/
    /**
     * 添加监听器（具体报文）
     * @param sBusiType  业务类型
     * @param sExchCode  交易代码
     * @param sSeqNo     报文流水号
     * @param listener 监听器
     */
    public void addWsMsgRecvListener(String sBusiType,String sExchCode,String sSeqNo,IWsMsgRecvListener listener)
    {
        String sKey = sBusiType + ":" + sExchCode + ":" + sSeqNo ;
        this.addListener(sKey, listener);
    }

    /**
     * 添加监听器（具体交易代码 ）
     * @param sBusiType  业务类型
     * @param sExchCode  交易代码
     * @param listener 监听器
     */
    public void addWsMsgRecvListener(String sBusiType,String sExchCode,IWsMsgRecvListener listener)
    {
        String sKey = sBusiType + ":" + sExchCode + ":*" ;
        this.addListener(sKey, listener);
    }

    /**
     * 添加监听器（具体业务类型）
     * @param sBusiType  业务类型
     * @param listener 监听器
     */
    public void addWsMsgRecvListener(String sBusiType,IWsMsgRecvListener listener)
    {
        String sKey = sBusiType + ":*:*" ;
        this.addListener(sKey, listener);
    }

    /**
     * 添加监听器（所有业务 ）
     * @param listener  监听
     */
    public void addWsMsgRecvListener(IWsMsgRecvListener listener)
    {
        String sKey = "*:*:*" ;
        this.addListener(sKey, listener);
    }

    /**
     * 监听推送消息
     * @param sExchCode
     * @param listener
     */
    public void addPushLfvMsgRecvListener(String sExchCode,IWsLfvMsgRecvListener listener){
        String sKey= MsgUtil.getLfvListenerKey(sExchCode);
        this.addListener(sKey,listener);
    }

    //------------------------------------ 删除监听

    /**
     * 删除监听器（具体报文）
     * @param sBusiType  业务类型
     * @param sExchCode  交易代码
     * @param sSeqNo     报文流水号
     */
    public void removeWsMsgRecvListener(String sBusiType,String sExchCode,String sSeqNo)
    {
        String sKey = sBusiType + ":" + sExchCode + ":" + sSeqNo ;
        this.removeListener(sKey);
    }

    /**
     * 删除监听器（具体报文）
     * @param sBusiType  业务类型
     * @param sExchCode  交易代码
     * @param sSeqNo     报文流水号
     * @param listener 监听器
     */
    public void removeWsMsgRecvListener(String sBusiType,String sExchCode,String sSeqNo,IWsMsgRecvListener listener)
    {
        String sKey = sBusiType + ":" + sExchCode + ":" + sSeqNo ;
        this.removeListener(sKey, listener);
    }

    /**
     * 删除监听器（具体交易代码 ）
     * @param sBusiType  业务类型
     * @param sExchCode  交易代码
     * @param listener 监听器
     */
    public void removeWsMsgRecvListener(String sBusiType,String sExchCode,IWsMsgRecvListener listener)
    {
        String sKey = sBusiType + ":" + sExchCode + ":*" ;
        this.removeListener(sKey, listener);
    }

    /**
     * 删除监听器（具体业务类型）
     * @param sBusiType  业务类型
     * @param listener 监听器
     */
    public void removeWsMsgRecvListener(String sBusiType,IWsMsgRecvListener listener)
    {
        String sKey = sBusiType + ":*:*" ;
        this.removeListener(sKey, listener);
    }

    /**
     * 删除监听器（所有业务 ）
     * @param listener  监听
     */
    public void removeWsMsgRecvListener(IWsMsgRecvListener listener)
    {
        String sKey = "*:*:*" ;
        this.removeListener(sKey, listener);
    }

    /**
     *  移除行情监听
     * @param sExchCode
     */
    public void removePushLfvMsgRecvListener(String sExchCode){
        String sKey=MsgUtil.getLfvListenerKey(sExchCode);
        this.removeListener(sKey);
    }

    /**
     * 移除行情指定监听
     * @param sExchCode
     * @param listener
     */
    public void removePushLfvMsgRecvListener(String sExchCode, IWsLfvMsgRecvListener listener){
        String sKey=MsgUtil.getLfvListenerKey(sExchCode);
        this.removeListener(sExchCode,listener);
    }
}
