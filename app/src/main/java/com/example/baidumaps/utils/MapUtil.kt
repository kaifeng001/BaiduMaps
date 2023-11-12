package com.example.baidumaps.utils

import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.BitmapDescriptor
import com.baidu.mapapi.map.MapStatus
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MapView
import com.baidu.mapapi.map.Marker
import com.baidu.mapapi.map.MarkerOptions
import com.baidu.mapapi.map.Overlay
import com.baidu.mapapi.map.OverlayOptions
import com.baidu.mapapi.map.PolylineOptions
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.model.LatLngBounds
import com.baidu.mapapi.utils.CoordinateConverter
import com.baidu.trace.model.CoordType
import com.baidu.trace.model.SortType
import com.baidu.trace.model.TraceLocation
import com.example.baidumaps.utils.CommonUtil.getInterception
import com.example.baidumaps.utils.CommonUtil.getSlope
import com.example.baidumaps.utils.CommonUtil.getXMoveDistance
import com.example.baidumaps.utils.CommonUtil.isZeroPoint
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Created by baidu on 17/2/9.
 */
class MapUtil private constructor() {
    private var mapStatus: MapStatus? = null
    private var mMoveMarker: Marker? = null
    var mapView: MapView? = null
    var baiduMap: BaiduMap? = null
    var lastPoint: LatLng? = null

    /**
     * 路线覆盖物
     */
    var polylineOverlay: Overlay? = null
    fun init(view: MapView?) {
        mapView = view
        baiduMap = mapView!!.map
        mapView!!.showZoomControls(false)
    }

    fun onPause() {
        if (null != mapView) {
            mapView!!.onPause()
        }
    }

    fun onResume() {
        if (null != mapView) {
            mapView!!.onResume()
        }
    }

    fun clear() {
        lastPoint = null
        if (null != mMoveMarker) {
            mMoveMarker!!.remove()
            mMoveMarker = null
        }
        if (null != polylineOverlay) {
            polylineOverlay!!.remove()
            polylineOverlay = null
        }
        if (null != baiduMap) {
            baiduMap!!.clear()
            baiduMap = null
        }
        mapStatus = null
        if (null != mapView) {
            mapView!!.onDestroy()
            mapView = null
        }
    }

    fun addOverlay(currentPoint: LatLng?, icon: BitmapDescriptor?, bundle: Bundle?): Marker {
        val overlayOptions: OverlayOptions = MarkerOptions().position(currentPoint)
            .icon(icon).zIndex(9).draggable(true)
        val marker = baiduMap!!.addOverlay(overlayOptions) as Marker
        if (null != bundle) {
            marker.extraInfo = bundle
        }
        return marker
    }

    fun animateMapStatus(points: List<LatLng?>?) {
        if (null == points || points.isEmpty()) {
            return
        }
        val builder = LatLngBounds.Builder()
        for (point in points) {
            builder.include(point)
        }
        val msUpdate = MapStatusUpdateFactory.newLatLngBounds(builder.build())
        baiduMap!!.animateMapStatus(msUpdate)
    }

    fun animateMapStatus(point: LatLng?, zoom: Float) {
        val builder = MapStatus.Builder()
        mapStatus = builder.target(point).zoom(zoom).build()
        baiduMap!!.animateMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatus))
    }

    fun setMapStatus(point: LatLng?, zoom: Float) {
        val builder = MapStatus.Builder()
        mapStatus = builder.target(point).zoom(zoom).build()
        baiduMap!!.setMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatus))
    }

    fun refresh() {
        val mapCenter = baiduMap!!.mapStatus.target
        val mapZoom = baiduMap!!.mapStatus.zoom - 1.0f
        setMapStatus(mapCenter, mapZoom)
    }

    fun locTimeMinutes(startTime: Long, endTime: Long): Boolean {
        try {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val date1 = formatter.format(Date(startTime * 1000))
            val date2 = formatter.format(Date(endTime * 1000))
            // 获取服务器返回的时间戳 转换成"yyyy-MM-dd HH:mm:ss"
            // 计算的时间差
            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val d1 = df.parse(date1)
            val d2 = df.parse(date2)
            val diff = d2.time - d1.time
            val days = diff / (1000 * 60 * 60 * 24)
            val hours = ((diff - days * (1000 * 60 * 60 * 24))
                    / (1000 * 60 * 60))
            val minutes = ((diff - days * (1000 * 60 * 60 * 24) - (hours
                    * (1000 * 60 * 60)))
                    / (1000 * 60))
            Log.d("MapUtils", "差值:" + diff + "分钟" + minutes)
            if (minutes > 5) {
                return true
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return false
    }

    companion object {
        val instance = MapUtil()

        /**
         * 将轨迹实时定位点转换为地图坐标
         *
         * @param location
         *
         * @return
         */
        fun convertTraceLocation2Map(location: TraceLocation?): LatLng? {
            if (null == location) {
                return null
            }
            val latitude = location.latitude
            val longitude = location.longitude
            if (Math.abs(latitude - 0.0) < 0.000001 && Math.abs(longitude - 0.0) < 0.000001) {
                return null
            }
            var currentLatLng = LatLng(latitude, longitude)
            if (CoordType.wgs84 == location.coordType) {
                val sourceLatLng = currentLatLng
                val converter = CoordinateConverter()
                converter.from(CoordinateConverter.CoordType.GPS)
                converter.coord(sourceLatLng)
                currentLatLng = converter.convert()
            }
            return currentLatLng
        }

        /**
         * 将地图坐标转换轨迹坐标
         *
         * @param latLng
         *
         * @return
         */
        fun convertMap2Trace(latLng: LatLng): com.baidu.trace.model.LatLng {
            return com.baidu.trace.model.LatLng(latLng.latitude, latLng.longitude)
        }

        /**
         * 将轨迹坐标对象转换为地图坐标对象
         *
         * @param traceLatLng
         *
         * @return
         */
        fun convertTrace2Map(traceLatLng: com.baidu.trace.model.LatLng): LatLng {
            return LatLng(traceLatLng.latitude, traceLatLng.longitude)
        }
    }
}