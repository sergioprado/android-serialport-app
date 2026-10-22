package com.labworks.serialportapp;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserManager;

public class DevAdmin extends DeviceAdminReceiver {

    private static final String[] RESTRICTIONS = {
            UserManager.DISALLOW_FACTORY_RESET,
            UserManager.DISALLOW_SAFE_BOOT,
            UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA,
            UserManager.DISALLOW_ADJUST_VOLUME,
            UserManager.DISALLOW_ADD_USER,
            UserManager.DISALLOW_SYSTEM_ERROR_DIALOGS};

    // called when the app is set as device owner (dpm set-device-owner)
    @Override
    public void onEnabled(Context context, Intent intent) {
        DevicePolicyManager dpm = getManager(context);
        if (!dpm.isDeviceOwnerApp(context.getPackageName()))
            return;

        ComponentName admin = getWho(context);

        // restrictions
        for (String restriction : RESTRICTIONS)
            dpm.addUserRestriction(admin, restriction);

        // disable the lock screen
        dpm.setKeyguardDisabled(admin, true);

        // allow the app to run in lock task mode, without any system UI
        dpm.setLockTaskPackages(admin, new String[] {context.getPackageName()});
        dpm.setLockTaskFeatures(admin, DevicePolicyManager.LOCK_TASK_FEATURE_NONE);

        // set the app as the default home app
        IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_HOME);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        dpm.addPersistentPreferredActivity(admin, filter,
                new ComponentName(context, MainActivity.class));
    }
}
