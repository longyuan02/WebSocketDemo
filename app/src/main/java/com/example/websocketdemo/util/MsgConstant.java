package com.example.websocketdemo.util;

/**
 * 报文相关的常量定义
 * Created by tangyb on 2017/9/27.
 */

public class MsgConstant {
    //字符集
    public final static String CHARSET_NAME = "GBK";

    //行情消息监听前缀
    public final static String LFV_ONPUSH_KEY_PREFIX="KEY_LISTENER_LFV";

    public static String[] getDefaultLfvFields(){
        String[] lfv_fields=new String[72];
        lfv_fields[0] = "lastSettle";
        lfv_fields[1] = "lastClose";
        lfv_fields[2] = "open";
        lfv_fields[3] = "high";
        lfv_fields[4] = "low";
        lfv_fields[5] = "last";
        lfv_fields[6] = "close";
        lfv_fields[7] = "settle";
        lfv_fields[8] = "bid1";
        lfv_fields[9] = "bidLot1";
        lfv_fields[10] = "bid2";
        lfv_fields[11] = "bidLot2";
        lfv_fields[12] = "bid3";
        lfv_fields[13] = "bidLot3";
        lfv_fields[14] = "bid4";
        lfv_fields[15] = "bidLot4";
        lfv_fields[16] = "bid5";
        lfv_fields[17] = "bidLot5";
        lfv_fields[18] = "ask1";
        lfv_fields[19] = "askLot1";
        lfv_fields[20] = "ask2";
        lfv_fields[21] = "askLot2";
        lfv_fields[22] = "ask3";
        lfv_fields[23] = "askLot3";
        lfv_fields[24] = "ask4";
        lfv_fields[25] = "askLot4";
        lfv_fields[26] = "ask5";
        lfv_fields[27] = "askLot5";
        lfv_fields[28] = "volume";
        lfv_fields[29] = "weight";
        lfv_fields[30] = "highLimit";
        lfv_fields[31] = "lowLimit";
        lfv_fields[32] = "Posi";
        lfv_fields[33] = "upDown";
        lfv_fields[34] = "turnOver";
        lfv_fields[35] = "average";
        lfv_fields[36] = "sequenceNo";
        lfv_fields[37] = "quoteTime";
        lfv_fields[38] = "upDownRate";
        lfv_fields[39] = "bid6";
        lfv_fields[40] = "bidLot6";
        lfv_fields[41] = "bid7";
        lfv_fields[42] = "bidLot7";
        lfv_fields[43] = "bid8";
        lfv_fields[44] = "bidLot8";
        lfv_fields[45] = "bid9";
        lfv_fields[46] = "bidLot9";
        lfv_fields[47] = "bid10";
        lfv_fields[48] = "bidLot10";
        lfv_fields[49] = "ask6";
        lfv_fields[50] = "askLot6";
        lfv_fields[51] = "ask7";
        lfv_fields[52] = "askLot7";
        lfv_fields[53] = "ask8";
        lfv_fields[54] = "askLot8";
        lfv_fields[55] = "ask9";
        lfv_fields[56] = "askLot9";
        lfv_fields[57] = "ask10";
        lfv_fields[58] = "askLot10";
        lfv_fields[59] = "tradeState";
        lfv_fields[60] = "instID";
        lfv_fields[61] = "name";
        lfv_fields[62] = "quoteDate";
        lfv_fields[63] = "bidLot";
        lfv_fields[64] = "askLot";
        lfv_fields[65] = "midBidLot";
        lfv_fields[66] = "midAskLot";
        lfv_fields[67] = "quoteMinute";
        lfv_fields[68] = "quoteDateMinute";
        lfv_fields[69] = "turnOverBillion";
        lfv_fields[70] = "marketCode";
        lfv_fields[71] = "priceUnit";
        return lfv_fields;
    }
}

