package com.amakaflow.companion.data.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

/**
 * Workout interval types - sealed class to represent the different interval kinds
 */
@Serializable(with = WorkoutIntervalSerializer::class)
sealed class WorkoutInterval {

    @Serializable
    data class Warmup(
        val seconds: Int,
        val target: String? = null
    ) : WorkoutInterval()

    @Serializable
    data class Cooldown(
        val seconds: Int,
        val target: String? = null
    ) : WorkoutInterval()

    @Serializable
    data class Time(
        val seconds: Int,
        val target: String? = null
    ) : WorkoutInterval()

    @Serializable
    data class Reps(
        val sets: Int? = null,
        val reps: Int,
        val name: String,
        val load: String? = null,
        val restSec: Int? = null,
        val followAlongUrl: String? = null
    ) : WorkoutInterval()

    @Serializable
    data class Distance(
        val meters: Int,
        val target: String? = null
    ) : WorkoutInterval()

    @Serializable
    data class Repeat(
        val reps: Int,
        val intervals: List<WorkoutInterval>
    ) : WorkoutInterval()

    /**
     * Rest interval - either timed or manual ("tap when ready")
     * @param seconds Duration in seconds, or null for manual rest
     */
    @Serializable
    data class Rest(
        val seconds: Int?  // null = manual rest ("tap when ready")
    ) : WorkoutInterval()
}

/**
 * Custom serializer for WorkoutInterval sealed class
 * Handles the "kind" discriminator field from the API
 */
object WorkoutIntervalSerializer : KSerializer<WorkoutInterval> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("WorkoutInterval")

    override fun serialize(encoder: Encoder, value: WorkoutInterval) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: throw IllegalStateException("This serializer only works with JSON")

        val jsonObject = buildJsonObject {
            when (value) {
                is WorkoutInterval.Warmup -> {
                    put("kind", "warmup")
                    put("seconds", value.seconds)
                    value.target?.let { put("target", it) }
                }
                is WorkoutInterval.Cooldown -> {
                    put("kind", "cooldown")
                    put("seconds", value.seconds)
                    value.target?.let { put("target", it) }
                }
                is WorkoutInterval.Time -> {
                    put("kind", "time")
                    put("seconds", value.seconds)
                    value.target?.let { put("target", it) }
                }
                is WorkoutInterval.Reps -> {
                    put("kind", "reps")
                    value.sets?.let { put("sets", it) }
                    put("reps", value.reps)
                    put("name", value.name)
                    value.load?.let { put("load", it) }
                    value.restSec?.let { put("restSec", it) }
                    value.followAlongUrl?.let { put("followAlongUrl", it) }
                }
                is WorkoutInterval.Distance -> {
                    put("kind", "distance")
                    put("meters", value.meters)
                    value.target?.let { put("target", it) }
                }
                is WorkoutInterval.Repeat -> {
                    put("kind", "repeat")
                    put("reps", value.reps)
                    put("intervals", Json.encodeToJsonElement(value.intervals))
                }
                is WorkoutInterval.Rest -> {
                    put("kind", "rest")
                    value.seconds?.let { put("seconds", it) }
                }
            }
        }
        jsonEncoder.encodeJsonElement(jsonObject)
    }

    override fun deserialize(decoder: Decoder): WorkoutInterval {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw IllegalStateException("This serializer only works with JSON")

        val element = jsonDecoder.decodeJsonElement().jsonObject
        val kind = element["kind"]?.jsonPrimitive?.content
            ?: throw IllegalArgumentException("Missing 'kind' field in WorkoutInterval")

        return when (kind) {
            "warmup" -> WorkoutInterval.Warmup(
                seconds = element["seconds"]!!.jsonPrimitive.int,
                target = element["target"]?.jsonPrimitive?.contentOrNull
            )
            "cooldown" -> WorkoutInterval.Cooldown(
                seconds = element["seconds"]!!.jsonPrimitive.int,
                target = element["target"]?.jsonPrimitive?.contentOrNull
            )
            "time" -> WorkoutInterval.Time(
                seconds = element["seconds"]!!.jsonPrimitive.int,
                target = element["target"]?.jsonPrimitive?.contentOrNull
            )
            "reps" -> WorkoutInterval.Reps(
                sets = element["sets"]?.jsonPrimitive?.intOrNull,
                reps = element["reps"]!!.jsonPrimitive.int,
                name = element["name"]!!.jsonPrimitive.content,
                load = element["load"]?.jsonPrimitive?.contentOrNull,
                restSec = element["restSec"]?.jsonPrimitive?.intOrNull,
                followAlongUrl = element["followAlongUrl"]?.jsonPrimitive?.contentOrNull
            )
            "distance" -> WorkoutInterval.Distance(
                meters = element["meters"]!!.jsonPrimitive.int,
                target = element["target"]?.jsonPrimitive?.contentOrNull
            )
            "repeat" -> WorkoutInterval.Repeat(
                reps = element["reps"]!!.jsonPrimitive.int,
                intervals = Json.decodeFromJsonElement(element["intervals"]!!)
            )
            "rest" -> WorkoutInterval.Rest(
                seconds = element["seconds"]?.jsonPrimitive?.intOrNull
            )
            else -> throw IllegalArgumentException("Unknown interval kind: $kind")
        }
    }
}

/**
 * Convert a WorkoutInterval to a simplified submission format
 */
fun WorkoutInterval.toSubmissionInterval(): WorkoutIntervalSubmission {
    return when (this) {
        is WorkoutInterval.Warmup -> WorkoutIntervalSubmission(
            type = "warmup",
            seconds = seconds,
            target = target
        )
        is WorkoutInterval.Cooldown -> WorkoutIntervalSubmission(
            type = "cooldown",
            seconds = seconds,
            target = target
        )
        is WorkoutInterval.Time -> WorkoutIntervalSubmission(
            type = "time",
            seconds = seconds,
            target = target
        )
        is WorkoutInterval.Reps -> WorkoutIntervalSubmission(
            type = "reps",
            reps = reps,
            sets = sets,
            name = name
        )
        is WorkoutInterval.Distance -> WorkoutIntervalSubmission(
            type = "distance",
            seconds = null,
            target = target
        )
        is WorkoutInterval.Repeat -> WorkoutIntervalSubmission(
            type = "repeat",
            reps = reps,
            intervals = intervals.map { it.toSubmissionInterval() }
        )
        is WorkoutInterval.Rest -> WorkoutIntervalSubmission(
            type = "rest",
            seconds = seconds
        )
    }
}
