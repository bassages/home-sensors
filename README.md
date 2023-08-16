# home-sensors

Reads data from (1) Smart Meter and (2) Texas Instruments SensorTag (CC2540/CC2541) and uploads the data to [home-server](https://github.com/bassages/home-server).
Application is intended to run on a on a Raspberry PI.

![Build home-sensors](https://github.com/bassages/home-sensors/workflows/Build%20home-sensors/badge.svg)
<a href="https://sonarcloud.io/dashboard?id=home-sensors" target="_blank"><img src="https://sonarcloud.io/api/project_badges/measure?project=home-sensors&metric=coverage" alt="SonarCloud"></a>
<a href="https://sonarcloud.io/dashboard?id=home-sensors" target="_blank"><img src="https://sonarcloud.io/api/project_badges/measure?project=home-sensors&metric=code_smells" alt="SonarCloud"></a>
<a href="https://sonarcloud.io/dashboard?id=home-sensors" target="_blank"><img src="https://sonarcloud.io/api/project_badges/measure?project=home-sensors&metric=bugs" alt="SonarCloud"></a>
<a href="https://sonarcloud.io/dashboard?id=home-sensors" target="_blank"><img src="https://sonarcloud.io/api/project_badges/measure?project=home-sensors&metric=vulnerabilities" alt="SonarCloud"></a>

## Common development tasks

### Update versions

#### Gradle
For this, you can use the following command: `gradlew wrapper --gradle-version <new version>`

### Dependencies
For this, you can use the following command: `gradlew dependencyUpdates`.
Inspect the output of the command, update build.gradle manually and test if the application still builds and executes correctly.
