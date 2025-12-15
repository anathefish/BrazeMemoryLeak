package com.example.brazememoryleak.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.brazememoryleak.api.LaunchImage
import com.example.brazememoryleak.api.SpaceXApiClient

class SpaceXPagingSource(
    private val spaceXApiClient: SpaceXApiClient
) : PagingSource<Int, LaunchImage>() {

    override fun getRefreshKey(state: PagingState<Int, LaunchImage>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LaunchImage> {
        val page = params.key ?: 0
        val offset = page * params.loadSize

        return try {
            val launches = spaceXApiClient.getLaunches(
                limit = params.loadSize,
                offset = offset
            )
            LoadResult.Page(
                data = launches,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (launches.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
