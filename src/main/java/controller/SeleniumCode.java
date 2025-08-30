package controller;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;


@RestController
@RequestMapping("/api/code")
public class SeleniumCode {

    @PostMapping("/open-chrome")
    public String openChrome(@RequestBody Map<String, String> body) throws InterruptedException, IOException {
        String username = body.get("username");
        String runHeadless = System.getenv().getOrDefault("HEADLESS", "true");

        ChromeOptions options = new ChromeOptions();
        if (runHeadless.equalsIgnoreCase("true")) {
            options.addArguments("--headless=new");
        }

        // Force desktop mode
//        options.addArguments("--disable-gpu");
//        options.addArguments("--no-sandbox");
//        options.addArguments("--disable-dev-shm-usage");
//        options.addArguments("--remote-allow-origins=*");
//        options.addArguments("--window-size=1920,1080");
//        options.addArguments(
//                "user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139 Safari/537.36"
//        );

        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139 Safari/537.36");

        WebDriver driver = new ChromeDriver(options);
        driver.get("https://www.naukri.com");
        new WebDriverWait(driver, Duration.ofSeconds(30))
                .until(webDriver -> ((JavascriptExecutor) webDriver)
                        .executeScript("return document.readyState").equals("complete"));
//        takeScreenshot(driver, "step2_after_login_click.png");
        Thread.sleep(5000);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(),'Login')] | //div[text()='Login'] | //button[text()='Login']")
        ));
        loginBtn.click();
//        takeScreenshot(driver, "step2_after_login_click.png");
        Thread.sleep(2000);
        WebElement usernameField = driver.findElement(
                By.xpath("//input[@placeholder='Enter your active Email ID / Username']")
        );
        usernameField.sendKeys(username);
        WebElement password = driver.findElement(
                By.xpath("//input[@placeholder='Enter your password']")
        );
        password.sendKeys(body.get("password"));
        WebElement loginB = driver.findElement(By.xpath("//button[text()='Login']"));
        loginB.click();
        Thread.sleep(2000);
        WebDriverWait waits = new WebDriverWait(driver, Duration.ofSeconds(30));
        WebElement viewProfile = waits.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'drawer__bars')]")
        ));
        viewProfile.click();
        Thread.sleep(2000);
        WebElement view = driver.findElement(By.xpath("//a[@class='nI-gNb-info__sub-link']"));
        view.click();
        Thread.sleep(2000);
        WebElement edit = driver.findElement(By.xpath("//em[text()='editOneTheme']"));
        edit.click();
        Thread.sleep(2000);
        WebElement nameField = driver.findElement(By.id("name"));
        String currentValue = nameField.getAttribute("value");
        System.out.println("Current Name: " + currentValue);
        return currentValue;
    }
    private void takeScreenshot(WebDriver driver, String fileName) throws IOException {
        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        Files.copy(screenshot.toPath(), Paths.get(fileName));
        System.out.println("Saved screenshot: " + fileName);
    }
}

