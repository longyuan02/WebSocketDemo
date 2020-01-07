package com.example.websocketdemo.bean;

import com.example.websocketdemo.util.MsgConstant;
import com.example.websocketdemo.util.MsgUtil;

import java.util.HashMap;
import java.util.Map;

public class MsgModel {
    public static final int LEN_SERIAL_NO=8;
    public static final int LEN_cEncrytMode=1;
    public static final int LEN_EXCH_CODE=5;
    public static final int LEN_MSG_TYPE=1;
    public static final int LEN_USER_ID=10;
    public static final int LEN_SESSION_ID=12;//10;

    //报文长度
    public static final int LEN_SUM = LEN_SERIAL_NO + LEN_cEncrytMode + LEN_EXCH_CODE
            +LEN_MSG_TYPE + LEN_USER_ID + LEN_SESSION_ID;

    /**************报文加解密标志定义**************/
    //加解密标志：无加密，明文传输
    public static final char ENCRYPT_MODE_NONE = '0';
    //加解密标志：RSA算法加密
    public static final char ENCRYPT_MODE_RSA = '1';
    //加解密标志：3DES加密（会话加密）
    public static final char ENCRYPT_MODE_3DES_SESSION = '2';
    //加解密标志：3DES加密（默认加密）
    public static final char ENCRYPT_MODE_3DES_DEFAULT = '3';
    //加解密标志：ZIP压缩
    public static final char ENCRYPT_MODE_ZIP = '4';
    //加解密标志：先ZIP压缩后3DES加密（会话加密）
    public static final char ENCRYPT_MODE_ZIP_3DES_SESSION = '5';
    //加解密标志：先ZIP压缩后3DES加密（默认加密）
    public static final char ENCRYPT_MODE_ZIP_3DES_DEFAULT = '6';

    /********************报文类型*********************/
    //报文类型：0 请求
    public static final char MSG_TYPE_REQ = '0';
    //报文类型：1 应答
    public static final char MSG_TYPE_RSP = '1';
    //报文类型：2 推送
    public static final char MSG_TYPE_PUSH = '2';

    //交易流水号  8
    public String sSerialNo="";
    //报文加密标志  1
    public char cEncryptMode='\0';
    //交易代码  5
    public String sExchCode="";
    //报文类型  1  // 0：客户请求 1：服务端应答 2：服务端推送
    public char cMsgType='\0';
    //用户ID  10
    public String sUserID="";
    //会话ID  10
    public String sSessionID="";

    //明文报文体
    public byte[] arrCleanBody=null;
    //密文报文体
    public byte[] arrClipherBody=null;

    //字符串Json原文
    public String sSrcJsonMsg="";

    //消息来源ws

    //所属业务类型  通过从配置文件中获取
    public String sBusiType="";
    //所属代理机构  从证书编码配置文件中获取
    public String sBranchID="";
    //所属终端类型  从证书编码配置文件中获取
    public String sTermType="";
    //所属用户类型  从证书编码配置文件中获取
    public String sUserType="";

    //操作码  0:正常，1:连接断开，网络异常，2：请求超时，3：数据异常，4：连接初始化失败，-1：取消请求，
    public int optionCode = 0;

    //缓存消息KEY
    public String getCacheKey() {
        return this.sBusiType + ":" + this.sExchCode + ":" + this.sSerialNo;
    }

    /**
     * 解析报文
     * @param arrSrcMsg
     */
    public void parse(byte[] arrSrcMsg){
        int iOffset=0;
        this.sSerialNo=new String(arrSrcMsg,iOffset,MsgModel.LEN_SERIAL_NO).trim();
        iOffset+=MsgModel.LEN_SERIAL_NO;
        this.cEncryptMode=(char)arrSrcMsg[iOffset];
        iOffset+=MsgModel.LEN_cEncrytMode;

        this.sExchCode=new String(arrSrcMsg,iOffset,MsgModel.LEN_EXCH_CODE).trim();
        iOffset+=MsgModel.LEN_EXCH_CODE;

        this.cMsgType=(char)arrSrcMsg[iOffset];
        iOffset+=MsgModel.LEN_MSG_TYPE;

        this.sUserID=new String(arrSrcMsg,iOffset,MsgModel.LEN_USER_ID).trim();
        iOffset+=MsgModel.LEN_USER_ID;

        this.sSessionID=new String(arrSrcMsg,iOffset,LEN_SESSION_ID).trim();
        iOffset+=MsgModel.LEN_SESSION_ID;

        this.arrClipherBody=new byte[arrSrcMsg.length-MsgModel.LEN_SUM];
        System.arraycopy(arrSrcMsg,iOffset,this.arrClipherBody,0,this.arrClipherBody.length);

        if('0' == this.cEncryptMode){//明文
            this.arrCleanBody = this.arrClipherBody;
        }else{
            //解密

        }
    }

    public void decrypt() throws Exception{

    }

    public void encryt() throws Exception{

    }

    public String toString(){
        StringBuffer sb=new StringBuffer();
        sb.append(MsgUtil.fill(this.sSerialNo,' ',MsgModel.LEN_SERIAL_NO,'R'));
        sb.append(this.cEncryptMode);
        sb.append(MsgUtil.fill(this.sExchCode,' ',MsgModel.LEN_EXCH_CODE,'L'));
        sb.append(this.cMsgType);
        sb.append(MsgUtil.fill(this.sUserID,' ',MsgModel.LEN_USER_ID,'R'));
        sb.append(MsgUtil.fill(this.sSessionID,' ',MsgModel.LEN_SESSION_ID,'R'));
        sb.append(this.sSrcJsonMsg);
        return sb.toString();
    }

    /**
     * 生成密文字节数组
     * @return
     */
    public byte[] toBytes(){
        StringBuilder sb=new StringBuilder();
        sb.append(MsgUtil.fill(this.sSerialNo,' ',MsgModel.LEN_SERIAL_NO,'R'));
        sb.append(this.cEncryptMode);
        sb.append(MsgUtil.fill(this.sExchCode,' ',MsgModel.LEN_EXCH_CODE,'L'));
        sb.append(this.cMsgType);
        sb.append(MsgUtil.fill(this.sUserID,' ',MsgModel.LEN_USER_ID,'R'));
        sb.append(MsgUtil.fill(this.sSessionID,' ',MsgModel.LEN_SESSION_ID,'R'));

        if ( this.arrClipherBody == null )
            this.arrClipherBody = new byte[0];

        byte[] arrHead = sb.toString().getBytes();
        if ( arrHead.length != MsgModel.LEN_SUM )
            throw new RuntimeException("实际长度[" + arrHead.length + "]与理论长度[" + MsgModel.LEN_SUM + "]不符！");

        byte[] result = new byte[MsgModel.LEN_SUM + this.arrClipherBody.length];
        System.arraycopy(arrHead, 0, result, 0, arrHead.length);
        System.arraycopy(this.arrClipherBody, 0, result, arrHead.length, this.arrClipherBody.length);

        return result;
    }

    /**
     * 解析LFV消息
     * @return
     */
    public Map<String,String> decodingLFVMsg(){
        Map<String,String> lfvMap=new HashMap<>();
        int arr_index=0;
        while (arr_index<this.arrClipherBody.length){
            String l=MsgUtil.getByteBinary(this.arrClipherBody[arr_index++]);
            int vLen=Integer.parseInt(l.substring(4),2);

            String f= MsgUtil.getByteBinary(this.arrClipherBody[arr_index++]);
            String v="";
            for(int i=0;i<vLen;i++){
                v+=MsgUtil.getByteBinary(this.arrClipherBody[arr_index++]);
            }
            String[] kv=this.decodingLFV(l+f+v);
            lfvMap.put(kv[0],kv[1]);
        }
        return lfvMap;
    }

    /**
     *  解析单个LFV
     * @param srcLFV
     * @return
     */
    private String[] decodingLFV(String srcLFV) {
        String fh;
        int xs_n;
        String ls = srcLFV.substring(0, 8);

        if (ls.substring(0, 1).equals("0"))
            fh = "";
        else
            fh = "-";
        String xs_n_str = ls.substring(1, 4);
        xs_n = Integer.parseInt(xs_n_str, 2);//转十进制
        //长度
        String v_n_str = ls.substring(4, 8);
        int v_n = Integer.parseInt(v_n_str, 2);
        //行情字段序号
        String fs_str = srcLFV.substring(8, 16);
        int fs = Integer.parseInt(fs_str, 2);
        //字段值
        String vs_str = srcLFV.substring(16, 16 + 8 * v_n);
        String vs = "" + Long.parseLong(vs_str, 2);
        if (xs_n == 0) {
            if (fs == 37) {//行情时间（quoteTime）的值为：09:45:53
                if (vs.length() < 6)
                    vs = "0" + vs;
                vs = vs.substring(0, 2) + ":" + vs.substring(2, 4) + ":" + vs.substring(4, 6);
            }
        } else {
            if (vs.length() == xs_n) {
                vs = "0." + vs;
            } else if (xs_n < vs.length()) {
                vs = vs.substring(0, vs.length() - xs_n) + "." + vs.substring(vs.length() - xs_n, vs.length());
            } else {
                int length = vs.length();
                for (int i = 0; i < xs_n - length; i++) {
                    vs = "0" + vs;
                }
                vs = "0" + "."+ vs;
            }
        }
        String[] result = new String[2];
        result[0] = MsgConstant.getDefaultLfvFields()[fs];
        result[1] = fh + vs;
        return result;
    }
}
