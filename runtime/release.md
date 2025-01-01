# Release history

### v0.7.0 (2024-01-01)
- Throw JournoException on errors instead of exposing the underlying exception directly
- upgrade freemarker 2.3.32 -> 2.3.33
- replace flying saucer with openhtmltopdf
- add support for math ML
- upgrade batik-transcoder 1.17 -> 1.18
- Add stream method for rendering output
- Add ReportEngine constructor for a custom TemplateLoader
- Rename ReportEngine to JournoEngine

## v0.6.3 (2024-06-13)
- Synchronize xhtmlToPdf to avoid concurrent modification issues on the pdf renderer
- upgrade flying saucer 9.7.2 -> 9.8.0

## v0.6.2 (2024-05-06)
- revert auto installation of declared fonts and document "the flying saucer approach"
- Include the static bean wrapper as part of default config
- Expose access to the Freemarker Configuration and the Flying Saucer renderer from the ReportEngine
  to allow for customizations not available from the ReportEngine api.

## v0.6.1 (2024-04-26)
- Change scope of slf4j-simple to test (it should not be part of the published jar)
- add auto installation of declared fonts
- add javadocs
- Add fallbacks to locate the resource in ImageUtil and use commons-io to read the bytes.

## v0.6.0 (2024-03-31)
- Downgrade to Java 17
- Add docs and tests
- upgrade flying saucer version

## v0.5.0 (2024-01-28)
- Initial release requiring java 21