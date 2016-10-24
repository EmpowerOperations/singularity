package com.empowerops.singularity

import com.google.gson.Gson
import dagger.Component
import java.io.FileReader
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.util.*
import javax.inject.Inject

class SingularityExecutor @Inject constructor(
        val commander: JCommanderWrapper,
        val readerFactory: FileReadingStrategy.Factory,
        val renderFactory: FileRenderingStrategy.Factory,
        val workSimulator: WorkSimulator,
        val hangwoman: Hangwoman,
        val improbabilityDrive: ImprobabilityDrive
) {

    @Component(modules = arrayOf(SingularityModule::class))
    interface Factory { fun makeExecutor(): SingularityExecutor }

    val random = Random()
    val gson = Gson()
    val configPath = Paths["./singularity.config.json"]

    fun execute(args: Array<String>): Int {

        val (cmd, cliConfig) = commander.parse(args) { CLIConfiguration() }

        when {
            cliConfig == null -> return 400
            cliConfig.help -> {
                cmd.usage()
                return 0
            }
            else -> { /*no-op*/ }
        }

        try {

            val externalConfig = if ( ! Files.exists(configPath)) CLIConfiguration()
            else gson.fromJson(FileReader(configPath.toFile()), CLIConfiguration::class.java)

            val updatedConfig = cliConfig.merge(externalConfig).withoutIntermediateObjects()

            println("singularity executing $updatedConfig")

            val inputExtension = updatedConfig.input.fileName.extension
                    ?: throw IllegalArgumentException("unknown file type ${updatedConfig.input}")

            val inputReadingStrategy: FileReadingStrategy = readerFactory.findReadingStrategyFor(inputExtension)

            val inputValues = inputReadingStrategy.read(updatedConfig.input)

            workSimulator.runFor(Duration.ofMillis(updatedConfig.time))

            val exitCode = hangOrGenerateExitCode(updatedConfig)

            val result = updatedConfig.generator.evaluate(inputValues)

            val outputExtension = updatedConfig.output.fileName.extension
                    ?: throw IllegalArgumentException("unknown file type ${updatedConfig.output}")

            val outputRenderingStrategy = renderFactory.findRenderingStrategyFor(outputExtension)

            outputRenderingStrategy.write(result, updatedConfig.output)

            return exitCode ?: 500
        }
        catch(ex: Exception) {
            ex.printStackTrace(System.err)

            println(ex.message)
            cmd.usage()

            return 500
        }
    }

    private fun hangOrGenerateExitCode(updatedConfig: CLIConfiguration): Int? {

        val nextChance = random.nextDouble()

        val map = improbabilityDrive.makeRangeMap(updatedConfig.behaviour)

        val outcome: ExitClause = map[nextChance]!!

        return when(outcome){
            is ExitCodeClause -> outcome.exitCode
            is HungClause ->     hangwoman.hang()
            else ->              throw UnsupportedOperationException()
        }
    }

    val Path.extension: String? get()
            = toString().run { if(lastIndexOf(".") == -1) null else substring(lastIndexOf(".")+1) }
}

