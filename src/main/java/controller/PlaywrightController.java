package controller;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/code")
public class PlaywrightController {

    @PostMapping("/open-chrome")
    public String openChrome(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        try (Playwright playwright = Playwright.create()) {

            // Launch Chromium in headless mode for Render deployment
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions()
                            .setHeadless(true)  // MUST be true on Render
                            .setArgs(List.of(
                                    "--no-sandbox",
                                    "--disable-dev-shm-usage",
                                    "--disable-gpu",
                                    "--disable-software-rasterizer",
                                    "--disable-setuid-sandbox",
                                    "--disable-features=VizDisplayCompositor",
                                    "--single-process",
                                    "--disable-background-timer-throttling",
                                    "--disable-backgrounding-occluded-windows",
                                    "--disable-renderer-backgrounding"
                            ))
            );

            Page page = browser.newPage();
            page.setDefaultTimeout(90000); // Increased timeout for Render

            // Open Naukri website
            page.navigate("https://www.naukri.com");
            page.waitForLoadState(LoadState.NETWORKIDLE);

            // Click Login button
            Locator loginBtn = page.locator("//a[normalize-space(text())='Login']");
            loginBtn.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(90000));
            loginBtn.click();

            // Enter credentials
            page.locator("//input[@placeholder='Enter your active Email ID / Username']").fill(username);
            page.locator("//input[@placeholder='Enter your password']").fill(password);

            // Click Login button
            page.locator("//button[text()='Login']").click();

            // Wait for profile drawer to appear
            Locator viewProfile = page.locator("//div[contains(@class,'drawer__bars')]");
            viewProfile.waitFor(new Locator.WaitForOptions().setTimeout(60000));
            viewProfile.click();

            // Go to profile view
            page.locator("//a[@class='nI-gNb-info__sub-link']").click();

            // Click edit option and fetch current name
            page.locator("//em[text()='editOneTheme']").click();
            String currentName = page.locator("#name").inputValue();

            browser.close();
            return currentName;

        } catch (Exception e) {
            e.printStackTrace();
            return "Failed: " + e.getMessage();
        }
    }
}
