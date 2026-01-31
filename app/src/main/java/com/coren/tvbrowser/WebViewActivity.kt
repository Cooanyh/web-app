package com.coren.tvbrowser

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class WebViewActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_URL = "extra_url"
        const val EXTRA_TITLE = "extra_title"
    }

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 设置全屏
        setupFullscreen()
        
        setContentView(R.layout.activity_webview)

        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)

        val url = intent.getStringExtra(EXTRA_URL) ?: ""
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "网页"

        if (url.isEmpty()) {
            Toast.makeText(this, "网址无效", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupWebView()
        webView.loadUrl(url)
    }

    private fun setupFullscreen() {
        // 隐藏ActionBar
        supportActionBar?.hide()
        
        // 全屏沉浸式
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        
        // 保持屏幕常亮
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            // ========== 基础功能 ==========
            // 启用JavaScript（音乐播放必需）
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = false
            
            // DOM存储（网页功能必需）
            domStorageEnabled = true
            databaseEnabled = true
            
            // ========== 性能优化 - 针对低性能电视 ==========
            // 缓存策略：优先使用缓存减少网络请求
            cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            
            // 媒体设置
            mediaPlaybackRequiresUserGesture = false
            
            // 禁用缩放功能（电视不需要，减少计算）
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
            
            // 视口设置 - 简化渲染
            useWideViewPort = true
            loadWithOverviewMode = true
            
            // 文本缩放 - 电视大屏适配
            textZoom = 100
            
            // 混合内容
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            
            // 禁用不必要的功能以提升性能
            allowFileAccess = true
            allowContentAccess = true
            
            // 禁用地理位置（电视不需要）
            setGeolocationEnabled(false)
            
            // 图片加载策略
            loadsImagesAutomatically = true
            blockNetworkImage = false
            
            // 默认编码
            defaultTextEncodingName = "UTF-8"
            
            // 渲染优化
            @Suppress("DEPRECATION")
            setRenderPriority(WebSettings.RenderPriority.HIGH)
            
            // 启用平滑滚动
            @Suppress("DEPRECATION")
            setEnableSmoothTransition(true)
        }

        // ========== 渲染层设置 ==========
        // 对于低性能设备，使用软件渲染可能更稳定
        // 但先尝试硬件加速，如果还是卡可以改成 LAYER_TYPE_SOFTWARE
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        
        // 禁用滚动条（减少绘制）
        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false
        
        // 禁用过度滚动效果
        webView.overScrollMode = View.OVER_SCROLL_NEVER

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
                
                // 页面加载完成后注入CSS优化性能
                injectPerformanceCSS()
            }

            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                url?.let { view?.loadUrl(it) }
                return true
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                progressBar.progress = newProgress
            }
        }
    }
    
    /**
     * 注入CSS优化网页渲染性能
     */
    private fun injectPerformanceCSS() {
        val css = """
            * {
                -webkit-transform: translateZ(0);
                transform: translateZ(0);
            }
            *::-webkit-scrollbar {
                display: none !important;
            }
            body {
                -webkit-font-smoothing: antialiased;
                -webkit-overflow-scrolling: touch;
            }
        """.trimIndent().replace("\n", " ")
        
        val js = """
            (function() {
                var style = document.createElement('style');
                style.type = 'text/css';
                style.innerHTML = '$css';
                document.head.appendChild(style);
            })();
        """.trimIndent()
        
        webView.evaluateJavascript(js, null)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // 返回键处理：如果WebView可以后退则后退，否则退出Activity
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                webView.goBack()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
