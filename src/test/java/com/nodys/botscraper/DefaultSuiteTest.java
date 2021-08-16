package com.nodys.botscraper;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Ignore
public class DefaultSuiteTest {
    static WebDriverWait wait;
    JavascriptExecutor js;
    private WebDriver driver;
    private Map<String, Object> vars;

    /* static {
         System.setProperty("webdriver.gecko.driver", findFile("geckodriver.exe"));
     }*/
    static private String findFile(String filename) {
        String[] paths = {"", "bin/", "target/classes"};
        for (String path : paths) {
            if (new File(path + filename).exists())
                return path + filename;
        }
        return "";
    }

    @Before
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", new File("C:\\local\\workspaces\\nodyes/chromedriver.exe").getAbsolutePath());
        driver = new ChromeDriver();
        js = (JavascriptExecutor) driver;
        vars = new HashMap<String, Object>();
        wait = new WebDriverWait(driver, 40);
    }

    @After
    public void tearDown() {
        driver.quit();
    }


    private void extracted() {
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


    @Test
    public void salah() {
        extracted();
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

        for (int i = 0; i < 5; i++) {
            extractedss();
        }


        driver.close();
    }

    private void extractedss() {
        {
            wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.xpath("/html/body/ytd-app/div/ytd-page-manager/ytd-watch-flexy/div[5]/div[2]/div/div[3]/ytd-watch-next-secondary-results-renderer/div[2]/ytd-compact-video-renderer"), 1));

            List<WebElement> ytdCompactVideoRenderer = driver.findElements(By.xpath("/html/body/ytd-app/div/ytd-page-manager/ytd-watch-flexy/div[5]/div[2]/div/div[3]/ytd-watch-next-secondary-results-renderer/div[2]/ytd-compact-video-renderer"));

            for (WebElement webElement : ytdCompactVideoRenderer) {
                try {
                    List<WebElement> children = webElement.findElements(By.xpath("./child::*"));
                    WebElement thumbnail = webElement.findElement(By.id("thumbnail"));
                    thumbnail.getAttribute("href");
                    for (WebElement webElementId : children) {
                        log.info("" + webElementId.getText());
                    }
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
