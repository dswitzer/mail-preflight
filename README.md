# Mail Preflight [![Build Status](https://travis-ci.org/kkonstantin42/mail-preflight.svg?branch=master)](https://travis-ci.org/kkonstantin42/mail-preflight)

Mail Preflight is a tool for preprocessing email HTML.
Currently that includes:
* Inlining css
* Removing html comments
* Applying augmented css (a silly name that means setting html attributes using special css properties)

## State of the project
__Project is currently in a process of being open sourced.__ It started as a css inliner tool in a different closed source project. 
Main functionality is tested but there is no documentation, no proper tests and it's __not published to maven central yet__. 
Api might also change in the near future.

## Contributions
Contributions are currently not accepted (or rather not expected) but that will change soon.

## Why this project is needed

For better client compatibility css styles in email html should be inlined, but at the time of writing there were no pure java solutions for inlining css in email html. 
We needed a solution that would be able to inline css and which would:

* be relatively efficient so that emails could be preprocessed at runtime
* respect css specificity rules
* handle @media selectors correctly by leaving them untouched

I did some googling and found that currently there are a few options available to handle css inlining:

* Mailchimp's [__Css Inliner Tool__](https://templates.mailchimp.com/resources/inline-css/): nice little utility but it is an online tool 
and not a library and it does not have any open API. It is possible to use it to manually preprocess email templates every time they are 
changed, but that is not what we wanted. 

* [__Premailer__](https://github.com/premailer/premailer) with jruby based [java wrapper](https://github.com/mdedetrich/java-premailer-wrapper): works great, but it is written in Ruby. 
We found that jruby startup takes a long time and bringing it as a dependency just for css inlining would be an overkill.

* All mighty __StackOverflow__: there are useful SO answers by [jnr](https://stackoverflow.com/a/21757928/2270046) and [Moody Salem](https://stackoverflow.com/a/33196033/2270046). 
The first one introduces the idea of using [cssparser](http://cssparser.sourceforge.net/) library for parsing css, 
and the second one gives a basic concept on how to handle css specificity. And then there was this comment 
by [seanf](https://stackoverflow.com/users/14379/seanf):
>Moody Salem's answer doesn't use CSSOMParser (so the CSS parsing may not handle edge cases), but it looks like it does try to handle priorities. 
Dear lazyweb, please combine the two solutions! 
 
 
Well, that is exactly what "lazyweb" (me) decided to do. Or at least that was a start.
While this project is originally based on these two SO answers, most of the code was refactored, improved, bugs fixed and 
new functionality added.
  
  
## Getting started

To start using the library just include it as maven or gradle dependency.

!!!__NOT YET PUBLISHED__!!!


Maven dependency:

    <dependency>
      <groupId>eu.kk42</groupId>
      <artifactId>mail-preflight</artifactId>
      <version>0.1</version>
    </dependency>  
    
Gradle dependency:

    implementation 'eu.kk42:mail-preflight:0.1'
    
#### How to use it

Simplest case with default configuration (with all features enabled):

    MailPreflight preflight = new MailPreflight(); //MailPreflight is thread safe
    String result = preflight.preprocessEmailHtml(someHtml);

