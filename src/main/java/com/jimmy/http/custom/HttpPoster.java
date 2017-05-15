package com.jimmy.http.custom;


import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.StringWriter;


/**
 * Created by liyongbao on 15-5-18.
 */
public class HttpPoster {
    private static Logger logger = LoggerFactory.getLogger(HttpPoster.class);
    //private static CloseableHttpClient httpClient = HttpClients.createDefault();

    public String send(String host, String requestJsonString) throws Exception{
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(host);
        httpPost.setHeader("Content-Type","application/json; charset=UTF-8");
        httpPost.setHeader("MaxRTB-version", "1.0");
        httpPost.setEntity(new StringEntity(requestJsonString,"UTF-8"));
        try {
            HttpResponse response = httpClient.execute(httpPost);
            int status = response.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_OK) {
                InputStream inputStream = response.getEntity().getContent();
                StringWriter writer = new StringWriter();
                IOUtils.copy(inputStream, writer, "UTF-8");
                String bidResponseString = writer.toString();
                logger.debug("response.getEntity().getContent().toString() :{}", bidResponseString);
                return bidResponseString;
            } else if (status == HttpStatus.SC_NO_CONTENT) {
                logger.error("No bid : HttpStatus.SC_NO_CONTENT \t requestUrl :{} \t requestJsonString:{}", host, requestJsonString);
                throw new IllegalMonitorStateException("HttpStatus: SC_NO_CONTENT");
            } else {
                logger.error("Wrong return HttpStatus:{} \t requestUrl and JsonBody:{}", status, host + "\t" + requestJsonString);
                throw new IllegalMonitorStateException("HttpStatus: " + status);
            }
        } finally {
            httpPost.releaseConnection();
        }
    }
}
