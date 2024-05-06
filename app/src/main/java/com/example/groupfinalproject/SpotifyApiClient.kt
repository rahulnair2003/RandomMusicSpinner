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

    suspend fun getPlaylistItems(accessToken: String, playlistId: String): Map<String, List<Triple<String, String, String>>> {
        val decadeToSongArtistsMap = mutableMapOf<String, MutableList<Triple<String, String, String>>>()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.spotify.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val getPlaylistApi = retrofit.create(GetPlaylistFromCategory::class.java)
        Log.d("Test", "$playlistId")

        return withContext(Dispatchers.IO) {
            try {
                val response = getPlaylistApi.getTracks("Bearer $accessToken", playlistId).execute()
                if (response.isSuccessful) {
                    response.body()?.items?.forEach { item ->
                        val releaseYear = item.track.album.release_date?.substring(0, 4)?.toIntOrNull()
                        releaseYear?.let { year ->
                            val decade = getDecade(year)
                            item.track.artists.forEach { artist ->
                                val imageURL = item.track.album.images.firstOrNull()?.url ?: "https://static.vecteezy.com/system/resources/thumbnails/001/840/618/small/picture-profile-icon-male-icon-human-or-people-sign-and-symbol-free-vector.jpg"
                                val songArtistImageTriple = Triple(item.track.name, artist.name, imageURL)
                                val songArtistsList = decadeToSongArtistsMap.getOrPut(decade) { mutableListOf() }
                                songArtistsList.add(songArtistImageTriple)
                            }
                        }
                    }
                    decadeToSongArtistsMap
                } else {
                    throw RuntimeException("Failed to get playlist items: ${response.code()}")
                }
            } catch (e: Exception) {
                throw RuntimeException("Failed to get playlist items", e)
            }
        }
    }


    private fun getDecade(year: Int): String {
        return when (year) {
            in 1970..1979 -> "1970"
            in 1980..1989 -> "1980"
            in 1990..1999 -> "1990"
            in 2000..2009 -> "2000"
            in 2010..2019 -> "2010"
            in 2020..2029 -> "2020"
            else -> throw IllegalArgumentException("Invalid year: $year")
        }
    }


    fun getSongs(map: Map<String, List<Triple<String, String, String>>>, year: String): Set<String> {
        return (map[year]?.map { it.first } ?: emptyList()).toSet()
    }

    fun getArtists(map: Map<String, List<Triple<String, String, String>>>, year: String): Set<String> {
        return (map[year]?.map { it.second } ?: emptyList()).toSet()
    }

    fun getRandomSongArtistPair(map: Map<String, List<Triple<String, String, String>>>, year: String): Triple<String, String, String>? {
        val songArtistsList = map[year]
        if (!songArtistsList.isNullOrEmpty()) {
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