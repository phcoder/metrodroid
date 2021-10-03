package au.id.micolous.metrodroid.serializers

import au.id.micolous.metrodroid.card.Card
import kotlinx.io.core.Output

interface CardExporter {
    fun writeCard(s: Output, card: Card)
}
