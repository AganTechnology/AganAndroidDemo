package com.agan.aganyun;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.agan.agan_engine_kit.open.AganAccount;
import com.agan.agan_engine_kit.open.AganCall;
import com.agan.agan_engine_kit.open.AganEngine;
import com.agan.agan_engine_kit.open.AganEngineConfig;
import com.agan.agan_engine_kit.open.AganEngineLoginEventHandler;
import com.agan.agan_engine_kit.open.AganEnvironment;
import com.agan.agan_engine_kit.open.AganError;
import com.agan.agan_engine_kit.open.IAganEngineAccountEventHandler;
import com.agan.agan_engine_kit.open.IAganEngineAuthEventHandler;
import com.agan.agan_engine_kit.open.IAganUploadEventHandler;
import com.agan.agan_engine_kit.open.models.AganBaseResult;
import com.agan.aganyun.R;
import com.google.gson.JsonObject;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hms.location.LocationRequest;
import com.huawei.hms.location.LocationServices;
import com.huawei.hms.location.LocationSettingsRequest;
import com.huawei.hms.location.LocationSettingsResponse;
import com.huawei.hms.location.LocationSettingsStates;
import com.huawei.hms.location.SettingsClient;

import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    TextView usernameTV;
    TextView userIdTV;
    TextView userVerifyTV;
    Button button;
    Button verifyButton;

    EditText phoneEditText;

    Button callButton;
    Button initButton;
    Button uploadButton;

    String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        usernameTV = findViewById(R.id.username);
        userIdTV = findViewById(R.id.userid);
        userVerifyTV = findViewById(R.id.verifyStatus);
        button = findViewById(R.id.doLoginButton);
        phoneEditText = findViewById(R.id.phoneEditText);
        callButton = findViewById(R.id.doCallButton);
        verifyButton = findViewById(R.id.doVerifiedButton);
        initButton = findViewById(R.id.doInitButton);
        uploadButton = findViewById(R.id.doUploadLogButton);


        initButton.setOnClickListener(v -> {
            // 初始化SDK前需要确保有定位权限开启
            XXPermissions.with(this)
                    // 申请权限
                    .permission(Permission.ACCESS_FINE_LOCATION)
                    .permission(Permission.ACCESS_COARSE_LOCATION)
                    .request(new OnPermissionCallback() {
                        @Override
                        public void onGranted(List<String> permissions, boolean allGranted) {
                            if (permissions.contains(Permission.ACCESS_FINE_LOCATION)) {
                                initSDK();
                            }
                        }

                        @Override
                        public void onDenied(List<String> permissions, boolean doNotAskAgain) {
                            OnPermissionCallback.super.onDenied(permissions, doNotAskAgain);
                        }
                    });
        });


        SettingsClient settingsClient = LocationServices.getSettingsClient(this);


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        LocationRequest mLocationRequest = new LocationRequest();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();
        // 检查设备定位设置
        settingsClient.checkLocationSettings(locationSettingsRequest)
                // 检查设备定位设置接口调用成功监听
                .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        LocationSettingsStates locationSettingsStates =
                                locationSettingsResponse.getLocationSettingsStates();
                        StringBuilder stringBuilder = new StringBuilder();
                        // 定位开关是否打开
                        stringBuilder.append(",\nisLocationUsable=")
                                .append(locationSettingsStates.isLocationUsable());
                        // HMS Core是否可用
                        stringBuilder.append(",\nisHMSLocationUsable=")
                                .append(locationSettingsStates.isHMSLocationUsable());
                        Log.i(TAG, "checkLocationSetting onComplete:" + stringBuilder.toString());
                    }
                })
                // 检查设备定位设置接口失败监听回调
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.i(TAG, "checkLocationSetting onFailure:" + e.getMessage());
                    }
                });
    }

    void initSDK() {
        AganEngine.setEnvironment(AganEnvironment.DEBUG);
        // 1. 初始化 SDK
        AganEngineConfig config = new AganEngineConfig("23f7abbab0bd420ea8defbcd7ffec56b");
        AganEngine.init(getApplicationContext(), config);

        // 2. 获取 metaInfo
        String metaInfo = AganEngine.getMetaInfo();

        JsonObject object = new JsonObject();
        object.addProperty("userId", 130272);
        object.addProperty("metaInfo", metaInfo);

        // (推荐添加定位模块)
        AganEngine.getInstance().addLocationModule(new AMapLocationModule(this));

//        // ... 通过 mateInfo 从接入方服务端请求 token
        // 2. 获取 metaInfo
        accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJURVNUOkNPTVBBTllfVVNFUiIsImlhdCI6MTcxMjU2MzEwOSwiZXhwIjoxNzI4MTE1MTA5LCJuYmYiOjE3MTI1NjMxMDksImp0aSI6IlVxYlRjeGIyWVNHUElGaUgiLCJzdWIiOiJpZCIsInBydiI6ImNmNGFmYmU0MjgxN2E4ZjJkMmMyYmJiMzdiZmUwZjczYjkwNTE3N2IiLCJpZCI6MjM1MDI3LCJ0aXRsZSI6IuiQp-WJkea1i-ivlS3kuLvotKbmiLciLCJjb21wYW55SWQiOjExMzA5NH0.2gchUiJz9F2amrFFNRtN5KK_fRG3xrm1eec6cbhwmn-xDIi3yXt9ohum2PrgbtQFClPW-MZ-eU0JCgPgnz9Htg";

        AganEngineLoginEventHandler loginEventHandler = new AganEngineLoginEventHandler() {
            @Override
            public void onLoginSuccess(AganAccount account) {
                usernameTV.setText("用户名："+account.getUsername());
                userIdTV.setText("用户id："+account.getUserId());
                userVerifyTV.setText("用户认证状态："+account.getVerifyStatus().name());
                onWatch(account);
            }

            @Override
            public void onLoginError(AganError error) {
                // 登录错误
                Toast.makeText(MainActivity.this, error.getMsg(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLogout(AganAccount account) {
                // 用户登出
            }
        };

        // 3. 登录用户
        button.setOnClickListener(v -> AganEngine.login(accessToken , loginEventHandler));

        // 4. 用户认证
        verifyButton.setOnClickListener(v -> AganEngine.authAccount(new IAganEngineAuthEventHandler() {
            @Override
            public void onAuthError(AganError error) {
                Toast.makeText(MainActivity.this, error.getMsg(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAuthSuccess() {
                userVerifyTV.setText("用户认证状态："+ AganEngine.currentAccount().getVerifyStatus().name());
            }
        }));

        // 5. 跳转页面, 拨打电话
        callButton.setOnClickListener(v -> {
            String phone = phoneEditText.getText().toString();

            Intent intent = new Intent(this, CallingActivity.class);
            intent.putExtra("phone", phone);
            startActivity(intent);
        });

        uploadButton.setOnClickListener(v -> {
            AganEngine.uploadLog(new IAganUploadEventHandler() {
                @Override
                public void onSuccess() {
                    Toast.makeText(MainActivity.this, "上传成功", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onError(AganError error) {
                    Toast.makeText(MainActivity.this, error.getMsg(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void onWatch(AganAccount account) {
        account.setAccountEventHandler(new IAganEngineAccountEventHandler() {
            @Override
            public void onIncomingCall(AganCall call) {
                // 弹起来电页面
                Intent intent = new Intent(MainActivity.this, IncomingActivity.class);
                intent.putExtra("callId", call.getSipCallId());
                startActivity(intent);
            }
        });
    }
}
