package com.twinalyze.servicedemo.adapter

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class StringOrArrayAdapter : JsonDeserializer<List<String>?> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, ctx: JsonDeserializationContext?): List<String>? {
        if (json == null || json.isJsonNull) return null

        return when {
            json.isJsonArray -> json.asJsonArray.mapNotNull { el ->
                if (el.isJsonPrimitive && el.asJsonPrimitive.isString) el.asString else null
            }
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                // allow "Veg, Non Veg." etc.
                json.asString.split(",", "/", "&").map { it.trim() }.filter { it.isNotEmpty() }
            }
            else -> null
        }
    }
}