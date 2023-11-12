package com.example.baidumaps.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Process
import com.baidu.mapapi.model.LatLng
import java.sql.Timestamp
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Created by baidu on 17/1/23.
 */
object CommonUtil {
    private val df = DecimalFormat("######0.00")
    const val DISTANCE = 0.0001

    /**
     * 获取EntityName
     *
     * @return EntityName
     */
    const val entityName = "myTrace"
    fun getCurProcessName(context: Context): String {
        val pid = Process.myPid()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (appProcess in activityManager.runningAppProcesses) {
            if (appProcess.pid == pid) {
                return appProcess.processName
            }
        }
        return ""
    }

    val currentTime: Long
        /**
         * 获取当前时间戳(单位：秒)
         *
         * @return
         */
        get() = System.currentTimeMillis() / 1000

    /**
     * 校验double数值是否为0
     *
     * @param value
     *
     * @return
     */
    fun isEqualToZero(value: Double): Boolean {
        return if (Math.abs(value - 0.0) < 0.01) true else false
    }

    /**
     * 经纬度是否为(0,0)点
     *
     * @return
     */
    fun isZeroPoint(latitude: Double, longitude: Double): Boolean {
        return isEqualToZero(latitude) && isEqualToZero(longitude)
    }

    /**
     * 将字符串转为时间戳
     */
    fun toTimeStamp(time: String?): Long {
        val sdf = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.CHINA
        )
        val date: Date
        date = try {
            sdf.parse(time)
        } catch (e: ParseException) {
            e.printStackTrace()
            return 0
        }
        return date.time / 1000
    }

    /**
     * 获取时分秒
     *
     * @param timestamp 时间戳（单位：毫秒）
     *
     * @return
     */
    fun getHMS(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm:ss")
        try {
            return sdf.format(Timestamp(timestamp))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return timestamp.toString()
    }

    /**
     * 获取年月日 时分秒
     *
     * @param timestamp 时间戳（单位：毫秒）
     *
     * @return
     */
    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        try {
            return sdf.format(Timestamp(timestamp))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return timestamp.toString()
    }

    fun formatSecond(second: Int): String {
        val format = "%1$,02d:%2$,02d:%3$,02d"
        val hours = second / (60 * 60)
        val minutes = second / 60 - hours * 60
        val seconds = second - minutes * 60 - hours * 60 * 60
        val array = arrayOf<Any>(hours, minutes, seconds)
        return String.format(format, *array)
    }

    fun formatDouble(doubleValue: Double): String {
        return df.format(doubleValue)
    }

    /**
     * 计算x方向每次移动的距离
     */
    fun getXMoveDistance(slope: Double): Double {
        return if (slope == Double.MAX_VALUE) {
            DISTANCE
        } else Math.abs(
            DISTANCE * slope / Math.sqrt(
                1 + slope * slope
            )
        )
    }

    /**
     * 根据点和斜率算取截距
     */
    fun getInterception(slope: Double, point: LatLng): Double {
        return point.latitude - slope * point.longitude
    }

    /**
     * 算斜率
     */
    fun getSlope(fromPoint: LatLng, toPoint: LatLng): Double {
        return if (toPoint.longitude == fromPoint.longitude) {
            Double.MAX_VALUE
        } else (toPoint.latitude - fromPoint.latitude) / (toPoint.longitude - fromPoint.longitude)
    }

}