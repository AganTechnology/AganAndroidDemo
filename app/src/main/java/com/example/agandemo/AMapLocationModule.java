package com.example.agandemo;

import android.content.Context;

import com.agan.agan_engine_kit.open.AganLocation;
import com.agan.agan_engine_kit.open.AganLocationUpdater;
import com.agan.agan_engine_kit.open.IAganEngineLocationHandler;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

public class AMapLocationModule implements IAganEngineLocationHandler {

    //声明AMapLocationClient类对象
    AMapLocationClient mLocationClient = null;
    //声明定位回调监听器
    AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (updater == null) return;
            if (aMapLocation.getCity() != null && aMapLocation.getCity().isEmpty()) {
                updater.update(new AganLocation(aMapLocation.getLongitude(), aMapLocation.getLatitude(), aMapLocation.getCity()));
            }
        }
    };

    private AganLocationUpdater updater;

    AMapLocationModule(Context context) {
        try {
            AMapLocationClient.updatePrivacyAgree(context, true);
            AMapLocationClient.updatePrivacyShow(context, true, true);
            //初始化定位
            mLocationClient = new AMapLocationClient(context.getApplicationContext());
            //设置定位回调监听
            mLocationClient.setLocationListener(mLocationListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void locationUpdate(AganLocationUpdater updater) {
        AMapLocationClientOption option = new AMapLocationClientOption();
        option.setOnceLocation(true);
        option.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.SignIn);
        if(null != mLocationClient){
            mLocationClient.setLocationOption(option);
            //设置场景模式后最好调用一次stop，再调用start以保证场景模式生效
            mLocationClient.stopLocation();
            mLocationClient.startLocation();
            this.updater = updater;
        }
    }
}
