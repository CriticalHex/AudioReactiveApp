package com.audioreactive.data.database

import androidx.room.TypeConverter

class AudioReactiveTypeConverters {
    @TypeConverter
    fun fromListStringToString(list: List<String>) =
        list.foldIndexed("") { index, prior, item ->
            if(index > 0) {
                "$prior,$item"
            } else {
                item
            }
        }

    @TypeConverter
    fun toListStringFromString(listString: String?) =
        listString?.split(",") ?: emptyList()
}