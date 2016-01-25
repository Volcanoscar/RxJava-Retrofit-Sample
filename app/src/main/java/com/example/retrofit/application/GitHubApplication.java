package com.example.retrofit.application;

import android.app.Application;

import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;

import timber.log.Timber;

/**
 * 类描述:
 * 创建人:lin.ma@renren-inc.com
 * 创建时间:16-1-25
 * 备注:{@link } Thanks for  ,Her code is very good ! I made reference to his code,It saves me a lot of time!
 * 修改人:
 * 修改时间:
 * 修改备注:
 */
public class GitHubApplication extends Application {
    private static final String TAG = GitHubApplication.class.getSimpleName();
    @Override
    public void onCreate() {
        super.onCreate();
        Logger.init(TAG).logLevel(LogLevel.FULL);
        Timber.plant(new Timber.DebugTree());
    }
}
