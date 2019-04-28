package com.ajb.robot.wxpay;

import android.content.res.AssetManager;

import com.ajb.robot.common.base.BaseApplication;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;


public class HttpUtil {

    public static final String GET = "GET";
    public static final String PUT = "PUT";
    public static final String POST = "POST";

    //请求方法
    public static String httpsRequest(String requestUrl, String data) {
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            // 设置请求方式（GET/POST）
            conn.setRequestMethod(POST);
            conn.setRequestProperty("content-type", "application/x-www-form-urlencoded");
            // 当outputStr不为null时向输出流写数据
            if (null != data) {
                OutputStream outputStream = conn.getOutputStream();
                // 注意编码格式
                outputStream.write(data.getBytes("UTF-8"));
                outputStream.close();
            }
            // 从输入流读取返回内容
            InputStream inputStream = conn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String str = null;
            StringBuffer buffer = new StringBuffer();
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            // 释放资源
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
            inputStream = null;
            conn.disconnect();
            return buffer.toString();
        } catch (ConnectException ce) {
            System.out.println("连接超时");
            ce.printStackTrace();
        } catch (Exception e) {
            System.out.println("https请求异常");
            e.printStackTrace();
        }
        return null;
    }

    //带双向验证的请求方法
    public static String httpsSSLRequest(String urlstr, String data) throws IOException, KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyManagementException, CertificateException, NoSuchProviderException {
        String result = null;
        // 证书密码（默认为商户ID）
        String password = WxConstant.MAH_ID;
        // 实例化密钥库
        KeyStore ks = KeyStore.getInstance("PKCS12");
        // 获得密钥库文件流

        AssetManager am = BaseApplication.getContext().getResources().getAssets();
        InputStream fis = am.open("apiclient_cert.p12");
//        File file = new File("/Users/chenyulong/Desktop/apiclient_cert.p12");
//        InputStream fis = new FileInputStream(file);

        // 加载密钥库
        ks.load(fis, password.toCharArray());
        // 关闭密钥库文件流
        fis.close();
        // 实例化密钥库
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        // 初始化密钥工厂
        kmf.init(ks, password.toCharArray());
        // 创建SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());
        // 获取SSLSocketFactory对象
        SSLSocketFactory ssf = sslContext.getSocketFactory();
        URL url = new URL(urlstr);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        //设置当前实例使用的SSLSocketFactory
        conn.setSSLSocketFactory(ssf);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.connect();
        DataOutputStream out = new DataOutputStream(
                conn.getOutputStream());
        if (data != null)
            out.writeBytes(data);
        out.flush();
        out.close();
        //获取输入流
        InputStream inputStream = conn.getInputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        int code = conn.getResponseCode();
        if (HttpsURLConnection.HTTP_OK == code) {
            String temp = in.readLine();
            while (temp != null) {
                if (result != null)
                    result += temp;
                else
                    result = temp;
                temp = in.readLine();
            }
        }
        return result;
    }
}
