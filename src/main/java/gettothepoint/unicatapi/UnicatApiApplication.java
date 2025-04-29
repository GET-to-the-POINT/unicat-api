package gettothepoint.unicatapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan("gettothepoint.unicatapi.common.properties")
public class UnicatApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(UnicatApiApplication.class, args);
	}

}
