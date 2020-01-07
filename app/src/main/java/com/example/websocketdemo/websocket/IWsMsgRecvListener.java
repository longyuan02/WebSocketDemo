package com.example.websocketdemo.websocket;

import com.example.websocketdemo.bean.MsgModel;
/**
 * 响应报文接口
 * Created by tangyb on 2017/9/26.
 */

public interface IWsMsgRecvListener extends IListener {
    /**
     * 接收到一条完整的业务报文
     * @param rspMsg 响应报文
     * @return true:继续下级回调     false:不继续回调
     */
    boolean onReceivedMsg(MsgModel rspMsg);
}
