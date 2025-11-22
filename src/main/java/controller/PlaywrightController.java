package controller;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
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
                    new BrowserType.LaunchOptions()
                            .setHeadless(true)
                            .setArgs(Arrays.asList("--disable-blink-features=AutomationControlled"))
            );

            Page page = browser.newPage();

            page.navigate("https://www.naukri.com/nlogin/login");
            page.waitForLoadState(LoadState.NETWORKIDLE);

            page.waitForSelector("#usernameField", new Page.WaitForSelectorOptions().setTimeout(60000));
            page.fill("#usernameField", username);
            page.fill("#passwordField", password);
            page.click("button:has-text('Login')");
            page.waitForTimeout(6000);

            page.navigate("https://www.naukri.com/mnjuser/profile");
            page.waitForLoadState(LoadState.NETWORKIDLE);

            String name = page.inputValue("input[placeholder='Enter Your Name']");
            browser.close();

            return name;
        } catch (Exception ex) {
            return "Error: " + ex.getMessage();
        }
    }

    @PostMapping("/uploadResume")
    public ResponseEntity<String> uploadResume(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String driveLink) {

        try {
            uploadResumeProcess(email, password, driveLink);
            return ResponseEntity.ok("Resume Upload Successfully Started!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    public void uploadResumeProcess(String email, String password, String driveLink) throws Exception {

        String resumePath = downloadGoogleDriveFile(driveLink);

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions()
                            .setHeadless(true)
                            .setArgs(Arrays.asList("--disable-blink-features=AutomationControlled"))
            );

            Page page = browser.newPage();
            page.setExtraHTTPHeaders(Map.of(
                    "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome Safari"
            ));

            page.navigate("https://www.naukri.com/nlogin/login");
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);

            page.waitForSelector("#usernameField", new Page.WaitForSelectorOptions().setTimeout(60000));
            page.fill("#usernameField", email);
            page.fill("#passwordField", password);
            page.click("button:has-text('Login')");
            page.waitForTimeout(7000);

            page.navigate("https://www.naukri.com/mnjuser/profile");
            page.waitForTimeout(7000);

            page.evaluate("window.scrollBy(0, 800)");
            page.waitForSelector("input[type='file']");

            page.setInputFiles("input[type='file']", Paths.get(resumePath));

            page.click("button:has-text('Save'), button:has-text('Upload'), span:has-text('Save')");
            page.waitForTimeout(6000);

            System.out.println("Resume uploaded successfully");
            browser.close();
        }
    }

    private String downloadGoogleDriveFile(String driveLink) throws Exception {

        String fileId = driveLink.split("/d/")[1].split("/")[0];
        String downloadUrl = "https://drive.google.com/uc?export=download&id=" + fileId;

        String targetPath = System.getProperty("java.io.tmpdir") + "/resume.pdf";
        File file = new File(targetPath);

        try (InputStream in = new URL(downloadUrl).openStream();
             FileOutputStream fos = new FileOutputStream(file)) {

            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        }

        System.out.println("Resume path: " + file.getAbsolutePath());
        return file.getAbsolutePath();
    }
}
