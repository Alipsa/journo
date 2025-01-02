# Journo Viewer Release History
(Note, dates are in yyyy-MM-dd format)

## v0.7.1 (in progress)
- Handle no groovy content without throwing exceptions

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