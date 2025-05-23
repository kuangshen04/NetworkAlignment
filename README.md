# NetworkAlignment

A Java implementation of a genetic algorithm for graph matching and network alignment, using [JGraphT](https://jgrapht.org/) for graph operations.

## Usage

- The program finds the best mapping between two undirected graphs using a genetic algorithm.
- It can be used to align networks based on various criteria, such as node degree or edge weights.
- The main class generates random graphs to demonstrate the algorithm's functionality.

## Features

- Genetic algorithm for graph matching
- Customizable chromosome, crossover, mutation, and improvement operators
- Configurable population size, elitism, generations, and more

## Requirements

- Java 21 or higher
- Maven

## Dependencies

- [JGraphT Core 1.5.1](https://mvnrepository.com/artifact/org.jgrapht/jgrapht-core/1.5.1)

## Build

To build the project, run:

```sh
mvn clean package
```

## Run

To run the main program:

```sh
mvn exec:java -Dexec.mainClass="Main"
```

Or, if you use an IDE, run the `Main` class directly.

## Project Structure

- `src/main/java/algorithm/GraphMatchingGA.java` - Core genetic algorithm and interfaces
- `src/main/java/Main.java` - Entry point, example usage, and random graph generation
- `pom.xml` - Maven build configuration


## License

This project is for academic and research purposes.