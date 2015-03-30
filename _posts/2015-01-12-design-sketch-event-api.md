---
layout: post
title:  "Design Sketch: Event REST API"
author: Olivier Liechti
date:   2015-01-12 12:35:00
categories: design-sketch
---

In a [previous design sketch]({{ site.baseurl }}{% post_url 2015-01-09-design-sketch-events-actions %}), we have introduced the high-level programming model for iFLUX. We have discussed the notions of **event sources** and **action targets** and explained how Event-Condition-Action **rules** are used to integrate them.

In this new sketch, we take a first look at the **Event REST API**, which is used by event sources (sensors, data processing services, etc.) to report the occurrence of events (measures, alerts, etc.). At this stage, there are two important elements to be aware of:

* We have decided to build iFLUX in **a very iterative and open fashion**. What we describe here is the very first specification of the Event API. It is extremely simple and many issues that we are already aware of have been left out intentionally. One important motivation for releasing basic specifications and implementations early is to enable other groups in the iNUIT research programme to get familiar with our service as early as possible.

* The REST API that we describe here is **only one of the messaging interfaces** that we plan to offer. WebSockets, in particular, are also on our roadmap. We will however be releasing a version of the middleware as soon as the REST API is implemented.


### The `/events` endpoint

The first version of the Event API is very simple. The only thing that we need is a way for event sources to notify iFLUX that an event of some sort has occurred. The events posted by clients are processed asynchronously by the backend and the client should not expect a body in the response. The API does not offer a way to retrieve events posted in the past. In other words, the `/events` endpoint is write-only. Beyond that, we have the following requirements:

* The payload POSTed to the endpoint is a **list of event payloads**, even if the event source wants to report a single event.
* Every event should contain a **timestamp**.
* Event event should contain the reference of an **event source**. We propose to use URLs for this, following the [HATEOAS](http://en.wikipedia.org/wiki/HATEOAS) principle.
* Every event should contain the reference of an **event type**. We plan use [JSON schemas](http://json-schema.org/) here, but that is not a requirement initially.
* Every event should contain a **list of properties**. When we use JSON schemas, we will be able to validate that the list of properties sent with an event payload are valid.
* In the initial version of the API, we **do not consider security constraints**.


As described in the [online API documentation]({{ site.baseurl }}/api/reference/#events), here is an example of HTTP request that could be sent by an event source:

{% highlight http %}
POST /events/ HTTP/1.1
Content-type: application/json

[
  {
    "timestamp" : "2015-01-12T05:21:07Z",
    "source" : "/event-sources/JI8928JFK",
    "type" : "/eventTypes/temperatureEventSchema",
    "properties" : {
      "temperature" : 22.5,
      "location" : "room 1"
    }
  },
  {
    "timestamp" : "2015-01-12T05:22:07Z",
    "source" : "/event-sources/JI8928JFK",
    "type" : "/eventTypes/temperatureEventSchema",
    "properties" : {
      "temperature" : 22.8,
      "location" : "room 1"
    }
  }
]
{% endhighlight %}


### Sandbox implementation

We have implemented a minimal implementation of the `/events` endpoint. Events are not processed, not even stored in persistence storage. The sandbox, however, is useful for developers who want to get started with the implementation of iFLUX clients. The implementation is also useful to validate the JSON serialization that can be tricky in some situations (such as the use of dynamic properties lists).

Later in the project, we plan to make extensive use of [Docker](http://www.docker.com) to make it possible for developers to have a production-like environment on their laptops. For the first sandbox, we have opted for a simpler setup. We do not use Docker yet, but use [Vagrant](http://www.vagrantup.com). This allows developers to quickly get a virtual machine up and running with the sandbox components.

#### Vagrant box

The sandbox is packaged as a virtual machine that can be launched with VirtualBox. The VM is based on a Ubuntu 14.04 image. We use Vagrant to provision the VM and install the required software components (Oracle JDK 8, Glassfish 4.1, Node.js, maven and various utilities). With this approach, it is very easy for someone to get a fully configured environment on a development machine. We will describe the procedure in an upcoming post.

#### Java EE server implementation

The first component of the sandbox is a basic Java EE implementation of the `/events` endpoint. The implementation is a maven project, based on the Java EE 7 web profile (the result of the build process is a .war file). It uses the JAX-RS standard API for building RESTful services in Java. The .war file can be deployed in any container. The sandbox includes a Glassfish 4.1 installation (listening on ports 8080 and 4848). At the end of the Vagrant provisioning process, the .war file is built and deployed in Glassfish.

The implementation is pretty straightforward. One requirement in the area of JSON serialization was a bit tricky to implement. Remember that we said that event payload should include a list of dynamic properties (events of different types have different properties). Consider the following two examples for the JSON that we want to use. The first event has two dynamic properties (temperature and location). The second event has three dynamic properties (density, riskLevel and location). The important point is the flat structure of the "properties" object.

{% highlight json %}
{
  "timestamp" : "2015-01-12T05:22:07Z",
  "source" : "/event-sources/JI8928JFK",
  "type" : "/eventTypes/temperatureEventSchema",
  "properties" : {
    "temperature" : 22.8,
    "location" : "room 1"
  }
}

{
  "timestamp" : "2015-01-12T05:22:07Z",
  "source" : "/event-sources/LLKR88292IM",
  "type" : "/eventTypes/crowdDensityEventSchema",
  "properties" : {
    "density" : 72,
    "riskLevel" : "medium",
    "location" : "Stadium - main gate"
  }
}
{% endhighlight %}

To achieve this, we have used a special featured of the [Jackson](https://github.com/FasterXML/jackson) JSON serializer: the [@AnyGetter](http://wiki.fasterxml.com/JacksonFeatureAnyGetter) annotation (see [here](https://github.com/FasterXML/jackson-annotations/wiki/Jackson-Annotations) also). This means that we also had to configure our JAX-RS application so that it would use Jackson for JSON serialization, which is not the default behavior.

The first thing that we had to to was to add two dependencies in our `pom.xml` maven project file. Watch out for the `<groupId>`and `<version>` values: there are different artifacts published in different maven repositories and it can be a bit painful to get the right combination. Be also aware that there might be issues with other versions of Glassfish (we use 4.1).

{% highlight xml %}
<dependency>
  <groupId>org.glassfish.jersey.media</groupId>
  <artifactId>jersey-media-json-jackson</artifactId>
  <version>2.14</version>
  <scope>compile</scope>
</dependency>
<dependency>
  <groupId>com.fasterxml.jackson.core</groupId>
  <artifactId>jackson-annotations</artifactId>
  <version>2.3.2</version>
</dependency>
{% endhighlight %}

The next step was to configure Jersey (the JAX-RS implementation bundled with Glassfish) so that it would use Jackson for JSON serialization (see [documentation](https://jersey.java.net/documentation/latest/media.html#json.jackson)).

{% highlight java %}
@javax.ws.rs.ApplicationPath("api")
public class ApplicationConfig extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> resources = new java.util.HashSet<>();
    addRestResourceClasses(resources);

    /*
     * The following block is needed in order to use jackson as the JSON provider for
     * Jersey. It is also required to add jersey-media-json-jackson as a dependency for
     * this to work. Using jackson allows us to use the @JsonAnySetter annotation, so
     * that we can handle dynamic property names.
     */
    Class jsonProvider;
    try {
      jsonProvider = Class.forName("org.glassfish.jersey.jackson.JacksonFeature");
      resources.add(jsonProvider);
    } catch (ClassNotFoundException ex) {
      Logger.getLogger(ApplicationConfig.class.getName()).log(Level.SEVERE, "*** Problem while configuring JSON!", ex);
    }

    return resources;
  }

  /**
   */
  private void addRestResourceClasses(Set<Class<?>> resources) {
    resources.add(io.iflux.api.event.enpoints.EventResource.class);
    resources.add(io.iflux.api.event.enpoints.JacksonConfigurationProvider.class);
    resources.add(io.iflux.api.event.util.InMemoryManager.class);
  }

}
{% endhighlight %}

The next step was to configure the Jackson JSON serializer. This is done in a class annoted with `@Provider`. In the code, notice that this is also where we specify how dates should be serialized.

{% highlight java %}
@Provider
@Produces("application/json")
public class JacksonConfigurationProvider implements ContextResolver<ObjectMapper> {

  private ObjectMapper mapper = new ObjectMapper();

  public JacksonConfigurationProvider() {
    SerializationConfig serConfig = mapper.getSerializationConfig();
    DeserializationConfig deserializationConfig = mapper.getDeserializationConfig();
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  @Override
  public ObjectMapper getContext(Class<?> type) {
    return mapper;
  }

}
{% endhighlight %}


Thanks to the previous configuration, we were finally able to implement the Data Transfer Object used as a payload for the `/events` endpoint.

{% highlight java %}
public class Event {

  public class EventProperties {

    private final Map<String, Object> dynamicProperties = new HashMap<>();

    @JsonAnySetter
    public void addProperty(String name, Object value) {
      dynamicProperties.put(name, value);
    }

    @JsonAnyGetter
    public Map<String,Object> any() {
        return dynamicProperties;
    }

    public Object get(String name) {
      return dynamicProperties.get(name);
    }

  }

  private Date timestamp;

  private String source;

  private String type;

  @JsonProperty("properties")
  private EventProperties properties = new EventProperties();


  public Date getTimestamp() {
    return timestamp;
  }

  public String getSource() {
    return source;
  }

  public String getType() {
    return type;
  }

  @JsonIgnore
  public List<String> getPropertyNames() {
    return new ArrayList(properties.dynamicProperties.keySet());
  }

  public Object get(String name) {
    return properties.get(name);
  }

}
{% endhighlight %}

#### Node.js server implementation

We have also implemented the endpoint specification in Node.js and used the express framework for that purpose. The implementation is trivial, since the transformation of JSON payloads into Javascript objects is automatic:

{% highlight javascript %}
var express = require('express');
var router = express.Router();

router.logger = {
	history : [],
	push : function(event) {
		this.history.push(event);
	},
	getList : function() {
		return this.history;
	}
};

router.post('/', function(req, res) {
  console.log("ts: " + req.body.timestamp);
  var events = req.body;
  for (var i=0; i<events.length; i++) {
    router.logger.push(events[i]);
  }
  res.status(202).send();
});

router.get('/debug', function(req, res) {
	res.send(router.logger.getList());
});


module.exports = router;
{% endhighlight %}

#### Node.js client implementation

Finally, we have also implemented a Javascript client to test the API. Here again, the code is straightforward:

{% highlight javascript %}
var Client = require('node-rest-client').Client;
var moment = require('moment');

client = new Client();

var now = moment.utc();
var formatted = now.format('YYYY-MM-DDTHH:mm:ss.SSS') + 'Z';


var event = {
	timestamp: formatted,
  source: "anonymous event source",
  type: "undefined type",
	properties: {
		room: "living room",
		temperature: 22.3
	}
}

var events = [];
events.push(event);


var args = {
  data: events,
  headers:{"Content-Type": "application/json"}
};

client.post("http://localhost:3000/events/", args, function(data, response) {
	console.log(data);
});
{% endhighlight %}
