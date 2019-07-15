package com.nick318.search.by.frames;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Tests.
 *
 * @author Nikita Salomatin (nsalomatin@hotmail.com)
 */
public class SearchFramesTest {

    private static WebDriver driver;
    private static SearchByFramesFactory searchFactory;

    @BeforeClass
    public static void beforeClass() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        searchFactory = new SearchByFramesFactory(driver);
    }

    @Before
    public void beforeTest() {
        Path sampleFile = Paths.get("src/test/resources/html/0001.html");
        driver.get(sampleFile.toUri().toString());
    }

    @AfterClass
    public static void afterClass() {
        driver.close();
    }

    @Test
    public void searchShouldReturnNotEmptyOptionalIfElementWasFound() {
        By locator = By.xpath(".//input[@name='firstname']");
        SearchByFrames searchInFrame = searchFactory.search(locator);
        Optional<WebElement> elem = searchInFrame.getElem();
        assertTrue("Elem was found, but optional was an empty", elem.isPresent());
    }

    @Test
    public void searchShouldReturnEmptyOptionalWhenElemWasNotFound() {
        By locator = By.xpath(".//input[@name='firstname1']");
        SearchByFrames searchInFrame = searchFactory.search(locator);
        Optional<WebElement> elem = searchInFrame.getElem();
        assertFalse("Elem was not found, but optional was not empty", elem.isPresent());
    }

    @Test
    public void elementShouldBeFoundInFrame() {
        By locator = By.xpath(".//input[@name='firstname']");
        SearchByFrames searchInFrame = searchFactory.search(locator);
        Optional<WebElement> elem = searchInFrame.getElem();
        assertTrue("Element was not found", elem.get().isEnabled());
    }


    @Test
    public void elementShouldBeFoundInFrameBySupplier() {
        By locator = By.xpath(".//input[@name='firstname']");
        SearchByFrames searchInFrame = searchFactory.search(
                () -> driver.findElement(By.tagName("body")).findElement(locator)
        );
        Optional<WebElement> elem = searchInFrame.getElem();
        assertTrue("Element was not found", elem.get().isEnabled());
    }

    @Test
    public void searchShouldBeDoneBySupplierWhichThrowsException() {
        SearchByFrames searchInFrame = searchFactory.search(() -> {
            throw new NoSuchElementException("Fake one");
        });
        Optional<WebElement> elem = searchInFrame.getElem();
        assertFalse("Optional was not empty, but should be", elem.isPresent());
        //should not throw exception
        driver.findElement(By.xpath(".//iframe[@name='1']"));
    }

    @Test
    public void elementShouldBeFoundInFrameInsideFrame() {
        By locator = By.xpath(".//input[@name='firstname_child_1']");
        SearchByFrames searchInFrame = searchFactory.search(locator);
        assertTrue("Element was not found", searchInFrame.getElem().get().isEnabled());
    }

    @Test
    public void elementShouldBeFoundInDeepTree() {
        By locator = By.xpath(".//input[@name='firstname_child_deep_tree']");
        SearchByFrames searchInFrame = searchFactory.search(locator);
        assertTrue("Element was not found", searchInFrame.getElem().get().isEnabled());
    }

    @Test
    public void elementShouldBeFoundInLastFrameInTree() {
        By locator = By.xpath(".//input[@name='firstname_framesource_2']");
        SearchByFrames searchInFrame = searchFactory.search(locator);
        Optional<WebElement> elem = searchInFrame.getElem();
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

        SearchByFrames searchInFrame = searchFactory.search(By.xpath(".//input[@name='not exist']"));
        searchInFrame.getElem();

        assertTrue(driver.findElement(By.xpath(".//input[@name='main']")).isEnabled());
    }

    @Test
    public void returnEmptyOptionWhenNoFramesAtAll() {
        Path sampleFile = Paths.get("src/test/resources/html/page_without_frames.html");
        driver.get(sampleFile.toUri().toString());

        By locator = By.xpath(".//input[@name='firstname']");
        SearchByFrames searchInFrame = searchFactory.search(locator);
        Optional<WebElement> elem = searchInFrame.getElem();
        assertFalse("There is no frames, optional should be empty!", elem.isPresent());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void whenFrameDisappearByAjaxReturnEmptyOptional() {
        WebDriver mockDriver = mock(WebDriver.class);
        WebDriver.TargetLocator mockTargetLocator = mock(WebDriver.TargetLocator.class);
        WebElement frame = mock(WebElement.class);
        List<WebElement> frames = Collections.singletonList(frame);

        when(mockDriver.switchTo()).thenReturn(mockTargetLocator);
        when(mockDriver.switchTo().frame(frame)).thenThrow(NoSuchFrameException.class);
        when(mockDriver.switchTo().parentFrame()).thenReturn(mockDriver);
        when(mockDriver.findElements(any(By.class))).thenReturn(frames);

        By locator = By.xpath(".//input[@name='firstname']");
        SearchByFrames searchInFrame = SearchByFrames.of(locator, mockDriver, Duration.ofMillis(100));
        Optional<WebElement> elem = searchInFrame.getElem();
        assertFalse("There is no frames, optional should be empty!", elem.isPresent());
        verify(mockDriver.switchTo());
    }
}
