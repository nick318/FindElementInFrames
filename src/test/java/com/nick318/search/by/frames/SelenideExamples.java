package com.nick318.search.by.frames;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Supplier;

public class SelenideExamples {
    private WebDriver driver;
    private SelenideWrapper searchFactory;

    /**
     * Example of selenide wrapper to avoid casting {@link WebElement} to {@link SelenideElement}.
     */
    private static class SelenideWrapper {
        private SearchByFramesFactory factory;

        SelenideWrapper() {
            this.factory = new SearchByFramesFactory(WebDriverRunner.getWebDriver());
        }

        public SearchByFramesSelenide search(SelenideElement element) {
            return new SearchByFramesSelenide(() -> element);
        }

        public SearchByFramesSelenide search(Supplier<WebElement> elementSupplier) {
            return new SearchByFramesSelenide(elementSupplier);
        }

        private class SearchByFramesSelenide {
            private final SearchByFrames search;

            public SearchByFramesSelenide(Supplier<WebElement> elementSupplier) {
                this.search = factory.search(elementSupplier);
            }

            public Optional<SelenideElement> getElem() {
                return search.getElem().map(Selenide::$);
            }

        }
    }

    @Before
    public void setUp() {
        Path sampleFile = Paths.get("src/test/resources/html/0001.html");
        open(sampleFile.toUri().toString());
        this.searchFactory = new SelenideWrapper();
        this.driver = WebDriverRunner.getWebDriver();
    }

    @Test
    public void searchShouldReturnNotEmptyOptionalIfElementWasFound() {
        By locator = By.xpath(".//input[@name='firstname']");
        SelenideWrapper.SearchByFramesSelenide search = searchFactory.search($(locator));
        Optional<SelenideElement> elem = search.getElem();
        assertTrue("Elem was found, but optional was an empty", elem.isPresent());
    }

    @Test
    public void searchShouldReturnEmptyOptionalWhenElemWasNotFound() {
        By locator = By.xpath(".//input[@name='firstname1']");
        SelenideWrapper.SearchByFramesSelenide searchInFrame = searchFactory.search($(locator));
        Optional<SelenideElement> elem = searchInFrame.getElem();
        assertFalse("Elem was not found, but optional was not empty", elem.isPresent());
    }

    @Test
    public void elementShouldBeFoundInFrame() {
        By locator = By.xpath(".//input[@name='firstname']");
        SelenideWrapper.SearchByFramesSelenide searchInFrame = searchFactory.search($(locator));
        Optional<SelenideElement> elem = searchInFrame.getElem();
        assertTrue("Element was not found", elem.get().isEnabled());
    }


    @Test
    public void elementShouldBeFoundInFrameBySupplier() {
        By locator = By.xpath(".//input[@name='firstname']");
        SelenideWrapper.SearchByFramesSelenide searchInFrame = searchFactory.search(
                $(By.tagName("body")).$(locator)
        );
        Optional<SelenideElement> elem = searchInFrame.getElem();
        assertTrue("Element was not found", elem.get().isEnabled());
    }

    @Test
    public void searchShouldBeDoneBySupplierWhichThrowsException() {
        SelenideWrapper.SearchByFramesSelenide searchInFrame = searchFactory.search($(By.id("fake one")));
        Optional<SelenideElement> elem = searchInFrame.getElem();
        assertFalse("Optional was not empty, but should be", elem.isPresent());
        //should not throw exception
        driver.findElement(By.xpath(".//iframe[@name='1']"));
    }

    @Test
    public void elementShouldBeFoundInFrameInsideFrame() {
        By locator = By.xpath(".//input[@name='firstname_child_1']");
        SelenideWrapper.SearchByFramesSelenide searchInFrame = searchFactory.search($(locator));
        assertTrue("Element was not found", searchInFrame.getElem().get().isEnabled());
    }

    @Test
    public void elementShouldBeFoundInDeepTree() {
        By locator = By.xpath(".//input[@name='firstname_child_deep_tree']");
        SelenideWrapper.SearchByFramesSelenide searchInFrame = searchFactory.search($(locator));
        assertTrue("Element was not found", searchInFrame.getElem().get().isEnabled());
    }

    @Test
    public void elementShouldBeFoundInLastFrameInTree() {
        By locator = By.xpath(".//input[@name='firstname_framesource_2']");
        SelenideWrapper.SearchByFramesSelenide searchInFrame = searchFactory.search($(locator));
        Optional<SelenideElement> elem = searchInFrame.getElem();
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

        SelenideWrapper.SearchByFramesSelenide searchInFrame = searchFactory.search($(By.xpath(".//input[@name='not exist']")));
        searchInFrame.getElem();

        assertTrue(driver.findElement(By.xpath(".//input[@name='main']")).isEnabled());
    }

    @Test
    public void returnEmptyOptionWhenNoFramesAtAll() {
        Path sampleFile = Paths.get("src/test/resources/html/page_without_frames.html");
        driver.get(sampleFile.toUri().toString());

        By locator = By.xpath(".//input[@name='firstname']");
        SelenideWrapper.SearchByFramesSelenide searchInFrame = searchFactory.search($(locator));
        Optional<SelenideElement> elem = searchInFrame.getElem();
        assertFalse("There is no frames, optional should be empty!", elem.isPresent());
    }
}
