package com.ajb.robot.wxpay;

public class WxConstant {
    public static final String APP_ID = "******";                       //微信平台应用id
    public static final String APP_SECRET = "******";     //应用对应的凭证
    public static final String MAH_ID = "********";                               //商业号
    public static final String API_KEY = "********";        //API KEY

    public static final String FAIL = "FAIL";
    public static final String SUCCESS = "SUCCESS";
    public static final String HMACSHA256 = "HMAC-SHA256";
    public static final String MD5 = "MD5";
    public static final String body = "安居宝_机器人停车系统";

    public static final String FIELD_SIGN = "sign";
    public static final String FIELD_SIGN_TYPE = "sign_type";

    public static final String BASE_URL = "https://api.mch.weixin.qq.com";
    public static final String MICROPAY_URL = BASE_URL + "/pay/micropay";
    public static final String ORDERQUERY_URL = BASE_URL + "/pay/orderquery";
    public static final String REVERSE_URL = BASE_URL + "/secapi/pay/reverse";


    //params name
    public static final String nAppid = "appid";
    public static final String nMchId = "mch_id";
    public static final String nRandomStr = "nonce_str";
    public static final String nSign = "sign";
    public static final String nSignType = "sign_type";
    public static final String nBody = "body";
    public static final String nTradeNo = "out_trade_no";
    public static final String nTotalFee = "total_fee";
    public static final String nDeviceIp = "spbill_create_ip";
    public static final String nAuthCode = "auth_code";


    //return status params name
    public static final String nReturnCode = "return_code";
    public static final String nReturnMsg = "return_msg";
    public static final String nTradeState = "trade_state";



    public static final String nResultCode = "result_code";
    public static final String nErrCode = "err_code";
    public static final String nErrCodeDes = "err_code_des";

    //err code name
    public static final String systemError = "SYSTEMERROR";
    public static final String userPaying = "USERPAYING";
    public static final String notPay = "NOTPAY";
    public static final String payError = "PAYERROR";


}
