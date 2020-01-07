package com.example.websocketdemo.websocket;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 监听管理器
 */

public class ListenerManager {
    protected Map<String, ArrayList<IListener>> _map_listener=new ConcurrentHashMap<String,ArrayList<IListener>>();

    /**
     * 注册监听事件
     * @param sListenerName
     * @param listener
     */
    public void addListener(String sListenerName,IListener listener){
        synchronized (this){
            ArrayList<IListener> listeners=_map_listener.get(sListenerName);
            if(listeners==null){
                listeners=new ArrayList<>();
                _map_listener.put(sListenerName,listeners);
            }
            listeners.add(listener);
        }
    }

    /**
     * 注销单个监听事件
     * @param sListenerName     事件名称
     * @param listener          对应的Listener
     */
    public boolean removeListener(String sListenerName,IListener listener)
    {
        synchronized(this)
        {
            ArrayList<IListener> listenerList = this._map_listener.get(sListenerName);
            if ( listenerList != null )
            {
                boolean isRemove = listenerList.remove(listener);
                if ( listenerList.size() <= 0 )
                    this._map_listener.remove(sListenerName);
                return isRemove;
            }else
            {
                return false;
            }
        }
    }

    /**
     * 注销所有监听事件
     * @param sListenerName     事件名称
     */
    public boolean removeListener(String sListenerName)
    {
        synchronized(this)
        {
            if ( this._map_listener.remove(sListenerName) != null )
                return true;
            else
                return false;
        }

    }
}
