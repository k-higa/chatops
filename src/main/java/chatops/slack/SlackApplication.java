package chatops.slack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = {"me.ramswaroop.jbot", "chatops.slack"})
public class SlackApplication {

	public static void main(String[] args) {
		SpringApplication.run(SlackApplication.class, args);
	}
}
