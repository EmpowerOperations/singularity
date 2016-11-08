package com.empowerops.singularity

import com.google.common.collect.ImmutableRangeMap.builder
import com.google.common.collect.Range.closedOpen
import com.google.common.collect.RangeMap
import java.util.logging.Logger
import javax.inject.Inject

class ImprobabilityDrive {

    @Inject constructor(){}

    val logger = Logger.getLogger(ImprobabilityDrive::class.java.canonicalName)

    fun makeRangeMap(behaviours: List<CLIConfiguration.ExecutionOutcome>): RangeMap<Double, ExitClause> {

        if(behaviours.isEmpty()) { return builder<Double, ExitClause>().put(closedOpen(0.0, 1.0), ExitZero).build() }

        //ok so the below code has a couple of odd requirements on it:
        // 1 - it must normalize the users supplied range.
        //        -- eg if their probabilities sum to 0.98, they should be normalized to 1.00, such that we never have an 'undefined' case.
        // 2 - it should log warnings when it encounters ranges that need to be adjusted, with details
        // 3 - it must produce a range that fits 0 .. 1.0 **with no error on the 1.0**.
        //        -- In other words, adding 0.01 together 100 times doesn't suffice because the result contains some error.

        val rangeMapBuilder = builder<Double, ExitClause>()
        var currentOffset = 0.0

        val correctiveFactor = 1.0 / behaviours.sumByDouble { it.probability }

        if(correctiveFactor !in 1.00 plusOrMinus 0.02) {
            logger.warning { "behaviour distribution doesn't sum to 100%. Distribution will be normalized. " }
        }

        for(behaviour in behaviours){

            val delta = correctiveFactor * behaviour.probability

            if(delta !in behaviour.probability plusOrMinus 0.01) {
                logger.warning { "Adjusted '$behaviour' to ${delta.percent}%" }
            }

            val upperBound = if(behaviour === behaviours.last()) 1.00 else currentOffset + delta

            rangeMapBuilder.put(closedOpen(currentOffset, upperBound), behaviour.exitClause)

            currentOffset += delta
        }

        return rangeMapBuilder.build()
    }

    infix fun Double.plusOrMinus(epsilon: Double): ClosedRange<Double> = this-epsilon .. this+epsilon
    val Double.percent: Int get() = (this * 100.0).toInt()
}