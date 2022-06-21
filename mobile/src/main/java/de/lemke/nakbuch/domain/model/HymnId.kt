package de.lemke.nakbuch.domain.model

class HymnId private constructor(
    val number: Int,
    val buchMode: BuchMode,
) {
    override fun toString(): String = "$buchMode: $number"

    fun toInt(): Int {
        return number + buchMode.toInt()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as HymnId
        if (toInt() != other.toInt()) return false
        return true
    }

    override fun hashCode(): Int = toInt()

    companion object {
        val hymnIdPlaceholder = HymnId(-1, BuchMode.Gesangbuch)
        fun create(number: Int, buchMode: BuchMode?): HymnId? =
            if (buchMode == null || number < 1 || number > buchMode.hymnCount) null
            else HymnId(number, buchMode)

        fun create(hymnIdInt: Int): HymnId? =
            if (hymnIdInt < 1) null
            else create(hymnIdInt.mod(BuchMode.intStep), BuchMode.fromHymnId(hymnIdInt))
    }
}