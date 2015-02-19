package cz.vse.startup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@EnableAutoConfiguration
@ImportResource("classpath:/application-config.xml")
public class Application {

    @Autowired
    JdbcTemplate template;

    private final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    public void initialize() {
    }

    @PreDestroy
    public void destroy() {
        template.execute("SHUTDOWN");
    }
}
