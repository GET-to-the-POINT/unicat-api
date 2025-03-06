package getToThePoint.unicatApi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import getToThePoint.unicatApi.common.propertie.AppProperties;

//유림 추가
@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class UnicatApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(UnicatApiApplication.class, args);
	}

}
