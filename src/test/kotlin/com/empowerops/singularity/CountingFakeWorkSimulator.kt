package com.empowerops.singularity

import java.time.Duration

class CountingFakeWorkSimulator: WorkSimulator {

    var workBatches: List<Duration> = emptyList()
        private set

    override fun runFor(time: Duration) {
        workBatches += time
    }
}