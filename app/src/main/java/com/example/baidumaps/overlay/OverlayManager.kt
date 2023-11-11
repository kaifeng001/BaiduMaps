package com.example.baidumaps.overlay

import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener
import com.baidu.mapapi.map.BaiduMap.OnPolylineClickListener
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.Marker
import com.baidu.mapapi.map.Overlay
import com.baidu.mapapi.map.OverlayOptions
import com.baidu.mapapi.model.LatLngBounds

/**
 * 该类提供一个能够显示和管理多个Overlay的基类
 *
 *
 * 复写[.getOverlayOptions] 设置欲显示和管理的Overlay列表
 *
 *
 *
 * 通过
 * [BaiduMap.setOnMarkerClickListener]
 * 将覆盖物点击事件传递给OverlayManager后，OverlayManager才能响应点击事件。
 *
 *
 * 复写[.onMarkerClick] 处理Marker点击事件
 *
 */
abstract class OverlayManager(baiduMap: BaiduMap?) : OnMarkerClickListener,
    OnPolylineClickListener {
    private var mBaiduMap: BaiduMap? = null
    private var mOverlayOptionList: MutableList<OverlayOptions>? = null
    protected var mOverlayList: MutableList<Overlay>? = null

    /**
     * 通过一个BaiduMap 对象构造
     *
     * @param baiduMap
     */
    init {
        mBaiduMap = baiduMap
        // mBaiduMap.setOnMarkerClickListener(this);
        if (mOverlayOptionList == null) {
            mOverlayOptionList = ArrayList()
        }
        if (mOverlayList == null) {
            mOverlayList = ArrayList()
        }
    }

    /**
     * 覆写此方法设置要管理的Overlay列表
     *
     * @return 管理的Overlay列表
     */
    abstract fun getOverlayOptions() : List<OverlayOptions>?

    /**
     * 将所有Overlay 添加到地图上
     */
    fun addToMap() {
        if (mBaiduMap == null) {
            return
        }
        removeFromMap()
        val overlayOptions = getOverlayOptions()
        if (overlayOptions != null) {
            mOverlayOptionList!!.addAll(overlayOptions)
        }
        for (option in mOverlayOptionList!!) {
            mOverlayList!!.add(mBaiduMap!!.addOverlay(option))
        }
    }

    /**
     * 将所有Overlay 从 地图上消除
     */
    fun removeFromMap() {
        if (mBaiduMap == null) {
            return
        }
        for (marker in mOverlayList!!) {
            marker.remove()
        }
        mOverlayOptionList!!.clear()
        mOverlayList!!.clear()
    }

    /**
     * 缩放地图，使所有Overlay都在合适的视野内
     *
     *
     * 注： 该方法只对Marker类型的overlay有效
     *
     */
    fun zoomToSpan() {
        if (mBaiduMap == null) {
            return
        }
        if (mOverlayList!!.size > 0) {
            val builder = LatLngBounds.Builder()
            for (overlay in mOverlayList!!) {
                // polyline 中的点可能太多，只按marker 缩放
                if (overlay is Marker) {
                    builder.include(overlay.position)
                }
            }
            val mapStatus = mBaiduMap!!.mapStatus
            if (null != mapStatus) {
                val width = mapStatus.winRound.right - mBaiduMap!!.mapStatus.winRound.left - 400
                val height = mapStatus.winRound.bottom - mBaiduMap!!.mapStatus.winRound.top - 400
                mBaiduMap!!.setMapStatus(
                    MapStatusUpdateFactory
                        .newLatLngBounds(builder.build(), width, height)
                )
            }
        }
    }

    /**
     * 设置显示在规定宽高中的地图地理范围
     */
    fun zoomToSpanPaddingBounds(
        paddingLeft: Int,
        paddingTop: Int,
        paddingRight: Int,
        paddingBottom: Int
    ) {
        if (mBaiduMap == null) {
            return
        }
        if (mOverlayList!!.size > 0) {
            val builder = LatLngBounds.Builder()
            for (overlay in mOverlayList!!) {
                // polyline 中的点可能太多，只按marker 缩放
                if (overlay is Marker) {
                    builder.include(overlay.position)
                }
            }
            mBaiduMap!!.setMapStatus(
                MapStatusUpdateFactory
                    .newLatLngBounds(
                        builder.build(),
                        paddingLeft,
                        paddingTop,
                        paddingRight,
                        paddingBottom
                    )
            )
        }
    }
}