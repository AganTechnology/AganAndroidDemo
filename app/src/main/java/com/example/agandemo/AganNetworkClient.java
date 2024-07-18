package com.example.agandemo;

import android.location.Address;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.agan.agan_engine_kit.open.AganEngine;
import com.agan.agan_engine_kit.open.AganError;
import com.agan.agan_engine_kit.open.AganLocation;
import com.agan.agan_engine_kit.open.models.AganBaseResult;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AganNetworkClient {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final OkHttpClient mOkHttpClient = new OkHttpClient.Builder().retryOnConnectionFailure(true).build();

    private static Date lastLocationTime = null;
    private static AganLocation location = null;

    private static String BASE_URL = "https://test-api.aganyunke.com";

    public static AganNetworkClient getInstance() {
        return AganNetworkClientSingle.single;
    }

    public static void resetHost(String host) {
        BASE_URL = host;
    }

    public Builder post(String url, JsonObject params) {
        Call call = null;
        Builder builder = new Builder();
        try {
            if (params == null) {
                params = new JsonObject();
            }
            RequestBody body = RequestBody.create(JSON, params.toString());
            Request.Builder builderRequest = new Request.Builder();
            Request request = builderRequest.post(body).url(BASE_URL+url).build();
            call = mOkHttpClient.newCall(request);
            call.enqueue(builder.callback);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return builder;
    }

    public Builder get(String url, HashMap<String, String> params) {
        Call call = null;
        Builder builder = new Builder();
        try {
            HttpUrl.Builder httpBuilder = HttpUrl.parse(BASE_URL+url).newBuilder();
            if (params != null) {
                for(Map.Entry<String, String> param : params.entrySet()) {
                    httpBuilder.addQueryParameter(param.getKey(),param.getValue());
                }
            }
            Request.Builder builderRequest = new Request.Builder();
            Request request = builderRequest.url(httpBuilder.build()).build();
            call = mOkHttpClient.newCall(request);
            call.enqueue(builder.callback);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return builder;
    }


    static class AganNetworkClientSingle {
        private static final AganNetworkClient single = new AganNetworkClient();
        private AganNetworkClientSingle() {}
    }

    public class Builder {

        Handler mainHandler = new Handler(Looper.getMainLooper());

        final Callback callback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> { if (encode.mCallback != null) encode.mCallback.onError(call, new AganError(e.getMessage(), 500)); });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() == null) {
                    mainHandler.post(() -> { if (encode.mCallback != null) encode.mCallback.onError(call, new AganError("返回数据不合法", 500)); });
                    return;
                }
                if (!response.isSuccessful()) {
                    mainHandler.post(() -> { if (encode.mCallback != null) encode.mCallback.onError(call, new AganError("返回数据不合法", 500)); });
                    return;
                }
                String json = response.body().string().trim();
                Type listType = new ParameterizedTypeImpl(AganBaseResult.class, new Class[]{encode.klass});
                AganBaseResult result = new Gson().fromJson(json, listType);

                if (result.getCode() == 200) {
                    mainHandler.post(() -> { if (encode.mCallback != null)  encode.mCallback.onSuccess(call, result); });
                } else {
                    if (result.getCode() == 401) {
                        mainHandler.post(() ->  AganEngine.getInstance().logout() );
                    }
                    mainHandler.post(() -> { if (encode.mCallback != null)  encode.mCallback.onError(call, new AganError(result.getMsg(), result.getCode())); });
                }
            }
        };


        private Encode encode;

        Builder() {}

        public <T> Encode<T> encode(Class<T> klass) {
            encode = new Encode(klass);
            return encode;
        }

        public class Encode<T> {

            private Class<T> klass;

            Encode(Class<T> klass) {
                this.klass = klass;
            }

            private AganHttpCallBack mCallback;


            public void callback(AganHttpCallBack<T> mCallback) {
                this.mCallback = mCallback;
            }
        }
    }

    public class ParameterizedTypeImpl implements ParameterizedType {
        private final Class raw;
        private final Type[] args;
        public ParameterizedTypeImpl(Class raw, Type[] args) {
            this.raw = raw;
            this.args = args != null ? args : new Type[0];
        }
        @Override
        public Type[] getActualTypeArguments() {
            return args;
        }
        @Override
        public Type getRawType() {
            return raw;
        }
        @Override
        public Type getOwnerType() {return null;}
    }

}
