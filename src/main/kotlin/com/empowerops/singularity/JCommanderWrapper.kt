package com.empowerops.singularity

import com.beust.jcommander.IStringConverter
import com.beust.jcommander.JCommander
import com.beust.jcommander.ParameterException
import java.nio.file.Path
import javax.inject.Inject

class JCommanderWrapper @Inject constructor() {

    fun <T> parse(args: Array<String>, config: () -> T): Pair<JCommander, T?>{

        val configInstance = config()

        val commander = JCommander(configInstance).apply {

            val converter = lazy { ExitClauseConverter("--exit", 0) }

            addConverterInstanceFactory { parameter, clazz ->
                if("--exit" in parameter.names) converter.value
                else null
            }

            addConverterInstanceFactory { parameter, clazz ->
                if(clazz.isAssignableTo<Path>()) IStringConverter { value: String -> Paths[value] }
                else null
            }
        }

        try {
            commander.parse(*args)
        }
        catch(ex: ParameterException) {
            println(ex.message)
            commander.usage()

            return commander to null
        }

        return commander to configInstance
    }

    fun Class<*>.isAssignableTo(other: Class<*>) = other.isAssignableFrom(this)
    inline fun <reified R: Any> Class<*>.isAssignableTo() = R::class.java.isAssignableFrom(this)


}

