package com.example.agandemo;

import com.agan.agan_engine_kit.open.AganError;
import com.agan.agan_engine_kit.open.models.AganBaseResult;

import okhttp3.Call;

public abstract class AganHttpCallBack<T> {

    public abstract void onSuccess(final Call call, AganBaseResult<T> result);

    public abstract void onError(final Call call, AganError error);

}
