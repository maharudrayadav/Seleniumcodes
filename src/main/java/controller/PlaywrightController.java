package controller;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/code")
public class PlaywrightController {

    @PostMapping("/open-chrome")
    public String openChrome(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(true)
            );
            Page page = browser.newPage();
            page.setDefaultTimeout(60000);

            // Open Naukri
            page.navigate("https://www.naukri.com");
            page.waitForLoadState(LoadState.NETWORKIDLE);
            new Locator.WaitForOptions().setTimeout(45000);

            // Click login button
            Locator loginBtn = page.locator("//a[text()='Login']");
            loginBtn.waitFor(new Locator.WaitForOptions().setTimeout(45000));
            loginBtn.click();

            // Enter credentials
            page.locator("//input[@placeholder='Enter your active Email ID / Username']").fill(username);
            page.locator("//input[@placeholder='Enter your password']").fill(password);

            // Click login
            page.locator("//button[text()='Login']").click();

            // Wait for profile drawer
            Locator viewProfile = page.locator("//div[contains(@class,'drawer__bars')]");
            viewProfile.waitFor(new Locator.WaitForOptions().setTimeout(45000));
            viewProfile.click();

            // Go to profile view
            page.locator("//a[@class='nI-gNb-info__sub-link']").click();

            // Edit and fetch name
            page.locator("//em[text()='editOneTheme']").click();
            String currentName = page.locator("#name").inputValue();

            browser.close();
            return currentName;
        }
    }
}
