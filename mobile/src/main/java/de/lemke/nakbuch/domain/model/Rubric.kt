package de.lemke.nakbuch.domain.model

data class Rubric(
    val rubricId: RubricId,
    val name: String,
    val isMain: Boolean,
) {
    constructor(rubricId: RubricId) :
            this(
                rubricId,
                rubricId.buchMode.rubricTitlesList[rubricId.index], //Get Name of Rubric
                rubricId.buchMode.rubricListItemList[rubricId.index] == 0, //Check if Rubric is Main-Rubric
            )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Rubric
        if (rubricId != other.rubricId) return false
        return true
    }

    override fun hashCode(): Int = rubricId.hashCode()
}
