package com.ajb.robot.wxpay;

import android.os.AsyncTask;
import android.util.Log;

import com.ajb.robot.common.utils.CommonUtils;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class WxPay {

    private static final int OUT_TIME = 50 * 1000;
    private static int INTERVAL_TIME = 5 * 1000;

    private static WxPay INSTANCE = null;

    public static WxPay getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new WxPay();
        }
        return INSTANCE;
    }

    /**
     * 获取付款码之后进行支付
     *
     * @param paycode 付款码
     * @param fee     订单的金额
     * @return
     */
    private Map<String, String> microPay(String paycode, int fee, String tradeNo) throws Exception {
        Map<String, Object> params = getBaseParams();

        params.put(WxConstant.nBody, WxConstant.body);
        params.put(WxConstant.nTradeNo, tradeNo);
        params.put(WxConstant.nTotalFee, fee);
        params.put(WxConstant.nDeviceIp, CommonUtils.getIPAddress());
        params.put(WxConstant.nAuthCode, paycode);

        return startRequest(params, WxConstant.MICROPAY_URL, false);

    }


    private Map<String, String> startRequest(Map<String, Object> params, String url, boolean isSsl) throws Exception {

        String sign = WxUtil.generateSignature(params, WxConstant.API_KEY, WxConstant.MD5);
        params.put(WxConstant.nSign, sign);
        String requestStr = WxUtil.mapToXml(params);
        String resultStr;
        if (!isSsl)
            resultStr = HttpUtil.httpsRequest(url, requestStr);
        else
            resultStr = HttpUtil.httpsSSLRequest(url, requestStr);
        return WxUtil.xmlToMap(resultStr);
    }

    /**
     * 查询订单
     *
     * @param tradeNo 查询的订单号（商户生成的)）
     * @return
     * @throws Exception
     */
    private Map<String, String> orderQuery(String tradeNo) throws Exception {
        Map<String, Object> params = getBaseParams();
        params.put(WxConstant.nTradeNo, tradeNo);

        return startRequest(params, WxConstant.ORDERQUERY_URL, false);

    }


    /**
     * 撤销订单
     *
     * @param tradeNo 查询的订单号（商户生成的)）
     * @return
     * @throws Exception
     */
    private Map<String, String> reverse(String tradeNo) throws Exception {
        Map<String, Object> params = getBaseParams();
        params.put(WxConstant.nTradeNo, tradeNo);

        return startRequest(params, WxConstant.REVERSE_URL, true);
    }

    //获取通用的参数
    private Map<String, Object> getBaseParams() {
        SortedMap<String, Object> params = new TreeMap<>();
        int randomNo = WxUtil.buildRandom(4);
        String timeSplit = WxUtil.getLastTime();
        params.put(WxConstant.nAppid, WxConstant.APP_ID);
        params.put(WxConstant.nMchId, WxConstant.MAH_ID);
        params.put(WxConstant.nRandomStr, timeSplit + randomNo);
        params.put(WxConstant.nSignType, WxConstant.MD5);
        return params;
    }

    public void startPay(String payCode, int fee) {
        PayTask payTask = new PayTask(payCode, fee);
        payTask.setListener(new OnPayResult() {
            @Override
            public void onSuccess(WxResult result) {
                /*当支付结果为 DIRECT_PAY_SUCCESS 或 QUERY_PAY_SUCCESS 为支付成功
                  当 返回 REVERSE_PAY_FAIL 代表支付失败！订单已经撤销
                */
                Log.d("WxPay", "支付结果为：" + WxUtil.resultToStr(result.getType()));
                Log.d("WxPay", "结果数据为：" + result.getResult().toString());
            }
        });
        payTask.execute();
    }

    private class PayTask extends AsyncTask<Void, Void, WxResult> {

        private String payCode;
        private int fee;
        private OnPayResult mListener;

        public PayTask(String payCode, int fee) {
            this.payCode = payCode;
            this.fee = fee;
        }

        public void setListener(OnPayResult listener) {
            mListener = listener;
        }

        @Override
        protected WxResult doInBackground(Void... voids) {
            return startMicroPay(payCode, fee);
        }


        @Override
        protected void onPostExecute(WxResult wxResult) {
            super.onPostExecute(wxResult);
            if (mListener != null) {
                mListener.onSuccess(wxResult);
            }
        }
    }

    public WxResult startMicroPay(String payCode, int fee) {
        //需要根据订单号进行查询和撤销订单操作，所以订单号放在外面生成
        String tradeNo = WxUtil.getTradeNo();
        try {
            Map<String, String> microResult = microPay(payCode, fee, tradeNo);
            String returnCode = microResult.get(WxConstant.nReturnCode);

            if (returnCode.equals(WxConstant.SUCCESS)) {
                String resultCode = microResult.get(WxConstant.nResultCode);

                if (resultCode.equals(WxConstant.SUCCESS)) {
                    return new WxResult(WxResult.DIRECT_PAY_SUCCESS, microResult);
                } else {

                    String errCode = microResult.get(WxConstant.nErrCode);
                    //当系统错误时进行查询订单操作
                    if (errCode.equals(WxConstant.systemError) || errCode.equals(WxConstant.userPaying)) {

                        return startQueryPay(tradeNo);
                    } else {
                        //显示错误信息
                        return new WxResult(WxResult.DIRECT_PAY_FAIL, microResult);

                    }
                }
            } else {
                return new WxResult(WxResult.DIRECT_COMM_FAIL, microResult);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private WxResult startQueryPay(String tradeNo) {
        System.out.println("进入查询方法");
        int queryTime = 0;
        while (queryTime < OUT_TIME) {
            queryTime += INTERVAL_TIME;
            try {
                Thread.sleep(INTERVAL_TIME);
                System.out.println("等待时长：" + queryTime + "毫秒！");
                Map<String, String> result = null;
                result = orderQuery(tradeNo);
                String returnCode = result.get(WxConstant.nReturnCode);
                if (returnCode.equals(WxConstant.SUCCESS)) {
                    String resultCode = result.get(WxConstant.nResultCode);
                    if (resultCode.equals(WxConstant.SUCCESS)) {
                        String tradeState = result.get(WxConstant.nTradeState);
                        //状态为交易成功
                        if (tradeState.equals(WxConstant.SUCCESS)) {

                            return new WxResult(WxResult.QUERY_PAY_SUCCESS, result);

                        } else if (tradeState.equals(WxConstant.userPaying)) {

                            //userPaying 状态说明用户正在付款，进入下一轮等待操作
                            System.out.println("查询订单状态结果：" + result.toString());

                        } else if (tradeState.equals(WxConstant.notPay) || tradeState.equals(WxConstant.payError)) {

                            /*当查询到用户状态为NOTPAY(用户取消了支付操作，关闭了支付界面)，或PAYERROR(验证密码出现错误等)
                              直接调用撤销订单api,这样做可以节省等待时间。
                            */
                            System.out.println("查询订单状态结果：" + result.toString());
                            return startReversePay(tradeNo);
                        }

                    } else {
                        System.out.println("查询失败，原因= [" + result.toString() + "]");
                        System.out.println("当前等待时间：" + queryTime + "毫秒");
//                        return new WxResult(WxResult.QUERY_PAY_FAIL, result);

                    }

                } else {
                    return new WxResult(WxResult.QUERY_COMM_FAIL, result);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        //当查询超时则 撤销订单处理
        return startReversePay(tradeNo);
    }

    private WxResult startReversePay(String tradeNo) {

        System.out.println("进入撤销订单方法");


        Map<String, String> result = null;
        try {
            result = reverse(tradeNo);
            String returnCode = result.get(WxConstant.nReturnCode);

            if (returnCode.equals(WxConstant.SUCCESS)) {
                String resultCode = result.get(WxConstant.nResultCode);

                if (resultCode.equals(WxConstant.SUCCESS)) {

                    return new WxResult(WxResult.REVERSE_PAY_SUCCESS, result);

                } else {
                    return new WxResult(WxResult.REVERSE_PAY_FAIL, result);

                }
            } else {
                return new WxResult(WxResult.REVERSE_COMM_FAIL, result);

            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

        return null;
    }

    private interface OnPayResult {
        void onSuccess(WxResult result);
    }
}
