package com.example.brazememoryleak

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.brazememoryleak.ui.theme.BrazeMemoryLeakTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Stress test activity that repeatedly sends the app to background and foreground
 * to reproduce Braze memory leaks related to lifecycle callbacks.
 *
 * This test uses moveTaskToBack() to simulate the user pressing the home button,
 * then brings the app back to foreground after a delay.
 */
@AndroidEntryPoint
class BackgroundStressTestActivity : ComponentActivity() {

    private var currentCycle by mutableIntStateOf(0)
    private var totalCycles by mutableIntStateOf(DEFAULT_CYCLES)
    private var isRunning by mutableStateOf(false)
    private var statusMessage by mutableStateOf("Ready to start background/foreground test")
    private var isInBackground by mutableStateOf(false)

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BrazeMemoryLeakTheme {
                BackgroundStressTestScreen(
                    currentCycle = currentCycle,
                    totalCycles = totalCycles,
                    isRunning = isRunning,
                    isInBackground = isInBackground,
                    statusMessage = statusMessage,
                    onStartTest = { cycles -> startBackgroundTest(cycles) },
                    onStopTest = { stopTest() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isRunning && isInBackground) {
            isInBackground = false
            Log.d(TAG, "Resumed from background, cycle $currentCycle/$totalCycles")
            statusMessage = "Cycle $currentCycle/$totalCycles - Back to foreground"

            // Schedule next background cycle
            handler.postDelayed({
                if (isRunning) {
                    currentCycle++
                    if (currentCycle <= totalCycles) {
                        goToBackground()
                    } else {
                        completeTest()
                    }
                }
            }, FOREGROUND_DURATION)
        }
    }

    override fun onStop() {
        super.onStop()
        if (isRunning && !isInBackground) {
            // We went to background unexpectedly (user pressed home manually)
            isInBackground = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        isTestRunning = false
    }

    private fun startBackgroundTest(cycles: Int) {
        Log.d(TAG, "Starting background stress test with $cycles cycles")
        currentCycle = 1
        totalCycles = cycles
        isRunning = true
        isTestRunning = true
        statusMessage = "Starting cycle 1/$cycles - Going to background..."

        // Start first background cycle after a short delay
        handler.postDelayed({
            if (isRunning) {
                goToBackground()
            }
        }, 500)
    }

    private fun goToBackground() {
        if (!isRunning) return

        Log.d(TAG, "Going to background, cycle $currentCycle/$totalCycles")
        statusMessage = "Cycle $currentCycle/$totalCycles - In background..."
        isInBackground = true

        // Move task to back (simulates pressing home button)
        moveTaskToBack(true)

        // Schedule return to foreground
        handler.postDelayed({
            if (isRunning) {
                bringToForeground()
            }
        }, BACKGROUND_DURATION)
    }

    private fun bringToForeground() {
        if (!isRunning) return

        Log.d(TAG, "Bringing back to foreground, cycle $currentCycle/$totalCycles")

        // Bring activity back to foreground
        val intent = Intent(this, BackgroundStressTestActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
    }

    private fun completeTest() {
        Log.d(TAG, "Background stress test completed after $totalCycles cycles")
        isRunning = false
        isTestRunning = false
        statusMessage = "Completed $totalCycles cycles! Check LeakCanary or Memory Profiler."
    }

    private fun stopTest() {
        Log.d(TAG, "Stopping background stress test")
        handler.removeCallbacksAndMessages(null)
        isRunning = false
        isTestRunning = false
        isInBackground = false
        statusMessage = "Test stopped at cycle $currentCycle/$totalCycles"
    }

    companion object {
        private const val TAG = "BgStressTest"
        private const val DEFAULT_CYCLES = 20
        private const val BACKGROUND_DURATION = 2000L // Time spent in background (ms)
        private const val FOREGROUND_DURATION = 1000L // Time spent in foreground (ms)

        @Volatile
        var isTestRunning = false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BackgroundStressTestScreen(
    currentCycle: Int,
    totalCycles: Int,
    isRunning: Boolean,
    isInBackground: Boolean,
    statusMessage: String,
    onStartTest: (Int) -> Unit,
    onStopTest: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Background/Foreground Test") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    titleContentColor = MaterialTheme.colorScheme.onTertiary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isInBackground)
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (isRunning) {
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { currentCycle.toFloat() / totalCycles.toFloat() },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "$currentCycle / $totalCycles cycles",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Instructions
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "How it works:",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. The app will automatically go to background\n" +
                                "2. After ${BACKGROUND_DURATION}ms it returns to foreground\n" +
                                "3. This triggers Braze lifecycle callbacks each time\n" +
                                "4. Repeat for specified number of cycles\n" +
                                "5. Check LeakCanary or Memory Profiler after completion",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Buttons
            if (!isRunning) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onStartTest(10) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("10 Cycles")
                    }
                    Button(
                        onClick = { onStartTest(20) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("20 Cycles")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onStartTest(50) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("50 Cycles")
                    }
                    Button(
                        onClick = { onStartTest(100) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("100 Cycles")
                    }
                }
            } else {
                Button(
                    onClick = onStopTest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Stop Test")
                }
            }
        }
    }
}

private const val BACKGROUND_DURATION = 2000L
