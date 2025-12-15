package com.example.brazememoryleak.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.brazememoryleak.api.ImageApiService
import com.example.brazememoryleak.api.PicsumImage

class ImagePagingSource(
    private val imageApiService: ImageApiService
) : PagingSource<Int, PicsumImage>() {

    override fun getRefreshKey(state: PagingState<Int, PicsumImage>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PicsumImage> {
        val page = params.key ?: STARTING_PAGE_INDEX
        return try {
            val images = imageApiService.getImages(
                page = page,
                limit = params.loadSize
            )
            LoadResult.Page(
                data = images,
                prevKey = if (page == STARTING_PAGE_INDEX) null else page - 1,
                nextKey = if (images.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    companion object {
        private const val STARTING_PAGE_INDEX = 1
    }
}
