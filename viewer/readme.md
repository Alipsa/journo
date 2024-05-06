# Journo Viewer

The Journo Viewer is a javafx application simplifying the workflow of creating Freemarker Templates that
are rendered into a PDF using Journo. It requires a JDK version 17 or higher with JavaFx included (such as the [Bellsoft full distribution](https://bell-sw.com/pages/downloads/#jdk-17-lts))
for both compilation and running.

You start the application with e.g. `java -jar journo-viewer-0.6.2.jar`.  

## Installing the zipped release package
Zipped releases are available on [github](https://github.com/Alipsa/journo/releases)

### Mac
1. Downloaded the mac release (journo-viewer-mac.zip) 
2. Move the journo.app to your applications folder
3. The first time you run it you must right click and choose open to establish it
as a trusted application

### Linux
1. Downloaded the linux release (journo-viewer-linux.zip)
2. Move the journoViewer folder to a location of choice
3. Run the createLauncher.sh script to create a launcher (shortcut)

### Windows
1. Downloaded the windows release (journo-viewer-win.zip)
2. Move the journoViewer folder to a location of choice
3. Run the createShortcut.ps1 script to create a shortcut

## Groovy code to generate data
Using groovy scripts makes it easy to create mock data to provide input data to Freemarker.
Groovy is almost 100% compatible with Java syntax but add some nice things.

- include dependencies using @Grab
- the last line (the return value) must be a Map<String, Object>

Here is a simple example:

assuming a ftl with the following content:
```injectedfreemarker
<html>
<body>
<h1>Hello</h1>
<p>Nice to see you ${name}!</p>
Your phone number is
<#if isValidNumber>
    valid
<#else>
    not valid
</#if>

<p>There you go, nice huh?</p>
</body>
</html>
```

You can populate the two variable used with the following groovy script:
```groovy
@Grab('com.googlecode.libphonenumber:libphonenumber:8.13.29')
import com.google.i18n.phonenumbers.PhoneNumberUtil

PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance()
def number = phoneNumberUtil.parseAndKeepRawInput('070-1232345', "SE")

// Using the groovy shorthand syntax to create a LinkedHashMAp<String, Object>
[
  name: 'Per',
  isValidNumber: phoneNumberUtil.isPossibleNumber(number)
]
```

# Building the journo viewer
Journo uses maven. The prerequisites for building are
1. A JDK version 17 or later with javafx included (e.g. the Bellsoft full distro)
2. Maven version 3.8.4 installed

Then it's just a matter of `mvn install`!