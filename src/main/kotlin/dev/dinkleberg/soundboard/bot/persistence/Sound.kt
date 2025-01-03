package dev.dinkleberg.soundboard.bot.persistence

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import io.micronaut.data.model.naming.NamingStrategies
import jakarta.persistence.Table

@MappedEntity(namingStrategy = NamingStrategies.Raw::class)
@Table(name = "Sounds")
data class Sound(
    @field:Id
    val id: String,
    val name: String,
    val localPath: String,
    val submittedBy: String,
    @TypeDef(type = DataType.JSON)
    val tags: List<String>
)
