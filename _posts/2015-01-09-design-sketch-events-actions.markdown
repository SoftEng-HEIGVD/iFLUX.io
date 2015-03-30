---
layout: post
title:  "Design Sketch: Conceptual Architecture"
author: Olivier Liechti
date:   2015-01-09 09:00:00
categories: design-sketch
---

In this first design sketch, we introduce the conceptual architecture of the iFLUX middleware. Our goal is to describe the high-level programming model and core abstractions and to illustrate them with a simple. In upcoming sketches, we will dig into more details and look at the actual APIs exposed by the middleware.

### Conceptual model

#### Event sources

The first abstraction in the iFLUX model is the notion of **event source**. As its names indicates, an event source is a component that emits a stream of events. There are different types of event sources, here are some examples:

* **Connected hardware sensors** that emit a continuous flow of low-level events. In this case, the events are observations or measures captured by the sensors.

* **Software sensors** that capture some kind of activity in a digital system and emit related events. For instance, one can think of a software sensor embedded in a business application that emits an event whenever some condition is met.

* **Data processing services** that emit higher-level events. Typically, a data processing service aggregates several streams of low level events and applies some kind of logic to produce a new stream.

* **User agents** used as proxy to emit human generated events. For instance, think of a mobile application used to report incidents.

The iFLUX architecture defines a standard API that event sources use to stream their data. The API specifies a simple payload structure: an **event** is defined by a timestamp, a source, a type and a list of properties. The list of properties depends on the type of event.

#### Action targets

The second abstraction in the iFLUX model is the notion of **action target**. An action target is a component that exposes functions that can be triggered from the iFLUX middleware. Here also, there are different types of action targets:

* **Connected hardware actuators** that can be remotely controlled. A smart street light or a large display located in a stadium are two examples for such installations.

* **Software actuators** that are typically business applications or gateways deployed for integration purposes.

* **Data processing services** that use actions as a way to receive events published by event sources. This pattern is described in more details below.

* **User interaction channel gateways** that are special software actuators geared at delivering notifications to people. Examples include gateways for delivering e-mails, push notifications and social network notifications.

The iFLUX architecture defines a standard API that makes it possible to send **actions** to action targets. An action is defined by a timestamp, a trigger context, a type and a list of properties.

#### Rules

The third abstraction in the iFLUX model is the notion of **rule**. Rules are what bind events and actions together. A rule specifies that **if** an event is notified **and** its properties meet certain criteria **then** an action has to be triggered with a list of arguments (which values are often computed based on the event properties).

{:.image.fit}
![My helpful screenshot]({{ site.baseurl }}/images/diagrams/ECA.png)


### Illustrative scenario

To illustrate the iFLUX model, let us consider a simple scenario and see what components are required to support it. Let us see how these components interact with each other in order to deliver functionality to end-users.

In our scenario, the goal is to detect risk situations in a city and to notify police staff when the current risk evaluation crosses a certain threshold in a particular location. The detection of risk situations is based on a model, which is fed with information coming from smart sensors and voluntary citizen notifications.

iFLUX enables the decoupling between the information sources and the application used to notify the police staff.

#### Components

The following components are part of the solution:

* **a set of crowd density sensors**, which are IP-connected hardware equipments deployed in selected locations (train stations, public spaces, areas within a stadium, etc.). Based on different measures (sound, video, etc.), they estimate the current density of population in their direct vicinity. The sensors push fairly low-level events to iFLUX, on a fixed interval. The events contain the estimation for the current density at the sensor location.

* **a mobile application** made available to citizen, who can use it to report incidents and to proactively notify risk situations. The mobile application is also an iFLUX event source.

* **one risk analysis module**, which responsibility is to maintain an up-to-date risk situation for the city. It continuously applies the information provided by the crowd density sensors and the mobile application to a risk model. When the estimated risk level in a location crosses a specified threshold, it issues an iFLUX event. The risk analysis module is at the same time an event source and an action target.

* **the police department information system** which acts as an action target. When alert events are emitted by the risk analysis module, an action should be triggered as a way to notify police staff.

{:.image.fit}
![My helpful screenshot]({{ site.baseurl }}/images/diagrams/scenario.png)


#### Processing

iFLUX integrates the previous components in a loosely coupled fashion:

* The iFLUX service collects the event streams emitted by the crowd density sensors and by the mobile app. When a new event comes in, the middleware evaluates a set of event-condition-action rules and decides whether certain actions need to be triggered.

* Two rules are configured and specify that whenever a low-level event is received from a sensor, respectively from the mobile app, then an action is triggered on the risk analysis module. The event is passed in the action parameters. In other words, iFLUX provides a mechanism to forward events between iFLUX components.

* A third rule is configured that whenever an alert event is received from the risk analysis module, then an action is triggered on the police department information system. In this case, the event is not passed as-is. Some of its attributes are extracted, formatted and passed as action parameters.


{% highlight bash %}

# Crowd density sensors (event sources)
1. measure current crowd density at location
2. prepare event payload
3. send event payload to iFLUX
4. sleep and go back to step 1

# Citizen mobile app (event source)
1. accept user input
2. prepare event payload
3. send payload to iFLUX
4. go back to step 1

# Risk analysis module (action target and event source)
1. accept action payload
2. if the action type is 'forwardEvent', extract event payload from action parameter
3. process event, update risk model and compute current risk level
4. if threshold reached, prepare event payload with risk situation details
5. send event payload to iFLUX
6. go back to step 1

# Police department information system
1. accept action payload
2. extract risk situation details from action parameters
3. inform police staff (push notifications, update visualization, etc.)
4. go back to step 1

# Rule 1 (event forwarding)
IF the 'event source' is the 'crowd density sensor' THEN trigger the 'forwardEvent' action on the 'risk analysis module' and pass the event as an action parameter.

# Rule 2 (event forwarding)
1. IF the 'event source' is the 'citizen mobile app', THEN trigger the 'forwardEvent' action on the 'risk analysis module' and pass the event as an action parameter.

# Rule 3 (notification)
1. IF the 'event source' is the 'risk analysis module' THEN trigger the 'notifyRiskSituation' action on the 'police department information system'.

{% endhighlight %}


