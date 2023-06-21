package com.labworks.serialportapp;

import static android.app.admin.DevicePolicyManager.LOCK_TASK_FEATURE_NONE;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.UserManager;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.EditText;

import vendor.labworks.serialportmanager.SerialPortManager;

public class MainActivity extends AppCompatActivity {

    DevicePolicyManager dpm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new SerialPortRx().execute();

        // hide title bar
        getSupportActionBar().hide();

        // immersive mode
        getWindow().setDecorFitsSystemWindows(false);
        WindowInsetsController controller = getWindow().getInsetsController();
        if (controller != null) {
            controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars() | WindowInsets.Type.captionBar() | WindowInsets.Type.systemBars());
            controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }

        // display always ON
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Create an intent filter to specify the Home category.
        IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_HOME);
        filter.addCategory(Intent.CATEGORY_DEFAULT);

        // restrictions
        String[] restrictions = {
                UserManager.DISALLOW_FACTORY_RESET,
                UserManager.DISALLOW_SAFE_BOOT,
                UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA,
                UserManager.DISALLOW_ADJUST_VOLUME,
                UserManager.DISALLOW_ADD_USER,
                UserManager.DISALLOW_SYSTEM_ERROR_DIALOGS};

        // get references
        dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName activity = new ComponentName(this, MainActivity.class);
        ComponentName admin = new ComponentName(this, DevAdmin.class);
        String[] packages = {this.getPackageName()};

        // configure dpm
        if (dpm.isDeviceOwnerApp(getPackageName())) {
            for (String restriction: restrictions)
                dpm.addUserRestriction(admin, restriction);
            dpm.setLockTaskFeatures(admin, LOCK_TASK_FEATURE_NONE);
            dpm.setLockTaskPackages(admin, packages);
            dpm.addPersistentPreferredActivity(admin, filter, activity);
        }
    }

    public void onResume() {
        super.onResume();
        if (dpm.isLockTaskPermitted(getPackageName()))
            startLockTask();
    }

    private void showErrorMessage(String title, String msg) {
        AlertDialog alertDialog =
                new AlertDialog.Builder (MainActivity.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(msg);
        alertDialog.show();
    }

    public void serialPortTx(View view)
    {
        EditText editText = (EditText) findViewById(R.id.editText);

        try {
            SerialPortManager sp = SerialPortManager.getInstance();
            for (int i = 0; i < editText.getText().length(); i++) {
                byte b = (byte) editText.getText().charAt(i);
                sp.tx(b);
            }
            editText.getText().clear();
        } catch (Exception e) {
            showErrorMessage("Error on TX!", e.getMessage());
        }
    }

    @SuppressWarnings("deprecation")
    class SerialPortRx extends AsyncTask<Void,Void,Byte> {
        private String errorMsg;

        @Override
        protected Byte doInBackground(Void... voids) {
            try {
                SerialPortManager sp = SerialPortManager.getInstance();
                return sp.rx();
            } catch (Exception e) {
                errorMsg = e.getMessage();
                return 0;
            }
        }

        @Override
        protected void onPostExecute(Byte b) {
            if (errorMsg == null) {
                EditText editText = (EditText) findViewById(R.id.editText);
                editText.getText().append((char) b.byteValue());
                new SerialPortRx().execute();
            } else {
                showErrorMessage("Error in RX!", errorMsg);
            }
        }
    }
}
