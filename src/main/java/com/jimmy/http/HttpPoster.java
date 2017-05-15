package com.jimmy.http;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
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
//        httpPost.setHeader("Content-Type","application/json; charset=UTF-8");
//        httpPost.setHeader("MaxRTB-version", "1.0");
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


    public static String sendForm(String host, Map<String, String> headers, Map<String, String> formData) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(host);
        if(headers != null) {
            for(String key: headers.keySet()) {
                httpPost.setHeader(key, headers.get(key));
            }
        }
//        httpPost.setHeader("Content-Type","application/json; charset=UTF-8");
//        httpPost.setHeader("MaxRTB-version", "1.0");

        List<BasicNameValuePair> form = new ArrayList<BasicNameValuePair>();
        for(String key: formData.keySet()) {
            BasicNameValuePair nameValuePair = new BasicNameValuePair(key, formData.get(key));
            form.add(nameValuePair);
        }

        //String content = URLEncodedUtils.format(form, HTTP.DEF_CONTENT_CHARSET);

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form,HTTP.UTF_8);
        //System.out.println(content);
        httpPost.setEntity(entity);

        Header[] hh = httpPost.getAllHeaders();
        for(Header h: hh) {
            System.out.println(h.getName() + ": " + h.getValue());
        }
        System.out.println("#############");

        try {
            HttpResponse response = httpClient.execute(httpPost);
            int status = response.getStatusLine().getStatusCode();
            System.out.println("Status: " + status);
            if (status == HttpStatus.SC_OK) {
                InputStream inputStream = response.getEntity().getContent();
                StringWriter writer = new StringWriter();
                IOUtils.copy(inputStream, writer, "UTF-8");
                String bidResponseString = writer.toString();
                System.out.println("response:" + bidResponseString);
                return bidResponseString;
            } else if (status == HttpStatus.SC_NO_CONTENT) {
                throw new IllegalMonitorStateException("HttpStatus: SC_NO_CONTENT");
            } else if (status == HttpStatus.SC_MOVED_TEMPORARILY) {
                Header[] hs = response.getAllHeaders();
                for(Header h: hs) {
                    System.out.println(h.getName() + ": " + h.getValue());
                }
                InputStream inputStream = response.getEntity().getContent();
                StringWriter writer = new StringWriter();
                IOUtils.copy(inputStream, writer, "UTF-8");
                String bidResponseString = writer.toString();
                System.out.println("response:" + bidResponseString);
                return bidResponseString;
            }

            else {
                throw new IllegalMonitorStateException("HttpStatus: " + status);
            }
        } finally {
            httpPost.releaseConnection();
        }
    }
}
