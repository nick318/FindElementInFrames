# FindElementInFrames

[![Run tests Actions Status](https://github.com/nick318/FindElementInFrames/workflows/run-tests/badge.svg)](https://github.com/nick318/FindElementInFrames/actions)


Helper can find an element through all frames that represent on page

Examples:

```
//setup once
SearchByFramesFactory searchFactory = new SearchByFramesFactory(driver);

//use in different places
SearchByFrames searchInFrame = searchFactory.search(locator);
Optional<WebElement> elem = searchInFrame.getElem();
elem.ifPresent(WebElement::click);

SearchByFrames searchInFrame = searchFactory.search(() -> driver.findElement(By.tagName("body")));
Optional<WebElement> elem = searchInFrame.getElem();
```

As you may noticed you can put a Supplier of WebElement as a constructor parameter, 
it will be invoked during search in frames.

All synchronization logic should not be placed here, it is responsibility of user.

More examples you can find in tests directory.