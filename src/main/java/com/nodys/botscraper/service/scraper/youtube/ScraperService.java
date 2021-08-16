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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public void run() {
        System.setProperty("webdriver.chrome.driver", new File("C:\\local\\workspaces\\nodyes/chromedriver.exe").getAbsolutePath());
        driver = new ChromeDriver();
        js = (JavascriptExecutor) driver;
        vars = new HashMap<String, Object>();
        wait = new WebDriverWait(driver, 40);
        this.scrape();

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
     *
     */
    private void scrape() {
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
        driver.findElement(By.name("search_query")).sendKeys("michael jackson");
        driver.findElement(By.id("container")).click();
        driver.findElement(By.id("search-icon-legacy")).click();
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        WebElement ytdThumbnail = driver.findElement(By.xpath("/html/body/ytd-app/div/ytd-page-manager/ytd-search/div[1]/ytd-two-column-search-results-renderer/div/ytd-section-list-renderer/div[2]/ytd-item-section-renderer/div[3]/ytd-video-renderer[1]/div[1]/ytd-thumbnail/a"));
        ytdThumbnail.click();

        for (int i = 0; i < 10000; i++) {
            commonExtractData();
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
    private void commonExtractData() {
        {
            wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.xpath("/html/body/ytd-app/div/ytd-page-manager/ytd-watch-flexy/div[5]/div[2]/div/div[3]/ytd-watch-next-secondary-results-renderer/div[2]/ytd-compact-video-renderer"), 1));

            List<WebElement> ytdCompactVideoRenderer = driver.findElements(By.xpath("/html/body/ytd-app/div/ytd-page-manager/ytd-watch-flexy/div[5]/div[2]/div/div[3]/ytd-watch-next-secondary-results-renderer/div[2]/ytd-compact-video-renderer"));

            for (WebElement webElement : ytdCompactVideoRenderer) {
                try {
                    Suggestion suggestion = new Suggestion();
                    List<WebElement> children = webElement.findElements(By.xpath("./child::*"));
                    WebElement thumbnail = webElement.findElement(By.id("thumbnail"));
                    suggestion.setHref(thumbnail.getAttribute("href"));
                    if (children != null && children.size() > 0) {
                        String[] lines = children.get(0).getText().split("\\r?\\n");
                        if (lines != null) {
                            if (lines.length >= 5) {
                                /**
                                 * 4:59
                                 * Michael Jackson - Beat It (Official Video)
                                 * Michael Jackson
                                 * 741 M de vues
                                 * il y a 10 ans
                                 */
                                suggestion.setDuration(lines[0]);
                                suggestion.setName(lines[1]);
                                suggestion.setAuthor(lines[2]);
                                suggestion.setNumberViews(lines[3]);
                                suggestion.setSince(lines[4]);

                            } else if (lines.length == 4) {
                                /**
                                 * Michael Jackson - Beat It (Official Video)
                                 * Michael Jackson
                                 * 741 M de vues
                                 * il y a 10 ans
                                 */
                                suggestion.setName(lines[0]);
                                suggestion.setAuthor(lines[1]);
                                suggestion.setNumberViews(lines[2]);
                                suggestion.setSince(lines[3]);

                            }
                        } else {
                            suggestion.setText(children.get(0).getText());
                        }
                    }
                    this.send(suggestion);

                } catch (Exception e) {
                    log.error(e.getMessage());
                }

            }


        }
        {
            WebElement ytdThumbnail = driver.findElement(By.xpath("/html/body/ytd-app/div/ytd-page-manager/ytd-watch-flexy/div[5]/div[2]/div/div[3]/ytd-watch-next-secondary-results-renderer/div[2]/ytd-compact-video-renderer[1]/div[1]/ytd-thumbnail/a"));
            ytdThumbnail.click();
        }
    }
}
