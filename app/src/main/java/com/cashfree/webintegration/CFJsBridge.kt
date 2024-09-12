package com.cashfree.webintegration

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.util.Log
import android.webkit.JavascriptInterface
import org.json.JSONArray
import org.json.JSONObject

class CFJsBridge constructor(private val context: Context) {

    @JavascriptInterface
    fun getAppList(name: String?): String {
        val intent = Intent()
        intent.setAction(Intent.ACTION_VIEW)
        intent.setData(Uri.parse(name))
        val pm: PackageManager = context.packageManager
        val resInfo: List<ResolveInfo> = pm.queryIntentActivities(intent, 0)
        val packageNames = JSONArray()
        try {
            for (info in resInfo) {
                val appInfo = JSONObject()
                appInfo.put(
                    "appName",
                    context.packageManager.getApplicationLabel(info.activityInfo.applicationInfo) as String
                )
                appInfo.put("appPackage", info.activityInfo.packageName)
                packageNames.put(appInfo)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return packageNames.toString()
    }

    @JavascriptInterface
    fun openApp(upiClientPackage: String?, upiURL: String?): Boolean {
        val intent = Intent()
        intent.setAction(Intent.ACTION_VIEW)
        intent.setData(Uri.parse(upiURL))
        val pm: PackageManager = context.packageManager
        val resInfo = pm.queryIntentActivities(intent, 0)
        var foundPackageFlag = false
        var upiClientResolveInfo: ResolveInfo? = null
        for (info in resInfo) {
            if (info.activityInfo.packageName == upiClientPackage) {
                foundPackageFlag = true
                upiClientResolveInfo = info
                break
            }
        }
        try {
            if (foundPackageFlag) {
                intent.setClassName(
                    upiClientResolveInfo!!.activityInfo.packageName,
                    upiClientResolveInfo.activityInfo.name
                )
                if (context is MainActivity) {
                    context.startActivityForResult(intent, 1000)
                }
            }
        } catch (exception: java.lang.Exception) {
            Log.d("Exception UPI app", "${exception.message}")
        }
        return true
    }
}