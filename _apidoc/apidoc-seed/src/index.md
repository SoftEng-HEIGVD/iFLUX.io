---
title: Overview
sectionName: API Reference
template: api.jade
menuIndex: 4
---

This pages contains general documentation about the API. Use the links on the
right to navigate to specific resources.


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
