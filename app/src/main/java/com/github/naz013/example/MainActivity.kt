package com.github.naz013.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.github.naz013.smoothbottombar.SmoothBottomBar
import com.github.naz013.smoothbottombar.Tab

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomBar = findViewById<SmoothBottomBar>(R.id.bottomBar)
        bottomBar.setOnTabSelectedListener {
            Log.d("MainActivity", "onTabSelected: $it")
        }
//        bottomBar.setTabs(createTabs())
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

    private fun createTabs(): List<Tab> {
        return listOf(
            Tab(icon = R.drawable.ic_home, title = "Home"),
            Tab(icon = R.drawable.ic_inbox, title = "Inbox"),
            Tab(icon = R.drawable.ic_user_avatar, title = "Profile")
        )
    }

    private fun showGithub() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://github.com/naz013/smooth-bottom-bar")
        startActivity(intent)
    }
}
