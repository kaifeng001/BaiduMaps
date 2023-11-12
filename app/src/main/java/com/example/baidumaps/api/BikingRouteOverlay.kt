/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.example.baidumaps.api

import android.util.Log
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.Marker
import com.baidu.mapapi.map.OverlayOptions
import com.baidu.mapapi.map.Polyline
import com.baidu.mapapi.map.PolylineOptions
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.route.BikingRouteLine
import com.example.baidumaps.overlay.OverlayManager

/**
 * 用于显示骑行路线的Overlay
 */
class BikingRouteOverlay(baiduMap: BaiduMap?) : OverlayManager(baiduMap) {
    private var mGreenTexture = BitmapDescriptorFactory.fromAsset("Icon_road_blue_arrow.png")

    private var mRouteLine: BikingRouteLine? = null

    /**
     * 设置路线数据。
     *
     * @param line
     * 路线数据
     */
    fun setData(line: BikingRouteLine?) {
        mRouteLine = line
    }

    override fun getOverlayOptions(): List<OverlayOptions>? {
        if (mRouteLine == null) {
            return null
        }
        val overlayList: MutableList<OverlayOptions> = ArrayList()
        // poly line list
        if (mRouteLine!!.allStep != null
            && mRouteLine!!.allStep.size > 0
        ) {
            var lastStepLastPoint: LatLng? = null
            for (step in mRouteLine!!.allStep) {
                val watPoints = step.wayPoints
                if (watPoints != null) {
                    val points: MutableList<LatLng> = ArrayList()
                    if (lastStepLastPoint != null) {
                        points.add(lastStepLastPoint)
                        Log.d(
                            "fengkai",
                            "getOverlayOptions:${lastStepLastPoint.latitude}  ${lastStepLastPoint.longitude}"
                        )
                    }
                    points.addAll(watPoints)
                    if (mGreenTexture == null){
                        mGreenTexture = BitmapDescriptorFactory.fromAsset("Icon_road_green_arrow.png")
                    }
                    overlayList.add(
                        PolylineOptions().points(points).width(10)
                            .customTexture(mGreenTexture).zIndex(0)
                    )
                    lastStepLastPoint = watPoints[watPoints.size - 1]
                }
            }
        }
        return overlayList
    }

    /**
     * 处理点击事件
     *
     * @param i
     * 被点击的step在
     * [BikingRouteLine.getAllStep]
     * 中的索引
     * @return 是否处理了该点击事件
     */
    fun onRouteNodeClick(i: Int): Boolean {
        if (mRouteLine!!.allStep != null
            && mRouteLine!!.allStep[i] != null
        ) {
            Log.i("baidumapsdk", "BikingRouteOverlay onRouteNodeClick")
        }
        return false
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        mOverlayList?.apply {
            for (mMarker in this) {
                if (mMarker is Marker && mMarker == marker) {
                    if (marker.extraInfo != null) {
                        onRouteNodeClick(marker.extraInfo.getInt("index"))
                    }
                }
            }
        }
        return true
    }

    override fun onPolylineClick(polyline: Polyline): Boolean {
        // TODO Auto-generated method stub
        return false
    }
}