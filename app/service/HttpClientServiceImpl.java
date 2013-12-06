package service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * desc:
 * User: weiguili(li5220008@gmail.com)
 * Date: 13-11-27
 * Time: 上午9:17
 */
public class HttpClientServiceImpl implements HCS {
    //cookie保持策略，这里用默认的。
    private static final CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(new BasicCookieStore()).build();
    private static final ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
        public String handleResponse(
                final HttpResponse response) throws ClientProtocolException, IOException {
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity) : null;
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
        }
    };

    //网关
    private String url;
    //登陆的参数
    private List <NameValuePair> nvps = new ArrayList<NameValuePair>();
    private StringEntity body;
    //提交的编码方式
    private HttpGet httpget;
    private HttpPost httpost;

    @Override
    public HttpClientServiceImpl url(String url) {
        this.url = url;
        setHttpget(new HttpGet(url));
        setHttpost(new HttpPost(url));
        return this;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public HttpClientServiceImpl nvp(List<NameValuePair> nvps) {
        this.nvps = nvps;
        return this;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public HttpClientServiceImpl body(String body) {
        try {
            this.body = new StringEntity(body);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return this;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private List<NameValuePair> getNvps() {
        return nvps;
    }

    private void setNvps(List<NameValuePair> nvps) {
        this.nvps = nvps;
    }

    private HttpGet getHttpget() {
        return httpget;
    }

    private void setHttpget(HttpGet httpget) {
        this.httpget = httpget;
    }

    private HttpPost getHttpost() {
        return httpost;
    }

    private void setHttpost(HttpPost httpost) {
        this.httpost = httpost;
    }

    @Override
    public String post(){
        String response = "";
        httpost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
        if(body !=null) httpost.setEntity(body);
        try {
            response = httpclient.execute(httpost, responseHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public String get(){
        String response = "";
        try {
            response = httpclient.execute(httpget,responseHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    //最后释放链接
    public void close(){
        try {
            httpclient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
