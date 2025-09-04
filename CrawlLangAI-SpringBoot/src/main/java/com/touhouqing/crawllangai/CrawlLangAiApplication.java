package com.touhouqing.crawllangai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@SpringBootApplication
@EnableNeo4jRepositories("com.touhouqing.crawllangai.repository")
public class CrawlLangAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrawlLangAiApplication.class, args);
    }

}
