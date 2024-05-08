package com.example.groupfinalproject

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path


interface GetPlaylistFromCategory {
    @GET("v1/playlists/{playlist_id}/tracks")
    fun getTracks(
        @Header("Authorization") auth: String,
        @Path("playlist_id") playlistId: String
    ): Call<PlaylistItems>
}

interface GetGenres {
    @GET("v1/browse/categories")
    fun getCategories(
        @Header("Authorization") auth: String
    ): Call<Genres>
}

interface GenerateAuthToken {
    @FormUrlEncoded
    @POST("api/token")
    fun getToken(
        @Header("Authorization") auth: String,
        @Field("grant_type") grantType: String
    ): Call<Token>
}