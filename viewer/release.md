# Journo Viewer Release History
(Note, dates are in yyyy-MM-dd format)

## v0.7.3, 2026-02-19
- io.github.classgraph:classgraph 4.8.181 -> 4.8.184
- org.apache.pdfbox:pdfbox 3.0.5 -> 3.0.6
- org.apache.groovy:groovy-all 5.0.0 -> 5.0.4
- com.github.hervegirod:fxsvgimage 1.4 -> se.alipsa:fxsvgimage 1.5.1
- Fix NPE in getProjectDir() when no project is selected
- Fix incorrect error message in viewHtml() (said "pdf" instead of "html")
- Auto-save template before rendering in run(), viewHtml(), and viewExternal()
- Fix fxsvgimage dependency not using its version property
- Fix non-final static logger fields

## v0.7.2, 2025-09-07
io.github.openhtmltopdf:openhtmltopdf-core 1.1.24 -> 1.1.30
io.github.openhtmltopdf:openhtmltopdf-mathml-support 1.1.24 -> 1.1.30
io.github.openhtmltopdf:openhtmltopdf-pdfbox  1.1.24 -> 1.1.30
io.github.openhtmltopdf:openhtmltopdf-svg-support 1.1.24 -> 1.1.30
org.apache.xmlgraphics:batik-transcoder 1.18 -> 1.19
org.apache.xmlgraphics:batik-codec 1.18 -> 1.19
org.jsoup:jsoup 1.18.3 -> 1.21.1
org.junit.jupiter:junit-jupiter 5.11.4 -> 5.13.4
org.slf4j:jcl-over-slf4j 2.0.16 -> 2.0.17
org.slf4j:slf4j-api 2.0.16 -> 2.0.17
org.slf4j:slf4j-simple 2.0.16 -> 2.0.17
com.github.hervegirod:fxsvgimage 1.1 -> 1.4
io.github.classgraph:classgraph 4.8.179 -> 4.8.181
org.apache.groovy:groovy-all 4.0.25 -> 5.0.5
org.apache.logging.log4j:log4j-core 2.24.3 -> 2.25.1
org.apache.pdfbox:pdfbox 3.0.4 -> 3.0.5
org.fxmisc.richtext:richtextfx 0.11.4 -> 0.11.6

## v0.7.1, 2025-01-27
- Handle no groovy content without throwing exceptions
- change artifact group name to se.alipsa.journo
- Change location of the pdf action buttons to give more space to the pdf content
- Ensure project is created when opening a project not created on this computer.
- Set taskbar/dock icon in the JournoViewer removing the ned for jvm properties enabling a nice journo icon when running from the journoViewer pom file
- Upgrade groovy 4.0.24 -> 4.0.25
- Upgrade pdfbox 3.0.3 -> 3.0.4
- Upgrade javafx 23.0.1 -> 23.0.2


## v0.7.0 (2024-01-01)
- fix bug that saved only selected content instead of all in the groovy script
- add journoViewer.xml for an alternative way to run the Journo Viewer Gui
- upgrade dependencies (notably to Javafx 23.0.1)
- enhance about dialog to include dependency version info

## v0.6.5
- fix npe bug when opening a project for the first time
- upgrade javafx version 22.0.1 -> 22.0.2

## v0.6.4
- Upgrade classgraph 4.8.172 -> 4.8.173

## V 0.6.3 (2024-06-13)
- Combine all 3 release packages into one.

## v 0.6.2 (2024-05-06)
- add windows support
- upgrade dependencies

## v 0.6.1 (2024-04-26)
- add render to html option
- add view pdf in external app option
- Preserve indentation from previous line
- Clear content when creating new project. Change title when saving as
- move file operations to menu, move view pdf buttons to a single one and extend the responsibility of save button
- Make project dir handling more consistent.
- avoid mark as changed when loading a file
- add support for back indent
- add logo and stylesheet to all dialogs
- add check for unsaved content
- upgrade groovy version, add mac icons