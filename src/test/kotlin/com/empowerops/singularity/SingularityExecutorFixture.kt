package com.empowerops.singularity

import com.google.common.jimfs.Jimfs
import dagger.Component
import dagger.Module
import dagger.Provides
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.ThrowableAssert
import org.junit.*
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.spi.FileSystemProvider
import java.time.Duration

/**
 * This suite represents an integration test against the whole of the singularity program.
 *
 * I wrote it in this way because:
 * - I think synchronous CLI programs are really nice to test when you simply do a whole-system test on them
 * - I'm lazy
 *
 * Created by Geoff on 2016-10-20.
 */
class SingularityExecutorFixture {

    lateinit var fileSystem: FileSystem
    lateinit var workSimulator: CountingFakeWorkSimulator

    @Before fun `setup path environment and fake work simulator`(){
        Paths.DefaultFS = Jimfs.newFileSystem() //note: default working dir is C:/Work to jimfs.

        fileSystem = Paths.DefaultFS
        workSimulator = CountingFakeWorkSimulator()

        assertThat(Paths["a/b"]).isInstanceOf(Class.forName("com.google.common.jimfs.JimfsPath"))
    }

    @After fun `restore default file system`(){
        Paths.DefaultFS = FileSystems.getDefault()
    }

    @Test fun `when running with no explicit params should run adaptive rosenbrock for 200 ms`(){

        //setup
        val instance = makeExecutor()
        fileSystem["./input.properties"] = listOf("x1=1.234", "x2=3.456")

        //act
        val exitCode = instance.execute(arrayOf(""))

        //assert
        assertThat(exitCode).isEqualTo(0)
        assertThat(fileSystem["./output.properties"]).contains("f1=11476.304848409598")
        assertThat(workSimulator.workBatches).containsExactly(200.ms)
    }

    @Test fun `when running rosenbrock 10 for 20 seconds should properly run evaluation`(){

        //setup
        val instance = makeExecutor()
        fileSystem["a/b/c.properties"] = (1..10).map { "x$it=0.$it" }

        //act
        val exitCode = instance.execute(arrayOf(
                "--input", "a/b/c.properties",
                "--output", "a/b/out.properties",
                "--time", "20000",
                "--generator", "RosenbrockX1ThroughX10",
                "--exit", "100%", "code:8"
        ))

        //assert
        assertThat(exitCode).isEqualTo(8)
        assertThat(fileSystem["a/b/out.properties"]).contains("f1=124.98000000000002")
        assertThat(workSimulator.workBatches).containsExactly(20000.ms)
    }

    @Test fun `when running rosenbrock to hang should properly attempt to hang evaluation`(){
        //setup
        val instance = makeExecutor()
        fileSystem["a/b/c.properties"] = (1..10).map { "x$it=0.$it" }

        //act -- note this code isnt actually executed until the "assert" section below.
        val act = ThrowableAssert.ThrowingCallable { instance.execute(arrayOf(
                "--input", "a/b/c.properties",
                "--exit", "100%", "hang"
        ))}

        //assert
        assertThatThrownBy(act).isInstanceOf(ControlFlowHanged::class.java)
    }

    @Test fun `when command is specified entirely through a file should properly load and run`(){
        //setup
        val instance = makeExecutor()
        fileSystem["singularity.config"] =
                """--input
                  |cmd/a/b/in.properties
                  |--output
                  |cmd/a/b/out.properties
                  |--time
                  |1990
                  |--exit
                  |100%
                  |code:9
                  """.trimMargin().lines()
        fileSystem["cmd/a/b/in.properties"] = (1..10).map { "parameter_$it=0.${it*it}" }


        //act
        val exitCode = instance.execute(emptyArray())

        //assert
        assertThat(exitCode).isEqualTo(9)
        assertThat(workSimulator.workBatches).containsExactly(1990.ms)
        assertThat(fileSystem["cmd/a/b/out.properties"]).contains("f1=133.40001500000002")
    }

    @Test fun `when command is specified both at cli and file but told to ignore cli should load and run`(){
        //setup
        val instance = makeExecutor()
        fileSystem["singularity.config"] =
                """#This is a comment to describe why input is important
                  |--input
                  |cmd/a/b/in.properties
                  |--output
                  |#and this is a comment describing why this output is important
                  |cmd/a/b/out.properties
                  |--time
                  |1990
                  |--exit
                  |100%
                  |code:9
                  |--ignoreCLI
                  """.trimMargin().lines()
        fileSystem["cmd/a/b/in.properties"] = (1..10).map { "parameter_$it=0.${it*it}" }

        //act
        val exitCode = instance.execute(arrayOf(
                "--time", "200", "--garbage", "Attempts_to_fuckup_our_parser", "-b", "-QuamquatsRAweomseDawg"
        ))

        //assert
        assertThat(exitCode).isEqualTo(9)
        assertThat(workSimulator.workBatches).containsExactly(1990.ms)
        assertThat(fileSystem["cmd/a/b/out.properties"]).contains("f1=133.40001500000002")
    }

    @Test fun `when spcifying text files should read and write as if properties files`(){
        //setup
        val instance = makeExecutor()
        fileSystem["./input.txt"] = (1..10).map { "x$it=0.$it" }

        //act -- note this code isnt actually executed until the "assert" section below.
        val result = instance.execute(arrayOf(
                "--input", "./input.txt",
                "--output", "./data.txt",
                "--exit", "100%", "code:0"
        ))

        //assert
        assertThat(result).isEqualTo(0)
        assertThat(fileSystem["C:/work/data.txt"]).contains("f1=118.67999999999999")
    }

    private fun makeExecutor(): SingularityExecutor {
        return DaggerTestExecutorFactory
                .builder()
                .singularityTestModule(SingularityTestModule(fileSystem.provider(), workSimulator))
                .build()
                .makeTestExecutor()
    }

    operator fun FileSystem.get(path: String) : List<String> = Files.readAllLines(getPath(path))
    operator fun FileSystem.set(path: String, lines: List<String>){
        val path = getPath(path)
        if(path.parent != null) { Files.createDirectories(path.parent) }
        Files.write(path, lines)
    }

    val Int.ms: Duration get() = Duration.ofMillis(this.toLong())
}

@Component(modules = arrayOf(SingularityTestModule::class))
interface TestExecutorFactory { fun makeTestExecutor(): SingularityExecutor }

@Module class SingularityTestModule(
        val fileSystemInstance: FileSystemProvider,
        val workSimulator: WorkSimulator
) {

    @Provides fun makeFileSystemProvider() = fileSystemInstance
    @Provides fun makeWorkSimulator() = workSimulator
    @Provides fun makeHangman(): Hangwoman = ErroredHangwoman
}


