package com.droidev.postgresqlchat;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MyApplication extends Application {

    private boolean isAppInForeground = false;

    @Override
    public void onCreate() {
        super.onCreate();

        // Register activity lifecycle callbacks
        registerActivityLifecycleCallbacks(new MyActivityLifecycleCallbacks());
    }

    public boolean isAppInForeground() {
        return isAppInForeground;
    }

    private class MyActivityLifecycleCallbacks implements ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
            isAppInForeground = true;
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {
            isAppInForeground = false;
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
        }
    }
}

