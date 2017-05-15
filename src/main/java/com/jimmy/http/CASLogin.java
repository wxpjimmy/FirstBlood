package com.jimmy.http;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by eagle on 17/5/14.
 */
public class CASLogin {
    private static String CAS_LOGIN_URL = "https://cas.mioffice.cn/login";
    private static String CAS_LOGOUT_URL = "https://cas.mioffice.cn/logout";

    public static void main(String[] args) throws Exception {
        HttpGetter.send(CAS_LOGOUT_URL);

        String content = HttpGetter.send(CAS_LOGIN_URL);
        System.out.println(content);
        Map<String, String> map = new HashMap<String, String>();
        Document dc = Jsoup.parse(content);
        Elements e = dc.getElementsByTag("script");
        Element scriptElement = e.last();
        System.out.println(scriptElement);
        String[] data = scriptElement.data().toString().split("\\r\\n");

            /*取得单个JS变量*/
        for(String variable : data){
            if(StringUtils.isBlank(variable)) {
                continue;
            }
            /*取到满足条件的JS变量*/
            String line = variable.split(" ")[1];
            int index = line.indexOf("=");
            if(index == -1) {
                continue;
            }
            String[]  kvp = line.split("=", 2);
            String key = kvp[0].trim();
            String value = kvp[1].endsWith(";")?kvp[1].trim().substring(0, kvp[1].length()-1):kvp[1].trim();
            value = value.substring(1, value.length()-1);
                /*取得JS变量存入map*/
            if(!map.containsKey(key))
                map.put(key, value);
        }

        System.out.println(map);

        String strInfo = map.get("strInfo");
        String lt = map.get("lt");
        System.out.println(lt);

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Upgrade-Insecure-Requests", "1");
        headers.put("Connection", "keep-alive");

        Map<String, String> cont = new HashMap<String, String>();
        cont.put("lt", lt);
        cont.put("username", "*****");
        cont.put("password", "*****");
        cont.put("_eventId", "submit");
        cont.put("execution", strInfo);
        cont.put("rememberMe", "on");
        cont.put("mi_service", "");
        cont.put("otpCode", "");


//        String fs = URLEncoder.encode(builder.toString());
//                System.out.println(fs);
        String resp = HttpPoster.sendForm(CAS_LOGIN_URL, headers, cont);
        System.out.println(resp);
       //String re =  HttpGetter.send("http://config.ad.xiaomi.srv/user");
       //         System.out.println(re);
    }
}
