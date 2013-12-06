package service;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.List;

/**
 * desc:
 * User: weiguili(li5220008@gmail.com)
 * Date: 13-12-5
 * Time: 下午4:40
 */
public interface HCS {
    /**
     * @param url 设置请求url
     * @return
     */
    HCS url(String url);

    /**
     * @param nvps 要传送的名值对
     * @return
     */
    HCS nvp( List<NameValuePair> nvps);

    /**
     * @param body 传送的body
     * @return
     */
    HCS body(String body);

    /**
     * 用post方法提交
     * @return
     */
    String post();

    /**
     * 用get方法提交
     * @return
     */
    String get();

    /**
     * 关闭连接（会话断开）
     */
    void close();
}
