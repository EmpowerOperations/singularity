package com.empowerops.singularity

import com.beust.jcommander.Parameter
import java.nio.file.Path

data class CLIConfiguration(

        @Parameter(
                help = true,
                description = "Prints a description and this usage summary",
                names = arrayOf("-h", "--help")
        )
        var help: Boolean = false,

        @Parameter(
                description = "time to execute for in millis",
                names = arrayOf("-t", "--time")
        )
        var time: Long = DefaultTime,

        @Parameter(
                description = "path read input values from",
                names = arrayOf("-i", "--input")
        )
        var input: Path = DefaultInput,

        @Parameter(
                description = "path to write simulation result values to",
                names = arrayOf("-o", "--output")
        )
        var output: Path = DefaultOutput,

        @Parameter(
                description = "executable behaiviour distribution, repeated once for each possible outcome " +
                        "eg '-e 10% code:2 -e 30% hang'",
                names = arrayOf("-e", "--exit"),
                arity = 2,
                listConverter = ExitClauseConverter::class
        )
        var behaviour: List<ExecutionOutcome> = DefaultBehaviour,

        @Parameter(
                description = "generator function to create result values",
                names = arrayOf("-g", "--generator"),
                converter = GeneratorFunction.Converter::class
        )
        var generator: GeneratorFunction = DefaultGenerator
){

    init {
        input.fileSystem.isOpen
    }
    fun merge(other: CLIConfiguration) = CLIConfiguration(
            time = if (time != DefaultTime) time else other.time,
            input = if (input != DefaultInput) input else other.input,
            output = if (output != DefaultOutput) output else other.output,
            behaviour = if (behaviour != DefaultBehaviour) behaviour else other.behaviour,
            generator = if (generator != DefaultGenerator) generator else other.generator
            //TODO reflectively enumerate fields?
    )

    fun withoutIntermediateObjects() = copy(behaviour = behaviour.filterNotNull())

    companion object {

        val DefaultTime = 200L
        val DefaultInput = Paths["./input.properties"]
        val DefaultOutput = Paths["./output.properties"]
        val DefaultBehaviour = listOf(ExecutionOutcome(1.0, ExitCodeClause(0)))
        val DefaultGenerator = RosenbrockAdaptiveN
    }

    data class ExecutionOutcome(val probability: Double, val exitClause: ExitClause){
        override fun toString() = "--exit ${probability * 100}% $exitClause"
    }
}