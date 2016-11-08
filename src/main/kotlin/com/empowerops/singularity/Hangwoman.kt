package com.empowerops.singularity

interface Hangwoman {
    fun hang(): Nothing
}

object LiveHangwoman : Hangwoman {
    override fun hang(): Nothing { while(true); }
}

object ErroredHangwoman : Hangwoman {
    override fun hang(): Nothing = throw ControlFlowHanged()
}

class ControlFlowHanged : Throwable("code requested that control flow become hanged an in environment where that is not allowed")