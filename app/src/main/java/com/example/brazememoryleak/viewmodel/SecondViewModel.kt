package com.example.brazememoryleak.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.braze.Braze
import com.example.brazememoryleak.api.GraphQLApiClient
import com.example.brazememoryleak.api.ImageApiService
import com.example.brazememoryleak.api.PicsumImage
import com.example.brazememoryleak.api.RestApiClient
import com.example.brazememoryleak.api.SampleApiService
import com.example.brazememoryleak.paging.ImagePagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class SecondViewModel @Inject constructor(
    val braze: Braze,
    val restApiClient: RestApiClient,
    val graphQLApiClient: GraphQLApiClient,
    val sampleApiService: SampleApiService,
    private val imageApiService: ImageApiService
) : ViewModel() {

    val images: Flow<PagingData<PicsumImage>> = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            enablePlaceholders = false,
            prefetchDistance = 5
        ),
        pagingSourceFactory = { ImagePagingSource(imageApiService) }
    ).flow.cachedIn(viewModelScope)

    companion object {
        private const val PAGE_SIZE = 20
    }
}
