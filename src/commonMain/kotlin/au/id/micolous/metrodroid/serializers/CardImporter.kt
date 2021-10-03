package au.id.micolous.metrodroid.serializers

import ByteArrayInput
import au.id.micolous.metrodroid.card.Card
import kotlinx.io.charsets.encodeToByteArray
import kotlinx.io.core.Input

/**
 * Interface for writing card data importers.
 *
 * By default, this adopts a binary-based ([InputStream]) model.
 */
interface CardImporter {

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
    fun readCards(stream: Input): Iterator<Card>? {
        val card = readCard(stream)
        return if (card == null) {
            null
        } else {
            listOf(card).iterator()
        }
    }

    /**
     * Reads cards from the given String.
     *
     * This method should only be used for data which is already in memory.
     * @param s String to read from.
     */
    fun readCards(s: String): Iterator<Card>? {
        return readCards(ByteArrayInput(s.encodeToByteArray()))
    }

    /**
     * Reads a single card from the given stream.
     *
     * Implementations should read the card immediately.
     *
     * @param stream Stream to read the card content from.
     */
    fun readCard(stream: Input): Card?

    fun readCard(input: String): Card? {
        return readCard(ByteArrayInput(input.encodeToByteArray()))
    }
}
