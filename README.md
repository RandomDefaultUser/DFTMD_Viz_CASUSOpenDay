# DFT-MD trajectory visualization for CASUS OpenDay

This is a visualization script/project to visualize a DFT-MD trajectory for the CASUS Open Day.

## Description

The main class of the project resides in `src/main/kotlin/graphics/scenery/DFTMDVisualizationCASUSOpenDay.kt`, while the Gradle build configuration is specified in `build.gradle.kts`.
This visualization is intended to be used with electronic density cube files from a trajectory of a Beryllium super cell with 128 atoms at the melting point (1560 K). The data set can be provided if desired - the reason I am not hosting it is purely due to size limitations. If you are interested, I'll gladly work with you to give you access to it.

## Building and running

On the command line, the project can be built by running
```bash
./gradlew build
```

The resulting application, consisting of the single class `DFTMDVisualizationCASUSOpenDay` can then be run via
```bash
./gradlew run
```

Of course, the Gradle project defined in `build.gradle.kts` can also be imported into an IDE such as [IntelliJ](https://www.jetbrains.com/idea).

## License

DFTMDVisualizationCASUSOpenDay is distributed under a
[Simplified BSD License](http://en.wikipedia.org/wiki/BSD_licenses);
for the full text of the license, see
[LICENSE.txt](https://github.com/imagej/imagej/blob/master/LICENSE.txt).

For the list of ImageJ developers and contributors, see
[the parent POM](https://github.com/imagej/pom-imagej/blob/master/pom.xml).

