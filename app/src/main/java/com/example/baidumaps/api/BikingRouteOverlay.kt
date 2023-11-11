/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.example.baidumaps.api

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.BitmapDescriptor
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.Marker
import com.baidu.mapapi.map.MarkerOptions
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
        if (mRouteLine!!.allStep != null
            && mRouteLine!!.allStep.size > 0
        ) {
            for (step in mRouteLine!!.allStep) {
                val b = Bundle()
                b.putInt("index", mRouteLine!!.allStep.indexOf(step))
                if (step.entrance != null) {
                    overlayList.add(
                        MarkerOptions()
                            .position(step.entrance.location)
                            .rotate((360 - step.direction).toFloat())
                            .zIndex(10)
                            .anchor(0.5f, 0.5f)
                            .extraInfo(b)
                            .icon(
                                BitmapDescriptorFactory
                                    .fromAssetWithDpi("Icon_line_node.png")
                            )
                    )
                }

//                // 最后路段绘制出口点
//                if (mRouteLine!!.allStep.indexOf(step) == mRouteLine!!
//                        .allStep.size - 1 && step.exit != null
//                ) {
//                    overlayList.add(
//                        MarkerOptions()
//                            .position(step.exit.location)
//                            .anchor(0.5f, 0.5f)
//                            .zIndex(10)
//                            .icon(
//                                BitmapDescriptorFactory
//                                    .fromAssetWithDpi("Icon_line_node.png")
//                            )
//                    )
//                }
            }
        }
//        // starting
//        if (mRouteLine!!.starting != null) {
//            overlayList.add(
//                MarkerOptions()
//                    .position(mRouteLine!!.starting.location)
//                    .icon(
//                        if (startMarker != null) startMarker else BitmapDescriptorFactory
//                            .fromAssetWithDpi("Icon_line_node.png")
//                    ).zIndex(10)
//            )
//        }
//         terminal
        if (mRouteLine!!.terminal != null) {
            overlayList
                .add(
                    MarkerOptions()
                        .position(mRouteLine!!.terminal.location)
                        .icon(
                            if (terminalMarker != null) terminalMarker else BitmapDescriptorFactory
                                .fromAssetWithDpi("Icon_line_node.png")
                        )
                        .zIndex(10)
                )
        }

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
                    }
                    points.addAll(watPoints)
                    overlayList.add(
                        PolylineOptions().points(points).width(10)
                            .color(if (lineColor != 0) lineColor else Color.argb(178, 0, 78, 255))
                            .zIndex(0)
                    )
                    lastStepLastPoint = watPoints[watPoints.size - 1]
                }
            }
        }
        return overlayList
    }

    val startMarker: BitmapDescriptor?
        /**
         * 覆写此方法以改变默认起点图标
         *
         * @return 起点图标
         */
        get() = null
    val lineColor: Int
        get() = 0
    val terminalMarker: BitmapDescriptor?
        /**
         * 覆写此方法以改变默认终点图标
         *
         * @return 终点图标
         */
        get() = null

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