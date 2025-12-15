package com.example.brazememoryleak.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.braze.Braze
import com.example.brazememoryleak.api.GraphQLApiClient
import com.example.brazememoryleak.api.LaunchImage
import com.example.brazememoryleak.api.RestApiClient
import com.example.brazememoryleak.api.SampleApiService
import com.example.brazememoryleak.api.SpaceXApiClient
import com.example.brazememoryleak.paging.SpaceXPagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class ThirdViewModel @Inject constructor(
    val braze: Braze,
    val restApiClient: RestApiClient,
    val graphQLApiClient: GraphQLApiClient,
    val sampleApiService: SampleApiService,
    private val spaceXApiClient: SpaceXApiClient
) : ViewModel() {

    val launches: Flow<PagingData<LaunchImage>> = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            enablePlaceholders = false,
            prefetchDistance = 5
        ),
        pagingSourceFactory = { SpaceXPagingSource(spaceXApiClient) }
    ).flow.cachedIn(viewModelScope)

    companion object {
        private const val PAGE_SIZE = 10
    }
}
