package com.jimmy.http;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Http 相关的一些静态方法
 * Created by zengdejun on 2016/6/5.
 */
public class HttpUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);

    public static Map<String, String> getHttpRequestParam(String paramRequestUrl) {
        HashMap<String, String> paramMap = new HashMap<String, String>();
        String str = paramRequestUrl;

        String[] params = str.split("&");
        for (String param : params) {
            LOGGER.debug("param [" + param + "]");
            String[] values = param.split("=");
            if (2 == values.length) {
                paramMap.put(values[0], values[1]);
            }
        }

        return paramMap;
    }

    public static String getSHA1(String decript) {
        try {
            MessageDigest digest = MessageDigest
                    .getInstance("SHA-1");
            digest.update(decript.getBytes());
            byte messageDigest[] = digest.digest();
            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            // 字节数组转换为 十六进制 数
            for (int i = 0; i < messageDigest.length; i++) {
                String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static List<String> parseFileToLines(String filePath) {
        BufferedReader br = null;
        String line = "";
        List<String> lines = new ArrayList<String>();

        try {
            br = new BufferedReader(new FileReader(filePath));
            while ((line = br.readLine()) != null) {
                if(StringUtils.isNotBlank(line)) {
                    lines.add(line);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return lines;
    }

    public static String sendHttpGet(String urlQuery) {
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL(urlQuery);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
        } catch (Exception e) {
            System.out.println("sendHttpGet" + e.toString());
        }

        return result.toString();
    }

    public static String sendHttpGet2(String urlQuery) throws Exception {
        HttpGet request = new HttpGet(urlQuery);
        String result = "0";
        int status = -1;
        try {
            HttpResponse response = HttpClients.createDefault().execute(request);
            status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                result = EntityUtils.toString(response.getEntity());
            }
        } catch (ClientProtocolException e) {
            System.out.println("Status:" + status);
            System.out.println(e.toString());
        } catch (Exception e) {
            System.out.println("Status:" + status);
            System.out.println(e.toString());
        }

        return result;
    }

    public static String sendCurlRequest(String cmdLine) {
        String result = "";
        try {

            ProcessBuilder pb = new ProcessBuilder(
                    "curl",
                    "http://api.e.qq.com/luna/v2/adnetwork_report/select?auth_type=TokenAuth&token=MTQ2Mjk1LDE0NjI5NSwxNDQzMTU3MjY1LDY0MmM2YTY1NTE4NmQwMTFkMzQwMjFiMmY4ZmRhM2FhZDk4ZjE1MjY=",
                    "-d \"memberId=473417324&start_date=20150923&end_date=20150923&appid=146295&agid=146295&key=387e4f15e100d27863674c96e6720b62\"");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            InputStream is = p.getInputStream();
            String line;
            BufferedInputStream in = new BufferedInputStream(is);
            StringBuffer buffer = new StringBuffer();
            int ptr = 0;
            while ((ptr = in.read()) != -1) {
                buffer.append((char) ptr);
            }

            result = buffer.toString();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }

        return result;
    }

    public static String sendHttpPost(String host, String paramString) {
        /*建立HTTPost对象*/
        HttpPost httpRequest = new HttpPost(host);
        /*
         * NameValuePair实现请求参数的封装
        */
        Map<String, String> paramsMap = getHttpRequestParam(paramString);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        for(Map.Entry<String, String> entry : paramsMap.entrySet()) {
            params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        String resultString = "";
        try
        {
          /* 添加请求参数到请求对象*/
//            System.out.println(params);
            httpRequest.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
          /*发送请求并等待响应*/
            HttpResponse httpResponse = HttpClients.createDefault().execute(httpRequest);
          /*若状态码为200 ok*/
            if(httpResponse.getStatusLine().getStatusCode() == 200)
            {
            /*读返回数据*/
                resultString = EntityUtils.toString(httpResponse.getEntity());
            } else {
                System.out.println("Error Response: " + httpResponse.getStatusLine().toString());
            }
        }
        catch (ClientProtocolException e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return resultString;
    }
}
