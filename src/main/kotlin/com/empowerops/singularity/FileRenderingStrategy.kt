package com.empowerops.singularity

import java.nio.file.Path
import java.nio.file.spi.FileSystemProvider
import java.util.*
import javax.inject.Inject
import javax.inject.Provider

interface FileRenderingStrategy {

    fun write(values: Map<String, Double>, target: Path)

    class Factory @Inject constructor(
            val propertiesFileProvider: Provider<PropertiesFile>
    ){
        fun findRenderingStrategyFor(extension: String) = when(extension){
            "properties", "ini" -> propertiesFileProvider.get()
            else -> throw IllegalArgumentException("no output strategy for $extension")
        }
    }

    class PropertiesFile @Inject constructor(
            val fileSystem: FileSystemProvider
    ): FileRenderingStrategy {

        override fun write(values: Map<String, Double>, target: Path){

            Properties().apply {
                putAll(values.mapValues { it.value.toString() })
                store(fileSystem.newOutputStream(target), "singularity simulation output")
            }
        }
    }
}
