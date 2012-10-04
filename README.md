#Overview

dmk-android is an Android app for connecting to a DMK yacht instruments box (http://dmkyacht.com/).  This code is meant to demonstrate how an Android developer may start to work with a DMK box. 

#Features

* Displays data stream from a DMK box in a simple list view.

* Start and Stop listening controls.

#Not-featured

* This app does not yet display any graphical instrumentation.  

* Given the limitations of Android, it can not connect directly to a DMK box using the preferred ad-hoc Wifi connection type.  Rather, the DMK box must be set to infrastructure mode and connected to a wireless access point.  Once the device running this app is connected to the same access point then it will be possible to subscribe to the data stream from the DMK box.
