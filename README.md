# singularity
A testing application designed to emulate the behaviour of various simulation applications, such as ANSYS and MATLAB. In this sense singularity acts as a simple dynamic-argument count mathematical function, taking in some number of decimal inputs and producing one or more decimal outputs. 

## Running singularity

A sample usage of this application would look something like this:

```
singularity.exe --generator RosenbrockAdaptiveN --exit 80% code:0 --exit 20% hang
```

which would cause singularity to run the [generalized rosenbrock function](https://en.wikipedia.org/wiki/Rosenbrock_function) on key-value pairs found in `./input.txt` and write those values to `./output.txt` (relative to the working directory of the process)

you can also pass these arguments to singularity, and direct singularity to ignore arguments passed to it at the command line, like this:

```
singularity.exe -b -dir A/B/C -i input.apdl -o ANSYSLOG.out
```

where, given the working directory for the process is set to `/a/workingdir`, then you have the file `/a/workingdir/singularity.exclude.config`

```
#indicate that we need to ignore the values passed at the command line
--ignoreCLI
#also use a longer running time value -> typical of what ANSYS does
--input
./input.txt
#specifies the files, in standard ini/properties key-value pair format, that singularity should read and write from
--output
./data.txt
#amount of time the process is to run for
--time 
2000
#set the process to exit with code 0 for 20% of the time
--exit
20%
code:0
#set the process to exit with code 8 for 80% of the time
--exit
80%
code:8
```


## Building
To fully build singularity you require standard JDK tooling, gradle, and a copy of exe4j. To run the tests, only the former two are needed. 
   
Exe4j is a tool that allows for the creation of a single exe file that will use the systems JDK and any supplied & embedded jars to run a java program, allowing us to avoid the java syntax at invocation time. 

- `exe`: To create an `exe4j` configured binary, install exe4j, and create a file in this directory called `local.properties` that contains an entry for `exe4j.home` pointing to the installed directory of exe4j:

  ```
  #local.properties
  exe4j.home=C:/Program Files/exe4j
  ```

  Once you've done that, the task 
  
  `gradle createLauncherWin` 
  
  will generate an exectuable file under `/build/exe`. 
 
- `jar`: If you do not have `exe4j` then the task 

  `gradle shadowJar` 
  
  will create a monolithic jar file that can be used with the standard `java --jar` style invocation.  

## Testing
Singularity has a suite of integration tests against it. It uses [JimFS](https://github.com/google/jimfs) and a set of parameters for the `main` method to do full system testing. There are also a handful of unit tests for the more complex components.

These can be run simply with 

`gradle test`

## Future work

_there are a lot of things I would like to do with this application_

- [ ] support prefab embedded commands, such as 

  `singularity.exe ansys -b -i thing -o anotherthing` and 
  
  `singularity.exe matlab -nosplash -wait -automation -r : cd('C:\Users\[your directory here]\MATLAB Sample');run;exit`
  
- [ ] allow CLI validation, such that singularity emits some kind of failure if a `-b` is missing or a `-r` is not specified with a well formatted argument 
- [ ] allow non-file based IO, including stdin/stdout and pipes 
- [ ] replace JCommander with getoptk!!