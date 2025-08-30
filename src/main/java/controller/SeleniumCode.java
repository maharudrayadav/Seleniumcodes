package controller;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/code")
public class SeleniumCode {

    @PostMapping("/open-chrome")
    public String openChrome(@RequestBody Map<String, String> body) throws InterruptedException, IOException {
        String username = body.get("username");
        String passwordValue = body.get("password");
        String runHeadless = System.getenv().getOrDefault("HEADLESS", "true");

        ChromeOptions options = new ChromeOptions();
        if (runHeadless.equalsIgnoreCase("true")) {
            options.addArguments("--headless=new");
        }

        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139 Safari/537.36");

        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://www.naukri.com");

            // Wait until the page loads completely
            new WebDriverWait(driver, Duration.ofSeconds(40))
                    .until(webDriver -> ((JavascriptExecutor) webDriver)
                            .executeScript("return document.readyState").equals("complete"));

            // Extra buffer for dynamic content
            Thread.sleep(3000);

            // Close any popup if present
            closePopupIfPresent(driver);

            // Wait for login button with retry
            WebElement loginBtn = waitForElementWithRetry(driver,
                    By.xpath("//a[contains(text(),'Login')] | //div[text()='Login'] | //button[text()='Login']"),
                    40);

            loginBtn.click();
            Thread.sleep(2000);

            WebElement usernameField = new WebDriverWait(driver, Duration.ofSeconds(20))
                    .until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//input[@placeholder='Enter your active Email ID / Username']")));
            usernameField.sendKeys(username);

            WebElement passwordField = driver.findElement(
                    By.xpath("//input[@placeholder='Enter your password']"));
            passwordField.sendKeys(passwordValue);

            WebElement loginButton = driver.findElement(By.xpath("//button[text()='Login']"));
            loginButton.click();

            // Wait until profile drawer becomes clickable
            WebElement viewProfile = new WebDriverWait(driver, Duration.ofSeconds(40))
                    .until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//div[contains(@class,'drawer__bars')]")));
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

        } catch (TimeoutException e) {
            String screenshotPath = takeScreenshot(driver, "login_timeout.png");
            throw new RuntimeException(
                    "Login button was not clickable within the given time. Check screenshot at " + screenshotPath, e);
        } finally {
            driver.quit();
        }
    }

    private void closePopupIfPresent(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement popupCloseBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//div[contains(@class,'crossIcon')] | //span[text()='×']")));
            popupCloseBtn.click();
            Thread.sleep(1000);
        } catch (Exception ignored) {
            // No popup found — safe to continue
        }
    }

    private WebElement waitForElementWithRetry(WebDriver driver, By locator, int timeoutSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        for (int i = 0; i < 2; i++) {
            try {
                return wait.until(ExpectedConditions.elementToBeClickable(locator));
            } catch (TimeoutException e) {
                System.out.println("Retrying to find element: " + locator);
            }
        }
        throw new TimeoutException("Element not found after retry: " + locator);
    }

    private String takeScreenshot(WebDriver driver, String fileName) throws IOException {
        // Save to static/screenshots folder
        String relativePath = "src/main/resources/static/screenshots";
        File dir = new File(relativePath);

        if (!dir.exists()) {
            dir.mkdirs(); // create if not exists
        }

        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        String fullPath = relativePath + "/" + fileName;

        Files.copy(screenshot.toPath(), Paths.get(fullPath), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        System.out.println("Screenshot saved at: " + fullPath);

        // Return URL for accessing via browser
        return "/screenshots/" + fileName;
    }


}
