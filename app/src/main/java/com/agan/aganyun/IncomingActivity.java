package com.agan.aganyun;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class IncomingActivity extends AppCompatActivity {

    private AganCall call = null;

    private TextView phoneTV;
    private TextView statusTV;
    private Button answerButton;
    private Button rejectButton;
    private Button hangupButton;
    private Button muteButton;
    private Button handsfreeButton;
    
    private View incomingLayout;
    private View callingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: - 在此处，你需要开始播放振铃音乐
        setContentView(R.layout.activity_incoming);

        phoneTV = findViewById(R.id.incoming_phone);
        statusTV = findViewById(R.id.incoming_status);
        answerButton = findViewById(R.id.incoming_answer);
        rejectButton = findViewById(R.id.incoming_reject);
        hangupButton = findViewById(R.id.incoming_hangup);
        muteButton = findViewById(R.id.incoming_mute);
        handsfreeButton = findViewById(R.id.incoming_handsFree);
        incomingLayout = findViewById(R.id.incoming_layout);
        callingLayout = findViewById(R.id.calling_layout);

        Intent intent = getIntent();
        String callId = intent.getStringExtra("callId");
        call = AganEngine.getCall(callId);

        if (call != null) {
            setupCallEventHandler();
            phoneTV.setText(call.getDisplayPhone());
            statusTV.setText("来电中...");

            // 接听按钮
            answerButton.setOnClickListener(v -> {
                verifyAudioPermissions();
            });
            
            // 拒接按钮
            rejectButton.setOnClickListener(v -> {
                AganEngine.hangup(call);
                finish();
            });
        }
    }

    private void setupCallEventHandler() {

        AganEngine.receive(call, new AganCallEventHandler() {
            @Override
            public void onCallFailure(AganError error) {
                super.onCallFailure(error);
                Toast.makeText(IncomingActivity.this, error.getMsg(), Toast.LENGTH_LONG).show();
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
                                IncomingActivity.this.finish();
                            }
                        }, 2000); // 延时2秒
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
    }

    private void answerCall() {
        // 接听电话
        AganEngine.answer(call);
        
        // 切换到通话界面
        incomingLayout.setVisibility(View.GONE);
        callingLayout.setVisibility(View.VISIBLE);
        
        statusTV.setText("接听中...");
        
        // 设置通话控制按钮
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

    private void verifyAudioPermissions() {
        XXPermissions.with(this)
                .permission(Permission.RECORD_AUDIO)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean allGranted) {
                        if (permissions.contains(Permission.RECORD_AUDIO)) {
                            answerCall();
                        }
                    }

                    @Override
                    public void onDenied(List<String> permissions, boolean doNotAskAgain) {
                        OnPermissionCallback.super.onDenied(permissions, doNotAskAgain);
                        Toast.makeText(IncomingActivity.this, "需要录音权限才能接听电话", Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
