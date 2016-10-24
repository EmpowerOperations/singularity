package com.empowerops.singularity

import com.beust.jcommander.IStringConverter
import com.empowerops.singularity.RosenbrockAdaptiveN.pow

interface GeneratorFunction{
    fun evaluate(inputVector: Map<String, Double>): Map<String, Double>

    fun Double.pow(power: Int): Double = Math.pow(this, power + 0.0)

    class Converter: IStringConverter<GeneratorFunction> {

        override fun convert(value: String?): GeneratorFunction = when(value){

            "RosenbrockX1ThroughX10" -> RosenbrockX1ThroughX10
            "RosenbrockAdaptiveN" -> RosenbrockAdaptiveN

            //TODO: "com.empowerops.singularity.$value".isValidClassName() -> dagger.getInstance(that)

            else -> throw UnsupportedOperationException("unknown generator '$value'")
        }
    }
}

object RosenbrockX1ThroughX10: RosenbrockX1ThroughXN(10)

open class RosenbrockX1ThroughXN(val n: Int): GeneratorFunction{
    override fun evaluate(inputVector: Map<String, Double>): Map<String, Double> {

        val X1ThroughX10 = (1..n).map { "x$it" }
        require(inputVector.keys != X1ThroughX10) { "input values must be exclusively x1 through x10" }

        val vars = X1ThroughX10.map { Pair(it, inputVector[it]!!) }.toMap()

        return mapOf("f1" to rosenbrock(vars))
    }

    override fun toString() = "Rosenbrock x1 through x$n"
}

object RosenbrockAdaptiveN : GeneratorFunction {
    override fun evaluate(inputVector: Map<String, Double>) = mapOf("f1" to rosenbrock(inputVector))
    override fun toString() = "RosenbrockAdaptiveN"
}

private fun rosenbrock(inputVector: Map<String, Double>): Double {

    val vars = inputVector.values.toList()

    return vars.take(inputVector.size - 1).foldIndexed(0.0) { i, sum, next ->

        //sum(1, 99, i -> 100 * (var[i+1] - var[i]^2)^2 + (1 - var[i])^2)
        sum + (100 * (vars[i + 1] - vars[i].pow(2)).pow(2) + (1 - vars[i]).pow(2))
    }
}