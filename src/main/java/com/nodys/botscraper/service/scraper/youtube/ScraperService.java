package com.nodys.botscraper.service.scraper.youtube;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.nodys.botscraper.dto.youtube.Suggestion;
import com.nodys.botscraper.service.kafka.KafkaProducer;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ScraperService {
    private final String kafkaTopic;
    private final KafkaProducer kafkaProducer;
    private WebDriverWait wait;
    private JavascriptExecutor js;
    private WebDriver driver;
    private Map<String, Object> vars;

    public ScraperService(KafkaProducer kafkaProducer,
                          @Value(value = "${youtube-scraper.topic}") String kafkaTopic) {

        this.kafkaProducer = kafkaProducer;
        this.kafkaTopic = kafkaTopic;
    }

    /**
     * @param seed
     */
    public void run(String seed) {
        System.setProperty("webdriver.chrome.driver", new File("C:\\local\\workspaces\\nodyes/chromedriver.exe").getAbsolutePath());
        driver = new ChromeDriver();
        js = (JavascriptExecutor) driver;
        vars = new HashMap<String, Object>();
        wait = new WebDriverWait(driver, 40);
        this.scrape(seed);

    }

    /**
     * @param suggestion
     */
    public void send(Suggestion suggestion) {

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            String json = ow.writeValueAsString(suggestion);
            kafkaProducer.send(kafkaTopic, json);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

    }

    /**
     * @param seed
     */
    private void scrape(String seed) {
        Instant now = Instant.now();
        openUrlAndConfirmCookiesPolicy();
        driver.findElement(By.name("search_query")).click();

        {
            WebElement element = driver.findElement(By.cssSelector(".ytd-masthead > .yt-simple-endpoint > #button > #button > .style-scope"));
            Actions builder = new Actions(driver);
            builder.moveToElement(element).perform();
        }
        {
            WebElement element = driver.findElement(By.tagName("body"));
            Actions builder = new Actions(driver);
            builder.moveToElement(element, 0, 0).perform();
        }
        driver.findElement(By.name("search_query")).sendKeys(seed);
        driver.findElement(By.id("container")).click();
        driver.findElement(By.id("search-icon-legacy")).click();
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        WebElement ytdThumbnail = driver.findElement(By.xpath("/html/body/ytd-app/div/ytd-page-manager/ytd-search/div[1]/ytd-two-column-search-results-renderer/div/ytd-section-list-renderer/div[2]/ytd-item-section-renderer/div[3]/ytd-video-renderer[1]/div[1]/ytd-thumbnail/a"));
        ytdThumbnail.click();

        for (int i = 0; i < 10000; i++) {
            commonExtractData(i + 1, now, seed);
        }


        driver.close();
    }

    /**
     *
     */
    private void openUrlAndConfirmCookiesPolicy() {
        driver.get("https://www.youtube.com/");
        driver.manage().window().setSize(new Dimension(1106, 1004));
        {
            WebElement element = driver.findElement(By.cssSelector(".buttons > .style-scope:nth-child(1) #button"));
            Actions builder = new Actions(driver);
            builder.moveToElement(element).perform();
        }
        {
            WebElement element = driver.findElement(By.tagName("body"));
            Actions builder = new Actions(driver);
            builder.moveToElement(element, 0, 0).perform();
        }
        js.executeScript("window.scrollTo(0,10)");
        driver.findElement(By.cssSelector(".buttons > .style-scope:nth-child(1) #button")).click();
        List<WebElement> elementName = driver.findElements(By.xpath("//span[text()='Activer']"));
        if (elementName != null && elementName.size() > 0) {
            for (WebElement webElement : elementName) {
                try {
                    webElement.click();
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
            driver.findElement(By.xpath("//span[text()='Confirmer']")).click();
        }
        driver.findElement(By.cssSelector("#search-form > #container")).click();
    }

    /**
     *
     */
    private void commonExtractData(int iteration, Instant now, String seed) {
        {
            wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.xpath("/html/body/ytd-app/div/ytd-page-manager/ytd-watch-flexy/div[5]/div[2]/div/div[3]/ytd-watch-next-secondary-results-renderer/div[2]/ytd-compact-video-renderer"), 1));

            List<WebElement> ytdCompactVideoRenderer = driver.findElements(By.xpath("/html/body/ytd-app/div/ytd-page-manager/ytd-watch-flexy/div[5]/div[2]/div/div[3]/ytd-watch-next-secondary-results-renderer/div[2]/ytd-compact-video-renderer"));

            for (WebElement webElement : ytdCompactVideoRenderer) {
                try {
                    Suggestion suggestion = new Suggestion();
                    suggestion.setIdExp(now.toString());
                    WebElement thumbnail = webElement.findElement(By.id("thumbnail"));
                    String href = thumbnail.getAttribute("href");
                    suggestion.setHref(thumbnail.getAttribute("href"));
                    suggestion.setVideoId(href.split("v=")[1]);
                    suggestion.setIterNumber(iteration);
                    suggestion.setSeed(seed);
                    this.send(suggestion);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }

            }

        }
        {
            int min = 1;
            int max = 5;
            Random random = new Random();
            int value = random.nextInt(max + min) + min;
            WebElement ytdThumbnail = driver.findElement(By.xpath("/html/body/ytd-app/div/ytd-page-manager/ytd-watch-flexy/div[5]/div[2]/div/div[3]/ytd-watch-next-secondary-results-renderer/div[2]/ytd-compact-video-renderer[" + value + "]/div[1]/ytd-thumbnail/a"));
            ytdThumbnail.click();
        }
    }
}
