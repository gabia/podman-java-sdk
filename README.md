# Podman Java SDK

Podman REST API를 사용하는 Java 클라이언트 라이브러리

## Requirements

- Java 21 이상
- 접근 가능한 Podman REST API 서비스 (Podman 2.0부터 제공)

## Supported APIs

| API            | Operations                                                                                                                                                                     |
| -------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `SystemApi`    | <ul><li>Ping service</li><li>Get info</li></ul>                                                                                                                                |
| `ContainerApi` | <ul><li>Check if container exists</li><li>Inspect container</li><li>List containers</li><li>Create a container</li><li>Start a container</li><li>Wait on a container</li></ul> |
| `PodApi`       | <ul><li>Pod exists</li><li>Inspect pod</li><li>List pods</li><li>Start a pod</li><li>Stop a pod</li><li>Restart a pod</li><li>Kill a pod</li><li>Remove pod</li></ul>          |
| `ImageApi`     | <ul><li>Image exists</li><li>Pull images</li></ul>                                                                                                                             |

## Modules

| 모듈                                   | 용도                                     |
| -------------------------------------- | ---------------------------------------- |
| `podman-java-sdk`                      | 코어 라이브러리(API, 모델, 연결 처리)    |
| `podman-java-sdk-jackson2`             | Jackson 2 통합 모듈                      |
| `podman-java-sdk-jackson3`             | Jackson 3 통합 모듈                      |
| `podman-java-sdk-spring-boot4-starter` | Spring Boot 4 Auto Configuration Starter |
| `podman-java-sdk-bom`                  | 의존성 버전 정합성 관리 모듈             |

Jackson 모듈과 스타터에는 코어 모듈이 전이 의존성으로 포함됩니다.

### Gradle Kotlin DSL

```kotlin
dependencies {
    implementation(platform("com.gabiacloud:podman-java-sdk-bom:0.2.0"))
    implementation("com.gabiacloud:podman-java-sdk-jackson2") // jackson 2 사용 시
    implementation("com.gabiacloud:podman-java-sdk-jackson3") // jackson 3 사용 시
    implementation("com.gabiacloud:podman-java-sdk-spring-boot4-starter") // Spring Boot 4 사용 시
}
```

## Quick Start

접근 가능한 Podman API 가 필요합니다. 다음은 기본 Unix 소켓에 연결하는 Jackson 3 예제입니다.

```java
import com.gabiacloud.podman.api.SystemApi;
import com.gabiacloud.podman.component.PodmanRequestExecutor;
import com.gabiacloud.podman.component.PodmanRequestProcessor;
import com.gabiacloud.podman.jackson3.Jackson3ErrorMessageResolver;
import com.gabiacloud.podman.jackson3.Jackson3ResponseBodyConverter;
import com.gabiacloud.podman.property.PodmanProperty;
import tools.jackson.databind.json.JsonMapper;

public class Example {
    public static void main(String[] args) {
        var properties = PodmanProperty.defaults();
        var mapper = JsonMapper.builder().build();
        var executor = new PodmanRequestExecutor(
                PodmanRequestProcessor.forHost(properties),
                new Jackson3ErrorMessageResolver(mapper));
        var system = new SystemApi(
                properties, executor, new Jackson3ResponseBodyConverter(mapper));

        system.ping();
        System.out.println(system.info());
    }
}
```

Jackson 2를 사용하려면 `podman-java-sdk-jackson2` 의존성을 추가합니다. 날짜·시간 처리에 필요한 `jackson-datatype-jsr310`은 전이 의존성으로 포함되며, 사용하는 Jackson 매퍼에 해당 모듈을 등록해야 합니다.

## Connection Settings

| 설정          | 기본값                           |
| ------------- | -------------------------------- |
| 호스트        | `unix:///run/podman/podman.sock` |
| 요청 타임아웃 | 30초                             |

`unix://`와 `tcp://` URI를 지원합니다. TCP 연결은 HTTP를 사용하며, unix 소켓은 실제 경로(예: `unix:///run/user/1000/podman/podman.sock`)를 지정합니다.

```java
import com.gabiacloud.podman.property.PodmanProperty;
import java.net.URI;
import java.time.Duration;

var properties = new PodmanProperty(
        URI.create("tcp://127.0.0.1:8080"),
        Duration.ofSeconds(60));
```

## Spring Boot 4

스타터 의존성을 추가합니다.

```kotlin
dependencies {
    implementation(platform("com.gabiacloud:podman-java-sdk-bom:0.2.0"))
    implementation("com.gabiacloud:podman-java-sdk-spring-boot4-starter")
}
```

`application.yml`에서 연결을 설정합니다.

```yaml
podman:
    host: tcp://127.0.0.1:8080
    timeout: 30s
```

`SystemApi`, `ContainerApi`, `PodApi`, `ImageApi`를 빈으로 주입받아 사용합니다.

```java
import com.gabiacloud.podman.api.SystemApi;
import org.springframework.stereotype.Service;

@Service
public class PodmanService {
    private final SystemApi system;

    public PodmanService(SystemApi system) {
        this.system = system;
    }

    public void checkConnection() {
        system.ping();
    }
}
```

## License

- [Apache License 2.0](LICENSE)
