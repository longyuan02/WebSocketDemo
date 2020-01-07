package com.example.websocketdemo.util;

import java.text.DecimalFormat;
import java.util.regex.Pattern;

public class Utils {
    public static boolean isNumPlusOrMinus(String data) {

        Pattern pattern = Pattern.compile("^[-+]?[0-9]+(\\.[0-9]+)?$");

        if (pattern.matcher(data).matches()) {
            Double aDouble = Double.valueOf(data);
            if (aDouble >= 0) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    public static String isNumToPercentage(String data) {

        Pattern pattern = Pattern.compile("^[-+]?[0-9]+(\\.[0-9]+)?$");

        if (pattern.matcher(data).matches()) {
            Double aDouble = Double.valueOf(data);
            DecimalFormat df = new DecimalFormat("0.00");
            return df.format(aDouble * 100) + "%";
        } else {
            return "--";
        }
    }
    //字符串保留两位小数
    public static String isNumToTwoDecimal(String data) {
        Pattern pattern = Pattern.compile("^[-+]?[0-9]+(\\.[0-9]+)?$");
        if (pattern.matcher(data).matches()) {
            Double aDouble = Double.valueOf(data);
            DecimalFormat df = new DecimalFormat("0.00");
            return df.format(aDouble);
        } else {
            return "--";
        }
    }
    //省略整数后面小数，展示为 .00
    public static String isNumToInteger(String data) {
        Pattern pattern = Pattern.compile("^[-+]?[0-9]+(\\.[0-9]+)?$");
        if (pattern.matcher(data).matches()) {
            try {
                Double aDouble = Double.valueOf(data);
                int i = aDouble.intValue();
                if (i == 0) {
                    return "0.00";
                } else {
                    return i+"";
                }
            }catch (Exception e){
                return "0.00";
            }

        } else {
            return "--";
        }
    }
}
