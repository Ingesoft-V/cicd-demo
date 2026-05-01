package au.com.equifax.cicddemo;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;

@Disabled("Selenium hub is not available in local/basic jenkins environment")
public class SeleniumExampleTest {
    private WebDriver driver;

    @Test
    public void testOs () {
        System.out.println("APP_URL :"+System.getenv("APP_URL"));
        if (System.getenv("APP_URL") != null) {
            driver.get(System.getenv("APP_URL"));
            String bodyText = driver.findElement(By.tagName("body")).getText();
            Assertions.assertTrue(bodyText.contains("Linux"));
            Assertions.assertFalse(bodyText.contains("Windows"));
        }
    }

    @BeforeEach
    public void beforeTest() throws MalformedURLException {
        driver = new RemoteWebDriver(new URL("http://selenium:4444/wd/hub"), new ChromeOptions());
    }

    @AfterEach
    public void afterTest() {
        if (driver != null) {
            driver.quit();
        }
    }
}
