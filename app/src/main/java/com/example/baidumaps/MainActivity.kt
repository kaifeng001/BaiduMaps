package com.example.baidumaps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.baidu.location.BDLocation
import com.baidu.location.BDLocationListener
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.bikenavi.BikeNavigateHelper
import com.baidu.mapapi.bikenavi.adapter.IBEngineInitListener
import com.baidu.mapapi.bikenavi.adapter.IBRoutePlanListener
import com.baidu.mapapi.bikenavi.model.BikeRoutePlanError
import com.baidu.mapapi.bikenavi.params.BikeNaviLaunchParam
import com.baidu.mapapi.bikenavi.params.BikeRouteNodeInfo
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.BaiduMap.OnMarkerDragListener
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.MapPoi
import com.baidu.mapapi.map.MapStatus
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MapView
import com.baidu.mapapi.map.Marker
import com.baidu.mapapi.map.MarkerOptions
import com.baidu.mapapi.map.MyLocationData
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.route.BikingRoutePlanOption
import com.baidu.mapapi.search.route.BikingRouteResult
import com.baidu.mapapi.search.route.DrivingRouteResult
import com.baidu.mapapi.search.route.IndoorRouteResult
import com.baidu.mapapi.search.route.MassTransitRouteResult
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener
import com.baidu.mapapi.search.route.PlanNode
import com.baidu.mapapi.search.route.RoutePlanSearch
import com.baidu.mapapi.search.route.TransitRouteResult
import com.baidu.mapapi.search.route.WalkingRouteResult
import com.baidu.mapapi.walknavi.WalkNavigateHelper
import com.baidu.mapapi.walknavi.adapter.IWEngineInitListener
import com.baidu.mapapi.walknavi.adapter.IWRoutePlanListener
import com.baidu.mapapi.walknavi.model.WalkRoutePlanError
import com.baidu.mapapi.walknavi.params.WalkNaviLaunchParam
import com.baidu.mapapi.walknavi.params.WalkRouteNodeInfo
import com.example.baidumaps.api.BikingRouteOverlay
import java.lang.ref.WeakReference


class MainActivity : AppCompatActivity() {
    private val TAG: String = MainActivity::class.java.getSimpleName()
    private var mMapView: MapView? = null
    private var mBaiduMap: BaiduMap? = null
    private var mLocClient: LocationClient? = null;

    /*导航起终点Marker，可拖动改变起终点的坐标*/
    private var mStartMarker: Marker? = null
    private var mEndMarker: Marker? = null

    private var startPt: LatLng? = null
    private var endPt: LatLng? = null

    private var mBikeParam: BikeNaviLaunchParam? = null
    private var mWalkParam: WalkNaviLaunchParam? = null

    private var isPermissionRequested = false
    private var isHadEndPosition = false

    // 是否首次定位
    var isFirstLoc: Boolean = true

    private val bdStart = BitmapDescriptorFactory.fromResource(R.drawable.icon_start)
    private val bdEnd = BitmapDescriptorFactory.fromResource(R.drawable.icon_end)

    private var mSearch: RoutePlanSearch? = null // 搜索模块，也可去掉地图模块独立使用


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide_main)
        requestPermission()
    }

    private fun initParam() {/*构造导航起终点参数对象*/
        val bikeStartNode = BikeRouteNodeInfo()
        bikeStartNode.location = startPt
        val bikeEndNode = BikeRouteNodeInfo()
        bikeEndNode.location = endPt
        mBikeParam = BikeNaviLaunchParam().startNodeInfo(bikeStartNode).endNodeInfo(bikeEndNode)
        val walkStartNode = WalkRouteNodeInfo()
        walkStartNode.location = startPt
        val walkEndNode = WalkRouteNodeInfo()
        walkEndNode.location = endPt
        mWalkParam = WalkNaviLaunchParam().startNodeInfo(walkStartNode).endNodeInfo(walkEndNode)
    }

    private fun initButton() {/*骑行导航入口*/
        val bikeBtn = findViewById<View>(R.id.btn_bikenavi) as Button
        bikeBtn.setOnClickListener { startBikeNavi() }

        /*普通步行导航入口*/
        val walkBtn = findViewById<View>(R.id.btn_walknavi_normal) as Button
        walkBtn.setOnClickListener {
            mWalkParam!!.extraNaviMode(0)
            startWalkNavi()
        }
        walkBtn.visibility = View.GONE

        /*AR步行导航入口*/
        val arWalkBtn = findViewById<View>(R.id.btn_walknavi_ar) as Button
        arWalkBtn.setOnClickListener {
            mWalkParam!!.extraNaviMode(1)
            startWalkNavi()
        }
        arWalkBtn.visibility = View.GONE
    }

    /**
     * 初始化地图状态
     */
    private fun initMapStatus() {
        mBaiduMap = mMapView!!.map
        // 定位初始化
        initLocation()
        // 地图定位图标点击事件监听
        mBaiduMap!!.setOnMyLocationClickListener {
            Toast.makeText(this, "点击定位图标", Toast.LENGTH_SHORT).show()
            true
        }
        mBaiduMap!!.setOnMarkerClickListener {
            Toast.makeText(this, "点击Marker图标", Toast.LENGTH_SHORT).show()
            true
        }
        mBaiduMap?.setOnMapClickListener(object : BaiduMap.OnMapClickListener {
            override fun onMapClick(p0: LatLng?) {
                endPt = p0
                mEndMarker?.remove()
                val ooB = MarkerOptions().position(endPt).icon(bdEnd).zIndex(2)
                mEndMarker = mBaiduMap!!.addOverlay(ooB) as Marker
                mEndMarker!!.isDraggable = true
                initParam()/* 初始化起终点Marker */
                isHadEndPosition = true

                searchTarget()
            }

            override fun onMapPoiClick(p0: MapPoi?) {
            }

        })
        mBaiduMap!!.setOnMarkerDragListener(object : OnMarkerDragListener {
            override fun onMarkerDrag(marker: Marker) {}
            override fun onMarkerDragEnd(marker: Marker) {
                if (marker == mStartMarker) {
                    startPt = marker.position
                } else if (marker == mEndMarker) {
                    endPt = marker.position
                }
                val bikeStartNode = BikeRouteNodeInfo()
                bikeStartNode.location = startPt
                val bikeEndNode = BikeRouteNodeInfo()
                bikeEndNode.location = endPt
                mBikeParam =
                    BikeNaviLaunchParam().startNodeInfo(bikeStartNode).endNodeInfo(bikeEndNode)
                val walkStartNode = WalkRouteNodeInfo()
                walkStartNode.location = startPt
                val walkEndNode = WalkRouteNodeInfo()
                walkEndNode.location = endPt
                mWalkParam =
                    WalkNaviLaunchParam().startNodeInfo(walkStartNode).endNodeInfo(walkEndNode)
                if (isHadEndPosition) {
                    searchTarget()
                }
            }

            override fun onMarkerDragStart(marker: Marker) {}
        })
    }

    private fun searchTarget() {
        val stNode = PlanNode.withLocation(startPt)
        val enNode = PlanNode.withLocation(endPt)
        mSearch?.bikingSearch(
            BikingRoutePlanOption()
                .from(stNode)
                .to(enNode) // ridingType  0 普通骑行，1 电动车骑行
                // 默认普通骑行
                .ridingType(1)
        )
    }


    /**
     * 定位初始化
     */
    private fun initLocation() {
        // 开启定位图层
        mBaiduMap!!.isMyLocationEnabled = true
        LocationClient.setAgreePrivacy(true)
        try {
            // 定位初始化
            mLocClient = LocationClient(this)
            val myListener = MyLocationListener(this)
            mLocClient?.registerLocationListener(myListener)
            val option = LocationClientOption()
            // 打开gps
            option.isOpenGps = true
            // 设置坐标类型
            option.setCoorType("bd09ll")
            option.setScanSpan(1000)
            mLocClient?.locOption = option
            mLocClient?.start()
        } catch (e: java.lang.Exception) {
        }
    }

    /**
     * 定位SDK监听函数
     */
    class MyLocationListener : BDLocationListener {
        private var activityWeakReference: WeakReference<MainActivity>

        constructor(activity: MainActivity) {
            activityWeakReference = WeakReference(activity)
        }

        override fun onReceiveLocation(location: BDLocation) {
            val mainActivity = activityWeakReference.get()
            // MapView 销毁后不在处理新接收的位置
            if (location == null || mainActivity?.mMapView == null) {
                return
            }
            val locData = MyLocationData.Builder().accuracy(location.radius) // 设置定位数据的精度信息，单位：米
                .direction(location.direction) // 此处设置开发者获取到的方向信息，顺时针0-360
                .latitude(location.latitude).longitude(location.longitude).build()
            Log.d("fengkai", "locData:$location.latitude")
            // 设置定位数据, 只有先允许定位图层后设置数据才会生效
            mainActivity?.mBaiduMap?.setMyLocationData(locData)
            if (mainActivity?.isFirstLoc == true) {
                mainActivity?.isFirstLoc = false
                val latLng = LatLng(location.latitude, location.longitude)
                val builder = MapStatus.Builder()
                builder.target(latLng).zoom(15.0f)
                mainActivity?.mBaiduMap?.animateMapStatus(
                    MapStatusUpdateFactory.newMapStatus(
                        builder.build()
                    )
                )
                mainActivity?.addStartMarker(latLng)
            }
            val myApplication: MyApplication = mainActivity?.application as MyApplication
            myApplication.mCurrentLocation = LatLng(location.latitude, location.longitude)
        }
    }

    /**
     * 添加marker
     *
     * @param latLng 经纬度
     */
    fun addStartMarker(latLng: LatLng) {
        if (latLng.latitude == 0.0 || latLng.longitude == 0.0) {
            return
        }
        startPt = latLng
        endPt = latLng
        initStartPt()
    }

    /**
     * 初始化导航起终点Marker
     */
    private fun initStartPt() {
        val ooA = MarkerOptions().position(startPt).icon(bdStart).zIndex(2).draggable(true)
        mStartMarker = mBaiduMap!!.addOverlay(ooA) as Marker
        mStartMarker!!.isDraggable = false
    }

    /**
     * 开始骑行导航
     */
    private fun startBikeNavi() {
        if (!isHadEndPosition) {
            Toast.makeText(this, "请在地图上选择终点", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d(TAG, "startBikeNavi")
        try {
            BikeNavigateHelper.getInstance().initNaviEngine(this, object : IBEngineInitListener {
                override fun engineInitSuccess() {
                    Log.d(
                        TAG, "BikeNavi engineInitSuccess"
                    )
                    routePlanWithBikeParam()
                }

                override fun engineInitFail() {
                    Log.d(TAG, "BikeNavi engineInitFail")
                    BikeNavigateHelper.getInstance().unInitNaviEngine()
                }
            })
        } catch (e: Exception) {
            Log.d(TAG, "startBikeNavi Exception")
            e.printStackTrace()
        }
    }

    /**
     * 开始步行导航
     */
    private fun startWalkNavi() {
        Log.d(TAG, "startWalkNavi")
        try {
            WalkNavigateHelper.getInstance().initNaviEngine(this, object : IWEngineInitListener {
                override fun engineInitSuccess() {
                    Log.d(
                        TAG, "WalkNavi engineInitSuccess"
                    )
                    routePlanWithWalkParam()
                }

                override fun engineInitFail() {
                    Log.d(TAG, "WalkNavi engineInitFail")
                    WalkNavigateHelper.getInstance().unInitNaviEngine()
                }
            })
        } catch (e: Exception) {
            Log.d(TAG, "startBikeNavi Exception")
            e.printStackTrace()
        }
    }

    /**
     * 发起骑行导航算路
     */
    private fun routePlanWithBikeParam() {
        BikeNavigateHelper.getInstance()
            .routePlanWithRouteNode(mBikeParam, object : IBRoutePlanListener {
                override fun onRoutePlanStart() {
                    Log.d(
                        TAG, "BikeNavi onRoutePlanStart"
                    )
                }

                override fun onRoutePlanSuccess() {
                    Log.d(
                        TAG, "BikeNavi onRoutePlanSuccess"
                    )
                    val intent = Intent()
                    intent.setClass(this@MainActivity, BNaviGuideActivity::class.java)
                    startActivity(intent)
                }

                override fun onRoutePlanFail(error: BikeRoutePlanError) {
                    Log.d(TAG, "BikeNavi onRoutePlanFail")
                }
            })
    }

    /**
     * 发起步行导航算路
     */
    private fun routePlanWithWalkParam() {
        WalkNavigateHelper.getInstance()
            .routePlanWithRouteNode(mWalkParam, object : IWRoutePlanListener {
                override fun onRoutePlanStart() {
                    Log.d(
                        TAG, "WalkNavi onRoutePlanStart"
                    )
                }

                override fun onRoutePlanSuccess() {
                    Log.d(TAG, "onRoutePlanSuccess")
                    val intent = Intent()
                    intent.setClass(this@MainActivity, WNaviGuideActivity::class.java)
                    startActivity(intent)
                }

                override fun onRoutePlanFail(error: WalkRoutePlanError) {
                    Log.d(TAG, "WalkNavi onRoutePlanFail")
                }
            })
    }

    /**
     * Android6.0之后需要动态申请权限
     */
    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= 23 && !isPermissionRequested) {
            isPermissionRequested = true
            val permissionsList = ArrayList<String>()
            val permissions = arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_MULTICAST_STATE
            )
            for (perm in permissions) {
                if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(perm)) {
                    permissionsList.add(perm)
                    // 进入到这里代表没有权限.
                }
            }
            if (permissionsList.isEmpty()) {
                return
            } else {
                requestPermissions(permissionsList.toTypedArray(), 0)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mMapView = findViewById<View>(R.id.mapview) as MapView
        initMapStatus()
        initButton()
        initSearch()
    }

    private fun initSearch() {
        // 初始化搜索模块，注册事件监听
        mSearch = RoutePlanSearch.newInstance()
        mSearch?.setOnGetRoutePlanResultListener(listener)
    }

    private var listener: OnGetRoutePlanResultListener = object : OnGetRoutePlanResultListener {
        var overlay: BikingRouteOverlay? = null

        override fun onGetWalkingRouteResult(p0: WalkingRouteResult?) {
        }

        override fun onGetTransitRouteResult(p0: TransitRouteResult?) {
        }

        override fun onGetMassTransitRouteResult(p0: MassTransitRouteResult?) {
        }

        override fun onGetDrivingRouteResult(p0: DrivingRouteResult?) {
        }

        override fun onGetIndoorRouteResult(p0: IndoorRouteResult?) {
        }

        override fun onGetBikingRouteResult(bikingRouteResult: BikingRouteResult) {
            //创建BikingRouteOverlay实例
            if (overlay == null) {
                overlay = BikingRouteOverlay(mBaiduMap)
            }
            if (bikingRouteResult.routeLines.size > 0) {
                overlay!!.removeFromMap()
                //获取路径规划数据,(以返回的第一条路线为例）
                //为BikingRouteOverlay实例设置数据
                overlay!!.setData(bikingRouteResult.routeLines[0])
                //在地图上绘制BikingRouteOverlay
                overlay!!.addToMap()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mMapView?.onPause()
    }

    override fun onResume() {
        super.onResume()
        mMapView?.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMapView?.onDestroy()
        bdStart.recycle()
        bdEnd.recycle()

        // 退出时销毁定位
        mLocClient?.stop()
        // 关闭定位图层
        mBaiduMap?.isMyLocationEnabled = false

        mSearch?.destroy()
    }
}