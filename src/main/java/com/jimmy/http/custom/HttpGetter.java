package com.jimmy.http.custom;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.StringWriter;


/**
 * Created by liyongbao on 15-5-18.
 */
public class HttpGetter{
    private static Logger logger = LoggerFactory.getLogger(HttpGetter.class);

    public String send(String host, String requestJsonString) throws Exception{
        String url = host + "" + requestJsonString;
        HttpGet httpGet = new HttpGet(url);
        logger.debug("httpGet.getURI().toString() : {}", httpGet.getURI().toString());
        String bidResponseString = null;
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse response = httpClient.execute(httpGet);
            int status = response.getStatusLine().getStatusCode();
            InputStream inputStream = null;
            try {
                if (status == HttpStatus.SC_OK) {
                    inputStream = response.getEntity().getContent();
                    StringWriter writer = new StringWriter();
                    IOUtils.copy(inputStream, writer, "UTF-8");
                    bidResponseString = writer.toString();
                } else {
                    throw new Exception("http status is not 200, it is : " + status);
                }
            } catch (Exception ex) {
                throw new Exception("inputSteam read from http response failed : " + ex.getMessage());
            } finally {
                inputStream.close();
                response.close(); // 短链接直接把response close ，此时连接也会断掉。如果想让连接还在，那就close input stream
            }
        } finally {
            httpGet.releaseConnection();
        }
        return bidResponseString;
    }
}
