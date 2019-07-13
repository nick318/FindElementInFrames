package com.nick318.search.by.frames;

import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.function.Supplier;

/**
 * This is the only way to create instance of {@link SearchByFrames}.
 * Can be used as singleton using your favorite DI, but not limited.
 *
 * @author Nikita Salomatin (nsalomatin@hotmail.com)
 */
@RequiredArgsConstructor
public class SearchByFramesFactory {
    private final WebDriver driver;

    public SearchByFrames search(By locator) {
        return SearchByFrames.of(locator, driver);
    }

    public SearchByFrames search(Supplier<WebElement> elementSupplier) {
        return SearchByFrames.of(elementSupplier, driver);
    }
}
