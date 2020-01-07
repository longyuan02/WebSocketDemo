package com.example.websocketdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.websocketdemo.bean.MsgModel;
import com.example.websocketdemo.websocket.IWsLfvMsgRecvListener;
import com.example.websocketdemo.websocket.IWsMsgRecvListener;
import com.example.websocketdemo.websocket.MultigoldApi;
import com.example.websocketdemo.util.Utils;
import com.example.websocketdemo.websocket.WebSocketClient;
import com.example.websocketdemo.websocket.ws.WsService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private Map<String, Map<String, String>> quotesDataList = new HashMap();
    private List<String> quotesTypeList = new ArrayList();
    private int plus, minus;
    private TextView title1;
    private TextView gold_extension;
    private TextView gold_extension_price;
    private TextView gold_extension_gain1;
    private TextView gold_extension_gain2;
    private TextView title2;
    private TextView mini_gold_extension;
    private TextView mini_gold_price;
    private TextView mini_gold_gain1;
    private TextView mini_gold_gain2;
    private TextView title3;
    private TextView silver_extension;
    private TextView silver_price;
    private TextView silver_gain1;
    private TextView silver_gain2;
    private ConstraintLayout quotes_layout_1;
    private ConstraintLayout quotes_layout_2;
    private ConstraintLayout quotes_layout_3;
    private LinearLayout more;
    private ImageView imgTitle;
    private TextView subTitle;
    private IWsMsgRecvListener recvListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initNetListener();
        initViews();
        plus = Color.parseColor("#D00202");
        minus = Color.parseColor("#038505");
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\"Au(T+D)\",\"Ag(T+D)\",\"mAu(T+D)\"");
        //开启行情推送链接
        WsService.open(MultigoldApi.WS_ADDRESS, new WsService.WsConnectHandler() {
            @Override
            public void onStatusChanged(WebSocketClient.LINK_STATUS status) {
                //Log.e("LINK_STATUS",status.toString());
                if (status == WebSocketClient.LINK_STATUS.OPEN) {
                    WsService.asyncSend(quotesParams(stringBuilder.toString()), recvListener, 1);
                }
            }
        });
        WsService.asyncSend(quotesParams(stringBuilder.toString()), recvListener, 1);
    }

    private void initViews() {
        title1 = findViewById(R.id.title1);
        quotes_layout_1 = findViewById(R.id.quotes_layout_1);
        gold_extension = findViewById(R.id.gold_extension);
        gold_extension_price = findViewById(R.id.gold_extension_price);
        gold_extension_gain1 = findViewById(R.id.gold_extension_gain1);
        gold_extension_gain2 = findViewById(R.id.gold_extension_gain2);

        title2 = findViewById(R.id.title2);
        quotes_layout_2 = findViewById(R.id.quotes_layout_2);
        mini_gold_extension = findViewById(R.id.mini_gold_extension);
        mini_gold_price = findViewById(R.id.mini_gold_price);
        mini_gold_gain1 = findViewById(R.id.mini_gold_gain1);
        mini_gold_gain2 = findViewById(R.id.mini_gold_gain2);

        title3 = findViewById(R.id.title3);
        quotes_layout_3 = findViewById(R.id.quotes_layout_3);
        silver_extension = findViewById(R.id.silver_extension);
        silver_price = findViewById(R.id.silver_price);
        silver_gain1 = findViewById(R.id.silver_gain1);
        silver_gain2 = findViewById(R.id.silver_gain2);
    }

    /*
     *  行情数据请求参数
     */
    public MsgModel quotesParams(String str) {
        final MsgModel reqMsg = new MsgModel();
        reqMsg.sSerialNo = "JSU7BN2E";
        reqMsg.cEncryptMode = '0';
        reqMsg.sExchCode = "10611";
        reqMsg.cMsgType = '0';
        reqMsg.sUserID = "";
        //reqMsg.sSrcJsonMsg = "{\"oper_flag\":\"1\",\"list_subs_prod_code\":[\"Au(T+D)\",\"Ag(T+D)\",\"mAu(T+D)\"]}";
        reqMsg.sSrcJsonMsg = "{\"oper_flag\":\"1\",\"list_subs_prod_code\":[" + str + "]}";
        return reqMsg;

    }

    private void initNetListener() {
        //先把监听初始化先
        recvListener = new IWsMsgRecvListener() {
            @Override
            public boolean onReceivedMsg(final MsgModel rspMsg) {
                return false;
            }
        };
        WsService.unRegisterPushMsg("10611");
        //行情消息推送回来的监听和数据处理
        WsService.registerPushMsg("10611", new IWsLfvMsgRecvListener() {
            @Override
            public void onReceivedLfv(String sExchCode, final Map<String, String> map, MsgModel msg) {
                final String sUserID = msg.sUserID;
                Log.e("LINK_STATUS======", map.toString() + "quotesTypeList:==" + quotesTypeList.size() + "sUserID:===" + sUserID);
                String last = map.get("last");
                String upDown = map.get("upDown");
                String upDownRate = map.get("upDownRate");

                Map<String, String> stringMap = quotesDataList.get(sUserID);
                if (stringMap == null) {
                    stringMap = new HashMap();
                }
                if (last != null) {
                    stringMap.put("last", last);
                }
                if (upDown != null) {
                    stringMap.put("upDown", upDown);
                }
                if (upDownRate != null) {
                    stringMap.put("upDownRate", upDownRate);
                }
                quotesDataList.put(sUserID, stringMap);

//                switch (i) {
//                    case 0:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setQuotesData(sUserID, gold_extension_price, gold_extension_gain1, gold_extension_gain2);
                    }
                });
//                        break;
//                    case 1:
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                setQuotesData(sUserID, mini_gold_price, mini_gold_gain1, mini_gold_gain2);
//                            }
//                        });
//
//                        break;
//                    case 2:
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                setQuotesData(sUserID, silver_price, silver_gain1, silver_gain2);
//                            }
//                        });
//                        break;
//                }

//                if (!TextUtils.isEmpty(sUserID)) {
//                    for (int i = 0; i < quotesTypeList.size(); i++) {
//                        if (sUserID.equals(quotesTypeList.get(i))) {
//                            String last = map.get("last");
//                            String upDown = map.get("upDown");
//                            String upDownRate = map.get("upDownRate");
//
//                            Map<String, String> stringMap = quotesDataList.get(sUserID);
//                            if (stringMap == null) {
//                                stringMap = new HashMap();
//                            }
//                            if (last != null) {
//                                stringMap.put("last", last);
//                            }
//                            if (upDown != null) {
//                                stringMap.put("upDown", upDown);
//                            }
//                            if (upDownRate != null) {
//                                stringMap.put("upDownRate", upDownRate);
//                            }
//                            quotesDataList.put(sUserID, stringMap);
//
//                            switch (i) {
//                                case 0:
//                                    runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            setQuotesData(sUserID, gold_extension_price, gold_extension_gain1, gold_extension_gain2);
//                                        }
//                                    });
//                                    break;
//                                case 1:
//                                    runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            setQuotesData(sUserID, mini_gold_price, mini_gold_gain1, mini_gold_gain2);
//                                        }
//                                    });
//
//                                    break;
//                                case 2:
//                                    runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            setQuotesData(sUserID, silver_price, silver_gain1, silver_gain2);
//                                        }
//                                    });
//                                    break;
//                            }
//
//                        }
//                    }
//                }
            }
        });
    }

    public void setQuotesData(String type, TextView price, TextView gain1, TextView gain2) {

        Map<String, String> data = quotesDataList.get(type);
        if (data != null) {
            String last = data.get("last");
            String upDown = data.get("upDown");
            String upDownRate = data.get("upDownRate");
            if (last != null) {
                if (type.equals("Ag(T+D)")) {
                    price.setText(Utils.isNumToInteger(last));
                } else {
                    price.setText(Utils.isNumToTwoDecimal(last));
                }
            }
            if (upDown != null) {
                gain1.setText(Utils.isNumToTwoDecimal(upDown));
            }
            if (upDownRate != null) {
                gain2.setText(Utils.isNumToPercentage(upDownRate));
            }
            if (Utils.isNumPlusOrMinus(upDown)) {
                price.setTextColor(plus);
                gain1.setTextColor(plus);
                gain2.setTextColor(plus);
                gain1.setText("+" + gain1.getText());
                gain2.setText("+" + gain2.getText());
            } else {
                price.setTextColor(minus);
                gain1.setTextColor(minus);
                gain2.setTextColor(minus);
                gain2.setText(" " + gain2.getText());
            }

        }

    }
}
