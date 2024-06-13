# Release history

## v 0.6.3
- Synchronize xhtmlToPdf to avoid concurrent modification issues on the pdf renderer
- upgrade flying saucer 9.7.2 -> 9.8.0
## v 0.6.2 (2024-05-06)
- revert auto installation of declared fonts and document "the flying saucer approach"
- Include the static bean wrapper as par of default config
- Expose access to the Freemarker Configuration and the Flying Saucer renderer from the ReportEngine
  to allow for customizations not available from the ReportEngine api.

## v 0.6.1 (2024-04-26)
- Change scope of slf4j-simple to test (it should not be part of the published jar)
- add auto installation of declared fonts
- add javadocs
- Add fallbacks to locate the resource in ImageUtil and use commons-io to read the bytes.

## v 0.6.0 (2024-03-31)
- Downgrade to Java 17
- Add docs and tests
- upgrade flying saucer version

## v 0.5.0 (2024-01-28)
- Initial release requiring java 21