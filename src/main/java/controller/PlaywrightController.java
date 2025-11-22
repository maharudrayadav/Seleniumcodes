package controller;

import com.microsoft.playwright.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

@RestController
@RequestMapping("/api/code")
public class PlaywrightController {

    @PostMapping("/open-chrome")
    public String openChrome(@RequestBody Map<String, String> body) throws InterruptedException {
        String username = body.get("username");
        String password = body.get("password");

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false)
            );
            Page page = browser.newPage();

            page.navigate("https://www.naukri.com/mnjuser/login");

            page.fill("#usernameField", username);
            page.fill("#passwordField", password);
            page.click("button:has-text('Login')");
            page.waitForLoadState(LoadState.NETWORKIDLE);

            page.navigate("https://www.naukri.com/mnjuser/profile");
            page.waitForLoadState(LoadState.NETWORKIDLE);

            String name = page.inputValue("input[placeholder='Enter Your Name']");
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

        // Step 1: Download file
        String resumePath = downloadGoogleDriveFile(driveLink);

        // Step 2: Playwright Browser
        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions()
                            .setHeadless(false)
                            .setArgs(new String[]{
                                    "--disable-blink-features=AutomationControlled",
                                    "--start-maximized"
                            })
            );

            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            System.out.println("=== Step 1: LOGIN ===");

            page.navigate("https://www.naukri.com/nlogin/login");
            page.waitForLoadState(LoadState.NETWORKIDLE);

            page.fill("#usernameField", email);
            page.fill("#passwordField", password);
            page.click("button:has-text('Login')");
            page.waitForTimeout(5000);

            System.out.println("Navigating to Profile Page...");
            page.navigate("https://www.naukri.com/mnjuser/profile");
            page.waitForTimeout(5000);

            // Scroll & upload file
            page.evaluate("window.scrollBy(0, 800)");
            page.waitForTimeout(2000);

            page.setInputFiles("input[type='file']", resumePath);

            page.click("button:has-text('Save'), button:has-text('Upload'), span:has-text('Save')");
            page.waitForTimeout(6000);

            System.out.println("✓ Resume Updated Successfully!");

            browser.close();
        }
    }


    private String downloadGoogleDriveFile(String driveLink) throws Exception {

        String fileId = driveLink.split("/d/")[1].split("/")[0];
        String downloadUrl = "https://drive.google.com/uc?export=download&id=" + fileId;

        String targetPath = "D:/UploadedResume/resume.pdf";
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
