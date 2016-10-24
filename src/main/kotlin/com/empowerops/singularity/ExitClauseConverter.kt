package com.empowerops.singularity

import com.beust.jcommander.IStringConverter
import com.beust.jcommander.ParameterException
import com.beust.jcommander.converters.DoubleConverter

class ExitClauseConverter(
        var optionName: String,
        val ignored: Int

): IStringConverter<CLIConfiguration.ExecutionOutcome> {

    val exitCodeRegex = "^code:(?<value>\\d+)$".toRegex()

    var probability: Double? = null
    var exitClause: ExitClause? = null

    override fun convert(value: String) = updateWith {
        when {
            probability == null -> probability = expectIsProbability(value)
            exitClause == null -> exitClause = expectIsExitClause(value)
            else -> throw IllegalStateException()
        }
    }

    private fun updateWith(update: () -> Unit): CLIConfiguration.ExecutionOutcome?{
        update()
        if(probability != null && exitClause != null){
            val result = CLIConfiguration.ExecutionOutcome(probability!!, exitClause!!)
            probability = null
            exitClause = null
            return result;
        }
        else return null
    }

    private fun expectIsProbability(value: String): Double {
        val convertedValue =
                try { value.replace("%", "").trim().toDouble() }
                catch (ex: NumberFormatException){
                    throw ParameterException("expected $value to be a percentage of the form '12%'")
                }

        if(convertedValue > 100.0) throw ParameterException("$optionName value $value is more than 100")
        if(convertedValue < 0) throw ParameterException("$optionName value $value is less than 0")

        return convertedValue / 100.0
    }

    private fun expectIsExitClause(value: String) = when {
        value == "hang" -> HungClause
        value.matches(exitCodeRegex) -> {
            val result = exitCodeRegex.matchEntire(value)!!.groups[1]!!.value.toInt()
            ExitCodeClause(result)
        }
        else -> throw ParameterException("$optionName value $value must either match 'code:<int>' or the literal 'hang'")
    }
}