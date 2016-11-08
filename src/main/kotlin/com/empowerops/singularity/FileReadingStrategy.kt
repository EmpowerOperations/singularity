package com.empowerops.singularity

import java.nio.file.Path
import java.nio.file.spi.FileSystemProvider
import java.util.*
import javax.inject.Inject
import javax.inject.Provider

interface FileReadingStrategy {

    fun read(file: Path): Map<String, Double>

    class Factory @Inject constructor(
            val propertiesStrategy: Provider<PropertiesFile>
    ) {
        fun findReadingStrategyFor(extension: String) = when(extension){
            "txt" -> propertiesStrategy.get()
            "properties", "ini" -> propertiesStrategy.get()
            else -> throw UnsupportedOperationException("no strategy to read files with extension $extension")
        }
    }

    class PropertiesFile @Inject constructor(
            val fileSystem: FileSystemProvider
    ) : FileReadingStrategy {

        override fun read(file: Path): Map<String, Double> {
            val result = Properties()
            val propertiesFilePath = file.toAbsolutePath().normalize()
            val inputStream = fileSystem.newInputStream(propertiesFilePath)
            result.load(inputStream)

            return result
                    .mapKeys { it.key.toString() }
                    .mapValues { it.value.toString().toNullableDouble() ?: reportUncoercedValue(file, it.value) }
                    .filterValuesNotNull()
        }

        fun reportUncoercedValue(file: Path, value: Any): Nothing? {
            println("warning: singularity couldn't convert '$value' in $file to a double")
            return null
        }

        fun String.toNullableDouble() = try { toDouble() } catch(ex: NumberFormatException){ null }

        @Suppress("UNCHECKED_CAST") //type system cant see that `filterValues { it != null }` wont return null values.
        fun <K, V> Map<K, V?>.filterValuesNotNull(): Map<K, V>
                = filterValues { it != null } as Map<K, V>
    }
}