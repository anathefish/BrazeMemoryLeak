package com.example.brazememoryleak

import android.content.Intent
import android.os.Bundle
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
 * Stress test activity that rapidly creates and destroys activities
 * to reproduce the Braze memory leak.
 *
 * The test cycles through MainActivity -> SecondActivity -> ThirdActivity
 * multiple times to accumulate leaked Activity references in Braze's
 * internal LinkedBlockingQueue.
 */
@AndroidEntryPoint
class StressTestActivity : ComponentActivity() {

    private var currentCycle by mutableIntStateOf(0)
    private var totalCycles by mutableIntStateOf(DEFAULT_CYCLES)
    private var isRunning by mutableStateOf(false)
    private var statusMessage by mutableStateOf("Ready to start stress test")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if we're in the middle of a stress test
        val cycleFromIntent = intent.getIntExtra(EXTRA_CURRENT_CYCLE, -1)
        val totalFromIntent = intent.getIntExtra(EXTRA_TOTAL_CYCLES, -1)
        val activityIndex = intent.getIntExtra(EXTRA_ACTIVITY_INDEX, -1)

        if (cycleFromIntent >= 0 && activityIndex >= 0 && isTestRunning) {
            currentCycle = cycleFromIntent
            totalCycles = totalFromIntent
            isRunning = true
            statusMessage = "Cycle $currentCycle/$totalCycles - Activity ${activityIndex + 1}/3"
        }

        setContent {
            BrazeMemoryLeakTheme {
                StressTestScreen(
                    currentCycle = currentCycle,
                    totalCycles = totalCycles,
                    isRunning = isRunning,
                    statusMessage = statusMessage,
                    onStartTest = { cycles -> startStressTest(cycles) },
                    onNavigateToBackgroundTest = {
                        startActivity(Intent(this, BackgroundStressTestActivity::class.java))
                    }
                )
            }
        }
    }

    private fun startStressTest(cycles: Int) {
        Log.d(TAG, "Starting stress test with $cycles cycles")
        currentCycle = 1
        totalCycles = cycles
        isRunning = true
        isTestRunning = true
        statusMessage = "Starting cycle 1/$cycles"

        // Start with MainActivity
        launchActivityInChain(0, 1, cycles)
    }

    private fun launchActivityInChain(activityIndex: Int, cycle: Int, total: Int) {
        if (!isRunning || !isTestRunning) {
            Log.d(TAG, "Test stopped, not launching next activity")
            return
        }

        val targetClass = when (activityIndex) {
            0 -> MainActivity::class.java
            1 -> SecondActivity::class.java
            2 -> ThirdActivity::class.java
            else -> null
        }

        if (targetClass != null) {
            Log.d(TAG, "Launching ${targetClass.simpleName} (cycle $cycle, activity ${activityIndex + 1})")
            val intent = Intent(this, targetClass).apply {
                putExtra(EXTRA_STRESS_TEST_MODE, true)
                putExtra(EXTRA_CURRENT_CYCLE, cycle)
                putExtra(EXTRA_TOTAL_CYCLES, total)
                putExtra(EXTRA_ACTIVITY_INDEX, activityIndex)
            }
            startActivity(intent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        if (intent.getBooleanExtra(EXTRA_TEST_COMPLETED, false)) {
            val completedCycles = intent.getIntExtra(EXTRA_COMPLETED_CYCLES, 0)
            isRunning = false
            currentCycle = completedCycles
            statusMessage = "Completed $completedCycles cycles! Check LeakCanary or Memory Profiler."
        }
    }

    companion object {
        private const val TAG = "StressTest"
        private const val DEFAULT_CYCLES = 20
        private const val DELAY_BETWEEN_ACTIVITIES = 300L // ms

        const val EXTRA_STRESS_TEST_MODE = "stress_test_mode"
        const val EXTRA_CURRENT_CYCLE = "current_cycle"
        const val EXTRA_TOTAL_CYCLES = "total_cycles"
        const val EXTRA_ACTIVITY_INDEX = "activity_index"
        const val EXTRA_TEST_COMPLETED = "test_completed"
        const val EXTRA_COMPLETED_CYCLES = "completed_cycles"

        // Shared flag to allow stopping test from any activity
        @Volatile
        var isTestRunning = false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StressTestScreen(
    currentCycle: Int,
    totalCycles: Int,
    isRunning: Boolean,
    statusMessage: String,
    onStartTest: (Int) -> Unit,
    onNavigateToBackgroundTest: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Memory Leak Stress Test") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    titleContentColor = MaterialTheme.colorScheme.onError
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
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                        text = "How to use:",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. Start the stress test (20-50 cycles recommended)\n" +
                                "2. Wait for completion\n" +
                                "3. Check LeakCanary notification or Memory Profiler\n" +
                                "4. Look for retained Activity instances",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Start Test Buttons (only when not running)
            if (!isRunning) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onStartTest(20) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("20 Cycles")
                    }
                    Button(
                        onClick = { onStartTest(50) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("50 Cycles")
                    }
                }

                Button(
                    onClick = { onStartTest(100) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("100 Cycles (Intensive)")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onNavigateToBackgroundTest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Background/Foreground Test")
                }
            }
        }
    }
}
