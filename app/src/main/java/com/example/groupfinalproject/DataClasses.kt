package com.example.groupfinalproject


data class AddedBy(
    val external_urls: ExternalUrls,
    val followers: Followers,
    val href: String,
    val id: String,
    val type: String,
    val uri: String
)

data class Album(
    val album_type: String,
    val artists: List<Artist>,
    val available_markets: List<String>,
    val external_urls: ExternalUrls,
    val href: String,
    val id: String,
    val images: List<Image>,
    val name: String,
    val release_date: String,
    val release_date_precision: String,
    val restrictions: Restrictions,
    val total_tracks: Int,
    val type: String,
    val uri: String
)

data class Artist(
    val external_urls: ExternalUrls,
    val followers: Followers,
    val genres: List<String>,
    val href: String,
    val id: String,
    val images: List<Image>,
    val name: String,
    val popularity: Int,
    val type: String,
    val uri: String,
)

data class Artists(
    val items: List<Artist>
)


data class Categories(
    val href: String,
    val items: List<Item>,
    val limit: Int,
    val next: String,
    val offset: Int,
    val previous: String,
    val total: Int
)

data class ExternalUrls(
    val spotify: String
)

data class ExternalIds(
    val ean: String,
    val isrc: String,
    val upc: String
)

data class Followers(
    val href: String,
    val total: Int
)

data class Genres(
    val categories: Categories
)

data class Icon(
    val height: Int,
    val url: String,
    val width: Int
)

data class Image(
    val height: Int,
    val url: String,
    val width: Int
)

data class Item(
    val collaborative: Boolean,
    val description: String,
    val external_urls: ExternalUrls,
    val href: String,
    val id: String,
    val images: List<Image>,
    val name: String,
    val owner: Owner,
    val `public`: Boolean,
    val snapshot_id: String,
    val tracks: ItemTracks,
    val type: String,
    val uri: String,
    val icons: List<Icon>
)

data class ItemTracks(
    val href: String,
    val total: Int
)

class LinkedFrom

data class Owner(
    val display_name: String,
    val external_urls: ExternalUrls,
    val followers: Followers,
    val href: String,
    val id: String,
    val type: String,
    val uri: String
)

data class Playlist(
    val href: String,
    val items: List<Item>,
    val limit: Int,
    val next: String,
    val offset: Int,
    val previous: String,
    val total: Int
)

data class PlaylistItem(
    val added_at: String,
    val added_by: AddedBy,
    val is_local: Boolean,
    val track: Track
)

data class PlaylistItems(
    val href: String,
    val items: List<PlaylistItem>,
    val limit: Int,
    val next: String,
    val offset: Int,
    val previous: String,
    val total: Int
)

data class Playlists(
    val message: String,
    val playlists: Playlist
)

data class Restrictions(
    val reason: String
)

data class SearchResult(
    val artists: Artists
)

data class Token(
    val access_token: String,
    val expires_in: Int,
    val token_type: String
)

data class Track(
    val album: Album,
    val artists: List<Artist>,
    val available_markets: List<String>,
    val disc_number: Int,
    val duration_ms: Int,
    val explicit: Boolean,
    val external_ids: ExternalIds,
    val external_urls: ExternalUrls,
    val href: String,
    val id: String,
    val is_local: Boolean,
    val is_playable: Boolean,
    val linked_from: LinkedFrom,
    val name: String,
    val popularity: Int,
    val preview_url: String,
    val restrictions: Restrictions,
    val track_number: Int,
    val type: String,
    val uri: String,
)

data class Tracks(
    val tracks: List<Track>
)