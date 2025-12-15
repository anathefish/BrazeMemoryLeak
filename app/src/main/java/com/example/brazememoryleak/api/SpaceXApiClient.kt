package com.example.brazememoryleak.api

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.example.brazememoryleak.graphql.GetLaunchesQuery
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GraphQL API client for SpaceX launches.
 * Uses Apollo to fetch launch data with images.
 */
@Singleton
class SpaceXApiClient @Inject constructor(
    private val apolloClient: ApolloClient
) {
    suspend fun getLaunches(limit: Int, offset: Int): List<LaunchImage> {
        val response = apolloClient.query(
            GetLaunchesQuery(
                limit = Optional.present(limit),
                offset = Optional.present(offset)
            )
        ).execute()

        return response.data?.launches?.flatMap { launch ->
            val missionName = launch?.mission_name ?: "Unknown Mission"
            val launchDate = launch?.launch_date_utc ?: ""
            val details = launch?.details ?: ""

            // Get all flickr images for this launch
            val flickrImages = launch?.links?.flickr_images?.filterNotNull() ?: emptyList()

            // If no flickr images, use mission patch
            if (flickrImages.isEmpty()) {
                val patchUrl = launch?.links?.mission_patch ?: launch?.links?.mission_patch_small
                if (patchUrl != null) {
                    listOf(
                        LaunchImage(
                            id = "${launch?.id}_patch",
                            imageUrl = patchUrl,
                            missionName = missionName,
                            launchDate = launchDate,
                            details = details
                        )
                    )
                } else {
                    emptyList()
                }
            } else {
                flickrImages.mapIndexed { index, url ->
                    LaunchImage(
                        id = "${launch?.id}_$index",
                        imageUrl = url,
                        missionName = missionName,
                        launchDate = launchDate,
                        details = details
                    )
                }
            }
        } ?: emptyList()
    }
}

data class LaunchImage(
    val id: String,
    val imageUrl: String,
    val missionName: String,
    val launchDate: String,
    val details: String
)
