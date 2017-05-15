package com.jimmy.http.custom;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.TimeUnit;

/**
 * Created by DELL on 2015/9/10.
 */
public class KeepAliveHttpBinaryPoster {
    private static Logger logger = LoggerFactory.getLogger(KeepAliveHttpBinaryPoster.class);
    private CloseableHttpClient httpClient;
    private BinaryType binaryType = BinaryType.OCTET;

    public KeepAliveHttpBinaryPoster(BinaryType binaryType){
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
        this.binaryType = binaryType;
    }


    public byte[] send(String host, byte[] requestData) throws Exception {
        HttpPost httpPost = new HttpPost(host);
        if(this.binaryType == BinaryType.PROTOBUF) {
            httpPost.addHeader("Content-Type", "application/x-protobuf; charset=UTF-8");
            httpPost.addHeader("Accept", "application/x-protobuf; charset=UTF-8");
        }else if(this.binaryType == BinaryType.OCTET) {
            httpPost.addHeader("Content-Type", "application/octet-stream");
        }else {
            //so far, default to same as tanx
            httpPost.addHeader("Content-Type", "application/octet-stream");
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(requestData);
        InputStreamEntity inputStreamEntity = new InputStreamEntity(inputStream, requestData.length);
        //TODO: for tanx test
        if(host.contains("140.205.241.3")) {
            inputStreamEntity.setChunked(false);
        }
        httpPost.setEntity(inputStreamEntity);
        byte[] responseBinary = null;
        //HttpContext httpContext = HttpClientContext.create();  //测试发现不加上httpcontext, 依然可以kept alive
        try {
            CloseableHttpResponse response = httpClient.execute(httpPost);
            int status = response.getStatusLine().getStatusCode();
            logger.debug("host:{}, status: {}", host, status);
            String statusMsg = "http.status." + HttpStatus.getStatusText(status);
            if (status == HttpStatus.SC_OK) {
                HttpEntity responseBody = response.getEntity();
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                try {
                    responseBody.writeTo(buf);
                    responseBinary = buf.toByteArray();
                    logger.debug("host :{} response.getEntity() :{}", host, responseBinary);
                } catch (Exception ex) {
                    logger.error(statusMsg + ", " + ExceptionUtils.getStackTrace(ex));
                    throw ex;
                } finally {
                    response.close();
                    IOUtils.closeQuietly(buf);
                }
            } else {
                throw new NoHttpResponseException(statusMsg);
            }
        } finally {
            httpPost.releaseConnection();
        }
        return responseBinary;
    }
}
