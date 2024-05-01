package com.example.groupfinalproject

import android.util.Base64
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SpotifyApiClient {
    private val BASE_URL = "https://accounts.spotify.com/"

    fun test() {
        // Initialize Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.spotify.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Example usage: generate token and get top genres
        val clientId = "8159a23628fd4715a195eef7926c0e1f"
        val clientSecret = "35449abf949340b0b2d1ca510ce96c5b"
        val authHeader = "Basic " + Base64.encodeToString("$clientId:$clientSecret".toByteArray(), Base64.NO_WRAP)
        val grantType = "client_credentials"

        val createAuthTokenService = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GenerateAuthToken::class.java)

        val call = createAuthTokenService.getToken(authHeader, grantType)

        call.enqueue(object : Callback<Token> {
            override fun onResponse(call: Call<Token>, response: Response<Token>) {
                if (response.isSuccessful) {
                    val token = response.body()?.access_token ?: ""
                    Log.d("SpotifyApiClient", "Token generated: $token")
                    // Once token is generated, you can make API calls using it
                    val getGenresApi = retrofit.create(GetGenres::class.java)
                    getTopGenres(getGenresApi, token)
                    val getPlaylistApi = retrofit.create(GetPlaylistFromCategory::class.java)
                    getPlaylistItems("Bearer $token", "3cEYpjA9oz9GiPac4AsH4n")
                } else {
                    Log.e("SpotifyApiClient", "Failed to generate token: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Token>, t: Throwable) {
                Log.e("SpotifyApiClient", "Failed to generate token", t)
            }
        })
    }

    private fun getTopGenres(api: GetGenres, accessToken: String) {
        // Make API call to get categories
        api.getCategories("Bearer $accessToken").enqueue(object : Callback<Genres> {
            override fun onResponse(call: Call<Genres>, response: Response<Genres>) {
                if (response.isSuccessful) {
                    val categories = response.body()?.categories?.items
                    categories?.let { categoryList ->
                        for (category in categoryList) {
                            // For each category, get the playlists
                            Log.d("SpotifyApiClient", "Name: ${category.name}; Id: ${category.id}")
                            getCategoryPlaylists(api, accessToken, category.id)
                        }
                    }
                } else {
                    Log.e("SpotifyApiClient", "Failed to get categories: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Genres>, t: Throwable) {
                Log.e("SpotifyApiClient", "Failed to get categories", t)
            }
        })
    }

    private fun getCategoryPlaylists(api: GetGenres, accessToken: String, categoryId: String) {
        // Make API call to get playlists for a category
        api.getCategoryPlaylists("Bearer $accessToken", categoryId).enqueue(object : Callback<Playlists> {
            override fun onResponse(call: Call<Playlists>, response: Response<Playlists>) {
                if (response.isSuccessful) {
                    val playlists = response.body()?.playlists?.items
                    playlists?.let { playlistList ->
                        // Calculate total tracks for each playlist
                        var totalTracks = 0
                        for (playlist in playlistList) {
                            totalTracks += playlist.tracks?.total ?: 0
                        }
                        Log.d("SpotifyApiClient", "Category: $categoryId, Total Tracks: $totalTracks")
                    }
                } else {
                    Log.e("SpotifyApiClient", "Failed to get playlists for category $categoryId: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<Playlists>, t: Throwable) {
                Log.e("SpotifyApiClient", "Failed to get playlists for category $categoryId", t)
            }
        })
    }

    private fun getPlaylistItems(accessToken: String, playlistId: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.spotify.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val getPlaylistApi = retrofit.create(GetPlaylistFromCategory::class.java)

        getPlaylistApi.getTracks(accessToken, playlistId).enqueue(object : Callback<PlaylistItems> {
            override fun onResponse(call: Call<PlaylistItems>, response: Response<PlaylistItems>) {
                if (response.isSuccessful) {
                    val playlistItems = response.body()
                    playlistItems?.items?.let { items ->
                        for (item in items) {
                            Log.d("SpotifyApiClient", "Track Name: ${item.track.name}")
                            for (artist in item.track.artists) {
                                Log.d("SpotifyApiClient", "Artist Name: ${artist.name}")
                            }
                        }
                    }
                } else {
                    Log.e("SpotifyApiClient", "Failed to get playlist items: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<PlaylistItems>, t: Throwable) {
                Log.e("SpotifyApiClient", "Failed to get playlist items", t)
            }
        })
    }
}