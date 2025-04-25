package gettothepoint.unicatapi.email.config;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainers implementation for MailPit.
 *
 * Supported image: {@code axllent/mailpit}
 *
 * Exposed ports:
 * <li>SMTP: 1025</li>
 * <li>Web: 8025</li>
 */
public class MailContainer extends GenericContainer<MailContainer> {

    private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("axllent/mailpit");
    private static final int SMTP_PORT = 1025;
    private static final int WEB_PORT = 8025;

    /**
     * Constructs a MailContainer with the default image.
     */
    public MailContainer(final String dockerImageName) {
        this(DockerImageName.parse(dockerImageName));
    }

    /**
     * Constructs a MailContainer with a custom image.
     * @param dockerImageName the full image name to use
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
     * @return the SMTP host URL
     */
    public String getSmtpHost() {
        return getHost();
    }

    /**
     * @return the mapped SMTP port
     */
    public int getSmtpPort() {
        return getMappedPort(SMTP_PORT);
    }

    /**
     * @return the Web UI URL
     */
    public String getWebUrl() {
        return String.format("http://%s:%d", getHost(), getMappedPort(WEB_PORT));
    }
}
