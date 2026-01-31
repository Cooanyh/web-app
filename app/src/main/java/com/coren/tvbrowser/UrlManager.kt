package com.coren.tvbrowser

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray

/**
 * 网址管理器 - 使用SharedPreferences存储网址列表
 */
class UrlManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "tv_browser_prefs"
        private const val KEY_URLS = "saved_urls"
        private const val KEY_INITIALIZED = "initialized"
        
        // 预设网址
        val DEFAULT_URLS = listOf(
            UrlItem("WinLyrics 视频", "https://www.coren.xin/winlyrics/video"),
            UrlItem("TuneFlow 音乐", "https://upyun.coren.xin/web/tuneflow.html")
        )
    }
    
    init {
        // 首次启动时添加预设网址
        if (!prefs.getBoolean(KEY_INITIALIZED, false)) {
            saveUrls(DEFAULT_URLS)
            prefs.edit().putBoolean(KEY_INITIALIZED, true).apply()
        }
    }
    
    /**
     * 获取所有保存的网址
     */
    fun getUrls(): List<UrlItem> {
        val json = prefs.getString(KEY_URLS, "[]") ?: "[]"
        val urls = mutableListOf<UrlItem>()
        try {
            val jsonArray = JSONArray(json)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                urls.add(UrlItem(
                    name = obj.getString("name"),
                    url = obj.getString("url")
                ))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return urls
    }
    
    /**
     * 保存网址列表
     */
    fun saveUrls(urls: List<UrlItem>) {
        val jsonArray = JSONArray()
        urls.forEach { item ->
            val obj = org.json.JSONObject()
            obj.put("name", item.name)
            obj.put("url", item.url)
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_URLS, jsonArray.toString()).apply()
    }
    
    /**
     * 添加新网址
     */
    fun addUrl(item: UrlItem) {
        val urls = getUrls().toMutableList()
        urls.add(item)
        saveUrls(urls)
    }
    
    /**
     * 删除网址
     */
    fun removeUrl(position: Int) {
        val urls = getUrls().toMutableList()
        if (position in urls.indices) {
            urls.removeAt(position)
            saveUrls(urls)
        }
    }
    
    /**
     * 更新网址
     */
    fun updateUrl(position: Int, item: UrlItem) {
        val urls = getUrls().toMutableList()
        if (position in urls.indices) {
            urls[position] = item
            saveUrls(urls)
        }
    }
}

/**
 * 网址数据类
 */
data class UrlItem(
    val name: String,
    val url: String
)
