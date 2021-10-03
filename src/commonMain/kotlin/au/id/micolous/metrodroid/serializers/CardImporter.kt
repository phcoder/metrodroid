package au.id.micolous.metrodroid.serializers

import au.id.micolous.metrodroid.card.Card
import kotlinx.io.ByteArrayInputStream
import kotlinx.io.InputStream
import kotlinx.serialization.toUtf8Bytes

/**
 * Interface for writing card data importers.
 *
 * By default, this adopts a binary-based ([InputStream]) model.
 */
interface CardImporter {
    /**
     * Reads a single card from the given stream.
     *
     * Implementations should read the card immediately.
     *
     * @param stream Stream to read the card content from.
     */
    fun readCard(stream: InputStream): Card?

    fun readCard(input: String): Card? {
        return readCard(ByteArrayInputStream(input.toUtf8Bytes()))
    }
}
