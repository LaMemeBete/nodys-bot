package com.nodys.botscraper;

import com.nodys.botscraper.service.scraper.youtube.ScraperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BotScraperApplication implements CommandLineRunner {

    @Autowired
    ScraperService scraperService;

    public static void main(String[] args) {
        SpringApplication.run(BotScraperApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        scraperService.run();
    }
}
