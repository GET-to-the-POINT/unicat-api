package gettothepoint.unicatapi.email.config;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainers 기반 MailPit 컨테이너 구현.
 * 테스트에서 실제 메일 서버처럼 동작하는 격리된 환경을 제공합니다.
 *
 * 지원 이미지: {@code axllent/mailpit}
 *
 * 노출된 포트:
 * <li>SMTP: 1025</li>
 * <li>웹 UI: 8025</li>
 */
public class MailContainer extends GenericContainer<MailContainer> {

    private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("axllent/mailpit");
    private static final int SMTP_PORT = 1025;
    private static final int WEB_PORT = 8025;

    /**
     * 기본 이미지로 MailContainer를 생성합니다.
     * 
     * @param dockerImageName 사용할 도커 이미지 이름
     */
    public MailContainer(final String dockerImageName) {
        this(DockerImageName.parse(dockerImageName));
    }

    /**
     * 커스텀 이미지로 MailContainer를 생성합니다.
     * 
     * @param dockerImageName 사용할 도커 이미지 이름
     */
    public MailContainer(final DockerImageName dockerImageName) {
        super(dockerImageName);
        dockerImageName.assertCompatibleWith(DEFAULT_IMAGE_NAME);
        withExposedPorts(SMTP_PORT, WEB_PORT);
        waitingFor(
            Wait.forHttp("/").forPort(WEB_PORT).withStartupTimeout(Duration.of(30, ChronoUnit.SECONDS))
        );
    }

    /**
     * @return SMTP 호스트 URL
     */
    public String getSmtpHost() {
        return getHost();
    }

    /**
     * @return 매핑된 SMTP 포트
     */
    public int getSmtpPort() {
        return getMappedPort(SMTP_PORT);
    }

    /**
     * @return 웹 UI URL
     */
    public String getWebUrl() {
        return String.format("http://%s:%d", getHost(), getMappedPort(WEB_PORT));
    }
}
