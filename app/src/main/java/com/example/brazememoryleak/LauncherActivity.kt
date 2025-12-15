package com.example.brazememoryleak

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.brazememoryleak.data.ActivityTag
import com.example.brazememoryleak.data.AppPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LauncherActivity : ComponentActivity() {

    @Inject
    lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val lastActivity = appPreferences.getLastActivity()

            val targetClass = when (lastActivity) {
                ActivityTag.MAIN -> MainActivity::class.java
                ActivityTag.SECOND -> SecondActivity::class.java
                ActivityTag.THIRD -> ThirdActivity::class.java
            }

            startActivity(Intent(this@LauncherActivity, targetClass))
            finish()
        }
    }
}
