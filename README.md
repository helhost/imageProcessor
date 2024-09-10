# ics432imgapp

This is a simple Java/JavaFX Application with which users can apply filters
to batches of image (jpg) files, and which serves as a basis for
all ICS432 programming assignments. 

The application is structured as a Maven project, with all source code in the 
`src/` directory and the Maven configuration in the `pom.xml` file.

The `external_filters` directory contains additional image filters, not necessarily implemented in Java, 
which will be relevant for later programming assignment. See `README` file within for details.

### (for me) run to use java21 
export JAVA_HOME=$HOME/OpenJDK/jdk-21.0.2.jdk/Contents/Home

# Homework 2:1

Invert:
- total execution time: 869ms
- processing time: 231ms
- writing time: 459ms
- reading time: 174ms
- Compute intesiveness: 0.36

Solarize:
- total execution time: 872ms
- processing time: 232ms
- writing time: 471ms
- reading time: 163ms
- Compute intesiveness: 0.37

Oil4:
- total execution time: 14511
- processing time: 13925
- writing time: 420
- reading time: 162ms
- Compute intesiveness:24.93

NOTE: there is a 5-6ms difference between the mesaured total time and the sum of the reading, writing and processing time. 

Ranking filter in order of decreasing compute intesiveness: (Oil4, Solarize, Invert)
