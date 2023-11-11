package com.example.baidumaps

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.baidu.mapapi.walknavi.WalkNavigateHelper
import com.baidu.mapapi.walknavi.adapter.IWNaviStatusListener
import com.baidu.mapapi.walknavi.adapter.IWRouteGuidanceListener
import com.baidu.mapapi.walknavi.adapter.IWTTSPlayer
import com.baidu.mapapi.walknavi.model.IWRouteIconInfo
import com.baidu.mapapi.walknavi.model.RouteGuideKind
import com.baidu.mapapi.walknavi.model.WalkNaviDisplayOption
import com.baidu.platform.comapi.walknavi.WalkNaviModeSwitchListener
import com.baidu.platform.comapi.walknavi.widget.ArCameraView

class WNaviGuideActivity : AppCompatActivity(){
    private val TAG = WNaviGuideActivity::class.java.simpleName

    private var mNaviHelper: WalkNavigateHelper? = null

    override fun onDestroy() {
        super.onDestroy()
        mNaviHelper!!.quit()
    }

    override fun onResume() {
        super.onResume()
        mNaviHelper!!.resume()
    }

    override fun onPause() {
        super.onPause()
        mNaviHelper!!.pause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mNaviHelper = WalkNavigateHelper.getInstance()
        val walkNaviDisplayOption = WalkNaviDisplayOption()
            .showImageToAr(true) // 是否展示AR图片
            .showCalorieLayoutEnable(true) // 是否展示热量消耗布局
            .showLocationImage(true) // 是否展示视角切换资源
        mNaviHelper?.setWalkNaviDisplayOption(walkNaviDisplayOption)
        try {
            val view = mNaviHelper?.onCreate(this@WNaviGuideActivity)
            view?.let { setContentView(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mNaviHelper?.setWalkNaviStatusListener(object : IWNaviStatusListener {
            override fun onWalkNaviModeChange(mode: Int, listener: WalkNaviModeSwitchListener) {
                Log.d(TAG, "onWalkNaviModeChange : $mode")
                mNaviHelper?.switchWalkNaviMode(this@WNaviGuideActivity, mode, listener)
            }

            override fun onNaviExit() {
                Log.d(TAG, "onNaviExit")
            }
        })
        mNaviHelper?.setTTsPlayer(IWTTSPlayer { s, b ->
            Log.d(TAG, "tts: $s")
            0
        })
        val startResult = mNaviHelper?.startWalkNavi(this@WNaviGuideActivity)
        Log.e(TAG, "startWalkNavi result : $startResult")
        mNaviHelper?.setRouteGuidanceListener(this, object : IWRouteGuidanceListener {
            override fun onRouteGuideIconInfoUpdate(routeIconInfo: IWRouteIconInfo) {
                if (routeIconInfo != null) {
                    Log.d(
                        TAG, "onRoadGuideTextUpdate   Drawable=: " + routeIconInfo.iconDrawable
                                + " Name=: " + routeIconInfo.iconName
                    )
                }
            }

            override fun onRouteGuideIconUpdate(icon: Drawable) {
                Log.d(TAG, "onRoadGuideTextUpdate   Drawable=: $icon")
            }

            override fun onRouteGuideKind(routeGuideKind: RouteGuideKind) {
                Log.d(TAG, "onRouteGuideKind: $routeGuideKind")
            }

            override fun onRoadGuideTextUpdate(
                charSequence: CharSequence,
                charSequence1: CharSequence
            ) {
                Log.d(
                    TAG,
                    "onRoadGuideTextUpdate   charSequence=: " + charSequence + "   charSequence1 = : " +
                            charSequence1
                )
            }

            override fun onRemainDistanceUpdate(charSequence: CharSequence) {
                Log.d(TAG, "onRemainDistanceUpdate: charSequence = :$charSequence")
            }

            override fun onRemainTimeUpdate(charSequence: CharSequence) {
                Log.d(TAG, "onRemainTimeUpdate: charSequence = :$charSequence")
            }

            override fun onGpsStatusChange(charSequence: CharSequence, drawable: Drawable) {
                Log.d(TAG, "onGpsStatusChange: charSequence = :$charSequence")
            }

            override fun onRouteFarAway(charSequence: CharSequence, drawable: Drawable) {
                Log.d(TAG, "onRouteFarAway: charSequence = :$charSequence")
            }

            override fun onRoutePlanYawing(charSequence: CharSequence, drawable: Drawable) {
                Log.d(TAG, "onRoutePlanYawing: charSequence = :$charSequence")
            }

            override fun onReRouteComplete() {}
            override fun onArriveDest() {}
            override fun onIndoorEnd(msg: Message) {}
            override fun onFinalEnd(msg: Message) {}
            override fun onVibrate() {}
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ArCameraView.WALK_AR_PERMISSION) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(
                    this@WNaviGuideActivity,
                    "没有相机权限,请打开后重试",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mNaviHelper!!.startCameraAndSetMapView(this@WNaviGuideActivity)
            }
        }
    }

}