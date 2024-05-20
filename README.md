<p>
    <img alt="" src="https://raw.githubusercontent.com/sopra-fs24-group-20/sopra-fs24-group-20-client/main/src/images/categories_logo.png" /><br/>
</p>

## Introduction
We all used to play Stadt-Land-Fluss at school, with friends or family. That's where our enthusiasm for this game comes from.
We put a lot of effort into our project to give this classic game its own twist and to develop an aesthetic but simple UI.

Categories is a game where you can play with several people in a lobby. In our version, you also have the option of changing various setting variables such as round duration or the categories themselves.

As soon as everyone is ready, the game and the timer start. You have to insert matching words into the categories as quickly as possible. You are given a random letter and the position in which it must appear in a word.
If someone finishes before the timer runs out, you can press stop, which would end the round for all players.
Afterwards you can see the answers of your opponents and rate them.

You can compete with your friends by scoring lots of points and leveling up.

## Technologies
- [Node.js](https://nodejs.org/en/docs) - JavaScript runtime environment
- [React](https://react.dev/learn) - JavaScript library for building user interfaces
- [Google Cloud](https://cloud.google.com/appengine/docs/flexible) - Deployment
- [RESTful](https://restfulapi.net/) - Web services for user control
- [Websocket](https://spring.io/guides/gs/messaging-stomp-websocket/) -  Real-time bidirectional communication between client and server
- [MySQL](https://cloud.google.com/sql/docs/mysql) - Cloud SQL for MySQL used for the database
- [Wiktionary API](https://en.wiktionary.org/w/api.php) - Provides dictionary data

## High-level components
### WebSocketController
The [WebSocketController](https://github.com/sopra-fs24-group-20/sopra-fs24-group-20-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs24/WebSocketController.java) is an essential part of this project to enable quick data transfers, e.g. when joining or leaving a lobby, when players get ready for a round and to ensure that the game ends at the same time for all players when someone presses stop.

### RoundService
The [RoundService](https://github.com/sopra-fs24-group-20/sopra-fs24-group-20-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs24/service/RoundService.java) handles the functionalities for the [Round entity](https://github.com/sopra-fs24-group-20/sopra-fs24-group-20-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs24/entity/Round.java), e.g. creating and saving a round, generating a random letter and determining its position, doing the automatic part of the answer evaluation and adjusting the points according to the players votes. It's also the place where the Wiktionary API is being used to check whether the answers are grammatically correct.

### PlayerService
The [PlayerService](https://github.com/sopra-fs24-group-20/sopra-fs24-group-20-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs24/service/PlayerService.java) is responsible for the functionality involving a [Player entity](https://github.com/sopra-fs24-group-20/sopra-fs24-group-20-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs24/entity/Player.java), e.g. creating, update, log in, log out. This is crucial because the [Player entity](https://github.com/sopra-fs24-group-20/sopra-fs24-group-20-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs24/entity/Player.java) is the persistent part of our project.


## Launch & Deployment
### Prerequisites and Installation
#### Step 1 Setup with your IDE of choice
Download your IDE of choice (e.g., [IntelliJ](https://www.jetbrains.com/idea/download/), [Visual Studio Code](https://code.visualstudio.com/), or [Eclipse](http://www.eclipse.org/downloads/)). Make sure Java 17 is installed on your system (for Windows, please make sure your `JAVA_HOME` environment variable is set to the correct version of Java).

###### IntelliJ
If you consider to use IntelliJ as your IDE of choice, you can make use of your free educational license [here](https://www.jetbrains.com/community/education/#students).
1. File -> Open... -> SoPra server template
2. Accept to import the project as a `gradle project`
3. To build right click the `build.gradle` file and choose `Run Build`

###### VS Code
The following extensions can help you get started more easily:
-   `vmware.vscode-spring-boot`
-   `vscjava.vscode-spring-initializr`
-   `vscjava.vscode-spring-boot-dashboard`
-   `vscjava.vscode-java-pack`

**Note:** You'll need to build the project first with Gradle, just click on the `build` command in the _Gradle Tasks_ extension. Then check the _Spring Boot Dashboard_ extension if it already shows `soprafs24` and hit the play button to start the server. If it doesn't show up, restart VS Code and check again.

#### Step 2 Building with Gradle
You can use the local Gradle Wrapper to build the application.
-   macOS: `./gradlew`
-   Linux: `./gradlew`
-   Windows: `./gradlew.bat`

More Information about [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) and [Gradle](https://gradle.org/docs/). Then run:

```bash
./gradlew build
```

#### Step 3 Running with Gradle


```bash
./gradlew bootRun
```

You can verify that the server is running by visiting `localhost:8080` in your browser.

#### Step 4 Testing with Gradle

```bash
./gradlew test
```

### Development Mode
You can start the backend in development mode, this will automatically trigger a new build and reload the application
once the content of a file has been changed.

Start two terminal windows and run:

`./gradlew build --continuous`

and in the other one:

`./gradlew bootRun`

If you want to avoid running all tests with every change, use the following command instead:

`./gradlew build --continuous -xtest`

### API Endpoint Testing with Postman
We recommend using [Postman](https://www.getpostman.com) to test your API Endpoints.

### Debugging
If something is not working and/or you don't know what is going on. We recommend using a debugger and step-through the process step-by-step.

To configure a debugger for SpringBoot's Tomcat servlet (i.e. the process you start with `./gradlew bootRun` command), do the following:

1. Open Tab: **Run**/Edit Configurations
2. Add a new Remote Configuration and name it properly
3. Start the Server in Debug mode: `./gradlew bootRun --debug-jvm`
4. Press `Shift + F9` or the use **Run**/Debug "Name of your task"
5. Set breakpoints in the application where you need it
6. Step through the process one step at a time

### Testing
Have a look here: https://www.baeldung.com/spring-boot-testing

## Roadmap
- Ability to add other players as friends
- Explore other ways to join a lobby, such as through QR codes.
- Global Top-Player Ranking

## Authors and acknowledgment.
- [Giuliano Bernasconi](https://github.com/GiulianoBernasconi)
- [Joshua Stebler](https://github.com/Joshuastebler)
- [Joshua Weder](https://github.com/joswed)
- [Leonora Horvatic](https://github.com/LeoHorv)
- [Mirjam Alexandra Weibel](https://github.com/mirjamweibel)

We would like to thank our mentor [Fengjiao Ji](https://github.com/feji08) for supporting us throughout the project.

## License
This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details

