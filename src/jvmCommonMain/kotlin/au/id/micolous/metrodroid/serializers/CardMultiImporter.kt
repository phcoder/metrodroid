package au.id.micolous.metrodroid.serializers

import au.id.micolous.metrodroid.card.Card
import kotlinx.io.ByteArrayInputStream
import kotlinx.io.InputStream
import kotlinx.serialization.toUtf8Bytes

interface CardMultiImporter : CardImporter {
    /**
     * Reads cards from the given stream.
     *
     * Implementations should read the file incrementally (lazy), to save memory.
     *
     * By default, this tries to read one card (using [readCard]), and returns a
     * singleton iterator.
     *
     * @param stream Stream to read the card content from.
     */
    fun readCards(stream: InputStream): Iterator<Card>?

    /**
     * Reads cards from the given String.
     *
     * This method should only be used for data which is already in memory.
     * @param s String to read from.
     */
    fun readCards(s: String): Iterator<Card>? {
        return readCards(ByteArrayInputStream(s.toUtf8Bytes()))
    }
}

class CardMultiImportAdapter (val base: CardImporter): CardMultiImporter {
    override fun readCard(stream: InputStream): Card? = base.readCard(stream)

    override fun readCards(stream: InputStream): Iterator<Card>? {
        val card = readCard(stream)
        return if (card == null) {
            null
        } else {
            listOf(card).iterator()
        }
    }
}
