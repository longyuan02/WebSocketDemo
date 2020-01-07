package com.example.websocketdemo.util;

import java.io.UnsupportedEncodingException;

/**
 * 报文处理工具类
 */

public class MsgUtil {
    /**
     *  获得行情监听KEY
     * @param sExchCode
     * @return
     */
    public static String getLfvListenerKey(String sExchCode){
        return MsgConstant.LFV_ONPUSH_KEY_PREFIX+"_"+sExchCode;
    }

    /**
     * 获得字节的二进制编码
     * @param b
     * @return
     */
    public static String getByteBinary(byte b){
        String bin=Integer.toBinaryString(b);
        if(bin.length()<8){
            return fill(bin,'0',8,'L');
        }else if(bin.length()==8){
            return bin;
        }else{
            return bin.substring(bin.length()-8);
        }
    }

    /**
     * 转换为UTF-8编码
     * @param s
     * @return
     */
    public static byte[] utf8Bytes(String s){
        try{
            return s.getBytes("UTF8");
        }catch (UnsupportedEncodingException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * 转换为ASCII编码
     * @param s
     * @return
     */
    public static byte[] asciiBytes(String s){
        try{
            return s.getBytes("ASCII");
        }catch (UnsupportedEncodingException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * 将字符串转为字符型
     * @param str
     * @return
     */
    public static char stringToChar(String str){
        if(str==null || str.length()<=0){
            return ' ';
        }else{
            return str.charAt(0);
        }
    }

    /**
     * 将字符串转为字节数组
     * @param str
     * @return
     */
    public static byte[] stringToBytes(String str){
        try{
            if(str== null || str.length()<=0){
                return new byte[0];
            }else{
                return str.getBytes(MsgConstant.CHARSET_NAME);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将字符串转为字节数组
     * @param bytes
     * @return
     */
    public static String byteToString(byte[] bytes){
        try {
            return new String(bytes,MsgConstant.CHARSET_NAME);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 字符串填充
     * @param s 需要填充的字符串
     * @param c 填充的字符
     * @param n 填充长度
     * @param f 左右标志
     * @return
     */
    public static String fill(String s,char c,int n,char f) {
        int iByteLen = stringToBytes(s).length;
        if (iByteLen >= n) {
            return s;
        } else {
            byte[] fillChars = new byte[n - iByteLen];
            for (int i = 0; i < fillChars.length; i++) {
                fillChars[i] = (byte) c;
            }
            if (f == 'L') {//左补
                return new String(fillChars) + s;
            } else {//右补
                return s + new String(fillChars);
            }
        }
    }

    public static String fill(int s,char c,int n,char f){
        return fill(""+s,c,n,f);
    }

    public static String fill(long s,char c,int n,char f){
        return fill(""+s,c,n,f);
    }

    /**
     * 将数字格式化为指定长度的字符串格式
     * @param num
     * @param len
     * @return
     */
    public static String formatNum(int num,int len ) {
        String s = String.valueOf(num);
        if ( s.length() < len ){
            s = MsgUtil.fill("0",'0',len-s.length(),'L') + s;
        }
        return s;
    }
}

