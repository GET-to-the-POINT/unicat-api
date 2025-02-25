package taeniverse.unicatApi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

//유림 추가
@EnableScheduling
@SpringBootApplication
public class UnicatApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(UnicatApiApplication.class, args);
	}

}
