---
title: Overview
sectionName: API Reference
template: api.jade
menuIndex: 4
---

This pages contains general documentation about the API. Use the links on the
right to navigate to specific resources.

### System components

An overview of a system built with iFLUX is shown in the diagram below:

* **Event sources** (e.g. sensors) report **events** using the `/events` endpoint exposed by the iFLUX middleware. An event is defined by a type, a timestamp and a list of properties.

* Event-Condition-Action **rules** are configured on the iFLUX middleware. Rules specifies what actions should be triggered when specific events are reported. Every rule has an associated schema (provided as a [handlebars](http://handlebarsjs.com/) template), which makes it possible to compute action properties from event properties.

* **Action targets** expose a `/actions` endpoint, used by the iFLUX middleware to trigger actions when rules are evaluated positively. An **action** is defined by a type and a list of properties. The diagram shows two examples of action targets: gateways used to relay user notifications via the [Slack](http://www.slack.com) collaboration platform or via email.

<center>
	<img src="/images/diagrams/apidoc-overview.png" alt="System overview" style="width: 500px;"/>
</center>



### Content-type

The API uses the JSON format.

<!--
### Authentication

To interact with the API, your client will need to be authenticated. This is done by using the **Authorization** header with the username and password of the client and gives something that looks like:

	Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==


### Errors

In case of error, the API will send a JSON response with the list of errors. 
Each error has a human-readable message and a code. The code identifies the 
error type and can be used to handle specific errors differently or for 
translation purposes.


```
HTTP/1.1 400 Bad Request
 
{
  "errors": [
    { 
      "message": "JSON parsing error.",
      "code": 10000
    }
  ]
}
```
-->

### Dates

All dates used in the API are in UTC and use the `ISO-8601` format (ex: 
`2015-02-15T05:21:07Z`).
