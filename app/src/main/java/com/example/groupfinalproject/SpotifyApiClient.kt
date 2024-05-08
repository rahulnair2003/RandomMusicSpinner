package com.example.groupfinalproject


import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SpotifyApiClient {

    private val BASE_URL = "https://accounts.spotify.com/"
    val playlistIds = mapOf(
        "rap" to mapOf("1970" to "37i9dQZF1EIee1TiRnBd3C", "1980" to "37i9dQZF1DX2XmsXL2WBQd", "1990" to "37i9dQZF1DX186v583rmzp", "2000" to "37i9dQZF1DX1lHW2vbQwNN", "2010" to "37i9dQZF1DX97h7ftpNSYT", "2020" to "37i9dQZF1EIezQcATIWbSB"),
        "pop" to mapOf("1970" to "37i9dQZF1EIg0r197lDGql", "1980" to "49PAThhKRCCTXeydvq9uAp", "1990" to "37i9dQZF1DWVcJK7WY4M52", "2000" to "5asIusKloOLOILpjhwjgPH", "2010" to "3FeewjLi5LMzIpV4h35QEz", "2020" to "7bfyBCaVhnd8OywuUVKlhN"),
        "country" to mapOf("1970" to "37i9dQZF1DWYP5PUsVbso9", "1980" to "37i9dQZF1DX6RCydf9ytsj", "1990" to "37i9dQZF1DWVpjAJGB70vU", "2000" to "37i9dQZF1DXdxUH6sNtcDe", "2010" to "0wqUVPa19eClnNClEMQQoY", "2020" to "7vGNRrlvEtUX6hRdQvLq7U"),
        "r&b" to mapOf("1970" to "37i9dQZF1EIdpeTOIJBUe0", "1980" to "7oSFWAqfNN4UON82z8yst0", "1990" to "37i9dQZF1DX6VDO8a6cQME", "2000" to "37i9dQZF1DWYmmr74INQlb", "2010" to "37i9dQZF1DWXbttAJcbphz", "2020" to "37i9dQZF1EIhKysdf5HuRS"),
        "indie" to mapOf("1970" to "37i9dQZF1EIfEuk5mHRSID", "1980" to "37i9dQZF1EIevGiMQyNtSW", "1990" to "37i9dQZF1EIdAFUuQXTjDp", "2000" to "4irf7OeR9mM7KVxNTYoiXx", "2010" to "2HgmyUctw7UAi6fLlIMZJH", "2020" to "37i9dQZF1EIgo0ld2W1RyS"),
        "rock" to mapOf("1970" to "3za8xUPaO5ng9AC7rpbMNB", "1980" to "37i9dQZF1EIelF7Dvo3Edn", "1990" to "2HfFccisPxQfprhgIHM7XH", "2000" to "37i9dQZF1DX3oM43CtKnRV", "2010" to "37i9dQZF1DX99DRG9N39X3", "2020" to "37i9dQZF1EIfFB4LmpxPTW")
    )

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
            else -> "Invalid Year"
        }
    }

    suspend fun generateArtists(playlistId: String, genre: String, decade: String) : Map<String, List<Triple<String, String, String>>> {
            val yearToSongArtistsMap = getPlaylistItems(MainActivity.token, playlistId)
            Log.d("Test", "Top Artists in $genre Category from $decade: $yearToSongArtistsMap")
            return yearToSongArtistsMap
    }

    fun getSongs(map: Map<String, List<Triple<String, String, String>>>, year: String): Set<String> {
        return if (map.containsKey(year)) {
            map[year]!!.map { it.first }.toSet()
        } else {
            emptySet()
        }
    }

    fun getArtists(map: Map<String, List<Triple<String, String, String>>>, year: String): Set<String> {
        return if (map.containsKey(year)) {
            map[year]!!.map { it.second }.toSet()
        } else {
            emptySet()
        }
    }

    fun getRandomSongArtistPair(map: Map<String, List<Triple<String, String, String>>>, year: String): Triple<String, String, String>? {
        val songArtistsList = map[year]
        if (!songArtistsList.isNullOrEmpty()) {
            val randomIndex = songArtistsList.indices.random()
            return songArtistsList[randomIndex]
        }
        return null
    }
}