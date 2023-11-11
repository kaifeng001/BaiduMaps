package com.example.baidumaps

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.baidu.mapapi.bikenavi.BikeNavigateHelper
import com.baidu.mapapi.bikenavi.adapter.IBNaviStatusListener
import com.baidu.mapapi.bikenavi.adapter.IBRouteGuidanceListener
import com.baidu.mapapi.bikenavi.adapter.IBTTSPlayer
import com.baidu.mapapi.bikenavi.model.BikeNaviDisplayOption
import com.baidu.mapapi.bikenavi.model.BikeRouteDetailInfo
import com.baidu.mapapi.bikenavi.model.IBRouteIconInfo
import com.baidu.mapapi.bikenavi.params.BikeNaviLaunchParam
import com.baidu.mapapi.walknavi.model.RouteGuideKind


class BNaviGuideActivity : AppCompatActivity() {
    private val TAG = BNaviGuideActivity::class.java.simpleName

    private var mNaviHelper: BikeNavigateHelper? = null

    var param: BikeNaviLaunchParam? = null

    override fun onDestroy() {
        super.onDestroy()
        mNaviHelper!!.quit()
    }

    override fun onResume() {
        super.onResume()
        mNaviHelper!!.resume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mNaviHelper = BikeNavigateHelper.getInstance()
        val bikeNaviDisplayOption = BikeNaviDisplayOption()
            .showSpeedLayout(true) // 是否展示速度切换布局
            .showTopGuideLayout(true) // 是否展示顶部引导布局
            .showLocationImage(true) // 是否展示视角切换资源
        mNaviHelper?.setBikeNaviDisplayOption(bikeNaviDisplayOption)
        val view = mNaviHelper?.onCreate(this@BNaviGuideActivity)
        view?.let { setContentView(it) }
        mNaviHelper?.setBikeNaviStatusListener(IBNaviStatusListener { Log.d(TAG, "onNaviExit") })
        mNaviHelper?.setTTsPlayer(IBTTSPlayer { s, b ->
            Log.d("tts", s)
            0
        })
        mNaviHelper?.startBikeNavi(this@BNaviGuideActivity)
        mNaviHelper?.setRouteGuidanceListener(this, object : IBRouteGuidanceListener {
            override fun onRouteGuideIconInfoUpdate(routeIconInfo: IBRouteIconInfo) {
                if (routeIconInfo != null) {
                    Log.d(
                        "GuideIconObjectUpdate",
                        "onRoadGuideTextUpdate   Drawable=: " + routeIconInfo.iconDrawable
                                + " Name=: " + routeIconInfo.iconName
                    )
                }
            }

            override fun onRouteGuideIconUpdate(icon: Drawable) {}
            override fun onRouteGuideKind(routeGuideKind: RouteGuideKind) {}
            override fun onRoadGuideTextUpdate(
                charSequence: CharSequence,
                charSequence1: CharSequence
            ) {
            }

            override fun onRemainDistanceUpdate(charSequence: CharSequence) {}
            override fun onRemainTimeUpdate(charSequence: CharSequence) {}
            override fun onGpsStatusChange(charSequence: CharSequence, drawable: Drawable) {}
            override fun onRouteFarAway(charSequence: CharSequence, drawable: Drawable) {}
            override fun onRoutePlanYawing(charSequence: CharSequence, drawable: Drawable) {}
            override fun onReRouteComplete() {}
            override fun onArriveDest() {}
            override fun onVibrate() {}
            override fun onGetRouteDetailInfo(bikeRouteDetailInfo: BikeRouteDetailInfo) {}
        })
    }
}