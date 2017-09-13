# FindElementInFrames

[![Build Status](https://travis-ci.org/iCok/FindElementInFrames.svg?branch=master)](https://travis-ci.org/iCok/FindElementInFrames)


Helper can find an element through all frames that represent on page

Examples:

```
SearchByFrames searchInFrame = SearchByFrames.of(locator);
Optional<SelenideElement> elem = searchInFrame.getElem();
elem.ifPresent(SelenideElement::click);

SearchByFrames searchInFrame = SearchByFrames.of(() -> $(By.tagName("body")).$(locator));
Optional<SelenideElement> elem = searchInFrame.getElem();
```

As you may noticed you can put a supplier of Selenide Element as a constructor parameter, 
it will be invoked during search in frames.

All synchronization logic should not be placed here, it is responsibility of user.

More examples you can find in tests directory.
