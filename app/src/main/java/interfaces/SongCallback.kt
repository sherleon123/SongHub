package interfaces

import model.Song

interface SongCallback {
    fun SongClicked(song: Song, position: Int)
}