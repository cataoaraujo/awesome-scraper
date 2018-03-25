# awesome-scraper

## Example

Open reddit page, and click in the link 'Users'
```java
AwesomeScrapper scrapper = new AwesomeScrapper("https://www.reddit.com");
scrapper.click("#sr-header-area > div > div.sr-list > ul:nth-child(1) > li:nth-child(4) > a");
Document document = scrapper.getDocument();
```
