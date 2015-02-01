---
layout: post
title:  "Analytics with iFLUX: the Metrics Action Target (Part 1)"
author: Olivier Liechti
categories: action target, analytics
---

In most IoT/WoT applications and deployments, there is some sort of **analytics** element. Indeed, if sensors are deployed to collect data, it is often because people and organizations expect to **get insights about a particular domain**. For example, if sensors are deployed to monitor road traffic, one would expect to be able to **detect** traffic jams, to **analyze** when they tend to happen and ultimately to **predict** them. As another example, if sensors are deployed to monitor the affluence of people in certain locations, one would expect be able to **detect** and to **react** to unusual and risky situations. 

To illustrate this point, consider [Urban Engines](https://urbanengines.com), a startup that processes various data streams (commuter fare card data, bus GPS traces, etc) to compute metrics and present synthetic information to transport system operators and commuters. The insights that people get from the metrics allow them to adjust their routes and schedules, so that they spend less time commuting. 

{:.image.fit}
![Urban engines]({{ baseurl }}/images/screenshots/urban-engines.jpg)


**Analytics** is defined as the *information resulting from the systematic analysis of data or statistics*. It is all about **processing** raw data, about **inferring** information and about **presenting** this information in ways that help people better understand what is happening in a specific context. The insights that emerge through an **analytics process** are helpful when it comes to **make decisions** or to **take actions**. 

The analytics process is supported by different **tools** (data processing, information management, visualization, presentation, etc.). When these tools are assembled and integrated to work together, one can speak of an **analytics solution** or an **analytics platform**. For end-users, an analytics platform often looks like some kind of personalized **dashboard** that presents facts in various **widgets**. The dashboard allows them to **explore** the information space and to **iteratively** understand what is happening. Under the hood, the analytics platform takes care of **technical issues**: data collection, data processing, scalable information storage, efficient query processing, etc.


### Analytics with iFLUX

As we continue our experimentation validation of the iFLUX model and APIs, we have taken a look at what analytics means in the context of iFLUX. There are two very different ways to look at this question.

* Firstly, we see a clear need to embed analytics in the iFLUX platform in order to **understand its own usage and behavior**. At which rate are new event sources being connected to the platform? Are there rules that trigger actions in unusual ways? Is there an increase in technical issues on the data storage front? These are all example of questions that we would like to answer when iFLUX is put in production.

* Secondly, and this is what this post is about, the iFLUX architecture **provides some of the blocks required to build an analytics platform**. To illustrate the point, we have designed an implemented a simple system where information is extracted from raw data and made available to UI widgets.

In the following paragraphs, we will describe a system that addresses a common use case encountered in an analytics process: the computation of aggregate statistics over time series. After introducing this use case with an example, we will see how iFLUX concepts can be applied in a system that computes the statistics from a flow of events.



#### Computing aggregate statistics over time series

**A time series is a sequence of values measured over time**. For instance, if a thermometer measures the current temperature at a given collection, recording the temperature on a regular interval produces a time series. A time series consists of raw data, which needs to be processed and analyzed in order to obtain useful information. A basic and common form of processing consists in **computing aggregate statistics** (min, max, average values) over time intervals (hourly, daily, monthly, etc.).


{:.image.fit}
![Analytics system overview]({{ baseurl }}/images/diagrams/analytics-data-information.png)

As illustrated in the previous diagram, what we want to do is to build a system that collects a continuous stream of measures, applies some logic to update the aggregate values and makes them available to a dashboard user interface. Measures are a special type of events, so it is easy to see how sensors and measures can be respectively mapped to iFLUX event sources and iFLUX events. In our approach, we have considered the component that computes the aggregate values and makes them available to the dashboard as an iFLUX action target. With this model, we then create iFLUX rules that express that *IF a measure is reported by a sensor, THEN an action is triggered to update the aggregate values for this sensor*. We refer to the aggregate values for a sensor as a **metric**.

#### Implementation with iFLUX

An overview of the system is shown in the following diagram. Its components interact as follows:

1. Some **iFLUX event sources emit events** that represent some kind of **measure**. In our implementation, a measure is defined by a name (e.g. temperature) and a numeric value (e.g. 21.3).

2. Other **iFLUX event sources emit events** that represent a simple **occurrence** (e.g. an alarm has been raised, an incident has happened, a presence has been detected, etc.). In this case, the numeric value is often omitted (it defaults to *1*, to indicate a single occurrence).

3. **Rules** are configured on the iFLUX middleware, so that whenever a new measure or occurrence is notified, an action is triggered to update the appropriate metrics.

4. The way to update a metric follows the standard iFLUX model. An **action payload** is POSTed to the `/actions` endpoint exposed by the metrics action target. Its type is something like `updateMetric` and its properties include the name of the metric and the value that was in the original measure.

5. The **metrics action target** is responsible to keep the aggregate values up-to-date. In our implementation, we have used **MongoDB** as a way to store them in persistent storage (analytics is a [typical use case](http://www.mongodb.com/use-cases/real-time-analytics) envisioned for the NoSQL database)

6. The **metrics action target** also exposes a REST API to the analytics dashboard, so that it can fetch the aggregate values and visualize them. For example, the dashboard could issue a GET HTTP request to `/metrics/temperatureInBaulmes/currentDay`, receive a JSON payload that contains average, min and max values for every hour of the current day. It would then pass that list to a graphing library, which would produce a column or a line chart.


{:.image.fit}
![Analytics system overview]({{ baseurl }}/images/diagrams/analytics-system-overview.png)

	

#### Computing and archiving aggregates with MongoDB

When using MongoDB to implement a use case such as this one, one decision to make is how to structure collections, documents and sub-documents. We have not yet run the model that we are about to present through performance tests, so we do not claim that is the most efficient and appropriate. Nevertheless, it illustrates some of the key concepts that MongoDB offers in the realm of analytics.

1. **Metrics and granularities**. In our model, a **metric** represents a set of aggregate values computed over a given time series. Examples of metrics include *the temperature measured in Baulmes*, *the number of incidents reported in the software platform* or *the duration of a commute trip*. We use the term **granularity** to express the fact that we compute aggregate values over intervals of different durations. Examples of granularities include *yearly*, *monthly*, *hourly* and *minutely*.

2. **Collections**. We use one collection for each granularity, for each metric. In other words, if we have a metric named *temperatureInBaulmes* and decide to compute **yearly** and **monthly** aggregate values, then we use 2 collections. Our convention is to call them metrics.temperatureInBaulmes.yearly and metrics.temperatureInBaulmes.monthly.

3. **Documents**. In every collection, we store one document per unit of time. For example, if we have a yearly collection, then we store one document for every year. If we have a monthly collection, then we have up to 31 documents for every month.

4. **Sub-documents**. In every document, we have the choice to store aggregate values at different levels. For instance, in a document of the yearly collection, we may decide to store the aggregate values for the whole year, but also for each month of that year, or for each day of that year. This highlight a trade-off when designing the MongoDB schema, which would need performance tests in order to be properly assessed. The number of documents that need to be fetched and processed in order to answer a user question, the size of documents, the time required to update the right documents when a new measure is reported are all aspects that need to be considered.

{:.image.fit}
![MongoJS schema]({{ baseurl }}/images/diagrams/analytics-mongo-documents.png)



### Implementation with Node.js, mongojs, express.js and highcharts.js

We have implemented a simple version of the above system with Node.js, mongojs (a node module that provides an interface to MongoDB) and the express.js framework. We have also used the highcharts.js visualization library to implement the analytics widgets. With the implementation, we are able to demonstrate the end-to-process: event produced by iFLUX event sources are evaluated by rules, which result in the trigger of udpateMetric actions. The aggregate values are updated and made available to the dasbhoard via a simple to use REST API.

In second part of this article, we will take a closer look at the implementation of the Node.js implementation and give more details about how to use the metrics action target in your own deployments.


