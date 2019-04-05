package com.github.naz013.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.naz013.smoothbottombar.SmoothBottomBar
import com.github.naz013.smoothbottombar.Tab

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomBar = findViewById<SmoothBottomBar>(R.id.bottomBar)
        bottomBar.setTabs(createTabs())
    }

    private fun createTabs(): List<Tab> {
        return listOf(
            Tab(icon = R.drawable.ic_home, title = "Home"),
            Tab(icon = R.drawable.ic_inbox, title = "Inbox"),
            Tab(icon = R.drawable.ic_user_avatar, title = "Profile")
        )
    }
}
