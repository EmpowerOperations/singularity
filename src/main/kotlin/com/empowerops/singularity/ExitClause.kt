package com.empowerops.singularity

/**
 * Created by Geoff on 2016-10-13.
 */
interface ExitClause

data class ExitCodeClause(val exitCode: Int) : ExitClause { override fun toString() = "code:$exitCode" }
object HungClause : ExitClause { override fun toString() = "HungClause" }

val ExitZero = ExitCodeClause(0)
