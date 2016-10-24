package com.empowerops.singularity

import java.nio.file.FileSystem
import java.nio.file.FileSystems

object Paths{

    var DefaultFS: FileSystem = FileSystems.getDefault()

    operator fun get(first: String, vararg more: String) = DefaultFS.getPath(first, *more)!!
    operator fun get(first: String) = DefaultFS.getPath(first, *emptyArray())!!
}