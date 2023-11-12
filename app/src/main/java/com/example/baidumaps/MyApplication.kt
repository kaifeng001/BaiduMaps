package com.example.baidumaps

import android.app.Application
import com.baidu.mapapi.SDKInitializer
import com.baidu.mapapi.model.LatLng
import com.baidu.trace.LBSTraceClient
import com.baidu.trace.Trace
import com.baidu.trace.model.OnTraceListener
import com.baidu.trace.model.PushMessage


class MyApplication : Application() {
    /**
     * 轨迹客户端
     */
    var mTraceClient: LBSTraceClient? = null

    var mStartTime: Long = 0;

    var mEndTime: Long = 0;

    var mCurrentLocation: LatLng = LatLng(40.05703723, 116.3078927)

    val mServiceId: Long = 238382

    /**
     * 轨迹服务
     */
    var mTrace: Trace? = null

    // 初始化轨迹服务监听器
    var mTraceListener: OnTraceListener = object : OnTraceListener {
        override fun onBindServiceCallback(p0: Int, p1: String?) {}

        // 开启服务回调
        override fun onStartTraceCallback(status: Int, message: String) {
            if (status == 0) {
                // 开启采集
                mTraceClient?.startGather(this)
            }
        }

        // 停止服务回调
        override fun onStopTraceCallback(status: Int, message: String) {}

        // 开启采集回调
        override fun onStartGatherCallback(status: Int, message: String) {
            mStartTime = System.currentTimeMillis() / 1000
        }

        // 停止采集回调
        override fun onStopGatherCallback(status: Int, message: String) {
            mEndTime = System.currentTimeMillis() / 1000
        }

        // 推送回调
        override fun onPushCallback(messageNo: Byte, message: PushMessage) {}
        override fun onInitBOSCallback(p0: Int, p1: String?) {}
        override fun onTraceDataUploadCallBack(p0: Int, p1: String?, p2: Int, p3: Int) {}
    }


    override fun onCreate() {
        super.onCreate()
        /**
         * 隐私政策统一接口：该接口必须在调用SDK初始化接口之前设置
         * 设为false不同意隐私政策：不支持发起检索、路线规划等数据请求, SDK抛出异常；
         * 设为true同意隐私政策：支持发起检索、路线规划等数据请求
         */
        SDKInitializer.setAgreePrivacy(this, true)
        LBSTraceClient.setAgreePrivacy(this, true)
        // 在SDK初始化时捕获抛出的异常
        try {
            SDKInitializer.initialize(this)
            // 设备标识
            val entityName = "myTrace"
            // 是否需要对象存储服务，默认为：false，关闭对象存储服务。注：鹰眼 Android SDK v3.0以上版本支持随轨迹上传图像等对象数据，若需使用此功能，该参数需设为 true，且需导入bos-android-sdk-1.0.2.jar。
            val isNeedObjectStorage = false
            // 初始化轨迹服务
            mTrace = Trace(mServiceId, entityName, isNeedObjectStorage)
            mTraceClient = LBSTraceClient(applicationContext)
            // 定位周期(单位:秒)
            val gatherInterval = 5
            val packInterval = 10

            // 设置定位和打包周期
            mTraceClient!!.setInterval(gatherInterval, packInterval)
            mTraceClient!!.startTrace(mTrace, mTraceListener)
        } catch (e: Exception) {
        }
    }
}