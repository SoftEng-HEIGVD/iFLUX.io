---
layout: post
title:  "Awareness with iFLUX and Slack at Novaccess (Part 1)"
author: Olivier Liechti
categories: proof-of-concept
---

We are progressing with the iterative design of the iFLUX APIs and are making good progress. We have achieved a milestone where we are able to demonstrate the end-to-end process enabled by the iFLUX programming model.

In order to challenge and validate our first draft, we have decided to implement and deploy [a very simple version](http://practicetrumpstheory.com/minimum-viable-product/) of the iFLUX server and to use it to [solve a concrete problem for us](http://en.wikipedia.org/wiki/Eating_your_own_dog_food). "We" refers to the partners involved in the iFLUX project and this includes the [Novaccess](http://www.novaccess.ch) team, which we are going to briefly introduce now.

### Novaccess, the industrial IoT and smart lighting

[Novaccess](http://www.novaccess.ch) is a **spinoff** from the [Institute for Information and Communication Technologies](http://iict.heig-vd.ch) at the [University of Applied Sciences Western Switzerland](http://www.hes-so.ch). Founded by a team of engineers with diverse backgrounds (embedded systems, networking, software), the company aims to develop innovative solutions for the industrial Internet of Things. The solutions are built on top of a platform that integrates  **hardware** components, **cutting-edge firmware** and a **cloud-based software platform**.

**Smart cities** and **smart lighting** are domains that Novaccess has decided to tackle in priority. The company has developed the **Novalight product suite**, which allows municipalities to make cost savings by lowering energy consumption and improving maintenance procedures. When the Novalight solution is deployed, measures are **collected** from street lights (consumption values, faults, etc.) and analyzed by the software platform. City staff also has the ability to **remotely control** the lights (state, power, etc.) at a fine granularity. A lot more could be said about the solution, but this brief introduction should be enough to understand the scope of the proof-of-concept.

### Functionality: awareness

As mentioned before, we wanted the first iFLUX proof-of-concept implementation to **solve a concrete problem for us**. The motivation was to be in a position to evaluate the programming model and APIs in a **practical situation** and to **gather useful feedback** as early as possible.

The problem that we have decided to address is the [awareness](http://scholar.google.ch/scholar?cluster=2636264938097959540&hl=en&as_sdt=0,5) that the Novaccess team would like to maintain about some of its customer deployments. Awareness is a concept that has been studied extensively in the Computer Supported Cooperative Work (CSCW) literature, long before social networks and mobile chat applications became popular. While awareness is a somewhat broad concept, with several definitions, we like to think of it as a *general sense of what is happening in a particular environment*. As an example, when colleagues share an open space, they get a constant flow of audio visual cues (people speaking, moving around, body language, etc.) that they process at the [*periphery*](http://www.ubiq.com/weiser/calmtech/calmtech.htm) of their attention. When they work remotely, these cues tend to disappear and as a consequence collaboration becomes more difficult. This observation has lead to the design of various [tools](http://www.dourish.com/publications/1992/chi92-portholes.pdf) that recreate the sense of a [shared space](http://en.wikipedia.org/wiki/Media_space) over distance. [Some](http://tangible.media.mit.edu/project/ambientroom/) of them put an emphasis on [peripheral awareness](http://scholar.google.ch/scholar?cluster=10956886817295254803&hl=en&as_sdt=0,5) and rely on [abstract representations](http://www.sigchi.org/chi97/proceedings/paper/erp.htm) of activity. These tools are the **ancestors of the social services**, **applications** and **devices** that have become common in work and domestic settings.

Applied to the **context of a Novalight deployment**, awareness means that the Novaccess team would like to get a better sense about what is happening, on a **continuous basis and without effort**. The team would like to be able to detect unusual or interesting patterns, to be able to react to them appropriately. There are actually **different dimensions** that the team would like to be aware of. For example, Novaccess **product owners** would like to get a sense of the activity of end-users (are they using the web front-end, are they facing issues, are there features that they use a lot, etc.). On the other hand, Novaccess **engineers** would like to get a sense of the  technical activity (are there communication issues, are there faulty components to replace, what is the amount of transmitted commands, etc.). We expect to find out which are the most useful as we use the system described in this article. One benefit of a middleware like iFLUX is that it enables **rapid prototyping and reconfiguration of smart environments**.

The first version of the awareness support system at Novaccess is based on the following components:

* At one end, **software sensors embedded in the Novalight components** collect various events (user activity, processed commands, faults, etc.).

* On the other end, the Novaccess team uses the [Slack](http://www.slack.com) collaborative tool as a way to **receive a representation of the ongoing activity**. They subscribe to chat channels dedicated to various dimensions.

* In the middle, **iFLUX** is used to collect events and, based on a set of rules, to generate the text messages that are pushed to the team via Slack. At a high level, the implemented model can be described with the following rule: ***IF** something interesting happens in Novalight, **THEN** push a message in a relevant Slack channel*. As a matter of fact, the flexibility of the setup comes from the fact that it is possible to configure more than a rule. This allows the team to fine tune the amount and destination of the generated awareness messsages.

### System architecture

The architecture of the system deployed for the proof-of-concept is shown in the diagram below. It shows the various components and the interactions between them.

{:.image.fit}
![System architecture]({{ site.baseurl }}/images/diagrams/poc-novaccess-slack.png)

1. The **Novaccess team** configures the iFLUX server and **decides how the events emitted by the Novalight components should be notified via Slack**. They may decide to use one or more Slack channels. They may also decide to ignore some of the event sources (perhaps because they are too noisy or not relevant). They also have means to specify what the messages posted in the Slack channel should contain (typically, a human-readable summary of the event posted to iFLUX). In terms of API, this step is done by POSTing rule payloads to the `/rules` endpoint (see [online documentation]({{ site.baseurl }}/api/reference/#rules)). To make this process easier, we have implemented a [simple rule editor]({{ site.ifluxUrl }}) as part of the proof-of-concept.

2. A collection of **event sources** emit events and issue HTTP POST requests to the `/events` endpoint. The proof-of-concept illustrates **different types of event sources**:
	* The first one (**2a**) is an application, which emits events that correspond to user actions (*user has logged in*, *user has changed a configuration setting*, *user has sent a command to a street light*, etc.)
	* The second one (**2b**) is a back-end platform, which emits events that can be either at the business logic (*a command has been forwarded to a controller*, *access has been denied to a user*, etc.) or technical level (*a communication failure has happened*, *the database size has reached a given threshold*, etc.).
	* The third one (**2c**) is a **smart object**, which emits events can also be functional (e.g. *a measure has been collected and sent to the platform*) or technical (e.g. *a connection has been established over a communication link*).

3. Whenever an event is received by the iFLUX server, **every rule configured by the Novaccess team configured is evaluated**. If all **conditions** are met (event source, event type, event properties), then the action defined in the rule is **triggered**.

4. In the proof-of-concept setup, there is only one type of action. The action consists in **posting** a *message* into a *Slack channel* (these are the two properties associated to the action). The content of the message is defined in a template (in the rule). Hence, the message can include event properties. The action payloads are POSTed by the iFLUX server to the **Slack Gateway**, which implements the `/actions` endpoint.

5. Finally, the Slack Gateway uses the Slack API to push the notification towards the Novaccess team. This last step is not in the scope of the iFLUX model.


### REST API Calls

In an upcoming article, we will give more details about the implementation of the system. Before that, let us take a look at the REST API calls involved in the end-to-end process. That will give a better sense of what is happening, how the system can be configured and how it can be extended.

#### Sending events to the iFLUX server

For sensors, it is very easy to send events to the iFLUX server. They send a payload to the `/events` endpoint, as described in the [API documentation]({{ site.baseurl }}/api/reference/#events). Several comments can be made about the example show below:

* The client sends an **array of events**, even if it sends a single event.

* At the moment, the client can send any string in the **source** property; the value is important because it is used in the rules, but the source does not need to be registered on iFLUX yet.

* At the moment, the client can also send any string in the **type** property; we plan to use JSON schemas soon, but for now the client may send a URL, a simple string or a fully qualified name (e.g. `ch.novaccess.events.NovaGateEvent`).

* The **event properties** obviously depend on the event type.

* Last but not least, the example gives the endpoint of the [sandbox iflux server](https://iflux.herokuapp.com), deployed on [heroku](http://www.heroku.com). Be aware that this is a highly dynamic and unstable environment! We use it as a prototyping environment and are aggressive in pushing new features to it. In the current version, configuration data is not persisted, so the event-condition-action rules need to be resubmitted after a new deployment. For the time being, make sure to script the rule configuration that you make with REST API calls!


{% highlight http %}
POST https://iflux.herokuapp.com/events/ HTTP/1.1
Content-type: application/json

[
  {
    "timestamp" : "2015-01-12T05:21:07Z",
    "source" : "/event-sources/novalight-webconsole",
    "type" : "/eventTypes/userAction",
    "properties" : {
      "user" : "John Doe",
      "action" : "has logged in"
    }
  }
]
{% endhighlight %}

#### Configuring rules on the iFLUX server

For the Novaccess team, the system is configured by POSTing rule payloads to the `/rules` endpoint. Here are a couple of comments about the example below:

* The rules must be pushed to the same iFLUX server, hosted on heroku.

* The **eventSource** and **eventType** properties can either contain a * (to indicate that any value will be accepted to trigger the rule) or a value that will be compared against what is in the event payload (see comment above). Later on, we are likely to support regular expressions as well. At the moment, the **eventProperties** property is not used (later on, we will add the ability to specify that a rule should be triggered only if some conditions are met on the custom event properties).

* The **actionTarget** property contains the endpoint of the **iFLUX action target** that will receive action payloads when the rule is triggered. As part of the proof-of-concept, we have implemented a **gateway to integrate with Slack via their realtime API**. Important: you should not send action payloads to the gateway. You should send events to the iFLUX server (with the possible side effect that one or more rules will be triggered, at which point iFLUX will send an action to the Slack gateway).

* The **actionSchema** defines how the action payload (which will be sent to the Slack gateway) should be generated, based on the values of a trigger event. The value of this property is a [handlebars](http://handlebarsjs.com/) template, which is something fairly easy to write. In this particular context, it is simply a JSON document in which you insert tags such as {% raw %}{{ properties.user }}, {{ timestamp }} or {{ source }}{% endraw %} (they all refer to the event JSON structure presented before). Note that in the schema, the value of the **type** must be a valid action type. In the proof-of-concept, there is a single action type: **sendSlackMessage**.

{% highlight json %}
{% raw %}
{
  "type": "sendSlackMessage",
  "properties": {
    "channel": "aware",
    "message": "NovaLight activity: {{ properties.user }} {{ properties.action }} (at timestamp)"
  }
}
{% endraw %}
{% endhighlight %}

* The schema shown in the above must be encoded (**double quotes must be escaped**) before being inserted in the rule payload. This is one of the reasons why we have implemented a first [rule editor](https://iflux.herokuapp.com/rule-editor), also available in the iFLUX sandbox. It should be pretty straightforward to use: fill out a couple of text fields with the rule description and conditions, write the schema in an editor and hit a button to generate your rule payload.

* As a result, you will end up with an HTTP request that you can send to the iFLUX server. If you need to change the rule, you can use the GET and DELETE verbs on the same endpoint.


{% highlight http %}
{% raw %}
POST https://iflux.herokuapp.com/rules/ HTTP/1.1
Content-type: application/json

{
  "description": "If a user does something in the Novalight Web Console, send a message via Slack.",
  "if": {
    "eventSource": "/event-sources/novalight-webconsole",
    "eventType": "/eventTypes/userAction",
    "eventProperties": {}
  },
  "then": {
    "actionTarget": "https://nova-slack.herokuapp.com",
    "actionSchema": "{\"type\":\"sendSlackMessage\",\"properties\":{\"channel\":\"aware\",\"message\":\"NovaLight activity: {{ properties.user }} {{ properties.action }} (at timestamp)\"}}"
  }
}
{% endraw %}
{% endhighlight %}


#### Actions sent the Slack gateway

For information, but again it is *not* something that you have to do (it is the responsibility of the iFLUX server to issue these requests), here is an example of payload sent to the Slack gateway.

{% highlight http %}
{% raw %}
POST https://nova-slack.herokuapp.com/actions/ HTTP/1.1
Content-type: application/json

{
  "type": "sendSlackMessage",
  "properties": {
    "channel": "aware",
    "message": "NovaLight activity: John Doe has logged in (at timestamp)"
  }
}
{% endraw %}
{% endhighlight %}


