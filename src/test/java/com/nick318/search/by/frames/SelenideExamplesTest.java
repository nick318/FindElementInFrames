package com.nick318.search.by.frames;

import static com.codeborne.selenide.Selenide.$;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideConfig;
import com.codeborne.selenide.SelenideDriver;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.impl.StaticDriver;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class SelenideExamplesTest {
    private WebDriver driver;
    private SelenideWrapper searchFactory;

    /**
     * Example of selenide wrapper to avoid casting {@link WebElement} to {@link SelenideElement}.
     */
    private static class SelenideWrapper {
        private final SearchByFramesFactory factory;

        SelenideWrapper(WebDriver webDriver) {
            this.factory = new SearchByFramesFactory(webDriver);
        }

        public Optional<SelenideElement> search(SelenideElement element) {
            long before = Configuration.timeout;
            try {
                //set timeout to 1ms to speed up search
                Configuration.timeout = 1;
                return factory.search(element::getWrappedElement).getElem().map(Selenide::$);
            } finally {
                //rollback custom change
                Configuration.timeout = before;
            }
        }
    }

    @Before
    public void openTestPageAndSetDriver() {
        Path sampleFile = Paths.get("src/test/resources/html/0001.html");
        SelenideDriver selenideDriver = new SelenideDriver(
                new SelenideConfig().browser(Configuration.browser),
                new StaticDriver()
        );
        selenideDriver.open(sampleFile.toUri().toString());
        this.searchFactory = new SelenideWrapper(selenideDriver.getWebDriver());
        this.driver = selenideDriver.getWebDriver();
    }

    @Test
    public void searchShouldReturnNotEmptyOptionalIfElementWasFound() {
        By locator = By.xpath(".//input[@name='firstname']");
        Optional<SelenideElement> elem = searchFactory.search($(locator));
        assertTrue("Elem was found, but optional was an empty", elem.isPresent());
    }

    @Test
    public void searchShouldReturnEmptyOptionalWhenElemWasNotFound() {
        By locator = By.xpath(".//input[@name='firstname1']");
        Optional<SelenideElement> elem = searchFactory.search($(locator));
        assertFalse("Elem was not found, but optional was not empty", elem.isPresent());
    }

    @Test
    public void elementShouldBeFoundInFrame() {
        By locator = By.xpath(".//input[@name='firstname']");
        Optional<SelenideElement> elem = searchFactory.search($(locator));
        assertTrue("Element was not found", elem.get().isEnabled());
    }


    @Test
    public void elementShouldBeFoundInFrameBySupplier() {
        By locator = By.xpath(".//input[@name='firstname']");
        Optional<SelenideElement> elem = searchFactory.search(
                $(By.tagName("body")).$(locator)
        );
        assertTrue("Element was not found", elem.get().isEnabled());
    }

    @Test
    public void searchShouldBeDoneBySupplierWhichThrowsException() {
        Optional<SelenideElement> elem = searchFactory.search($(By.id("fake one")));
        assertFalse("Optional was not empty, but should be", elem.isPresent());
        //should not throw exception
        driver.findElement(By.xpath(".//iframe[@name='1']"));
    }

    @Test
    public void elementShouldBeFoundInFrameInsideFrame() {
        By locator = By.xpath(".//input[@name='firstname_child_1']");
        Optional<SelenideElement> elem = searchFactory.search($(locator));
        assertTrue("Element was not found", elem.get().isEnabled());
    }

    @Test
    public void elementShouldBeFoundInDeepTree() {
        By locator = By.xpath(".//input[@name='firstname_child_deep_tree']");
        Optional<SelenideElement> elem = searchFactory.search($(locator));
        assertTrue("Element was not found", elem.get().isEnabled());
    }

    @Test
    public void elementShouldBeFoundInLastFrameInTree() {
        By locator = By.xpath(".//input[@name='firstname_framesource_2']");
        Optional<SelenideElement> elem = searchFactory.search($(locator));
        assertTrue("Element was not found", elem.get().isEnabled());
    }

    @Test
    public void ifElementWasNotFoundTheFrameShouldBeParent() {
        By iframe = By.tagName("iframe");
        driver.switchTo().defaultContent();
        driver.switchTo().frame(driver.findElement(By.xpath(".//iframe[@name='1']")));
        driver.switchTo().frame(driver.findElement(iframe));
        driver.switchTo().frame(driver.findElement(iframe));
        assumeTrue(driver.findElement(By.xpath(".//input[@name='firstname_child_2']")).isEnabled());

        searchFactory.search($(By.xpath(".//input[@name='not exist']")));

        assertTrue(driver.findElement(By.xpath(".//input[@name='main']")).isEnabled());
    }

    @Test
    public void returnEmptyOptionWhenNoFramesAtAll() {
        Path sampleFile = Paths.get("src/test/resources/html/page_without_frames.html");
        driver.get(sampleFile.toUri().toString());

        By locator = By.xpath(".//input[@name='firstname']");
        Optional<SelenideElement> elem = searchFactory.search($(locator));
        assertFalse("There is no frames, optional should be empty!", elem.isPresent());
    }
}
