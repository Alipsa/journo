# Journo Viewer

The Journo Viewer is a javafx application simplifying the workflow of creating Freemarker Templates that
are rendered into a PDF using Journo. It requires a JDK version 17 or higher with JavaFx included (such as the [Bellsoft full distribution](https://bell-sw.com/pages/downloads/#jdk-17-lts))
for both compilation and running.

You start the application with e.g. `java -jar journo-viewer-0.6.0.jar`.  

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