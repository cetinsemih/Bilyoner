package com.bilyoner.step;

import com.bilyoner.base.BaseTest;
import com.bilyoner.base.BaseTest;
import com.bilyoner.model.ElementInfo;
import com.thoughtworks.gauge.Logger;
import com.thoughtworks.gauge.Step;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

public class BaseSteps extends BaseTest {

    public static int DEFAULT_MAX_ITERATION_COUNT = 150;
    public static int DEFAULT_MILLISECOND_WAIT_AMOUNT = 100;
    private List<String> stringList = new ArrayList<>();
    private List<Integer> integerList = new ArrayList<>();

    private static String SAVED_ATTRIBUTE;

    private String compareText;

    private String strTut;
    private  int   intTut;
    private String userName;
    private String password;

    private List<String> unClikabledList = new ArrayList<>();

    public BaseSteps() {
        initMap(getFileList());
    }

    public String getAttribute(By locator, String attributeName) {
        return driver.findElement(locator).getAttribute(attributeName);
    }

    List<WebElement> findElements(String key) {
        return driver.findElements(getElementInfoToBy(findElementInfoByKey(key)));
    }

    @Step({"Go to <url> address",
            "<url> adresine git"})
    public void goToUrl(String url) {
        driver.get(url);
        logger.info(url + " adresine gidiliyor.");
    }

    public By getElementInfoToBy(ElementInfo elementInfo) {
        By by = null;
        if (elementInfo.getType().equals("css")) {
            by = By.cssSelector(elementInfo.getValue());
        } else if (elementInfo.getType().equals(("name"))) {
            by = By.name(elementInfo.getValue());
        } else if (elementInfo.getType().equals("id")) {
            by = By.id(elementInfo.getValue());
        } else if (elementInfo.getType().equals("xpath")) {
            by = By.xpath(elementInfo.getValue());
        } else if (elementInfo.getType().equals("linkText")) {
            by = By.linkText(elementInfo.getValue());
        } else if (elementInfo.getType().equals(("partialLinkText"))) {
            by = By.partialLinkText(elementInfo.getValue());
        }
        return by;
    }

    WebElement findElement(String key) {
        By infoParam = getElementInfoToBy(findElementInfoByKey(key));
        WebDriverWait webDriverWait = new WebDriverWait(driver, 60);
        WebElement webElement = webDriverWait
                .until(ExpectedConditions.presenceOfElementLocated(infoParam));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center', inline: 'center'})",
                webElement);
        return webElement;
    }

    private void clickElement(WebElement element) {
        element.click();
    }


    @Step({"Wait <value> seconds",
            "<int> saniye bekle"})
    public void waitBySeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void hoverElement(WebElement element) {
        actions.moveToElement(element).build().perform();
    }

    @Step({"Click to element <key>",
            "<key> elementine tikla",})
    public void clickElement(String key) {
        By infoParam = getElementInfoToBy(findElementInfoByKey(key));
        WebDriverWait wait = new WebDriverWait(driver, 20);
        wait.until(ExpectedConditions.visibilityOfElementLocated(infoParam));
        if (!key.isEmpty()) {
            hoverElement(findElement(key));
            clickElement(findElement(key));
            waitBySeconds(1);
            logger.info(key + " elementine tıklandı.");
        }
    }

    @Step({"Write value <text> to element <key>",
            "<text> textini <key> elemente yaz"})
    public void sendKeys(String text, String key) {
        if (!key.equals("")) {
            findElement(key).sendKeys(text);
            logger.info(key + " elementine ''" + text + "'' texti yazıldı.");
        }
    }

    @Step({"Is <text> from alert window? Is controlled.",
            "Uyari penceresinde <text> yazisi var mi? Kontrol edilir."})
    public void alertTextControl(String text) {
        assertEquals(text, driver.switchTo().alert().getText());
    }

    @Step({"Accept to alert.",
            "Uyarıyı kabul et."})
    public void alertAccept() {
        driver.switchTo().alert().accept();
    }

    @Step({"Dismiss to alert.",
            "Uyarıyı reddet."})
    public void alertDismiss() {
        driver.switchTo().alert().dismiss();
    }

    @Step({"Send ENTER key to element <key>",
            "Elemente ENTER keyi yolla <key>"})
    public void sendKeyToElementEnter(String key) {
        findElement(key).sendKeys(Keys.ENTER);
        logger.info(key + " elementine ENTER keyi yollandı.");
    }

    @Step({"Check if element <key> contains text <expectedText>",
            "<key> elementi <text> değerini içeriyor mu kontrol et"})
    public void checkElementContainsText(String key, String expectedText) {

        Boolean containsText = findElement(key).getText().contains(expectedText);
        assertTrue(containsText, "Expected text is not contained");
        logger.info(key + " elementi ''" + expectedText + "'' değerini içeriyor.");
    }

    @Step({"Wait <value> milliseconds",
            "<long> milisaniye bekle"})
    public void waitByMilliSeconds(long milliseconds) {
        try {
            logger.info(milliseconds + " milisaniye bekleniyor.");
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Step({"Check if <key> element's attribute <attribute> contains the value <expectedValue>",
            "<key> elementinin <attribute> niteliği <value> değerini içeriyor mu"})
    public void checkElementAttributeContains(String key, String attribute, String expectedValue) {
        WebElement element = findElement(key);

        String actualValue;
        int loopCount = 0;
        while (loopCount < DEFAULT_MAX_ITERATION_COUNT) {
            actualValue = element.getAttribute(attribute).trim();
            if (actualValue.contains(expectedValue)) {
                return;
            }
            loopCount++;
            waitByMilliSeconds(DEFAULT_MILLISECOND_WAIT_AMOUNT);
        }
        Assertions.fail("Element's attribute value doesn't contain expected value");
    }

    private JavascriptExecutor getJSExecutor() {
        return (JavascriptExecutor) driver;
    }

    //Javascript scriptlerinin çalışması için gerekli fonksiyon
    private Object executeJS(String script, boolean wait) {
        return wait ? getJSExecutor().executeScript(script, "") : getJSExecutor().executeAsyncScript(script, "");
    }

    //Belirli bir locasyona sayfanın kaydırılması
    private void scrollTo(int x, int y) {
        String script = String.format("window.scrollTo(%d, %d);", x, y);
        executeJS(script, true);
    }

    //Belirli bir elementin olduğu locasyona websayfasının kaydırılması
    public WebElement scrollToElementToBeVisible(String key) {
        ElementInfo elementInfo = findElementInfoByKey(key);
        WebElement webElement = driver.findElement(getElementInfoToBy(elementInfo));
        if (webElement != null) {
            scrollTo(webElement.getLocation().getX(), webElement.getLocation().getY() - 100);
        }
        return webElement;
    }


    @Step({"<key> alanına kaydır"})
    public void scrollToElement(String key) {
        WebElement element = findElement(key);
        if(!key.isEmpty())
        {
            {
                scrollToElementToBeVisible(key);
                logger.info(key + " elementinin olduğu alana kaydırıldı");
            }while (!element.isDisplayed());
            scrollToElementToBeVisible(key);
            waitBySeconds(1);
            findElement(key).isDisplayed();
            waitBySeconds(1);
        }
    }



    @Step({"<key> alanına js ile kaydır"})
    public void scrollToElementWithJs(String key) {
        ElementInfo elementInfo = findElementInfoByKey(key);
        WebElement element = driver.findElement(getElementInfoToBy(elementInfo));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
    }

    @Step("<key> elementinin textini string listesine kaydet")
    public void saveTextByKey(String key) {
        ElementInfo elementInfo = findElementInfoByKey(key);
        String text = driver.findElement(getElementInfoToBy(elementInfo)).getText();
        String text2 = text.replaceAll(" ", "");
        stringList.add(text2);
        logger.info(text2 + " texti listeye kaydedildi");
    }

    @Step("<key> elementini int listesine kaydet")
    public void saveIntByKey(String key) {
        ElementInfo elementInfo = findElementInfoByKey(key);
        String text = driver.findElement(getElementInfoToBy(elementInfo)).getText();
        String text2 = text.replaceAll("\\.", "");
        String text3 = text2.replaceAll(",", "");
        String text4 = text3.replaceAll(" TL", "");
        integerList.add(Integer.parseInt(text4));
        logger.info(text4 + " sayısı listeye kaydedildi");
    }

    @Step("<key> li elementlerden random birine tikla ve stringini listeye kaydet")
    public void clickRandomByKey(String key) {
        List<WebElement> elements = findElements(key);
        Random random = new Random();
        int randomNumber = random.nextInt(elements.size());
        String text = elements.get(randomNumber).getText();
        stringList.add(text);
        elements.get(randomNumber).click();
    }

    @Step("String listenin <index1>. elemaninin <index2>. elemanini icerip icermedigini karsilastir")
    public void compareTwoString(int index1, int index2) {
        assertTrue(stringList.get(index1 - 1).contains(stringList.get(index2 - 1)), index1 + ". eleman, " + index2 + ". elemanı içermiyor.Elemanlar: " + stringList.get(index1 - 1) + " ve " + stringList.get(index2 - 1));
        logger.info(index1 + ". eleman, " + index2 + ". elemanı içeriyor.Elemanlar: " + stringList.get(index1 - 1) + " ve " + stringList.get(index2 - 1));
    }

    @Step("Integer listenin <index1>. elemaninin <index2>. elemanina gore buyuk olup olmadigini karsilastir")
    public void compareTwoInteger(int index1, int index2) {
        assertTrue(integerList.get(index1 - 1) > integerList.get(index2 - 1), index1 + ". eleman, " + index2 + ". elemandan büyük değil.Elemanlar: " + integerList.get(index1 - 1) + " ve " + integerList.get(index2 - 1));
        logger.info(index1 + ". eleman, " + index2 + ". elemandan büyük.Elemanlar: " + integerList.get(index1 - 1) + " ve " + integerList.get(index2 - 1));
    }

    @Step("Csv dosyasından uyelik bilgilerini oku ve <key> elementine e posta bilgisini ve <key2> elementine sifre bilgisini gir")
    public void readCsvForLogin(String key, String key2) throws Exception {
        Scanner sc = new Scanner(new File("src/test/resources/uyelikBilgileri.csv"));
        String[] dizi = new String[2];
        int i = 0;
        while (sc.hasNextLine()) {
            //System.out.print(sc.nextLine());
            dizi[i] = sc.nextLine();
            System.out.println(dizi[i]);
            i++;
        }
        findElement(key).sendKeys(dizi[0]);
        findElement(key2).sendKeys(dizi[1]);
        sc.close();
    }

    @Step("<key> elementin üstünde bekle")
    public void hover(String key) {
        hoverElement(findElement(key));
    }

    @Step({"Check if element <key> exists else print message <message>",
            "Element <key> var mı kontrol et yoksa hata mesajı ver <message>"})
    public void getElementWithKeyIfExistsWithMessage(String key, String message) {
        ElementInfo elementInfo = findElementInfoByKey(key);
        By by = getElementInfoToBy(elementInfo);

        int loopCount = 0;
        while (loopCount < DEFAULT_MAX_ITERATION_COUNT) {
            if (driver.findElements(by).size() > 0) {
                logger.info(key + " Elementi Sayfada Mevcut");
                return;
            }
            loopCount++;
            waitByMilliSeconds(DEFAULT_MILLISECOND_WAIT_AMOUNT);
        }
        Assertions.fail(message);
    }

    @Step({"Clear text of element <key>",
            "<key> elementinin text alanını temizle"})
    public void clearInputArea(String key) {
        findElement(key).clear();
    }

    @Step("Onbellek Temizle")
    public void clearCache() throws InterruptedException {

        driver.manage().deleteAllCookies();//*[@id="checkbox"]
        driver.get("chrome://settings/clearBrowserData");
        Thread.sleep(2000);
        for (int i = 0; i < 7; i++) {

            driver.findElement(By.xpath("//settings-ui")).sendKeys(Keys.TAB);
            Thread.sleep(100);
        }
        driver.findElement(By.xpath("//settings-ui")).sendKeys(Keys.ENTER);
        Thread.sleep(3000);

    }

    @Step({"Clear text of element <key> with BACKSPACE",
            "<key> elementinin text alanını BACKSPACE ile temizle"})
    public void clearInputAreaWithBackspace(String key) {
        WebElement element = findElement(key);
        element.clear();
        element.sendKeys("a");
        actions.sendKeys(Keys.BACK_SPACE).build().perform();
    }

    @Step({"If element <key> exists else print message <message>",
            "Element <key> var mı kontrol et yoksa hata mesajı yazdır <message>"})
    public void getElementWithKeyIfExists(String key, String message) {
        ElementInfo elementInfo = findElementInfoByKey(key);
        By by = getElementInfoToBy(elementInfo);

        int loopCount = 0;
        while (loopCount < DEFAULT_MAX_ITERATION_COUNT) {
            if (driver.findElements(by).size() > 0) {
                logger.info(key + " Elementi Sayfada Mevcut");
                return;
            }
            loopCount++;
        }
        Assertions.fail(message);
    }


    @Step({"Sayfa uzerinde <key> objesi goruntulenene kadar bekle"})
    public void waitForElementVisibility(String key) {
        this.findElement(key);
        ExpectedConditions.elementToBeClickable(By.xpath("key"));
        this.logger.info(key + " objesi sayfa uzerinde goruntulene kadar beklendi.");

    }

    @Step("<locator> a js ile tıkla")
    public void clickByJs(String locator) {
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) driver;
        javascriptExecutor.executeScript("arguments[0].click();", driver.findElement(By.xpath(locator)));
        this.logger.info("slm nbr" + locator);
    }

    @Step({"İd'si <key> li elementin Attribute'u alınır",
            "Get attribute from <key>"})
    public void getAttribute(String key) {
        String a = getAttribute(By.id(key), "innerHTML");
        logger.info(key + " elementin attribute'u " + a + " olduğu kontrol edildi ");

    }

    @Step("<key> elementini kontrol et")
    public void checkElement(String key) {
        assertTrue(findElement(key).isDisplayed(), "Aranan element bulunamadı");
        logger.info(key + " elementi bulundu ");
    }

    @Step("<key>'li elementin sayisi <number> sayisina esit mi?")
    public void compareElements(String key, int number) {

        List<WebElement> fiyatList = findElements(key);
        int a = fiyatList.size();
        logger.info(key + "  Sonuclarda Fiyatı Olmayan Otel Listelenmiyor");
        assertFalse(a == number, "Sonuclarda Fiyatı Olmayan Otellerde Listeleniyor");
    }

    @Step({"Refresh page",
            "Sayfayı yenile"})
    public void refreshPage() {
        driver.navigate().refresh();
    }

    @Step({"Check if element <key> not exists",
            "<key> elementi yokmu kontrol et"})
    public void checkElementNotExists(String key) {
        ElementInfo elementInfo = findElementInfoByKey(key);
        By by = getElementInfoToBy(elementInfo);

        int loopCount = 0;
        while (loopCount < 10) {
            if (driver.findElements(by).size() == 0) {
                logger.info(key + " elementinin olmadığı kontrol edildi.");
                return;
            }
            loopCount++;
        }
        Assertions.fail(key + "elementi hala görünür");
    }

    @Step({"<key> elementinin üzerinde durunca tasarımda beklenen renge donuyor mu donmuyorsa <message> yazdır"})
    public void getElementColor(String key, String message) {

        ElementInfo elementInfo = findElementInfoByKey(key);
        By by = getElementInfoToBy(elementInfo);

        if (driver.findElements(by).size() > 0) {
            hoverElement(findElement(key));
            WebElement element = findElement(key);
            String colorCode = element.getCssValue("background-color");

            String expectedColorCodeInRGB = "rgba(250, 88, 81, 0.1)";

            // Asserting actual and expected color codes
            Assertions.assertEquals(expectedColorCodeInRGB, colorCode);
            logger.info(key + "Elementinin rengi tasarımda beklenen renkle aynı. Elementinin Rengi: " + colorCode + " Tasarımda Beklenen Renk:  " + expectedColorCodeInRGB);


        }
        if (driver.findElements(by).size() == 0) {
            Assertions.fail(message);
        }
    }

    @Step({"Check if current URL contains the value <expectedURL>",
            "Şuanki URL <url> değerini içeriyor mu kontrol et"})
    public void checkURLContainsRepeat(String expectedURL) {
        int loopCount = 0;
        String actualURL = "";
        while (loopCount < DEFAULT_MAX_ITERATION_COUNT) {
            actualURL = driver.getCurrentUrl();

            if (actualURL != null && actualURL.contains(expectedURL)) {
                logger.info("Suanki URL  " + expectedURL + "  degerini iceriyor.");
                return;
            }
            loopCount++;
            waitByMilliSeconds(DEFAULT_MILLISECOND_WAIT_AMOUNT);
        }
        //Assertions.fail
        //      "Actual URL doesn't match the expected." + "Expected: " + expectedURL + ", Actual: "
        //            + actualURL);
        logger.info("Suanki URL  " + expectedURL + "  degerini icermiyor.");
    }

    @Step("<key> elementi otel sayisi kontrol edilir")
    public void otelSayisi(String key) {

        List<WebElement> elements = findElements(key);
        logger.info(elements.size() + " tane otel listelendi");

    }

    @Step("Otel sayısı <key> ve otel fiyatları <key2> sayısı eşit mi kontrol edilir")
    public void otelAndOtelPriceCompare(String key, String key2) {
        List<WebElement> element1 = findElements(key);
        List<WebElement> element2 = findElements(key2);

        int otelSayisi = element1.size();
        int otelFiyatSayisi = element2.size();
        if (otelSayisi == otelFiyatSayisi) {
            logger.info("Fiyati olmayan otel yoktur");
        } else {
            logger.info("Fiyati olmayan otel sayisi- " + (otelSayisi - otelFiyatSayisi));
        }
    }

    @Step("<key> elementiyle <key2> elementi String degerleri esit mi")
    public void stringCompare(String key, String key2) {
        String text = findElement(key).getText();
        String text2 = findElement(key2).getText();
        if (text.equals(text2)) {
            logger.info("Otel sayisi ve Musait Otel sayisi esitir");
        } else {
            logger.info("Otel sayisi ve Musait Otel sayisi esit degildir");
        }
    }

    @Step({"Write value <text> to element <key>",
            "<text> textini <key> li yere yaz"})
    public void ssendKeys1(String text, String key) {
        if (!key.equals("")) {
            findElement(key).sendKeys(text);
            logger.info(key + " elementine " + text + " texti yazıldı.");
        }
    }

    @Step({"Print element text by  <key>",
            "<key>'in text değerini yazdır"})
    public void printElementText(String key) {
        By infoParam = getElementInfoToBy(findElementInfoByKey(key));
        WebDriverWait wait = new WebDriverWait(driver, 20);
        wait.until(ExpectedConditions.visibilityOfElementLocated(infoParam));
        String text = findElement(key).getText();
        logger.info("Elementin texti yazdırıldı-" + text);
    }

    @Step({"<key>'li elementten sayfada kac adet gorundugu yazdirilir"})
    public void printNumberOfElements(String key) {
        List<WebElement> elementSayisi = findElements(key);
        int a = elementSayisi.size();
        logger.info(key + " elementinden " + a + " tane var.");
    }

    @Step("<key> elementi varsa tikla")
    public void clickElementIfExist(String key) {
        By infoParam = getElementInfoToBy(findElementInfoByKey(key));
        WebDriverWait wait = new WebDriverWait(driver, 1);
        if (!key.isEmpty()) {
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(infoParam));
                hoverElement(findElement(key));
                clickElement(findElement(key));
                waitBySeconds(1);
                logger.info(key + " elementine tıklandı.");
            } catch (Exception e) {
                logger.info(key + " elementi bulunamadı.");
            }
        }
    }

    @Step("<key> li elementlerden <sira>. olana tikla ve stringini listeye kaydet")
    public void clickFromListByKey(String key, int sira) {
        List<WebElement> elements = findElements(key);
        String text = elements.get(sira).getText();
        stringList.add(text);
        elements.get(sira).click();
        logger.info(key + " Key'li elementlerden " + sira + ". olana tiklandı");
    }

    @Step({"Press Enter",
            "Enter'e Bas"})
    public void PressEnter() {
        actions.sendKeys(Keys.ENTER).build().perform();
    }


    @Step("<key> li elementlerden <sira>. olana tikla ve tiklayamadigini listeye ekle")
    public void tryclickElement(String key, int sira) {
        if (!key.isEmpty()) {
            try {
                List<WebElement> elements = findElements(key);
                String text = elements.get(sira).getText();
                stringList.add(text);
                elements.get(sira).click();
                logger.info(key + " Key'li elementlerden " + sira + ". olana tiklandı");
            } catch (Exception e) {
                logger.info(key + " elementi bulunamadı.");
                unClikabledList.add(sira + ". Elemente Tıklanamadı");
            }
        }
    }

    @Step({"Unclickable list control",
            "Tiklanamayanlar liste kontrolü"})
    public void clickableListcontrol() {
        if (unClikabledList.isEmpty()) {
            logger.info("Listedeki Bütün Elementlere Tıklanabilmiştir.");
        } else {
            for (String i : unClikabledList) {
                Logger.info(i);
            }

            assertTrue(false, "Listede Tıklanamayanlar var");

        }
    }

    @Step({"Clean unclickable list",
            "Tiklanamayanlar listesini temizle"})
    public void clickableListClean() {
        unClikabledList.clear();
    }

    @Step({"Go to Iframe.<key>",
            "Sayfasina gecilir.<key>"})
    public void switchToIframe(String key) {
        //Robot robot = new Robot();
        //robot.keyPress(27);
        WebElement iframe = findElement(key);
        driver.switchTo().frame(iframe);
        logger.info(key + " Sayfasina gecilir.");
    }

    @Step({"<objeKey> li elementleri sırayla dene. İleri butonu key <rightButtonkey>, ekran URL:<unExpectedUrl>"})
    public void searchAllList(String objeKey, String rightButtonkey, String unExpectedUrl) {
        if (!objeKey.isEmpty() || !unExpectedUrl.isEmpty() || !rightButtonkey.isEmpty()) {
            int j = 0;
            List<WebElement> elements = findElements(objeKey);
            Logger.info(" " + elements.size());
            String actualURL = "";
            for (WebElement i : elements) {
                try {
                    List<WebElement> elementsClick = findElements(objeKey);
                    if (!elementsClick.get(0).isDisplayed()){scrollToElement(objeKey);}
                    if(rightButtonkey.equals("")||rightButtonkey.equals("false")||rightButtonkey.equals("False")||rightButtonkey.equals("rightButtonkey")) {
                        while (!elementsClick.get(j).isDisplayed())
                        {
                            clickElement(findElement(rightButtonkey));
                        }
                    }
                    logger.info(objeKey + " Key'li elementlerden " + (j + 1) + ". olana tiklandı");
                    hoverElement(elementsClick.get(j));
                    clickElement(elementsClick.get(j));
                    actualURL = driver.getCurrentUrl();
                    logger.info((j + 1) + ". olarak " + actualURL + " sayfası açıldı");
                    //logger.info(unExpectedUrl);
                    if (isElementExist("404_NotFound")) {
                        logger.info((j + 1) + ". elemente tıklandıktan sonra, '404 Aradığınız Sayfa Bulunamadı' mesajı alındı");
                        unClikabledList.add((j + 1) + ". elementine tıklandıktan sonra, '404 Aradığınız Sayfa Bulunamadı' mesajı alındı");
                        goToUrl(unExpectedUrl);
                    } else if (isElementExist("502_BadGateway")) {
                        logger.info((j + 1) + ". elemente tıklandıktan sonra, '502 Bad Gateway' mesajı alındı");
                        unClikabledList.add((j + 1) + ". elementine tıklandıktan sonra, '502 Bad Gateway' mesajı alındı");
                        goToUrl(unExpectedUrl);
                    } else if (actualURL.equals(unExpectedUrl)) {
                        logger.info((j + 1) + ". elemente tıklandıktan sonra, aynı sayfada kalındı.");
                        unClikabledList.add((j + 1) + ". elementine tıklandıktan sonra, aynı sayfada kalındı.");
                    } else {
                        goToUrl(unExpectedUrl);
                    }

                } catch (Exception e) {
                    logger.info((j + 1) + ". Elemente Tıklanamadı");
                    unClikabledList.add((j + 1) + ". Elemente Tıklanamadı");
                }
                j++;
            }

            if (unClikabledList.isEmpty()) {
                logger.info("Listedeki Bütün Elementlere Tıklanabilmiştir.");
            } else {
                for (String i : unClikabledList) {
                    Logger.info(i);
                }
                assertTrue(false, "Listede Tıklanamayanlar var");
            }
        }
    }

    @Step({"<key> is Exist",
            "<key>'li Element var mı?"})
    public boolean isElementExist(String key)
    {
        try
        {
            By infoParam = getElementInfoToBy(findElementInfoByKey(key));
            WebDriverWait webDriverWait = new WebDriverWait(driver, 5);
            WebElement webElement = webDriverWait
                    .until(ExpectedConditions.presenceOfElementLocated(infoParam));
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center', inline: 'center'})",
                    webElement);
            return true;
        }catch (Exception e)
        {
            return false;
        }

    }




    @Step({"<key> Urun resmi yok mu kontrol edilir"})
    public void productbildexist(String key) {
        List<WebElement> resimYuklenmeyenler = driver.findElements(By.cssSelector(".home-datalayer-click>.img>img[src=\"\"]"));
        logger.info("Resmi yuklenmeyen element sayisi: " + resimYuklenmeyenler.size());
        WebDriverWait wait = new WebDriverWait(driver, 20);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.selectedProduct")));
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<WebElement> elements = driver.findElements(By.cssSelector("div.selectedProduct span.img img:not([src$=\".png\"])"));
        if (elements.size() > 0){
            for (WebElement webElement: elements) {
                System.out.println(webElement.getAttribute("alt"));
            }
        }else {
            System.out.println("resim olmayan ürün bulunamadı");
        }
        System.out.println("***** Resme sahip olanlar *****");
        List<WebElement> elementsImg = driver.findElements(By.cssSelector("div.selectedProduct span.img img[src$=\".png\"]"));
        if (elements.size() > 0){
            for (WebElement webElement: elementsImg) {
                System.out.println(webElement.getAttribute("src") + "    " + webElement.getAttribute("alt"));
            }
            assertTrue(resimYuklenmeyenler.size()==0,"Yuklenmeyen resimler var!!!\n"+"Resmi yuklenmeyen element sayisi: " + resimYuklenmeyenler.size());
        }
    }

    @Step({"Add file Path <text> to element <key>",
            "<text> dosya konumunu <key> elementine yaz"})
    public void addFilePath(String text, String key) {
        text = System.getProperty("user.dir") + "\\z_Depo\\" + text;
        Logger.info(text);
        text = text.replaceAll("\\\\", "/");
        Logger.info(text);
        if (!key.equals("")) {
            findElement(key).sendKeys(text);
            logger.info(key + " elementine ''" + text + "'' dosya konumu yazıldı.");
        }
    }

    @Step({"Element kontrol. Beklenen: <ExpectedProduct>, Beklenmeyen <UnExpectedProduct>, Listelenecek değer <value>"})
    public void productControl(String ExpectedProduct,String UnExpectedProduct, String value) {
        scrollToElement(ExpectedProduct);
        List<WebElement> elements = findElements(UnExpectedProduct);
        if (elements.size() > 0){
            System.out.println("\n***** Beklenildiği gibi olmayan elementler *****\n");
            for (WebElement webElement: elements) {
                System.out.println(webElement.getAttribute(value));
            }
        }else {
            System.out.println("***** Beklenildiği gibi olmayan element bulunamadı *****");
        }
        System.out.println("\n***** Beklenildiği gibi olan elementler *****\n");
        List<WebElement> elementsImg = findElements(ExpectedProduct);
        if (elementsImg.size() > 0){
            for (WebElement webElement: elementsImg) {
                System.out.println(webElement.getAttribute(value));
            }
            assertTrue(elements.size()==0,"İstenmeyen elementler var!!!\n"+"İstenmeyen element sayisi: " + elements.size());
        }
    }

    @Step({"Upload file <path> to element <key>",
            "<path> dosyayı <key> elemente upload et"})
    public void uploadFiletoTestinium(String path, String key) {
        String pathString = System.getProperty("user.dir") + "/";
        logger.info("user dir !!!!!!!" + System.getProperty("user.dir"));
        logger.info("Path info !!!!!!!" + pathString);
        pathString = pathString + path;
        logger.info("Path info !!!!!!!" + pathString);
        findElement(key).sendKeys(path);
        logger.info(path + " folder " + key + " to uploaded.");
    }

    @Step({"İd'si <key> li elementin Attribute'unu güncelle. Attribute adı = <attribute>, Attribute değeri = <attributeValue>",
            "Set attribute from <key>, Attribute name = <attribute>, Attribute value = <attributeValue>"})
    public void setAttribute(String key,String attribute,String attributeValue) {
        WebElement element = findElement(key);
        String attributeTut;
        attributeTut =element.getAttribute(attribute);
        logger.info("Güncelleme öncesi ilgili elementin "+ attribute +" değeri >> "+ attributeTut + " <<'dir");
        ((JavascriptExecutor) driver).executeScript("arguments[0].setAttribute('"+attribute+"', '"+attributeValue+"')",element);
        attributeTut =element.getAttribute(attribute);
        logger.info("Güncelleme sonrası ilgili elementin "+ attribute +" değeri >> "+ attributeTut + " <<'dir");
    }


    @Step({"Combine <text1> with <text2> and keep",
            "<text1> metni ile <text2> metnini birleştir, sonra tut"})
    public void combineTextsAndKeep(String text1, String text2) {
        strTut = text1+text2;
        System.out.println(strTut);
    }

    @Step({"Write retained value to element <key>",
            "Tutulan texti <key> elemente yaz"})
    public void sendKeys(String key) {
        if (!key.equals("")) {
            findElement(key).sendKeys(strTut);
            logger.info(key + " elementine ''" + strTut + "'' texti yazıldı.");
        }
    }

    @Step({"Check if element <key> contains retained value>",
            "<key> elementi tutulan değeri içeriyor mu kontrol et"})
    public void checkElementContainsText(String key) {
        System.out.println("Beklenen deger....: "+strTut);
        System.out.println("Gorunen deger.....: "+findElement(key).getText());
        Boolean containsText = findElement(key).getText().contains(strTut);
        assertTrue(containsText, "Expected text is not contained");
        logger.info(key + " elementi ''" + strTut + "'' değerini içeriyor.");
    }

    @Step({"Create 'UserName' and 'Password'",
            "KullanıcıAdı Oluştur"})
    public void createUserName() {
        Random r=new Random(); //random sınıfı
        int rnd=r.nextInt(1000000);
        userName = rnd+"@Bilyoner.com";
        System.out.println("Üretilen Kullanıcı Adı  : "+userName+"'dur");
        password = ""+rnd;
        System.out.println("Üretilen Şifre          : "+password+"'dur");
        combineTextsAndKeep("Welcome ",userName);
    }

    @Step({"Write created 'UserName' to element <key>",
            "Üretilen 'KullanıcıAdı'nı <key> elemente yaz"})
    public void addCreatedUserName(String key) {
        if (!key.equals("")) {
            findElement(key).sendKeys(userName);
            logger.info(key + " elementine ''" + userName + "'' texti yazıldı.");
        }
    }


    @Step({"Write created 'Password' to element <key>",
            "Üretilen 'Şifre'yi <key> elemente yaz"})
    public void addCreatedPassword(String key) {
        if (!key.equals("")) {
            findElement(key).sendKeys(password);
            logger.info(key + " elementine ''" + password + "'' texti yazıldı.");
        }
    }



}


