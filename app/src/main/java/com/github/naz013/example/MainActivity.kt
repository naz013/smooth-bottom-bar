package com.github.naz013.example

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.naz013.smoothbottombar.SmoothBottomBar
import com.github.naz013.smoothbottombar.Tab

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<SmoothBottomBar>(R.id.bottomBar2).setTabs(createTabs4())
        findViewById<SmoothBottomBar>(R.id.bottomBar3).setTabs(createTabs5())

        findViewById<SmoothBottomBar>(R.id.bottomBar).setOnTabSelectedListener { showTab(it) }
        findViewById<SmoothBottomBar>(R.id.bottomBar2).setOnTabSelectedListener { showTab(it) }
        findViewById<SmoothBottomBar>(R.id.bottomBar3).setOnTabSelectedListener { showTab(it) }
    }

    @SuppressLint("SetTextI18n")
    private fun showTab(i: Int) {
        findViewById<TextView>(R.id.labelView).text = "Selected Tab - $i"
        Log.d("MainActivity", "onTabSelected: $i")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_more) {
            showGithub()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun createTabs5(): List<Tab> {
        return listOf(
            Tab(icon = R.drawable.ic_home, title = "Home"),
            Tab(icon = R.drawable.ic_inbox, title = "Inbox"),
            Tab(icon = R.drawable.ic_user_avatar, title = "Profile"),
            Tab(icon = R.drawable.ic_map, title = "Map"),
            Tab(icon = R.drawable.ic_gear, title = "Settings")
        )
    }

    private fun createTabs4(): List<Tab> {
        return listOf(
            Tab(icon = R.drawable.ic_home, title = "Home"),
            Tab(icon = R.drawable.ic_inbox, title = "Inbox"),
            Tab(icon = R.drawable.ic_user_avatar, title = "Profile"),
            Tab(icon = R.drawable.ic_map, title = "Map")
        )
    }

    private fun showGithub() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://github.com/naz013/smooth-bottom-bar")
        startActivity(intent)
    }
}
