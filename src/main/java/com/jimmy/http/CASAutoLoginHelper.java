package com.jimmy.http;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by eagle on 17/5/14.
 */
public class CASAutoLoginHelper {
    private static String CAS_LOGIN_URL = "https://*******/login";
    private static String CAS_LOGOUT_URL = "https://********/logout";

    public static boolean autoLogin(CloseableHttpClient client, String username, String password) throws Exception {
        boolean loginSucceed = false;
        try {
            HttpGetter.send(client, CAS_LOGOUT_URL);

            String content = HttpGetter.send(client, CAS_LOGIN_URL);
            //System.out.println(content);
            Map<String, String> map = new HashMap<String, String>();
            Document dc = Jsoup.parse(content);
            Elements e = dc.getElementsByTag("script");
            Element scriptElement = e.last();
            System.out.println(scriptElement);
            String[] data = scriptElement.data().toString().split("\\r\\n");

            /*取得单个JS变量*/
            for (String variable : data) {
                if (StringUtils.isBlank(variable)) {
                    continue;
                }
            /*取到满足条件的JS变量*/
                String line = variable.split(" ")[1];
                int index = line.indexOf("=");
                if (index == -1) {
                    continue;
                }
                String[] kvp = line.split("=", 2);
                String key = kvp[0].trim();
                String value = kvp[1].endsWith(";") ? kvp[1].trim().substring(0, kvp[1].length() - 1) : kvp[1].trim();
                value = value.substring(1, value.length() - 1);
                /*取得JS变量存入map*/
                if (!map.containsKey(key))
                    map.put(key, value);
            }

            System.out.println(map);

            String strInfo = map.get("strInfo");
            String lt = map.get("lt");
            System.out.println(lt);

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/x-www-form-urlencoded");
//        headers.put("Upgrade-Insecure-Requests", "1");
            //headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.96 Safari/537.36");
            headers.put("Connection", "keep-alive");

            Map<String, String> cont = new HashMap<String, String>();
            cont.put("lt", lt);
            cont.put("username", username);
            cont.put("password", password);
            cont.put("_eventId", "submit");
            cont.put("execution", strInfo);
            cont.put("rememberMe", "on");
            cont.put("mi_service", "");
            cont.put("otpCode", "");


//        String fs = URLEncoder.encode(builder.toString());
//                System.out.println(fs);
            String resp = HttpPoster.sendForm(client, CAS_LOGIN_URL, headers, cont);
            //System.out.println(resp);
            Document respDoc = Jsoup.parse(resp);
            Elements es = respDoc.getElementsByTag("strong");
            for (Element element : es) {
                System.out.println(element);
            }
            Element f = es.last();

            try {
                TextNode tn = (TextNode) f.childNode(0);
                if ("你已成功登陆".equalsIgnoreCase(tn.text())) {
                    loginSucceed = true;
                }
            } catch (Exception ex) {
                System.out.println("login failed! " + ex);
            }
        }catch (Exception ex){
            System.out.println("Auto login failed! " + ex);
        }
        return loginSucceed;
    }

    public static void main(String[] args) throws Exception {
        CloseableHttpClient client = getHttpClient();
        try {
            boolean result = CASAutoLoginHelper.autoLogin(client, "*******", "********");
            if (result) {
                System.out.println("login succeed!");
                String verify = HttpGetter.send(client, "*********");
                System.out.println(verify);
            } else {
                System.out.println("login failed!");
            }
        }finally {
            client.close();
        }
    }


    static CloseableHttpClient getHttpClient() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext ctx = SSLContext.getInstance("TLS");
        X509TrustManager tm = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }
            @Override
            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        ctx.init(null, new TrustManager[]{tm}, null);
        HttpHost proxy = new HttpHost("127.0.0.1", 8888);
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(ctx);
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).setProxy(proxy).build();
        return httpClient;
    }
}
