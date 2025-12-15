package com.example.brazememoryleak

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.brazememoryleak.data.ActivityTag
import com.example.brazememoryleak.data.AppPreferences
import com.example.brazememoryleak.ui.screens.NavigationAction
import com.example.brazememoryleak.ui.screens.SpaceXGalleryScreen
import com.example.brazememoryleak.ui.theme.BrazeMemoryLeakTheme
import com.example.brazememoryleak.viewmodel.ThirdViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ThirdActivity : ComponentActivity() {

    private val viewModel: ThirdViewModel by viewModels()

    @Inject
    lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle stress test mode
        if (intent.getBooleanExtra(StressTestActivity.EXTRA_STRESS_TEST_MODE, false)) {
            handleStressTestMode()
            return
        }

        lifecycleScope.launch {
            appPreferences.setLastActivity(ActivityTag.THIRD)
        }

        setContent {
            val launches = viewModel.launches.collectAsLazyPagingItems()

            BrazeMemoryLeakTheme {
                SpaceXGalleryScreen(
                    title = "ThirdActivity",
                    launches = launches,
                    navigationActions = listOf(
                        NavigationAction(
                            label = "Main",
                            onClick = { startActivity(Intent(this, MainActivity::class.java)) }
                        ),
                        NavigationAction(
                            label = "Second",
                            onClick = { startActivity(Intent(this, SecondActivity::class.java)) }
                        ),
                        NavigationAction(
                            label = "Stress Test",
                            onClick = { startActivity(Intent(this, StressTestActivity::class.java)) },
                            isPrimary = false
                        )
                    )
                )
            }
        }
    }

    private fun handleStressTestMode() {
        val cycle = intent.getIntExtra(StressTestActivity.EXTRA_CURRENT_CYCLE, 0)
        val total = intent.getIntExtra(StressTestActivity.EXTRA_TOTAL_CYCLES, 0)

        Handler(Looper.getMainLooper()).postDelayed({
            if (!StressTestActivity.isTestRunning) {
                finish()
                return@postDelayed
            }

            // ThirdActivity is always the last in a cycle, so start next cycle
            val nextCycle = cycle + 1
            if (nextCycle <= total) {
                // Start next cycle with MainActivity
                val nextIntent = Intent(this, MainActivity::class.java).apply {
                    putExtra(StressTestActivity.EXTRA_STRESS_TEST_MODE, true)
                    putExtra(StressTestActivity.EXTRA_CURRENT_CYCLE, nextCycle)
                    putExtra(StressTestActivity.EXTRA_TOTAL_CYCLES, total)
                    putExtra(StressTestActivity.EXTRA_ACTIVITY_INDEX, 0)
                }
                startActivity(nextIntent)
            } else {
                // All cycles completed
                StressTestActivity.isTestRunning = false
                val nextIntent = Intent(this, StressTestActivity::class.java).apply {
                    putExtra(StressTestActivity.EXTRA_TEST_COMPLETED, true)
                    putExtra(StressTestActivity.EXTRA_COMPLETED_CYCLES, total)
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                startActivity(nextIntent)
            }
            finish()
        }, 200)
    }
}
