package edu.udacity.java.nano.chat;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class WebSocketChatApplicationTest {

    private static final String URL = "http://localhost:8080/";

    private WebDriver webDriver;

    @Before
    public void setup() {
        System.setProperty("webdriver.chrome.driver", "chromedriver");
        webDriver = new ChromeDriver();
        webDriver.get(URL);
    }

    @Test
    public void chat() throws Exception {
        // for user1
        WebElement username = webDriver.findElement(By.id("username"));
        username.sendKeys("user1");
        WebElement login = webDriver.findElement(By.className("submit"));
        login.click();

        // wait
        Thread.sleep(2000);

        // verify welcome message
        WebElement wel_msg = webDriver.findElement(By.xpath("//i[contains(@id,'username')]"));
        Assert.assertTrue("Welcome message shown", wel_msg.getText().equals("user1"));
        // verify online count
        WebElement countElement = webDriver.findElement(By.xpath("//i[contains(@class,'chat-num')]"));
        int onlineCount = Integer.parseInt(countElement.getText());
        Assert.assertTrue("User count should one", onlineCount == 1);
        // verify joined chat
        WebElement join_msg = webDriver.findElement(By.xpath("//div[contains(@class,'message-content')]"));
        Assert.assertTrue("Join message should be shown", join_msg.getText().contains("user1 joined chat"));

        ((JavascriptExecutor) webDriver).executeScript("window.open()");
        ArrayList<String> tabs = new ArrayList<String>(webDriver.getWindowHandles());
        Thread.sleep(2000); // wait
        webDriver.switchTo().window(tabs.get(1));
        webDriver.get(URL);

        // for user2
        username = webDriver.findElement(By.id("username"));
        username.sendKeys("user2");
        login = webDriver.findElement(By.className("submit"));
        login.click();

        // wait
        Thread.sleep(2000);

        countElement = webDriver.findElement(By.xpath("//i[contains(@class,'chat-num')]"));
        onlineCount = Integer.parseInt(countElement.getText());
        Assert.assertTrue("User count should two", onlineCount == 2);

        // user2 send message
        WebElement msgInput = webDriver.findElement(By.id("msg"));
        msgInput.sendKeys("Hello");
        msgInput.sendKeys(Keys.ENTER);

        // switch tab to user1 to verify above message
        webDriver.switchTo().window(tabs.get(0));
        WebElement msgText = webDriver.findElement(By.xpath("//div[contains(.,'user2：Hello')]"));
        Assert.assertTrue("User 2 message displayed", msgText.isDisplayed());
    }

    @After
    public void finish() {
        webDriver.quit();
    }

}