package de.lemke.nakbuch.domain.model

class RubricId private constructor(
    val index: Int,
    val buchMode: BuchMode,
) {
    fun toInt(): Int = index + buchMode.toInt()

    override fun hashCode(): Int = toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as RubricId
        if (toInt() != other.toInt()) return false
        return true
    }

    companion object {
        val rubricIdPlaceholder = RubricId(-1, BuchMode.Gesangbuch)
        fun create(index: Int, buchMode: BuchMode?): RubricId? =
            if (buchMode == null || index < 0 || index >= buchMode.rubricListItemList.size || index >= buchMode.rubricTitlesList.size) null
            else RubricId(index, buchMode)

        fun create(rubricIdInt: Int): RubricId? =
            if (rubricIdInt < 0) null
            else create(rubricIdInt.mod(BuchMode.intStep), BuchMode.fromHymnId(rubricIdInt))
    }
}
