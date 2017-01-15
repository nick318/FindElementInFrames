import com.codeborne.selenide.*;
import com.codeborne.selenide.ex.UIAssertionError;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.FluentWait;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static org.openqa.selenium.support.ui.ExpectedConditions.frameToBeAvailableAndSwitchToIt;

/**
 * Created by Cok on 13.01.2017.
 */
public class SearchByFrames {

    private final WebDriver driver;
    private final Supplier<SelenideElement> supplier;

    public static SearchByFrames of(By locator) {
        return new SearchByFrames(locator);
    }
    public static SearchByFrames of(Supplier<SelenideElement> supplier) {
        return new SearchByFrames(supplier);
    }

    private SearchByFrames(By locator) {
        this(() -> $(locator));
    }

    private SearchByFrames(Supplier<SelenideElement> supplier) {
        this.driver = WebDriverRunner.getWebDriver();
        this.supplier = supplier;
    }

    public Optional<SelenideElement> getElem() {
        long savedTimeout = Configuration.timeout;
        long savedCollectionTimeout = Configuration.collectionsTimeout;
        try {

            Configuration.timeout = 200;
            Configuration.collectionsTimeout = 200;

            return findElementByFrames();
        } catch (UIAssertionError error) {
            return Optional.empty();
        } finally {
            Configuration.timeout = savedTimeout;
            Configuration.collectionsTimeout = savedCollectionTimeout;
        }
    }

    private Optional<SelenideElement> findElementByFrames() {

        switchToTop();
        List<SelenideElement> frames = findFrames();

        if (isPresent(frames)) {
            return lookThroughFrames(frames);
        } else {
            return Optional.empty();
        }

    }

    private Optional<SelenideElement> lookThroughFrames(List<SelenideElement> frames) {

        for (int i = 0; i < frames.size(); i++) {
            if (i != 0) {
                driver.switchTo().parentFrame();
            }

            if (switchToFrame(findFrames().get(i))) {

                Optional<SelenideElement> result = lookElem();
                if (result.isPresent()) {
                    return result;
                } else {
                    List<SelenideElement> childFrames = findFrames();
                    if (isPresent(childFrames)) {
                        Optional<SelenideElement> selenideElement = lookThroughFrames(childFrames);
                        if (selenideElement.isPresent()) {
                             return selenideElement;
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

    private Optional<SelenideElement> lookElem() {

        try {
            SelenideElement element = supplier.get();
            if (element.is(Condition.exist)) {
                return Optional.of(element);
            } else {
                return Optional.empty();
            }
        } catch (UIAssertionError error) {
            return Optional.empty();
        }
    }

    private boolean switchToFrame(SelenideElement selenideElement) {
        try {
            new FluentWait<>(driver)
                    .withTimeout(300, TimeUnit.MILLISECONDS)
                    .pollingEvery(100, TimeUnit.MILLISECONDS)
                    .until(frameToBeAvailableAndSwitchToIt(selenideElement));
            return true;
        } catch (TimeoutException timeout) {
            return false;
        }
    }

    private boolean isPresent(List<SelenideElement> frames) {
        return frames.size() > 0;
    }

    private List<SelenideElement> findFrames() {
        try {
           return  $$(driver.findElements(By.tagName("iframe")));
        } catch (UIAssertionError error) {
            return new ArrayList<>();
        }
    }

    private void switchToTop() {
        driver.switchTo().defaultContent();
    }
}
