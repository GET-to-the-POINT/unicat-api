package gettothepoint.unicatapi;

import gettothepoint.unicatapi.test.config.TestDummyTextToSpeechConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestDummyTextToSpeechConfiguration.class)
class UnicatApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
