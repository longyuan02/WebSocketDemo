package com.example.websocketdemo.websocket;

import com.example.websocketdemo.bean.MsgModel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存管理
 */

public class ObjCacheManager {
    private static ObjCacheManager instance=null;
    public static ObjCacheManager getInstance(){
        if(instance == null) {
            synchronized (ObjCacheManager.class) {
                if (instance == null) {
                    instance = new ObjCacheManager();
                }
            }
        }
        return instance;
    }

    //缓存内容
    private Map<String,Object> objCacheAllData=new ConcurrentHashMap<>();

    public void add(MsgModel msg){
        if(objCacheAllData.containsKey(msg.getCacheKey())){
            if(objCacheAllData.get(msg.getCacheKey()) == "null"){//取消缓存
                objCacheAllData.remove(msg.getCacheKey());
                return;
            }else{
                objCacheAllData.remove(msg.getCacheKey());
            }
        }
        add(msg.getCacheKey(),msg);
    }

    public void add(String key , Object obj){
        if(!objCacheAllData.containsKey(key)){
            objCacheAllData.put(key,obj);
        }
    }

    public Object get(String key){
        return objCacheAllData.get(key);
    }

    public void remove(String key){
        if(objCacheAllData.containsKey(key)){
            objCacheAllData.remove(key);
        }
    }
}