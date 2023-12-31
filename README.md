# ⚠️ Hello there, fellow TUW connoisseurs
> note to all TUW students that found this in the github search:
> this solution is pretty overengineered in the aspect of modelling protocols, so using that would be easily detected as plagiarism.
> feel free to use as inspiration though, i really like the way it turned out!

distributed systems lab
=======================

Using gradle
------------

### Compile & Test

Gradle is the build tool we are using. Here are some instructions:

Compile the project using the gradle wrapper:

    ./gradlew assemble

Compile and run the tests:

    ./gradlew build

This command will inevitably run tests twice, once each Test Class individually, and once through the Test Suite (`Lab1Suite`).
Therefore, you can run individual tests or the test suite by executing:

    ./gradlew test --tests dslab.Lab1Suite

Or individual tests, for example:

    ./gradlew test --tests dslab.transfer.TransferServerTest

### Run the applications

The gradle config contains several tasks that start application components for you.
You can list them with

    ./gradlew tasks --all

And search for 'Other tasks' starting with `run-`. For example, to run the monitoring server, execute:
(the `--console=plain` flag disables CLI features, like color output, that may break the console output when running a interactive application)

    ./gradlew --console=plain run-monitoring
