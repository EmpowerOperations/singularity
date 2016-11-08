package com.empowerops.singularity

import dagger.Component
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
    val configPath = Paths["./singularity.exclude.config"]

    /*no-throw*/ fun execute(args: Array<String>): Int {

        val (fileCommander, fileCmd) = if ( ! Files.exists(configPath)) { null to CLIConfiguration() }
        else {
            val fileArgs: Array<String> = Files.readAllLines(configPath).filter { ! it.startsWith("#") }.toTypedArray()
            val (fileCommander, fileCmd) = commander.parse(fileArgs) { CLIConfiguration() }
            if(fileCmd == null) return 404

            fileCommander to fileCmd.withoutIntermediateObjects()
        }

        val (updatedCommander, updatedConfig) = when {
            fileCmd.ignoreCommandLine -> fileCommander!! to fileCmd
            else -> {
                val (cliCommander, cliCmd) = commander.parse(args) { CLIConfiguration() }
                if(cliCmd == null){
                    cliCommander.usage()
                    return 400
                }
                cliCommander to cliCmd.merge(fileCmd).withoutIntermediateObjects()
            }
        }

        try {

            println("singularity executing $updatedConfig")
            println("(working directory: "+System.getProperty("user.dir")+")")

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
            updatedCommander.usage()

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

