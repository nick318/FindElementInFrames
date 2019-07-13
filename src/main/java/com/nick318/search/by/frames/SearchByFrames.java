package com.nick318.search.by.frames;

import static org.openqa.selenium.support.ui.ExpectedConditions.frameToBeAvailableAndSwitchToIt;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Implementation of searching {@link WebElement} through several frames.
 * Instance of {@link SearchByFrames} should be created via {@link SearchByFramesFactory}
 *
 * @author Nikita Salomatin (nsalomatin@hotmail.com)
 */
public class SearchByFrames {

    private final WebDriver driver;
    private final Supplier<WebElement> supplier;

    static SearchByFrames of(By locator, WebDriver driver) {
        return new SearchByFrames(locator, driver);
    }
    static SearchByFrames of(Supplier<WebElement> supplier, WebDriver driver) {
        return new SearchByFrames(supplier, driver);
    }

    private SearchByFrames(By locator, WebDriver driver) {
        this(() -> driver.findElement(locator), driver);
    }

    private SearchByFrames(Supplier<WebElement> supplier, WebDriver driver) {
        this.driver = driver;
        this.supplier = supplier;
    }

    public Optional<WebElement> getElem() {
        return findElementByFrames();
    }

    private Optional<WebElement> findElementByFrames() {
        switchToTop();
        List<WebElement> frames = findFrames();

        if (!frames.isEmpty()) {
            return lookThroughFrames(frames);
        } else {
            return Optional.empty();
        }
    }

    private Optional<WebElement> lookThroughFrames(List<WebElement> frames) {
        for (int i = 0; i < frames.size(); i++) {
            if (i != 0) {
                driver.switchTo().parentFrame();
            }
            if (switchToFrame(findFrames().get(i))) {
                Optional<WebElement> result = lookElem();
                if (result.isPresent()) {
                    return result;
                } else {
                    List<WebElement> childFrames = findFrames();
                    if (!childFrames.isEmpty()) {
                        Optional<WebElement> WebElement = lookThroughFrames(childFrames);
                        if (WebElement.isPresent()) {
                             return WebElement;
                         }
                    }
                }
            } else {
                return Optional.empty();
            }
        }

        driver.switchTo().parentFrame();
        return Optional.empty();

    }

    private Optional<WebElement> lookElem() {
        try {
            WebElement element = supplier.get();
            if (isExist(element)) {
                return Optional.of(element);
            } else {
                return Optional.empty();
            }
        } catch (NoSuchElementException error) {
            return Optional.empty();
        }
    }

    private boolean isExist(WebElement element) {
        try {
            element.isDisplayed();
            return true;
        }
        catch (StaleElementReferenceException e) {
            return false;
        }
    }

    private boolean switchToFrame(WebElement WebElement) {
        try {
            new FluentWait<>(driver)
                    .withTimeout(300, TimeUnit.MILLISECONDS)
                    .pollingEvery(100, TimeUnit.MILLISECONDS)
                    .until(frameToBeAvailableAndSwitchToIt(WebElement));
            return true;
        } catch (TimeoutException timeout) {
            return false;
        }
    }

    private List<WebElement> findFrames() {
        return  driver.findElements(By.tagName("iframe"));
    }

    private void switchToTop() {
        driver.switchTo().defaultContent();
    }
}
