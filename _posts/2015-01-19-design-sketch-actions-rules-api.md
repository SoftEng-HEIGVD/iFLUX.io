---
layout: post
title:  "Design Sketch: Action and Rule REST API"
author: Olivier Liechti
categories: design-sketch
---

We have just updated the [API specification](http://www.iflux.io/api/) with two endpoints, which complement the `/events` endpoint that we have [recently]({% post_url 2015-01-12-design-sketch-event-api %}) presented. Together, the three endpoints provide the core functionality of the iFLUX model:

* **events** are notified via the `/events` endpoint
* **rules** are configured via the `/rules` endpoint and evaluated whenever a new event is reported
* **actions** are triggered via the `/actions` endpoint


### The `/actions` endpoint

This endpoint is not implemented by the iFLUX middleware, but by **action targets** (iFLUX is a client and issues POST requests to the endpoint). As an example, consider an e-mail gateway. By implementing the `/actions` endpoint, the gateway can be integrated in a system where an email is sent to users when specific events are reported to iFLUX.

### The `/rules` endpoint

This endpoint is used to configure the **event-condition-action rules** evaluated by iFLUX when events are reported. We will come back later at the implementation details, but we can already mention that our first implementation will rely on [handlebar](http://handlebarsjs.com/) templates for computing action properties based on event properties.

As an example, consider a rule that states that whenever a new temperature event is notified, an email should be sent to inform a certain person. In this use case, the following iFLUX elements would be used:

* a **temperature event type**, which would typically be defined by a **location** and a **temperature** properties.

* an **email notification action type**, which would be defined by a **recipient**, a **subject** and a **body** properties.

* a **rule**, which would trigger an email notification action whenever a temperature event is received. In this rule, we want to use the event properties to compute the action properties (e.g. the body should be something like "Temperature in [event.location] is now [event.temperature]"). Handlebars provide a way to specify these transformations.


