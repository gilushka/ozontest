import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class OzonTest {
    WebDriver driver;
    String baseUrl;

    @Before
    public void beforeTest() {
        System.setProperty("webdriver.chrome.driver", "drv/chromedriver.exe");
        baseUrl = "https://www.ozon.ru/";
        driver = new ChromeDriver();

        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        driver.manage().window().maximize();
        //Переходим на главную страницу
        driver.get(baseUrl);
    }

    @Test
    public void testCase() throws InterruptedException {
        Wait<WebDriver> wait = new WebDriverWait(driver, 10, 1000);
        String msiBrand = "MSI";
        String razerBrand = "Razer";

        //Кликаем в меню на раздел Электроника
        driver.findElement(By.xpath("//ul[@class='c6c9']/li/a[text()='Электроника']")).click();
        wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("//h1[normalize-space(text())='Электроника']"))));

        //Кликаем на раздел Ноутбуки и планшеты (по непонятным причинам иногда этот блок не отображается, поэтому для дальнейшего движения приходится использовать условие
        String noteAndPadMenuPath = "//div[@class='c6a0']//a[normalize-space(text())='Ноутбуки и планшеты']";
        if (!driver.findElements(By.xpath(noteAndPadMenuPath)).isEmpty()) {
            driver.findElement(By.xpath(noteAndPadMenuPath)).click();
            wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("//h1[normalize-space(text())='Ноутбуки, планшеты и электронные книги']"))));

            //Переходим в раздел Игровые ноутбуки
            driver.findElement(By.xpath("//div[@class='cv7']//a[normalize-space(text())='Игровые ноутбуки']")).click();
            wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("//h1[normalize-space(text())='Игровые ноутбуки']"))));
        } else {
            //Переходим сразу в раздел Игровые ноутбуки
            driver.findElement(By.xpath("//a[normalize-space(text())='Игровые ноутбуки']")).click();
            wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("//h1[normalize-space(text())='Игровые ноутбуки']"))));
        }

        String searchBasePath = "//div[@class='b7n filter-block']/div[contains(text(),'Бренды')]/following-sibling::div";

        //Устанавливаем чекбокс на MSI (при клике на чекбокс довольно часто не активируется скрипт. Необходимо перегрузить страницу. Не смог придумать адекватный способ обойти эту проблему)
        driver.findElement(By.xpath(searchBasePath + "//span[@class='show']")).click();
        WebElement searchBrand = driver.findElement(By.xpath(searchBasePath + "/div/input"));
        searchBrand.clear();
        searchBrand.sendKeys(msiBrand);
        driver.findElement(By.xpath(searchBasePath + "//span[normalize-space(text())='" + msiBrand + "']")).click();
        wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("//span[text()='Бренды: " + msiBrand + "']"))));

        //Устанавливаем чекбокс на Razer
        searchBrand = driver.findElement(By.xpath(searchBasePath + "/div/input"));
        searchBrand.clear();
        searchBrand.sendKeys(razerBrand);
        driver.findElement(By.xpath(searchBasePath + "//span[normalize-space(text())='" + razerBrand + "']")).click();
        wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("//span[text()='Бренды: " + razerBrand + "']"))));

        String firstElementPath = "//div[@class='widget-search-result-container ao3']/div/div[1]";

        //Устанавливаем сортировку (такой способ приходится использовать потому что элемент не Select, а задается через скрипт
        driver.findElement(By.xpath("//div[@class='b6i1 b5y1']//div[@class='_1w2B']/input")).click();
        for(int i = 0; i < 3; i++) {
            Actions actions = new Actions(driver);
            actions.sendKeys(Keys.DOWN).build().perform();
        }

        Actions actions = new Actions(driver);
        actions.sendKeys(Keys.ENTER).build().perform();

        Thread.sleep(5000);

        //Сохраняем цену убрав ненужные символы между цифрами
        String price = driver.findElement(By.xpath(firstElementPath + "//div[@class='b5v4 a5d2 item a1d1']/span[@class='b5v6 b5v7 c4v8']")).getText();
        price = getPriceValue(price, " ");

        //Открываем новое окно
        Set<String> oldTabs = driver.getWindowHandles();
        Actions newWin = new Actions(driver);
        WebElement link = driver.findElement(By.xpath(firstElementPath + "//div[2]/div/a"));
        newWin
                .keyDown(Keys.CONTROL)
                .click(link)
                .keyUp(Keys.CONTROL)
                .build()
                .perform();
        Set<String> newTabs = driver.getWindowHandles();
        newTabs.removeAll(oldTabs);
        driver.switchTo().window(newTabs.iterator().next());

        WebElement title = driver.findElement(By.xpath("//h1[@class='b3a8']"));
        wait.until(ExpectedConditions.visibilityOf(title));

        String newTabPrice = driver.findElement(By.xpath("//span[@class='c8q7 c8q8']/span")).getText();
        newTabPrice = getPriceValue(newTabPrice, " ");
        Assert.assertEquals("Цена ноутбука на странице поиска отличается от цены в карточке товара!", price, newTabPrice);

        String color = driver.findElement(By.xpath("//span[text()='Цвет: ']/following-sibling::span")).getText();
        String oS = driver.findElement(By.xpath("//span[text()='Операционная система']/../following-sibling::dd")).getText();

        System.out.println("Цвет ноутбука: " + color);
        System.out.println("Операционная система: " + oS);
    }

    public String getPriceValue(String price, String symbol) {
        String[] aPrice = price.split(symbol);
        price = "";
        for (int i = 0; i < aPrice.length; i++)
            price = price + aPrice[i];
        return price;
    }

    @After
    public void afterTest() {
        driver.quit();
    }
}
