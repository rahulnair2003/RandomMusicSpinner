package com.example.groupfinalproject

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SearchArtistApi {
    @GET("v1/search")
    fun searchForArtist(
        @Header("Authorization") auth: String,
        @Query("q") artistName: String,
        @Query("type") type: String,
        @Query("limit") limit: Int
    ): Call<SearchResult>
}


interface GetPlaylistFromCategory {
    @GET("v1/playlists/{playlist_id}/tracks")
    fun getTracks(
        @Header("Authorization") auth: String,
        @Path("playlist_id") playlistId: String
    ): Call<PlaylistItems>
}

interface GetTrack {
    @GET("v1/tracks/{id}")
    fun getTrack(
        @Header("Authorization") auth: String,
        @Path("id") trackId: String
    ): Call<Track>
}

interface GetGenres {
    @GET("v1/browse/categories")
    fun getCategories(
        @Header("Authorization") auth: String
    ): Call<Genres>

    @GET("v1/browse/categories/{category_id}/playlists")
    fun getCategoryPlaylists(
        @Header("Authorization") auth: String,
        @Path("category_id") categoryId: String
    ): Call<Playlists>
}

interface GenerateAuthToken {
    @FormUrlEncoded
    @POST("api/token")
    fun getToken(
        @Header("Authorization") auth: String,
        @Field("grant_type") grantType: String
    ): Call<Token>
}

interface ArtistTopTracksApi {
    @GET("v1/artists/{artist_id}/top-tracks")
    fun getSongsByArtist(
        @Header("Authorization") auth: String,
        @Path("artist_id") artistId: String,
        @Query("country") country: String
    ): Call<Tracks>
}