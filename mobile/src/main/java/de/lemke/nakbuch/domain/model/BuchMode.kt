package de.lemke.nakbuch.domain.model

enum class BuchMode {
    Gesangbuch, Chorbuch;

    override fun toString(): String {
        return when (this) {
            Gesangbuch -> "GB"
            Chorbuch -> "CB"
        }
    }

    fun toInt(): Int {
        return when (this) {
            Gesangbuch -> 0
            Chorbuch -> 1
        }
    }
    companion object {
        fun fromInt(mode: Int) : BuchMode = if (mode == 1) Chorbuch else Gesangbuch
    }
}