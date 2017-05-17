package com.jimmy.http;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by DELL on 2016/8/19.
 */
public class HttpPoster {
    public static String send(String host, Map<String, String> headers, String content) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(host);
        if(headers != null) {
            for(String key: headers.keySet()) {
                httpPost.setHeader(key, headers.get(key));
            }
        }
        httpPost.setEntity(new StringEntity(content,"UTF-8"));
        httpPost.toString();

        try {
            HttpResponse response = httpClient.execute(httpPost);
            int status = response.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_OK) {
                InputStream inputStream = response.getEntity().getContent();
                StringWriter writer = new StringWriter();
                IOUtils.copy(inputStream, writer, "UTF-8");
                String bidResponseString = writer.toString();
                System.out.println("response:" + bidResponseString);
                return bidResponseString;
            } else if (status == HttpStatus.SC_NO_CONTENT) {
                throw new IllegalMonitorStateException("HttpStatus: SC_NO_CONTENT");
            } else {
                throw new IllegalMonitorStateException("HttpStatus: " + status);
            }
        } finally {
            httpPost.releaseConnection();
        }
    }


    public static String sendForm(CloseableHttpClient client,  String host, Map<String, String> headers, Map<String, String> formData) throws Exception {
        HttpPost httppost = new HttpPost(host);
        if(headers != null) {
            for(String key: headers.keySet()) {
                httppost.setHeader(key, headers.get(key));
            }
        }

        List<BasicNameValuePair> form = new ArrayList<BasicNameValuePair>();
        for(String key: formData.keySet()) {
            BasicNameValuePair nameValuePair = new BasicNameValuePair(key, formData.get(key));
            form.add(nameValuePair);
        }

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, HTTP.UTF_8);
        //System.out.println(content);
        httppost.setEntity(entity);
//        Header[] hh = httppost.getAllHeaders();
//        for(Header h: hh) {
//            System.out.println(h.getName() + ": " + h.getValue());
//        }
//        System.out.println("#############");
        try {
            HttpResponse response = client.execute(httppost);
            int status = response.getStatusLine().getStatusCode();
            System.out.println("Status: " + status);
            if (status == HttpStatus.SC_OK) {
                InputStream inputStream = response.getEntity().getContent();
                StringWriter writer = new StringWriter();
                IOUtils.copy(inputStream, writer, "UTF-8");
                String bidResponseString = writer.toString();
                //System.out.println("response:" + bidResponseString);
                return bidResponseString;
            } else if (status == HttpStatus.SC_NO_CONTENT) {
                throw new IllegalMonitorStateException("HttpStatus: SC_NO_CONTENT");
            } else {
                throw new IllegalMonitorStateException("HttpStatus: " + status);
            }
        } finally {
            httppost.releaseConnection();
        }
    }
}
