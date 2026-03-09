package site.klade.webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class KladeWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(KladeWebApplication.class, args);
    }
}
