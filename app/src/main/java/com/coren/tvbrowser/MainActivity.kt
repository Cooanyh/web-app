package com.coren.tvbrowser

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var urlManager: UrlManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UrlAdapter
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var emptyView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 设置全屏
        setupFullscreen()
        
        setContentView(R.layout.activity_main)

        urlManager = UrlManager(this)
        
        recyclerView = findViewById(R.id.recyclerView)
        fabAdd = findViewById(R.id.fabAdd)
        emptyView = findViewById(R.id.emptyView)

        setupRecyclerView()
        setupFab()
        
        loadUrls()
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

    private fun setupRecyclerView() {
        // 使用网格布局，每行显示3个
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        adapter = UrlAdapter(
            onItemClick = { item -> openUrl(item) },
            onItemLongClick = { position -> showEditDeleteDialog(position) },
            onDeleteClick = { position -> confirmDelete(position) }
        )
        recyclerView.adapter = adapter
        
        // 让RecyclerView可以获取焦点（遥控器导航）
        recyclerView.isFocusable = true
        recyclerView.isFocusableInTouchMode = true
    }

    private fun setupFab() {
        fabAdd.setOnClickListener {
            showAddUrlDialog()
        }
    }

    private fun loadUrls() {
        val urls = urlManager.getUrls()
        adapter.submitList(urls)
        
        // 显示/隐藏空视图
        if (urls.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun openUrl(item: UrlItem) {
        val intent = Intent(this, WebViewActivity::class.java)
        intent.putExtra(WebViewActivity.EXTRA_URL, item.url)
        intent.putExtra(WebViewActivity.EXTRA_TITLE, item.name)
        startActivity(intent)
    }

    private fun showAddUrlDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_url, null)
        val editName = dialogView.findViewById<EditText>(R.id.editName)
        val editUrl = dialogView.findViewById<EditText>(R.id.editUrl)
        
        editUrl.setText("https://")

        AlertDialog.Builder(this, R.style.Theme_TVBrowser_Dialog)
            .setTitle("添加网址")
            .setView(dialogView)
            .setPositiveButton("添加") { _, _ ->
                val name = editName.text.toString().trim()
                val url = editUrl.text.toString().trim()
                
                if (name.isEmpty()) {
                    Toast.makeText(this, "请输入名称", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (url.isEmpty() || url == "https://") {
                    Toast.makeText(this, "请输入网址", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                urlManager.addUrl(UrlItem(name, url))
                loadUrls()
                Toast.makeText(this, "已添加: $name", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showEditDeleteDialog(position: Int) {
        val urls = urlManager.getUrls()
        if (position !in urls.indices) return
        
        val item = urls[position]
        
        AlertDialog.Builder(this, R.style.Theme_TVBrowser_Dialog)
            .setTitle(item.name)
            .setItems(arrayOf("编辑", "删除")) { _, which ->
                when (which) {
                    0 -> showEditDialog(position, item)
                    1 -> confirmDelete(position)
                }
            }
            .show()
    }

    private fun showEditDialog(position: Int, item: UrlItem) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_url, null)
        val editName = dialogView.findViewById<EditText>(R.id.editName)
        val editUrl = dialogView.findViewById<EditText>(R.id.editUrl)
        
        editName.setText(item.name)
        editUrl.setText(item.url)

        AlertDialog.Builder(this, R.style.Theme_TVBrowser_Dialog)
            .setTitle("编辑网址")
            .setView(dialogView)
            .setPositiveButton("保存") { _, _ ->
                val name = editName.text.toString().trim()
                val url = editUrl.text.toString().trim()
                
                if (name.isNotEmpty() && url.isNotEmpty()) {
                    urlManager.updateUrl(position, UrlItem(name, url))
                    loadUrls()
                    Toast.makeText(this, "已更新", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun confirmDelete(position: Int) {
        val urls = urlManager.getUrls()
        if (position !in urls.indices) return
        
        val item = urls[position]
        
        AlertDialog.Builder(this, R.style.Theme_TVBrowser_Dialog)
            .setTitle("确认删除")
            .setMessage("确定要删除 \"${item.name}\" 吗？")
            .setPositiveButton("删除") { _, _ ->
                urlManager.removeUrl(position)
                loadUrls()
                Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // 处理遥控器菜单键
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            showAddUrlDialog()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}

/**
 * 网址列表适配器
 */
class UrlAdapter(
    private val onItemClick: (UrlItem) -> Unit,
    private val onItemLongClick: (Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<UrlAdapter.ViewHolder>() {

    private var items: List<UrlItem> = emptyList()

    fun submitList(list: List<UrlItem>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_url, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textName: TextView = itemView.findViewById(R.id.textName)
        private val textUrl: TextView = itemView.findViewById(R.id.textUrl)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(item: UrlItem, position: Int) {
            textName.text = item.name
            textUrl.text = item.url
            
            // 点击打开网址
            itemView.setOnClickListener { onItemClick(item) }
            
            // 长按编辑/删除
            itemView.setOnLongClickListener {
                onItemLongClick(position)
                true
            }
            
            // 删除按钮
            btnDelete.setOnClickListener { onDeleteClick(position) }
            
            // 设置可聚焦（遥控器导航）
            itemView.isFocusable = true
            itemView.isFocusableInTouchMode = true
        }
    }
}
