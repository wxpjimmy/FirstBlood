package com.jimmy.http.custom;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;


/**
 * Created by liyongbao on 15-5-18.
 */
public class KeepAliveHttpPoster {
    private static Logger logger = LoggerFactory.getLogger(KeepAliveHttpPoster.class);
    private CloseableHttpClient httpClient;

    public KeepAliveHttpPoster(){
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        // Increase max total connection to 200
        cm.setMaxTotal(600);
        // Increase default max connection per route to 20
        cm.setDefaultMaxPerRoute(400);
        // Increase max connections for alimama
        // cm.setMaxPerRoute(new HttpRoute(new HttpHost("http://ssp.m.taobao.com/api/data", 80)), 50);
        // close all connections that have been idle over 4 seconds.
        cm.closeIdleConnections(60, TimeUnit.SECONDS);
        // keep alive strategy
        ConnectionKeepAliveStrategy keepAliveStrat = new DefaultConnectionKeepAliveStrategy() {
            @Override
            public long getKeepAliveDuration(
                    HttpResponse response,
                    HttpContext context) {
                long keepAlive = super.getKeepAliveDuration(response, context);
                if (keepAlive == -1) {
                    // Keep connections alive 120 seconds if a keep-alive value
                    // has not be explicitly set by the server
                    keepAlive = 60000;
                }
                return keepAlive;
            }
        };

/*      setConnectTimeout: Client tries to connect to the server. setConnectTimeout denotes the time elapsed
        before the connection established or Server responded to connection request.

        setSoTimeout: After establishing the connection, the client socket waits for response after sending the request.
        setSoTimeout is the elapsed time since the client has sent request to the server before server responds.
        Please note that this is not not same as HTTP Error 408 which the server sends to the client.
        In other words its maximum period inactivity between two consecutive data packets arriving at client
        side after connection is established.*/
        int connectTimeout = 300;
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(2000)
                .setConnectionRequestTimeout(connectTimeout)
                .setSocketTimeout(connectTimeout)
                .build();

        // create HttpClient
        httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .setKeepAliveStrategy(keepAliveStrat)
                .setConnectionTimeToLive(10, TimeUnit.MINUTES) //Sets maximum time to live for persistent connections
                .setDefaultRequestConfig(requestConfig)
                .disableConnectionState()
                .build();
    }

    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    public String send(String host, String requestJsonString) throws Exception{
        HttpPost httpPost = new HttpPost(host);
        httpPost.setHeader("Content-Type", "application/json; charset=UTF-8");
        httpPost.setEntity(new StringEntity(requestJsonString, "UTF-8"));
        if(logger.isDebugEnabled()) {
            logger.debug("httpGet.getURI().toString() : {}", httpPost.getURI().toString());
        }
        String bidResponseString = null;
        try {
            CloseableHttpResponse response = httpClient.execute(httpPost);

            int status = response.getStatusLine().getStatusCode();
            logger.debug("host:{}, status: {}", host, status);
            InputStream inputStream = null;
            String statusMsg = "http.status." + HttpStatus.getStatusText(status);
            if (status == HttpStatus.SC_OK) {
                try {
                    if (response.getEntity() == null) {
                        logger.error("host :{} response.getEntity() is null, response is :{}", host, response);
                        throw new Exception(statusMsg + ".response.entity.null");
                    }
                    inputStream = response.getEntity().getContent();
                    StringWriter writer = new StringWriter();
                    IOUtils.copy(inputStream, writer, "UTF-8");
                    bidResponseString = writer.toString();
                    logger.debug("host :{} response.getEntity().getContent().toString() :{}", host, bidResponseString);
                } catch (Exception e) {
                    logger.error("{} for requestString:{}", ExceptionUtils.getStackTrace(e), requestJsonString);
                    throw e;
                } finally {
                    response.close();
                    inputStream.close();
                }
            } else {
                if (status != HttpStatus.SC_NO_CONTENT) {
                    logger.error(statusMsg + " for requestString: " + requestJsonString);
                    throw new NoHttpResponseException(statusMsg);
                }
            }
        } finally {
            httpPost.releaseConnection();
        }

        return bidResponseString;
    }
}
