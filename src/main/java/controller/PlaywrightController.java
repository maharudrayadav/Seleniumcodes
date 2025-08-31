package controller;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;
import org.springframework.web.bind.annotation.*;

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
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
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
}