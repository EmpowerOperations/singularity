package com.empowerops.singularity

import com.empowerops.singularity.CLIConfiguration.ExecutionOutcome
import com.google.common.collect.Range
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

/**
 * Created by Geoff on 2016-10-24.
 */
class ImprobabilityDriveFixture {

    @Test fun `when running improbability drive with emptyset should get back 100% exit 0`(){
        //setup
        val drive = ImprobabilityDrive()

        //act
        val map = drive.makeRangeMap(emptyList())

        //assert
        assertThat(map.span()).isEqualTo(Range.closedOpen(0.0, 1.0))
        assertThat(map.asMapOfRanges()).hasSize(1)
        assertThat(map.asMapOfRanges()).isEqualTo(mapOf(Range.closedOpen(0.0, 1.0) to ExitZero))
    }

    @Test fun `when running improbabilityDrive with three behaviours should get evenly distrubuted range map`(){
        //setup
        val drive = ImprobabilityDrive()

        //act
        val map = drive.makeRangeMap(listOf(
                ExecutionOutcome(0.33, ExitCodeClause(1)),
                ExecutionOutcome(0.33, ExitCodeClause(2)),
                ExecutionOutcome(0.33, ExitCodeClause(3))
        ))

        //assert
        assertThat(map.span()).isEqualTo(Range.closedOpen(0.0, 1.0))
        assertThat(map.asMapOfRanges()).hasSize(3)
        assertThat(map.asMapOfRanges()).isEqualTo(mapOf(
                Range.closedOpen(0.0, 0.33333333333333337) to ExitCodeClause(1),
                Range.closedOpen(0.33333333333333337, 0.6666666666666667) to ExitCodeClause(2),
                Range.closedOpen(0.6666666666666667, 1.0) to ExitCodeClause(3)
        ))
    }

    @Test fun `when running improbabilityDrive with number that dont quite add up to 100% should get appropriate warnings`(){

        //setup
        val drive = ImprobabilityDrive()

        //act
        val map = drive.makeRangeMap(listOf(
                ExecutionOutcome(0.30, ExitCodeClause(1)),
                ExecutionOutcome(0.20, ExitCodeClause(2))
        ))

        //assert
        assertThat(map.span()).isEqualTo(Range.closedOpen(0.0, 1.0))
        assertThat(map.asMapOfRanges()).hasSize(2)
        assertThat(map.asMapOfRanges()).isEqualTo(mapOf(
                Range.closedOpen(0.0, 0.60) to ExitCodeClause(1),
                Range.closedOpen(0.60, 1.0) to ExitCodeClause(2)
        ))
    }

    @Test fun `when running improbabilityDrive with with redundant behaviours should properly generate distribution`(){
        //setup
        val drive = ImprobabilityDrive()

        //act
        val map = drive.makeRangeMap(listOf(
                ExecutionOutcome(0.20, ExitCodeClause(42)),
                ExecutionOutcome(0.60, ExitCodeClause(10)),
                ExecutionOutcome(0.20, ExitCodeClause(42))
        ))

        //assert
        assertThat(map.span()).isEqualTo(Range.closedOpen(0.0, 1.0))
        assertThat(map.asMapOfRanges()).hasSize(3)
        assertThat(map.asMapOfRanges()).isEqualTo(mapOf(
                Range.closedOpen(0.0,  0.20) to ExitCodeClause(42),
                Range.closedOpen(0.20, 0.80) to ExitCodeClause(10),
                Range.closedOpen(0.80, 1.00) to ExitCodeClause(42)
        ))
    }
}