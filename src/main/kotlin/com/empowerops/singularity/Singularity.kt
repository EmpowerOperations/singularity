package com.empowerops.singularity

import com.beust.jcommander.JCommander
import dagger.Module
import dagger.Provides
import sun.nio.fs.DefaultFileSystemProvider
import java.nio.file.FileSystem
import java.nio.file.spi.FileSystemProvider

/**
 * Created by Geoff on 2016-10-13.
 */
fun main(args: Array<String>) {

    val factory: SingularityExecutor.Factory = DaggerSingularityExecutor_Factory.create()

    val result = factory.makeExecutor().execute(args)

    System.exit(result)
}

@Module class SingularityModule(){
    @Provides fun makeFileSystemProvider():FileSystemProvider = DefaultFileSystemProvider.create()
    @Provides fun makeWorkSimulator(): WorkSimulator = WorkSimulator.Sleeping
    @Provides fun makeHangman(): Hangwoman = LiveHangwoman
}