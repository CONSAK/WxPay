package com.ajb.robot.wxpay;

public class WxPayTest {
    public static void main(String[] args) {
        WxResult result = WxPay.getInstance().startMicroPay("135466172872057731", 1500 * 100);

        WxResult resultQuery = WxPay.getInstance().startQueryPay("1901211607036846090");
        System.out.println(">>>>>>>>>支付结果为：" + WxUtil.resultToStr(resultQuery.getType()) + resultQuery.getResult());
            }

}
