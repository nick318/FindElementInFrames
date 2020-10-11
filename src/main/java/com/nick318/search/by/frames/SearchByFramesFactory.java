package com.nick318.search.by.frames;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * This is the only way to create instance of {@link SearchByFrames}.
 * Can be used as singleton using your favorite DI, but not limited.

 * @author Nikita Salomatin (nsalomatin@hotmail.com)
 */
public class SearchByFramesFactory {
    private final WebDriver driver;

    public SearchByFramesFactory(WebDriver driver) {
        Objects.requireNonNull(driver);
        this.driver = driver;
    }

    /**
     * Constructor.
     *
     * @param driver  incoming driver.
     * @param timeout timeout for checking element is exist at a frame. Default value is 100ms, try to avoid
     *                big numbers here. Up to 1 second, cause it will lead to low performance.
     */
    public SearchByFramesFactory(WebDriver driver, Duration timeout) {
        Objects.requireNonNull(driver);
        Objects.requireNonNull(timeout);
        this.driver = driver;
    }

    public SearchByFrames search(By locator) {
        return SearchByFrames.of(locator, driver);
    }

    public SearchByFrames search(Supplier<WebElement> elementSupplier) {
        return SearchByFrames.of(elementSupplier, driver);
    }
}
