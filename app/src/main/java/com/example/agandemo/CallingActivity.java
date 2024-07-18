package com.example.agandemo;

import static com.agan.agan_engine_kit.open.AganCallState.linking;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.agan.agan_engine_kit.open.AGNetworkQuality;
import com.agan.agan_engine_kit.open.AganCall;
import com.agan.agan_engine_kit.open.AganCallEventHandler;
import com.agan.agan_engine_kit.open.AganCallState;
import com.agan.agan_engine_kit.open.AganEngine;
import com.agan.agan_engine_kit.open.AganError;
import com.agan.agan_engine_kit.open.IAganEngineAuthEventHandler;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CallingActivity extends AppCompatActivity {

    private AganCall call = null;

    private String phone;

    private TextView phoneTV;
    private TextView statusTV;
    private Button hangupButton;
    private Button muteButton;
    private Button handsfreeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);

        phoneTV = findViewById(R.id.calling_phone);
        statusTV = findViewById(R.id.calling_status);
        hangupButton = findViewById(R.id.calling_hangup);
        muteButton = findViewById(R.id.calling_mute);
        handsfreeButton = findViewById(R.id.calling_handsFree);

        Intent intent = getIntent();
        String phone = intent.getStringExtra("phone");

        phoneTV.setText(phone);

        this.phone = phone;

        verifyAudioPermissions();
    }

    private void call() {
        // 拨打电话
        call = AganEngine.call(phone, "", this, new AganCallEventHandler() {
            @Override
            public void onCallFailure(AganError error) {
                super.onCallFailure(error);
                Toast.makeText(CallingActivity.this, error.getMsg(), Toast.LENGTH_LONG).show();

                if (error.getCode() == 7777) {
                    // 需要认证
                    authAccount();
                }
            }

            @Override
            public void onCallStateChange(AganCallState state) {
                super.onCallStateChange(state);
                switch (state) {
                    case idle:
                        statusTV.setText("准备中...");
                        break;
                    case linking:
                        statusTV.setText("链接中...");
                        break;
                    case ring:
                        statusTV.setText("振铃中...");
                        break;
                    case calling:
                        statusTV.setText("通话中...");
                        break;
                    case disconnect:
                        statusTV.setText("通话结束");
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                CallingActivity.this.finish();
                            }
                        },2000); // 延时1秒
                        break;
                }
            }

            @Override
            public void onHandsFreeStateChange(boolean isHandsFree) {
                super.onHandsFreeStateChange(isHandsFree);
                handsfreeButton.setSelected(isHandsFree);
                handsfreeButton.setText(isHandsFree ? "取消免提" : "免提");
            }

            @Override
            public void onMuteStateChange(boolean isMute) {
                super.onMuteStateChange(isMute);
                muteButton.setSelected(isMute);
                muteButton.setText(isMute ? "取消静音" : "静音");
            }

            @Override
            public void onUpdateNetworkState(AGNetworkQuality quality, double loss) {
                super.onUpdateNetworkState(quality, loss);
            }
        });

        hangupButton.setOnClickListener(v -> AganEngine.hangup(call));

        muteButton.setOnClickListener(v -> {
            if (muteButton.isSelected()) {
                AganEngine.unMute(call);
            } else {
                AganEngine.mute(call);
            }
        });

        handsfreeButton.setOnClickListener(v -> {
            if (handsfreeButton.isSelected()) {
                AganEngine.unHandsFree(call);
            } else {
                AganEngine.handsFree(call);
            }
        });
    }

    private void authAccount() {
        AganEngine.authAccount(new IAganEngineAuthEventHandler() {
            @Override
            public void onAuthError(AganError error) {
                Toast.makeText(CallingActivity.this, error.getMsg(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAuthSuccess() {
                call();
            }
        });
    }

    public void verifyAudioPermissions() {
        XXPermissions.with(this)
                // 申请单个权限
                .permission(Permission.RECORD_AUDIO)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean allGranted) {
                        if (permissions.contains(Permission.RECORD_AUDIO)) {
                            call();
                        }
                    }

                    @Override
                    public void onDenied(List<String> permissions, boolean doNotAskAgain) {
                        OnPermissionCallback.super.onDenied(permissions, doNotAskAgain);
                    }
                });
    }
}