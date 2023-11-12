package com.example.baidumaps

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MapView
import com.baidu.mapapi.map.PolylineOptions
import com.baidu.mapapi.model.LatLng
import com.baidu.trace.api.track.HistoryTrackRequest
import com.baidu.trace.api.track.HistoryTrackResponse
import com.baidu.trace.api.track.OnTrackListener
import com.example.baidumaps.utils.CommonUtil
import com.example.baidumaps.utils.MapUtil


/**
 * 移动轨迹
 */
class TraceActivity : AppCompatActivity() {
    // 地图View实例
    private var mMapView: MapView? = null
    private var mBaiduMap: BaiduMap? = null
    private lateinit var distanceView: TextView
    private lateinit var timeView: TextView

    private val mGreenTexture = BitmapDescriptorFactory.fromAsset("Icon_road_green_arrow.png")
    lateinit var myApplication: MyApplication
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trace_main)
        myApplication = application as MyApplication
        distanceView = findViewById(R.id.distance)
        timeView = findViewById(R.id.time)
        initMap()
        drawPolyLine()
    }

    override fun onResume() {
        super.onResume()
        if (null != mMapView) {
            mMapView!!.onResume()
        }
    }

    private fun initMap() {
        mMapView = findViewById(R.id.mapview)
        if (null == mMapView) {
            return
        }
        mBaiduMap = mMapView!!.map
        if (null == mBaiduMap) {
            return
        }
        // 设置初始中心点为北京
        val mapStatusUpdate =
            MapStatusUpdateFactory.newLatLngZoom(myApplication.mCurrentLocation, 18f)
        mBaiduMap!!.setMapStatus(mapStatusUpdate)
    }

    private fun drawPolyLine() {
        // 请求标识
        val tag = 1
        // 设备标识
        val entityName = "myTrace"
        // 创建历史轨迹请求实例
        val historyTrackRequest = HistoryTrackRequest(tag, myApplication.mServiceId, entityName)
        // 设置开始时间
        historyTrackRequest.startTime = myApplication.mStartTime
        // 设置结束时间
        historyTrackRequest.endTime = myApplication.mEndTime

        val minute = (myApplication.mEndTime - myApplication.mStartTime) / 60
        // 初始化轨迹监听器
        val mTrackListener: OnTrackListener = object : OnTrackListener() {
            // 历史轨迹回调
            override fun onHistoryTrackCallback(response: HistoryTrackResponse) {
                val points = response.getTrackPoints()
                distanceView.text = "骑行距离：${response.distance.toInt()}米"
                timeView.text = "骑行用时：$minute 分钟"
                points?.apply {
                    if (size <= 2) {
                        return
                    }
                    val trackPoints: MutableList<LatLng> = mutableListOf()
                    for (trackPoint in this) {
                        if (!CommonUtil.isZeroPoint(
                                trackPoint.location.getLatitude(),
                                trackPoint.location.getLongitude()
                            )
                        ) {
                            trackPoints.add(MapUtil.convertTrace2Map(trackPoint.location))
                        }
                    }
                    //         绘制纹理PolyLine
                    val polylineOptions =
                        PolylineOptions().points(trackPoints).width(20).customTexture(mGreenTexture)
                            .dottedLine(true)
                    mBaiduMap!!.addOverlay(polylineOptions)
                }
            }
        }

        // 查询历史轨迹
        myApplication.mTraceClient?.queryHistoryTrack(historyTrackRequest, mTrackListener)

    }

    override fun onPause() {
        super.onPause()
        if (null != mMapView) {
            mMapView!!.onPause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mGreenTexture?.recycle()
        if (null != mBaiduMap) {
            mBaiduMap!!.clear()
        }
        if (null != mMapView) {
            mMapView!!.onDestroy()
        }
    }

    companion object {
        private val latlngs = arrayOf(
            LatLng(40.055826, 116.307917),
            LatLng(40.055916, 116.308455),
            LatLng(40.055967, 116.308549),
            LatLng(40.056014, 116.308574),
            LatLng(40.056440, 116.308485),
            LatLng(40.056816, 116.308352),
            LatLng(40.057997, 116.307725),
            LatLng(40.058022, 116.307693),
            LatLng(40.058029, 116.307590),
            LatLng(40.057913, 116.307119),
            LatLng(40.057850, 116.306945),
            LatLng(40.057756, 116.306915),
            LatLng(40.057225, 116.307164),
            LatLng(40.056134, 116.307546),
            LatLng(40.055879, 116.307636),
            LatLng(40.055826, 116.307697)
        )
    }
}