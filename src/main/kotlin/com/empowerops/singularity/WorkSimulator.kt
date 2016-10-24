package com.empowerops.singularity

import java.time.Duration

interface WorkSimulator {

    fun runFor(time: Duration);

    object Sleeping: WorkSimulator {
        override fun runFor(time: Duration) = Thread.sleep(time.toMillis())
    }
}