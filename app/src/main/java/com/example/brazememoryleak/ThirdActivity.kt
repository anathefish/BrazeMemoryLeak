package com.example.brazememoryleak

import android.content.Intent
import android.os.Bundle
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
                            label = "Second Activity",
                            onClick = { startActivity(Intent(this, SecondActivity::class.java)) }
                        ),
                        NavigationAction(
                            label = "Back",
                            onClick = { finish() },
                            isPrimary = false
                        )
                    )
                )
            }
        }
    }
}
