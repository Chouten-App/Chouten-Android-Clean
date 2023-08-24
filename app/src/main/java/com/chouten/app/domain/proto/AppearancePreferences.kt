package com.chouten.app.domain.proto

import android.content.Context
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.dataStore
import com.chouten.app.R
import com.chouten.app.common.FallbackThemes
import com.chouten.app.common.UiText
import com.chouten.app.data.data_source.user_preferences.AppearancePreferencesSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.reflect.full.memberProperties

/**
 * Data class representing the user's appearance preferences.
 * @param theme The ColorScheme of the app.
 * @see [ColorScheme]
 * @param appearance The appearance of the app (light, dark, or system default).
 * @param isDynamicColor Whether the app should use dynamic colors (API 31+ only).
 * @param isAmoled Whether the app should use AMOLED colors (pure black).
 */
@Serializable
data class AppearancePreferences(
    @Serializable(with = ThemeSerializer::class)
    val theme: ColorScheme,
    val appearance: Appearance,
    val isDynamicColor: Boolean,
    val isAmoled: Boolean,
) {
    @Serializable
    enum class Appearance(val displayName: UiText) {
        SYSTEM(UiText.StringRes(R.string.system)),
        LIGHT(UiText.StringRes(R.string.light)),
        DARK(UiText.StringRes(R.string.dark)), ;

        fun toString(context: Context): String {
            return displayName.string(context)
        }
    }


    companion object {
        val DEFAULT = AppearancePreferences(
            theme = FallbackThemes.LIGHT,
            appearance = Appearance.SYSTEM,
            isDynamicColor = true,
            isAmoled = false,
        )
    }
}

val Context.appearanceDatastore by dataStore(
    fileName = "appearance_preferences.pb",
    serializer = AppearancePreferencesSerializer,
)

object ThemeSerializer : KSerializer<ColorScheme> {
    override fun deserialize(decoder: Decoder): ColorScheme {
        // Split at each comma
        val colors = decoder.decodeString().split(",").map { color -> Color(color.toInt()) }
        return ColorScheme(
            primary = colors[0],
            onPrimary = colors[1],
            primaryContainer = colors[2],
            onPrimaryContainer = colors[3],
            inversePrimary = colors[4],
            secondary = colors[5],
            onSecondary = colors[6],
            secondaryContainer = colors[7],
            onSecondaryContainer = colors[8],
            tertiary = colors[9],
            onTertiary = colors[10],
            tertiaryContainer = colors[11],
            onTertiaryContainer = colors[12],
            background = colors[13],
            onBackground = colors[14],
            surface = colors[15],
            onSurface = colors[16],
            surfaceVariant = colors[17],
            onSurfaceVariant = colors[18],
            surfaceTint = colors[19],
            inverseSurface = colors[20],
            inverseOnSurface = colors[21],
            error = colors[22],
            onError = colors[23],
            errorContainer = colors[24],
            onErrorContainer = colors[25],
            outline = colors[26],
            outlineVariant = colors[27],
            scrim = colors[28]
        )
    }

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("Theme", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ColorScheme) {
        val colors = value::class.memberProperties.map { property ->
            property.getter.call(value) as Color
        }
        encoder.encodeString(colors.joinToString(",") { color -> color.toArgb().toString() })
    }
}