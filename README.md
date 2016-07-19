# Slate Core [![Build Status](https://travis-ci.org/ChristopherDavenport/slate-core.svg?branch=master)](https://travis-ci.org/ChristopherDavenport/slate-core) [![codecov](https://codecov.io/gh/ChristopherDavenport/slate-core/branch/master/graph/badge.svg)](https://codecov.io/gh/ChristopherDavenport/slate-core) [![Stories in Ready](https://badge.waffle.io/ChristopherDavenport/slate-core.png?label=ready&title=Ready)](https://waffle.io/ChristopherDavenport/slate-core)


This is a base library for utilizing the ability to pull and parse
default responses from Slate. Currently this is used to parse their
default response format so that the developer can transition immediately
to consuming and working with the data they are trying to work with, 
rather than working on interacting with the Slate Json.

Those who utilize the library are going to need to extend the 
DefaultJsonProtocolwith their custom json class and then they can place a 
Request for the object.

To make a request simply create a request and then retrieve it, or utilize the
SingleRequest feature on the accompanying object. 