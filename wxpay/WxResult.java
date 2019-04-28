package com.ajb.robot.wxpay;

import java.util.Map;

/**
 * 用于微信扫码支付中的各种状态和返回
 */
public class WxResult {
    public static final int DIRECT_PAY_SUCCESS = 1;
    public static final int DIRECT_PAY_FAIL = 4;
    public static final int DIRECT_COMM_FAIL = 7;
    public static final int QUERY_PAY_SUCCESS = 2;
    public static final int QUERY_PAY_FAIL = 5;
    public static final int QUERY_COMM_FAIL = 8;
    public static final int REVERSE_PAY_SUCCESS = 3;
    public static final int REVERSE_PAY_FAIL = 6;
    public static final int REVERSE_COMM_FAIL = 9;


    private int type;
    private Map<String,String> result;

    public WxResult(int type, Map<String, String> result) {
        this.type = type;
        this.result = result;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Map<String, String> getResult() {
        return result;
    }

    public void setResult(Map<String, String> result) {
        this.result = result;
    }
}
