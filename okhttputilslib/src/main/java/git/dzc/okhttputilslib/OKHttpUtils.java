package git.dzc.okhttputilslib;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.CacheControl;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by dzc on 15/11/8.
 */
public class OKHttpUtils<T>{
    public static final String GET = "GET";
    public static final String POST = "POST";
    private boolean DEBUG = true;
    private OkHttpClient client = null;
    private Gson gson;
    private CacheType cacheType = CacheType.ONLY_NETWORK;

    public  OkHttpClient getClient(){
        return client;
    }

    private OKHttpUtils() {
    }
    private OKHttpUtils(Context context, int maxCacheSize, File cachedDir, final int maxCacheAge, CacheType cacheType , List<Interceptor> netWorkinterceptors, List<Interceptor> interceptors){
        client = new OkHttpClient();
        gson = new Gson();
        this.cacheType = cacheType;
        if(cachedDir!=null){
            client.setCache(new Cache(cachedDir,maxCacheSize));
        }else{
            client.setCache(new Cache(context.getCacheDir(),maxCacheSize));
        }
        Interceptor cacheInterceptor = new Interceptor() {
            @Override public Response intercept(Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder()
                        .removeHeader("Pragma")
                        .header("Cache-Control", String.format("max-age=%d", maxCacheAge))
                        .build();
            }
        };
        client.networkInterceptors().add(cacheInterceptor);
        if(netWorkinterceptors!=null && !netWorkinterceptors.isEmpty()){
            client.networkInterceptors().addAll(netWorkinterceptors);
        }
        if(interceptors!=null && !interceptors.isEmpty()){
            client.interceptors().addAll(interceptors);
        }
    }

    public OKHttpUtils initDefault(Context context){
        return  new Builder(context).build();
    }

    public void post(final String url,CacheType cacheType, Headers headers, Map<String,String> params, String encodedKey, String encodedValue, Callback callback){

        request(url,cacheType,POST,createRequestBody(params),headers,callback);
    }
    public void post(final String url,CacheType cacheType, Headers headers, Map<String,String> params, String encodedKey, String encodedValue, JsonCallback callback){

        request(url,cacheType,POST,createRequestBody(params),headers,callback);
    }
    public void post(final String url,CacheType cacheType, Map<String,String> params, String encodedKey, String encodedValue, Callback callback){

        request(url,cacheType,POST,createRequestBody(params),null,callback);
    }
    public void post(final String url,Headers headers, Map<String,String> params, String encodedKey, String encodedValue, Callback callback){

        request(url,cacheType,POST,createRequestBody(params),headers,callback);
    }
    public void post(final String url, Headers headers, Map<String,String> params, String encodedKey, String encodedValue, JsonCallback callback){

        request(url,cacheType,POST,createRequestBody(params),headers,callback);
    }
    public void post(final String url, Map<String,String> params, String encodedKey, String encodedValue, Callback callback){

        request(url,cacheType,POST,createRequestBody(params),null,callback);
    }
    public void post(final String url,CacheType cacheType, Map<String,String> params, String encodedKey, String encodedValue, JsonCallback callback){

        request(url,cacheType,POST,createRequestBody(params),null,callback);
    }
    public void get(final String url, Headers headers,Callback callback){
        request(url,cacheType,GET,null,headers,callback);
    }
    public void get(final String url, Headers headers, JsonCallback callback){
        request(url,cacheType,GET,null,headers,callback);
    }
    public void get(final String url,Callback callback){
        request(url,cacheType,GET,null,null,callback);
    }
    public void get(final String url,JsonCallback callback){
        request(url,cacheType,GET,null,null,callback);
    }
    public void get(final String url,CacheType cacheType, Headers headers,Callback callback){
        request(url,cacheType,GET,null,headers,callback);
    }
    public void get(final String url,CacheType cacheType, Headers headers, JsonCallback callback){
        request(url,cacheType,GET,null,headers,callback);
    }
    public void get(final String url,CacheType cacheType,Callback callback){
        request(url,cacheType,GET,null,null,callback);
    }
    public void get(final String url,CacheType cacheType,JsonCallback callback){
        request(url,cacheType,GET,null,null,callback);
    }

    public static RequestBody createRequestBody(Map<String,String> params,String encodedKey,String encodedValue){
        FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
        if(params!=null&&!params.isEmpty()){
            Set<String> keys = params.keySet();
            for(String key:keys){
                formEncodingBuilder.add(key,params.get(key));
            }
        }
        if(!TextUtils.isEmpty(encodedKey) && !TextUtils.isEmpty(encodedValue)){
            formEncodingBuilder.addEncoded(encodedKey,encodedValue);
        }
        return formEncodingBuilder.build();
    }
    public static RequestBody createRequestBody(Map<String,String> params){
        return createRequestBody(params,null,null);
    }



    public void request(final String url, final CacheType cacheType, final String method, final RequestBody requestBody, final Headers headers,final JsonCallback callback){
        request(url,cacheType, method,requestBody, headers, new Callback() {
            @Override
            public void onStart() {
                if(callback!=null){
                    callback.onStart();
                }
            }


            @Override
            public void onFailure(Request request, IOException e) {
                if(callback!=null){
                    callback.onFailure(request,e);
                    callback.onFinish();
                }
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if(response.isSuccessful() && callback!=null){
                    String jsonString = response.body().string();;
                    if(!TextUtils.isEmpty(jsonString)){
                        Object result = null;
                        try {
                            result =  gson.fromJson(jsonString,callback.getType());
                            callback.onResponse(result);
                            callback.onFinish();
                        } catch (JsonSyntaxException e) {
                            callback.onFailure(null,new Exception("json string parse error :"+e.toString()));
                            callback.onFinish();
                            e.printStackTrace();
                        }

                    }else{
                        callback.onFailure(null,new Exception("json string may be null"));
                        callback.onFinish();
                    }
                }
            }
        });
    }

    public void request(final String url, final CacheType cacheType, final String method, final RequestBody requestBody, final Headers headers, final Callback callback){
        if(callback!=null)callback.onStart();
        switch (cacheType){
            case ONLY_NETWORK:
                requestFromNetwork(url,method,requestBody,headers,callback);
                break;
            case ONLY_CACHED:
                requestFromCached(url,method,requestBody,headers,callback);
                break;
            case CACHED_ELSE_NETWORK:
                requestFromCached(url,method,requestBody,headers, new Callback() {
                    @Override
                    public void onStart() {
                        if(callback!=null){
                            callback.onStart();
                        }
                    }

                    @Override
                    public void onFinish() {
                        if(callback!=null){
                            callback.onFinish();
                        }
                    }

                    @Override
                    public void onFailure(Request request, IOException e) {
                        requestFromNetwork(url,method,requestBody,headers,callback);
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        if(response.code()==200){
                            if(callback!=null){
                                callback.onResponse(response);
                                callback.onFinish();
                            }
                        }else{
                            requestFromNetwork(url,method,requestBody,headers,callback);
                        }
                    }
                });
                break;
            case NETWORK_ELSE_CACHED:
                requestFromNetwork(url,method,requestBody,headers, new Callback() {
                    @Override
                    public void onStart() {
                        if(callback!=null){
                            callback.onStart();
                        }
                    }

                    @Override
                    public void onFinish() {
                        if(callback!=null){
                            callback.onFinish();
                        }
                    }

                    @Override
                    public void onFailure(Request request, IOException e) {
                        requestFromCached(url,method,requestBody,headers,callback);
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        if(response.code()==200){
                            if(callback!=null){
                                callback.onResponse(response);
                                callback.onFinish();
                            }
                        }else{
                            requestFromCached(url,method,requestBody,headers,callback);
                        }
                    }
                });
                break;
        }
    }

    public void requestFromNetwork(final String url,String method,RequestBody requestBody, Headers headers,final Callback callback){
        Log.d("httpUtils","getDataFromNetwork");
        request(url,method,requestBody,CacheControl.FORCE_NETWORK,headers,callback);
    }

    public void requestFromCached(String url,String method,RequestBody requestBody,Headers headers ,final Callback callback){
        Log.d("httpUtils","getDataFromCached");
        request(url,method,requestBody,CacheControl.FORCE_CACHE,headers,callback);
    }

    private void request(String url, String method, RequestBody requestBody, final CacheControl cacheControl, Headers headers, final Callback callback){
        final Request.Builder requestBuilder = new Request.Builder().url(url).cacheControl(cacheControl);
        if(headers!=null){
            requestBuilder.headers(headers);
        }
        requestBuilder.method(method,requestBody);
        requestBuilder.tag(url);

        final Request request = requestBuilder.build();
        request(request,new Callback() {

            @Override
            public void onStart() {
                if(callback!=null){
                    callback.onStart();
                }
            }

            @Override
            public void onFinish() {
                if(callback!=null){
                    callback.onFinish();
                }
            }

            @Override
            public void onFailure(Request request, IOException e) {
                if(callback!=null){
                    callback.onFailure(request,e);
                    callback.onFinish();
                }
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if(response.code()==504){
                    if(CacheControl.FORCE_CACHE == cacheControl){
                        if(callback!=null){
                            callback.onFailure(request,new IOException("cached not found"));
                            callback.onFinish();
                        }
                        return;
                    }
                }
                if(callback!=null){
                    callback.onResponse(response);
                    callback.onFinish();
                }
            }
        });
    }


    private void request(Request request, Callback callback){
        client.newCall(request).enqueue(callback);
    }





    public static class Builder{
        private int maxCachedSize = 5 * 1024 *1024;
        private File cachedDir;
        private Context context;
        private List<Interceptor> networkInterceptors;
        private List<Interceptor> interceptors;
        private int maxCacheAge = 3600 * 12;
        private CacheType cacheType = CacheType.NETWORK_ELSE_CACHED;


        public Builder(Context context) {
            this.context = context;
        }

        private Builder() {
        }

        public OKHttpUtils build(){
            return new OKHttpUtils(context,maxCachedSize,cachedDir,maxCacheAge,cacheType,networkInterceptors,interceptors);
        }

        public Builder cacheType(CacheType cacheType){
            this.cacheType = cacheType;
            return this;
        }

        public Builder cachedDir(File cachedDir) {
            this.cachedDir = cachedDir;
            return this;
        }

        public Builder context(Context context) {
            this.context = context;
            return this;
        }

        /**
         * 拦截器使用可参考这篇文章  <a href="http://www.tuicool.com/articles/Uf6bAnz">http://www.tuicool.com/articles/Uf6bAnz</a>
         * @param interceptors
         */
        public Builder interceptors(List<Interceptor> interceptors) {
            this.interceptors = interceptors;
            return this;
        }

        public Builder maxCachedSize(int maxCachedSize) {
            this.maxCachedSize = maxCachedSize;
            return this;
        }

        /**
         * 拦截器使用可参考这篇文章  <a href="http://www.tuicool.com/articles/Uf6bAnz">http://www.tuicool.com/articles/Uf6bAnz</a>
         * @param networkInterceptors
         */
        public Builder networkInterceptors(List<Interceptor> networkInterceptors) {
            this.networkInterceptors = networkInterceptors;
            return this;
        }

        public Builder maxCacheAge(int maxCacheAge){
            this.maxCacheAge = maxCacheAge;
            return this;
        }
    }



//    client.interceptors().add(new Interceptor() {
//            @Override
//            public Response intercept(Chain chain) throws IOException {
//                Request request = chain.request();
    //请求的url链接后面添加数据
//                request = request.newBuilder().url(request.urlString()+"s?ie=utf-8&f=8&rsv_bp=0&rsv_idx=1&tn=98996590_hao_pg&wd=he&rsv_pq=f4953a2f000ff24d&rsv_t=04cbC9bfdepmyhGqXX6mksqeAGbXZUDDVjdafZ4rpa6%2BPtgbIZHlYi2IqlmTxdO8OxBTvKr3&rsv_enter=1&rsv_sug3=2&rsv_sug1=1&rsv_sug2=0&inputT=680&rsv_sug4=680").build();
//                return chain.proceed(request);
//
//            }
//        });




    /**
     * 通过url来取消一个请求  如果使用自定义的Request,传入request的Tag为url才能有效
     * @param url
     */
    public void cancel(String url){
        try {
            client.cancel(url);
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

}
