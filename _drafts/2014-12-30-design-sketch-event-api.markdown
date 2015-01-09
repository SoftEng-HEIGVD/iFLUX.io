---
layout: post
title:  "Design Sketch: the Event API"
author: Olivier Liechti
date:   2014-12-30 14:20:14
categories: design-sketch
---

The iFLUX middleware exposes several REST API endpoints, which are used by developers to interact with the rule-based service. In this first design sketch, we introduce the endpoint that is used to report the occurrence of events. The endpoint is used by components of the iNUIT platform that can be of very different nature. It is used by sensors, which notify low-level events. It is also used by data processing modules, which notify higher-level events.

### Usage scenarios

#### 1. The crowd density sensor

Let us imagine that we have a sensor is capable to measure the density of population in a given place. It could be a train station, a stadium or a particular street. We want to allow the sensor to report a stream of events, so that services can process them and trigger various workflows. We could actually think of different types of events and of different emission patterns. A first pattern would be for the sensor to send a measure at a regular interval. Another pattern would be for the sensor to send an alert when the density crosses a specified threshold.

#### 2. The risk analysis module

In this scenario, let us imagine that we have a software module that collects data from various sensors and uses sophisticated algorithms to compute the current risk factor for a list of locations. Here again, we want a mechanism for the software module to publish the output of its computation as a series of events. Like in the previous example, we can think of different emission patterns. The module might emit events that capture the current risk factor for a location (or a list of locations) on a regular basis, or it might emit alerts that indicate that a risk factor has reached a particular level.

### Requirements

#### Simplicity

#### Loose coupling

#### Security


### REST API

{% highlight http %}
POST /events/ HTTP/1.1
Content-type: application/json
Content-length:

{
  "ts" : ,
  "properties" : {
    "foo1" : "bar1",
    "foo2" : "bar2"
  }
}
{% endhighlight %}

