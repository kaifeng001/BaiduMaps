package com.example.baidumaps

import android.app.Application
import com.baidu.mapapi.SDKInitializer

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        /**
         * 隐私政策统一接口：该接口必须在调用SDK初始化接口之前设置
         * 设为false不同意隐私政策：不支持发起检索、路线规划等数据请求, SDK抛出异常；
         * 设为true同意隐私政策：支持发起检索、路线规划等数据请求
         */
        SDKInitializer.setAgreePrivacy(this, true)

        // 在SDK初始化时捕获抛出的异常
        try {
            SDKInitializer.initialize(this)
        } catch (e: Exception) {
        }
    }
}