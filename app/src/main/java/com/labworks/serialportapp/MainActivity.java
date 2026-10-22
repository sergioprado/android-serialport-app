package com.labworks.serialportapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.admin.DevicePolicyManager;
import android.graphics.Insets;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import vendor.labworks.serialportmanager.SerialPortManager;

public class MainActivity extends AppCompatActivity {

    private DevicePolicyManager dpm;
    private SerialPortViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // keep the layout clear of the system bars (when visible) and of the keyboard
        findViewById(R.id.main).setOnApplyWindowInsetsListener((v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsets.Type.systemBars() | WindowInsets.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsets.CONSUMED;
        });

        // immersive mode
        WindowInsetsController controller = getWindow().getInsetsController();
        if (controller != null) {
            controller.hide(WindowInsets.Type.systemBars());
            controller.setSystemBarsBehavior(
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }

        // display always ON
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // RX
        TextView rxText = findViewById(R.id.rxText);
        ScrollView rxScroll = findViewById(R.id.rxScroll);
        model = new ViewModelProvider(this).get(SerialPortViewModel.class);
        model.getRxData().observe(this, data -> {
            rxText.setText(data);
            rxScroll.post(() -> rxScroll.fullScroll(View.FOCUS_DOWN));
        });
        model.getRxError().observe(this, msg ->
                showErrorMessage(getString(R.string.rx_error), msg));

        dpm = getSystemService(DevicePolicyManager.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // lock task mode, if allowed by the device owner
        if (dpm.isLockTaskPermitted(getPackageName()))
            startLockTask();
    }

    private void showErrorMessage(String title, String msg) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .show();
    }

    public void clearRx(View view) {
        model.clearRxData();
    }

    public void serialPortTx(View view) {
        EditText txText = findViewById(R.id.txText);

        try {
            SerialPortManager sp = SerialPortManager.getInstance();
            for (int i = 0; i < txText.getText().length(); i++) {
                byte b = (byte) txText.getText().charAt(i);
                sp.tx(b);
            }
            txText.getText().clear();
        } catch (Exception e) {
            showErrorMessage(getString(R.string.tx_error), e.getMessage());
        }
    }
}
