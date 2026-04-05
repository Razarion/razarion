# Razarion E2E Tests

Selenium-based end-to-end tests that run against a live server.

## Prerequisites

- **Java 21**
- **Google Chrome** installed (ChromeDriver is managed automatically via WebDriverManager)
- **Docker** running (for MariaDB + MongoDB databases)

## Build & Start

### 1. Start databases

```bash
cd razarion-server/docker
docker-compose up -d
```

MariaDB: `localhost:32788`, MongoDB: `localhost:27017`

### 2. Build the project

From the project root (`razarion/`):

```bash
# Set JDK 21 environment (Git Bash on Windows)
export JAVA_HOME="C:\dev\tech\Java\java-21-openjdk-21.0.1.0.12-3.win.jdk.x86_64"
export PATH="/c/dev/tech/apache-maven-3.9.6/bin:/c/dev/tech/Java/java-21-openjdk-21.0.1.0.12-3.win.jdk.x86_64/bin:$PATH"

# Full build (two passes needed for generated resources)
mvn clean install -DskipTests
mvn clean install -DskipTests
```

Or use the PowerShell build script:

```powershell
.\build.ps1
```

The build produces:
- `razarion-server/target/razarion-server-0.0.1-SNAPSHOT.jar` — Spring Boot fat JAR
- `razarion-frontend/` — Angular frontend (bundled into the server JAR)
- `razarion-client-teavm/` — TeaVM WASM client (bundled into the server JAR)

### 3. Start the server

**Option A: Via Maven (development, auto-recompiles)**

```bash
cd razarion-server
mvn spring-boot:run -DskipTests
```

**Option B: Via JAR (faster startup, no Maven overhead)**

```bash
cd razarion-server
java -jar target/razarion-server-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

The server starts on `http://localhost:8080`. Wait until the log shows `Started RazarionServerApplication` before running tests. The `local` profile connects to the Docker databases.

> **Note:** A non-fatal bot config error (`Bot must have at least one enragement state configured: Mountain`) may appear in the log — this does not affect the E2E tests.

### 4. Run the tests

From the root directory `razarion/`:

```bash
# Headless (default), skip unit tests in other modules
mvn verify -Pe2e -DskipTests

# Single test method
mvn verify -Pe2e -DskipTests -Dit.test=GameStartIT#fullGameFlow -pl razarion-e2e-test

# With visible browser (quote the -D parameter on Windows)
mvn verify -Pe2e -DskipTests "-De2e.headless=false"

# With video recording (requires visible browser)
mvn verify -Pe2e -DskipTests "-De2e.headless=false" "-De2e.record=true"

# Against a different URL
mvn verify -Pe2e -DskipTests "-De2e.baseUrl=http://localhost:4200"
```

### Quick Reference

| Step | Command | Duration |
|------|---------|----------|
| Build (full, 2 passes) | `.\build.ps1` or `mvn clean install -DskipTests` x2 | ~4 min |
| Start server (JAR) | `java -jar razarion-server/target/razarion-server-0.0.1-SNAPSHOT.jar --spring.profiles.active=local` | ~30 sec |
| Run E2E tests | `mvn verify -Pe2e -DskipTests -pl razarion-e2e-test` | ~5 min |

## Screenshots and recording

- **Screenshots on failure**: Automatically saved to `target/e2e-reports/screenshots/` when a test fails. Works in both headless and non-headless mode.
- **Recording**: Captures periodic browser screenshots (every 250ms) during each test. Enable with `-De2e.record=true`. Frames are saved as PNG files to `target/e2e-reports/recordings/<TestName>/`. Works in both headless and non-headless mode.

## Structure

```
src/test/java/com/btxtech/e2e/
  config/WebDriverConfig.java       # WebDriver factory (Chrome, timeouts)
  base/BaseE2eTest.java             # Abstract base class (setup, navigation)
  base/AdminApiClient.java          # REST API cleanup (delete bases, restart planet)
  base/E2eTestWatcher.java          # JUnit extension (screenshots, recording, driver cleanup)
  base/BrowserRecorder.java         # Periodic browser screenshot recorder
  page/LandingPage.java             # Page object: Landing Page (/, play button)
  page/GamePage.java                # Page object: Game Page (canvas, loading, cockpit)
  smoke/LandingPageSmokeIT.java     # Smoke tests: title, logo, play button
  smoke/GameStartIT.java            # Game start tests: play button → canvas, full game load
```

### Conventions

- **Page Object Pattern**: Each page gets its own class under `page/`
- **Test suffix `*IT.java`**: Recognized by the Maven Failsafe Plugin as an integration test
- **New tests** extend `BaseE2eTest` (provides the WebDriver instance)

## Notes

- This module is only active under the Maven profile `e2e`. A regular `mvn clean install -DskipTests` will not compile it.
- `-DskipTests` skips unit tests (Surefire) in other modules but the E2E integration tests (Failsafe) still run because the e2e module explicitly overrides this.
- Each test gets its own browser instance (full isolation).
- After changes to server/WASM/frontend code, rebuild with `mvn clean install -DskipTests` and restart the server before running tests.
