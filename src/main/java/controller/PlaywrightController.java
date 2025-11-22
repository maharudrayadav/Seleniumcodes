package controller;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;
import org.springframework.web.bind.annotation.*;
import java.io.File;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/code")
public class PlaywrightController {

    @PostMapping("/open-chrome")
    public String openChrome(@RequestBody Map<String, String> body) throws InterruptedException {
        String username = body.get("username");
        String password = body.get("password");
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            Page page = browser.newPage();
            page.navigate("https://www.naukri.com/mnjuser/login");

            page.fill("//input[@id='usernameField']\n", username);
            page.fill("//input[@id='passwordField']", password);
            page.click("//button[text()='Login']");
            Thread.sleep(2000);
            page.navigate("https://www.naukri.com/mnjuser/profile");
            page.click("//em[text()='editOneTheme']");

            // Fetch the displayed name, adjust selector based on actual HTML
            // Using the placeholder to locate the input field
            String name = page.inputValue("input[placeholder='Enter Your Name']");
            System.out.println(name);
            browser.close();
            return name;

        }
    }
     @PostMapping("/uploadResume")
    public ResponseEntity<String> uploadResume(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String driveLink) {

        try {
            uploadResumeProcess(email, password, driveLink);
            return ResponseEntity.ok("Resume Upload Started Successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    public void uploadResumeProcess(String email, String password, String driveLink) throws Exception {

        // Step 1: Download file from Google Drive
        String resumePath = downloadGoogleDriveFile(driveLink);

        // Step 2: Start Selenium
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--headless=new");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--start-maximized");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121 Safari/537.36");

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        options.setExperimentalOption("prefs", prefs);

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(35));
        JavascriptExecutor js = (JavascriptExecutor) driver;

        try {
            System.out.println("=== Step 1: LOGIN ===");
            driver.get("https://www.naukri.com/nlogin/login");

            js.executeScript("Object.defineProperty(navigator,'webdriver',{get:()=>undefined})");

            WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("usernameField")));
            WebElement passwordInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("passwordField")));
            WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(text(),'Login')]")));

            emailInput.sendKeys(email);
            passwordInput.sendKeys(password);
            loginBtn.click();
            Thread.sleep(8000);

            System.out.println("Navigating to Profile Page...");
            driver.get("https://www.naukri.com/mnjuser/profile");
            Thread.sleep(5000);

            js.executeScript("window.scrollBy(0,800)");

            WebElement fileUpload = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@type='file']")));
            fileUpload.sendKeys(resumePath);

            WebElement saveResumeBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(text(),'Save')] | //button[contains(text(),'Upload')] | //span[contains(text(),'Save')]")));

            saveResumeBtn.click();
            Thread.sleep(6000);

            System.out.println("✓ Resume Updated Successfully!");

        } catch (Exception e) {
            System.out.println("✗ Error: " + e.getMessage());
            throw e;
        } finally {
            driver.quit();
            System.out.println("✓ Browser Closed");
        }
    }

    private String downloadGoogleDriveFile(String driveLink) throws Exception {

        String fileId = driveLink.split("/d/")[1].split("/")[0];
        String downloadUrl = "https://drive.google.com/uc?export=download&id=" + fileId;

        String targetPath = "D:/UploadedResume/resume.pdf";   // final save location

        File file = new File(targetPath);
        file.getParentFile().mkdirs();

        try (InputStream in = new URL(downloadUrl).openStream();
             FileOutputStream fos = new FileOutputStream(file)) {

            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        }

        return file.getAbsolutePath();
    }

}
