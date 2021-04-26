package com.microsoft.fluentui.generator.model.resources

import com.microsoft.fluentui.generator.*
import java.io.File

private val interpolatorsFilePath = global_projectSourcePath.plus("main/java/com/microsoft/fluentui/generator/Interpolators.kt")

fun clearInterpolatorsFile() {
    StringBuilder().apply {
        append(AUTOGENERATED_CODE_HEADER)
        append("\npackage com.microsoft.fluentui.generator\n")
        append("import androidx.core.view.animation.PathInterpolatorCompat\n\n")
        append("fun createFluentUIInterpolator(x1: Float, y1: Float, x2: Float, y2: Float) = PathInterpolatorCompat.create(x1, y1, x2, y2)\n\n")
        File(interpolatorsFilePath).let {
            it.parentFile.mkdirs()
            it.writeText(toString())
        }
    }
}

fun generateInterpolator(name: String, value: String) {
    val valuesSequence = Regex(DIGITS_PATTERN).findAll(value)
    val valuesMap = valuesSequence.toList().let { values ->
        if (values.size != 4) {
            printError("Invalid timing function: $value")
            return
        }
        mapOf(
            "controlX1" to values[0].value,
            "controlY1" to values[1].value,
            "controlX2" to values[2].value,
            "controlY2" to values[3].value
        )
    }

    // Generate XML resources for API > 21
    StringBuilder().apply {
        append(AUTOGENERATED_XML_HEADER)
        append("<pathInterpolator xmlns:android=\"http://schemas.android.com/apk/res/android\"\n")
        valuesMap.forEach {
            append("    android:${it.key}=\"${it.value}\"\n")
        }
        append("/>")
        toString().writeToResourceFile(global_flavorPath.plus("res/anim-v21/${name.toLowerCase()}.xml"), "Interpolator")
    }

    // Generate programmatic interpolators for all APIs
    File(interpolatorsFilePath).let { interpolatorsFile ->
        if (!interpolatorsFile.exists()) clearInterpolatorsFile()

        val interpolatorName = "fluentUIInterpolator${name.substringAfterLast("_").capitalize()}"
        if (!interpolatorsFile.readText().contains(interpolatorName)) {
            File(interpolatorsFilePath).appendText(
                "val $interpolatorName = PathInterpolatorCompat.create" +
                 valuesMap.values.joinToString(separator = "f,", prefix = "(", postfix = "f)\n")
            )
        }
    }
}