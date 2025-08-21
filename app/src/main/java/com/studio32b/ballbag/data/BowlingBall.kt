package com.studio32b.ballbag.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull

object FlexibleDoubleSerializer : KSerializer<Double?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FlexibleDouble", PrimitiveKind.DOUBLE)
    override fun deserialize(decoder: Decoder): Double? {
        val input = decoder as? JsonDecoder ?: error("Can only be used with Json")
        val element: JsonElement = input.decodeJsonElement()
        val primitive = element as? JsonPrimitive ?: return null
        return primitive.doubleOrNull ?: primitive.content.toDoubleOrNull()
    }
    override fun serialize(encoder: Encoder, value: Double?) {
        if (value == null) encoder.encodeNull()
        else encoder.encodeDouble(value)
    }
}

@Entity(tableName = "bowling_balls")
@Serializable
data class BowlingBall(
    @PrimaryKey val id: Int,
    val ballName: String,
    val imageFile: String,
    val brand: String,
    val releaseDate: String,
    val coverstock: String,
    val factoryFinish: String,
    val core: String,
    val rg: Double,
    val diff: Double,
    @Serializable(with = FlexibleDoubleSerializer::class)
    val mbDiff: Double?,
    val releaseType: String,
    val discontinued: String,
    val acquiredDate: String? = null, // Date the user acquired the ball
    val gamesPlayed: Int = 0 // Number of games played with this ball
)
