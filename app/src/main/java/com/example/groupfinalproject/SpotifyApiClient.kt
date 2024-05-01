package com.example.groupfinalproject


import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SpotifyApiClient {

    private val BASE_URL = "https://accounts.spotify.com/"

    suspend fun generateToken(): String {
        val clientId = "8159a23628fd4715a195eef7926c0e1f"
        val clientSecret = "35449abf949340b0b2d1ca510ce96c5b"
        val authHeader = "Basic " + Base64.encodeToString(
            "$clientId:$clientSecret".toByteArray(),
            Base64.NO_WRAP
        )
        val grantType = "client_credentials"

        val createAuthTokenService = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GenerateAuthToken::class.java)

        return withContext(Dispatchers.IO) {
            try {
                val response = createAuthTokenService.getToken(authHeader, grantType).execute()
                if (response.isSuccessful) {
                    response.body()?.access_token ?: throw RuntimeException("Token not found")
                } else {
                    throw RuntimeException("Failed to generate token: ${response.code()}")
                }
            } catch (e: Exception) {
                throw RuntimeException("Failed to generate token", e)
            }
        }
    }

    suspend fun getTopGenres(token: String): List<String> {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.spotify.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val getGenresApi = retrofit.create(GetGenres::class.java)

        return withContext(Dispatchers.IO) {
            try {
                val response = getGenresApi.getCategories("Bearer $token").execute()
                if (response.isSuccessful) {
                    response.body()?.categories?.items?.mapNotNull { it.name } ?: emptyList()
                } else {
                    throw RuntimeException("Failed to get categories: ${response.code()}")
                }
            } catch (e: Exception) {
                throw RuntimeException("Failed to get categories", e)
            }
        }
    }

    suspend fun getPlaylistItems(accessToken: String, playlistId: String): Map<Int, List<Pair<String, String>>> {
        val yearToSongArtistsMap = mutableMapOf<Int, MutableList<Pair<String, String>>>()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.spotify.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val getPlaylistApi = retrofit.create(GetPlaylistFromCategory::class.java)

        return withContext(Dispatchers.IO) {
            try {
                val response = getPlaylistApi.getTracks("Bearer $accessToken", playlistId).execute()
                if (response.isSuccessful) {
                    response.body()?.items?.forEach { item ->
                        //Log.d("Whooo", "$item.track")
                        val releaseYear = item.track.album.release_date.substring(0, 4)?.toIntOrNull()
                        //Log.d("Whooo", releaseYear.toString())
                        item.track.artists.forEach { artist ->
                            releaseYear?.let {
                                val songArtistPair = Pair(item.track.name, artist.name)
                                val songArtistsList = yearToSongArtistsMap.getOrPut(releaseYear) { mutableListOf() }
                                songArtistsList.add(songArtistPair)
                            }
                        }
                    }
                    Log.d("whoooo", "$yearToSongArtistsMap")
                    yearToSongArtistsMap
                } else {
                    throw RuntimeException("Failed to get playlist items: ${response.code()}")
                }
            } catch (e: Exception) {
                throw RuntimeException("Failed to get playlist items", e)
            }
        }
    }

    fun getSongs(map: Map<Int, List<Pair<String, String>>>, year: Int): Set<String> {
        return (map[year]?.map { it.first } ?: emptyList()).toSet()
    }

    fun getArtists(map: Map<Int, List<Pair<String, String>>>, year: Int): Set<String> {
        return (map[year]?.map { it.second } ?: emptyList()).toSet()
    }

    fun getYears(map: Map<Int, List<Pair<String, String>>>): Array<String> {
        val yearSongCountMap = mutableMapOf<Int, Int>()
        for ((year, songArtistsList) in map) {
            yearSongCountMap[year] = songArtistsList.size
        }
        val sortedYears = yearSongCountMap.entries.sortedByDescending { it.value }
        val topYears = sortedYears.take(6)

        return topYears.map { it.key.toString() }.toTypedArray()
    }

    fun getRandomSongArtistPair(map: Map<Int, List<Pair<String, String>>>, year: Int): Pair<String, String>? {

        val songArtistsList = map[year]
        if (songArtistsList != null && songArtistsList.isNotEmpty()) {
            val randomIndex = (0 until songArtistsList.size).random()
            return songArtistsList[randomIndex]
        }

        return null
    }




    suspend fun filterArtistsByYear(token: String, artists: List<String>, year: Int): List<String> {
        val filteredArtists = mutableSetOf<String>()
        Log.d("SpotifyApiClient", "These are the top artists with songs released in $year")
        Log.d("Hope", "$artists")

        return withContext(Dispatchers.IO) {
            artists.forEach { artistName ->
                searchForArtist(token, artistName, year, filteredArtists)
            }
            filteredArtists.toList()
        }
    }

    suspend fun searchForArtist(
        token: String,
        artistName: String,
        year: Int,
        filteredArtists: MutableSet<String>
    ) {
        withContext(Dispatchers.IO) {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.spotify.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val searchArtistApi = retrofit.create(SearchArtistApi::class.java)

            try {
                val response =
                    searchArtistApi.searchForArtist("Bearer $token", artistName, "artist", 1)
                        .execute()
                if (response.isSuccessful) {
                    response.body()?.artists?.items?.forEach { artist ->
                        getTopTracksByArtist(
                            "Bearer $token",
                            artist.id,
                            artistName,
                            year,
                            filteredArtists
                        )
                    }
                } else {
                    throw RuntimeException("Failed to search for artist: ${response.code()}")
                }
            } catch (e: Exception) {
                throw RuntimeException("Failed to search for artist", e)
            }
        }
    }


    private suspend fun getTopTracksByArtist(token: String, artistId: String, artistName: String, year: Int, filteredArtists: MutableSet<String>) {
        withContext(Dispatchers.IO) {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.spotify.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val artistTopTracksApi = retrofit.create(ArtistTopTracksApi::class.java)

            try {
                val response = artistTopTracksApi.getSongsByArtist(token, artistId, "US").execute()
                if (response.isSuccessful) {
                    val tracks = response.body()?.tracks
                    tracks?.let { trackList ->
                        // Check if the artist has any track released in the specified year
                        val hasYearTrack = trackList.any { track ->
                            val releaseYear = track.album.release_date?.substring(0, 4)?.toIntOrNull()
                            releaseYear == year
                        }
                        if (hasYearTrack) {
                            // Add artist to filteredArtists if size is less than 8
                            if (filteredArtists.size < 8) {
                                filteredArtists.add(artistName)
                            }
                        }
                    }
                } else {
                    throw RuntimeException("Failed to get top tracks by artist: ${response.code()}")
                }
            } catch (e: Exception) {
                throw RuntimeException("Failed to get top tracks by artist", e)
            }
        }
    }



}