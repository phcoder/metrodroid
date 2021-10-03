package au.id.micolous.metrodroid.serializers

import au.id.micolous.metrodroid.card.Card
import au.id.micolous.metrodroid.multi.Log
import au.id.micolous.metrodroid.multi.NativeThrows
import au.id.micolous.metrodroid.multi.logAndSwiftWrap
import kotlinx.io.core.Input
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

object CardSerializer {
    fun load(importer: CardImporter, stream: Input): Card? {
        try {
            return importer.readCard(stream)
        } catch (ex: Exception) {
            Log.e("Card", "Failed to deserialize", ex)
            throw RuntimeException(ex)
        }
    }

    private fun fromJson(xml: String): Card = logAndSwiftWrap ("Card", "Failed to deserialize") {
        JsonKotlinFormat.readCard(xml)
    }

    @NativeThrows
    fun fromAutoJson(json: String): Iterator<Card> = logAndSwiftWrap ("Card", "Failed to deserialize") {
        AutoJsonFormat.readCardList(json).iterator()
    }

    @NativeThrows
    fun toJson(card: Card): JsonElement = logAndSwiftWrap ("Card", "Failed to serialize") {
        JsonKotlinFormat.writeCard(card)
    }

    @NativeThrows
    fun fromPersist(input: String): Card = fromJson(input)

    @NativeThrows
    fun toPersist(card: Card): String = toJson(card).toString()

    val jsonPlainStable get() = Json {
        useArrayPolymorphism = true
    }
}
