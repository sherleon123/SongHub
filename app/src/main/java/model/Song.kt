package model

import android.net.Uri

data class Song private constructor(
    val name: String,
    var private: Boolean,
    val duration:String,
    val url: String,
    var lon : Double,
    var lat : Double,)
{
    constructor():this(
        name="",
        private=true,
        duration="",
        url="",
        lon=0.0,
        lat=0.0

    )
    class Builder(
    ) {
        var name: String = ""
        var private:Boolean=true;
        var lon : Double=0.0
        var lat : Double=0.0
        var url:String=""
        var duration:String=""
        fun private(private: Boolean)=apply{this.private=private}
        fun name(name: String) = apply { this.name = name }
        fun url(url: String)=apply { this.url =url }
        fun lon(lon: Double)=apply { this.lon=lon }
        fun lat(lat: Double)=apply { this.lat=lat }
        fun duration(duration: String)=apply { this.duration=duration }
        fun build() = Song(name,private,duration,url,lon,lat)
    }


}