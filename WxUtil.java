package com.ajb.robot.wxpay;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class WxUtil {

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMddHHmmssSSS");

    /**
     * 取出一个指定长度大小的随机正整数.
     *
     * @param length 设定所取出随机数的长度。length小于11
     * @return int 返回生成的随机数。
     */
    public static int buildRandom(int length) {
        int num = 1;
        double random = Math.random();
        if (random < 0.1) {
            random = random + 0.1;
        }
        for (int i = 0; i < length; i++) {
            num = num * 10;
        }
        return (int) ((random * num));
    }

    /**
     * genarate current trade id
     *
     * @return
     */
    public static String getTradeNo() {
        return simpleDateFormat.format(new Date()) + buildRandom(4);
    }

    /**
     * 获取当前时间 yyyyMMddHHmmss
     *
     * @return String
     */
    public static String getCurrTime() {
        Date now = new Date();
        SimpleDateFormat outFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String s = outFormat.format(now);
        return s;
    }

    /**
     * 获取去掉前面8位的时间字符串
     *
     * @return
     */
    public static String getLastTime() {
        Date now = new Date();
        SimpleDateFormat outFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String s = outFormat.format(now);
        return s.substring(8, s.length());
    }


    /**
     * 生成签名. 注意，若含有sign_type字段，必须和signType参数保持一致。
     *
     * @param data     待签名数据
     * @param key      API密钥
     * @param signType 签名方式
     * @return 签名
     */
    public static String generateSignature(final Map<String, Object> data, String key, String signType) throws Exception {
        Set<String> keySet = data.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);
        StringBuilder sb = new StringBuilder();
        for (String k : keyArray) {
            if (k.equals(WxConstant.nSign)) {
                continue;
            }
            String value = String.valueOf(data.get(k));
            if (value.trim().length() > 0) // 参数值为空，则不参与签名
                sb.append(k).append("=").append(value).append("&");
        }
        sb.append("key=").append(key);
        if (WxConstant.MD5.equals(signType)) {
            return MD5(sb.toString()).toUpperCase();
        } else if (WxConstant.HMACSHA256.equals(signType)) {
            return HMACSHA256(sb.toString(), key);
        } else {
            throw new Exception(String.format("Invalid sign_type: %s", signType));
        }
    }

    /**
     * 生成 MD5
     *
     * @param data 待处理数据
     * @return MD5结果
     */
    public static String MD5(String data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] array = md.digest(data.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte item : array) {
            sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString().toUpperCase();
    }

    /**
     * 生成 HMACSHA256
     *
     * @param data 待处理数据
     * @param key  密钥
     * @return 加密结果
     * @throws Exception
     */
    public static String HMACSHA256(String data, String key) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] array = sha256_HMAC.doFinal(data.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte item : array) {
            sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString().toUpperCase();
    }

    /**
     * 把xml文件转换成Map
     *
     * @param strXML xml字符串
     * @return 转换后的Map
     * @throws Exception
     */
    public static Map<String, String> xmlToMap(String strXML) throws Exception {
        try {
            Map<String, String> data = new HashMap<String, String>();
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            InputStream stream = new ByteArrayInputStream(strXML.getBytes("UTF-8"));
            org.w3c.dom.Document doc = documentBuilder.parse(stream);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getDocumentElement().getChildNodes();
            for (int idx = 0; idx < nodeList.getLength(); ++idx) {
                Node node = nodeList.item(idx);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    org.w3c.dom.Element element = (org.w3c.dom.Element) node;
                    data.put(element.getNodeName(), element.getTextContent());
                }
            }
            try {
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return data;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    //把map转化成字符串类型的xml
    public static String mapToXml(Map<String, Object> parameters) {
        StringBuffer sb = new StringBuffer();
        sb.append("<xml>");
        Set es = parameters.entrySet();
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String key = String.valueOf(entry.getKey());
            String value = String.valueOf(entry.getValue());
            sb.append("<" + key + ">" + value + "</" + key + ">");
        }
        sb.append("</xml>");
        return sb.toString();
    }

    public static String resultToStr(int type) {
        switch (type) {
            case WxResult.DIRECT_PAY_SUCCESS:
                return "直接支付成功！";
            case WxResult.DIRECT_PAY_FAIL:
                return "直接支付失败！";
            case WxResult.DIRECT_COMM_FAIL:
                return "直接支付通讯失败！";
            case WxResult.QUERY_PAY_SUCCESS:
                return "查询支付成功！";
            case WxResult.QUERY_PAY_FAIL:
                return "查询支付失败！";
            case WxResult.QUERY_COMM_FAIL:
                return "查询支付通讯失败！";
            case WxResult.REVERSE_PAY_SUCCESS:
                return "撤销订单成功！";
            case WxResult.REVERSE_PAY_FAIL:
                return "撤销订单失败！";
            case WxResult.REVERSE_COMM_FAIL:
                return "撤销订单通讯失败！";
        }

        return "没有这种类型！";
    }

}
