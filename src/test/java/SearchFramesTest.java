import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.ex.UIAssertionError;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebDriver;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

/**
 * Created by Cok on 13.01.2017.
 */
public class SearchFramesTest {

    private WebDriver driver;

    @Before
    public void setUp() throws Exception {
        Path sampleFile = Paths.get("src/test/resources/html/0001.html");
        ChromeDriverManager.getInstance().setup();
        Configuration.browser = "chrome";
        WebDriverRunner.getWebDriver().get(sampleFile.toUri().toString());
        this.driver = WebDriverRunner.getWebDriver();

    }

    @Test
    public void searchShouldReturnNotEmptyOptionalIfElementWasFound() throws Exception {

        By locator = By.xpath(".//input[@name='firstname']");
        SearchByFrames searchInFrame = SearchByFrames.of(locator);
        Optional<SelenideElement> elem = searchInFrame.getElem();
        assertTrue("Elem was found, but optional was an empty", elem.isPresent());

    }

    @Test
    public void searchShouldReturnEmptyOptionalWhenElemWasNotFound() throws Exception {

        By locator = By.xpath(".//input[@name='firstname1']");
        SearchByFrames searchInFrame = SearchByFrames.of(locator);
        Optional<SelenideElement> elem = searchInFrame.getElem();
        assertFalse("Elem was not found, but optional was not empty", elem.isPresent());
    }

    @Test
    public void elementShouldBeFoundInFrame() throws Exception {

        By locator = By.xpath(".//input[@name='firstname']");
        SearchByFrames searchInFrame = SearchByFrames.of(locator);
        Optional<SelenideElement> elem = searchInFrame.getElem();
        if (elem.isPresent()) {
            SelenideElement selenideElement = elem.get();
            boolean isPresent = selenideElement.is(exist);

            assertTrue("Element was not found", isPresent);
        } else {
            assertTrue("Optional was empty, but should not be", elem.isPresent());
        }

    }


    @Test
    public void elementShouldBeFoundInFrameBySupplier() throws Exception {

        By locator = By.xpath(".//input[@name='firstname']");
        SearchByFrames searchInFrame = SearchByFrames.of(() -> $(By.tagName("body")).$(locator));
        Optional<SelenideElement> elem = searchInFrame.getElem();
        if (elem.isPresent()) {
            SelenideElement selenideElement = elem.get();
            boolean isPresent = selenideElement.is(exist);

            assertTrue("Element was not found", isPresent);
        } else {
            assertTrue("Optional was empty, but should not be", elem.isPresent());
        }

    }

    @Test
    public void searchShouldBeDoneBySupplierWhichThrowsException() throws Exception {

        SearchByFrames searchInFrame = SearchByFrames.of(() -> {
            throw new UIAssertionError(new NotFoundException());
        });
        Optional<SelenideElement> elem = searchInFrame.getElem();
        assertFalse("Optional was not empty, but should be", elem.isPresent());

        assertTrue($(By.xpath(".//iframe[@name='1']")).is(exist));

    }

    @Test
    public void elementShouldBeFoundInFrameInsideFrame() throws Exception {

        By locator = By.xpath(".//input[@name='firstname_child_1']");
        SearchByFrames searchInFrame = SearchByFrames.of(locator);

        try {
            SelenideElement selenideElement = searchInFrame.getElem().get();
            boolean isPresent = selenideElement.is(exist);
            assertTrue("Element was not found", isPresent);
        } catch (NoSuchElementException noElem) {
            fail("Optional was empty, but should not be empty");
        }
    }

    @Test
    public void elementShouldBeFoundInDeepTree() throws Exception {

        By locator = By.xpath(".//input[@name='firstname_child_deep_tree']");
        SearchByFrames searchInFrame = SearchByFrames.of(locator);

        try {
            SelenideElement selenideElement = searchInFrame.getElem().get();
            boolean isPresent = selenideElement.is(exist);
            assertTrue("Element was not found", isPresent);
        } catch (NoSuchElementException noElem) {
            fail("Optional was empty, but should not be empty");
        }

    }

    @Test
    public void elementShouldBeFoundInLastFrameInTree() throws Exception {

        By locator = By.xpath(".//input[@name='firstname_framesource_2']");
        SearchByFrames searchInFrame = SearchByFrames.of(locator);
        Optional<SelenideElement> elem = searchInFrame.getElem();
        if (elem.isPresent()) {
            SelenideElement selenideElement = elem.get();
            boolean isPresent = selenideElement.is(exist);

            assertTrue("Element was not found", isPresent);
        } else {
            assertTrue("Optional was empty, but should not be", elem.isPresent());
        }
    }

    @Test
    public void ifElementWasNotFoundTheFrameShouldBeParent() throws Exception {

        By iframe = By.tagName("iframe");
        switchTo().defaultContent();
        switchTo().frame($(By.xpath(".//iframe[@name='1']")));
        switchTo().frame($(iframe));
        switchTo().frame($(iframe));

        assumeTrue($(By.xpath(".//input[@name='firstname_child_2']")).is(exist));

        SearchByFrames searchInFrame = SearchByFrames.of(By.xpath(".//input[@name='not exist']"));
        Optional<SelenideElement> elem = searchInFrame.getElem();

        System.out.println(elem.toString());

        assertTrue($(By.xpath(".//input[@name='main']")).is(exist));

    }
}
