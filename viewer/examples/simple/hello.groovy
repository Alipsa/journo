/*
  Uses the following dependencies
  com.googlecode.libphonenumber:libphonenumber:8.13.29
*/
@Grab('com.googlecode.libphonenumber:libphonenumber:8.13.29')
import com.google.i18n.phonenumbers.PhoneNumberUtil

PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
def number = phoneNumberUtil.parseAndKeepRawInput('070-1232345', "SE");
def a = 'Per'
[
  name: 'Per',
  isValidNumber: phoneNumberUtil.isPossibleNumber(number)
]