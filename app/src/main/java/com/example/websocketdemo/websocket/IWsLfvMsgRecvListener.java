package com.example.websocketdemo.websocket;

import com.example.websocketdemo.bean.MsgModel;

import java.util.Map;

/**
 * 行情推送回调接口
 * Created by tangyb on 2017/10/16.
 */

public interface IWsLfvMsgRecvListener extends IListener {
    /**
     * 行情消息推送回调
     * @param sExchCode 交易代卖
     * @param lfvMsg 行情消息
     */
    void onReceivedLfv(String sExchCode, Map<String, String> lfvMsg, MsgModel msg);
}
