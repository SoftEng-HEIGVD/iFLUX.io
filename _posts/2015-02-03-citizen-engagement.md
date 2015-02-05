---
layout: post
title:  "iFLUX for citizen engagement"
author: Olivier Liechti
categories: use-case
---

The goal of iFLUX is to enable the **rapid development of applications** that integrate **heterogenous sources** of information. While we are doing a lot of work on the IoT/WoT front, by integrating all sorts of physical sensors and actuators, we are also interested in software-generated event streams (i.e. sensors that capture activity in a software system) and **human-generated event streams**.

In this article, we look at the use case of applications that foster **citizen engagement**, by making it possible and easy for people to raise issues and give feedback to local authorities. To illustrate the point, think of a mobile app that city inhabitants could use to report a **broken streetlight**, a **dangerous crossroad** or a **critical situation**. In iFLUX terms, **citizen become event sources** (the mobile app is a user agent that provides an interface to the system). Every issue raised by a citizen becomes an event, that can trigger actions based on configured rules.

As a matter of fact, there are quite a few initiatives for deploying this type of service. The [Open 311 standard]() as even been specified to address the requirements of location-based collaborative issue tracking. Various software platforms, such as [FixMyStreet](http://fixmystreet.org)
and [Shareabouts](http://openplans.org/shareabouts/) have been implemented and deployed in a number of cities across the world (see the two screenshots below).

{:.image.fit}
![FixMyStreet]({{ baseurl }}/images/screenshots/fixmystreet.png)

{:.image.fit}
![Shareabouts]({{ baseurl }}/images/screenshots/openplans.png)

Many things could be done to illustrate the relationship between iFLUX and citizen engagement platforms. One idea would be to modify one of the existing platforms (there are GitHub repos both for the [FixMyStreet](https://github.com/mysociety/fixmystreet) and the [Shareabouts](https://github.com/openplans/shareabouts) platforms) and to send iFLUX events whenever an issue is reported or updated. It would then be possible to define an iFLUX rule to trigger various actions, such as sending a notification in a Slack channel or updating some kind of metric. Another idea would be to consider the platform as an iFLUX action target. In this model, the idea would be to combine human-generated issues with machine-generated issues (sensors could report issues in the system).

**What we have decided to do as a first step is to design and implement a very basic collaborative location-based issue tracking system, which we then use primarily as an  event source**. One reason for making this choice was to create a bridge between the iFLUX project and two courses that we are teaching at the [University of Applied Sciences Western Switzerland](http://www.heig-vd.ch).

### Building a prototype with students

#### Context: Mobile Web Services course

Since a few years, we teach a bachelor level course entitled Mobile Web Services. It consists of two different parts:

* In the first part, we introduce the concepts of RESTful architectures and look at the **design of REST APIs**. We then focus on the **implementation** of the specified REST API, until now on top of the Java EE platform. Students get exposure to standard APIs, including JAX-RS, EJBs and JPA.

* In the second part, we study how **mobile apps can use the REST API**. The course starts with a practical introduction to mobile development. In the past, we have mostly used android for that purpose. We show students how to use platform APIs to access device features (location, camera, etc.).

Until last year, the course was taught as a two-weeks block course. This gave enough time for the students to define and implement a small project, which was providing a connection between the back-end and front-end components. We like for students to come up with their own ideas and to define the functional specs of their projects. So, in general, we provide them with a common theme and ask them to make a proposal that fits within this context:

* one year, we have worked on **gamification**. Students first had to design and implement a REST API for a generic gamification engine, then to implement a mobile app that would use this API in the domain of their choice (education, health and fitness, etc.).

* Another year, the theme was **photo sharing**. Students first had to implement a REST API that would allow the upload, browsing and rating of photographs. They then had to implement a mobile app that would either focus on the content creation process or the content exploration process.

* Last year, we have worked on **IoT** and **WoT**. Students had to build a platform that would collect streams of observations (from sensors), apply simple processing and compute higher-level facts. For instance, by processing a stream of temperature observations, the platform would compute facts such as *the coldest room in the building is room A3* or *the average temperature in the kitchen over the last 10 days is 21.3 degrees*. From this idea, one group had the idea to build a service that would process various data streams to compute the cost of owning and driving a car. The students then developed a car sharing mobile app that would let users know how they should spit travel costs.

#### Plans for the Upcoming Edition

We are now a couple of weeks away from the 2015 edition of the course. This year, it is actually split in two courses. We will start with the Web Services course and a few weeks later continue with the Mobile Application Development course. As hinted before, we will use iFLUX to provide some context for the course and ask students to design and implement a citizen engagement platform.

This year, we will broaden the scope of the technologies presented to the students. As usual, we will give an introduction to the Java EE stack with a particular focus on the JAX-RS API. Compared to previous editions, where we were using a full-fledged application server (Glassfish), we have made the choice to use [Spring Boot](http://projects.spring.io/spring-boot/) this year. One benefit of this approach is that it makes it easy for the students to deploy their apps in a public cloud environment (such as [heroku](http://docs.spring.io/spring-boot/docs/current/reference/html/cloud-deployment-heroku.html)). Also, we have decided to give an introduction to the Node.js ecosystem and to show how frameworks and tools such as [Express.js](http://expressjs.com/), [mongoose](http://mongoosejs.com/), [yeoman](http://yeoman.io) and [Grunt.js](http://gruntjs.com/) make the development of REST APIs a breeze. That is a lot of material to cover, so we will be spending less time on persistence-related issues (in the past, we were spending a fair amount of time on JPA). Here is the preliminary schedule for the course:

##### Day 1 (morning) : Introduction to RESTful APIs

* **Introduction** (08:30 - 09:00)
  * Objectives of the course
  * Theme for the project
  * Guidelines

* **Introduction to REST APIs** (09:00 - 10:00)
  * Big Web Services vs REST
  * Core REST concepts
  * Exercise: use a REST API with Postman
  * Exercise: write a REST client in Node.js

* **Designing and documenting a REST API** (10:30 - 12:00)
  * REST patterns (container-item, pagination, filter, sort)
  * Introduction to [RAML](http://raml.org/) and the [apidoc-seed](https://github.com/lotaris/apidoc-seed/) tool
  * Exercise: clone repo and build api doc, minor customization
  * Exercise: document one resource
    
##### Day 1 (afternoon) : Design and documentation of a REST API for the project

* **Analyze the domain model**
  * Identify the resources (13:00 - 13:30)
  * Sketch the URLs, defines which HTTP verbs should supported (13:30 - 14:00)
  * Decide how to handle references, pagination, filtering and sorting (14:00 - 14:30)

* **Specify and document the REST API in RAML**
  * Write the API documentation and deploy it on heroku (15:00 - 16:30)


##### Day 2 (morning) : Implementing REST APIs with Java EE

* **Introduction to Java EE** (08:30 - 09:00)
  * Overview of the platform
  * The role of the containers (web container, EJB container)
  * Key specifications: servlets, EJB, JPA and JAX-RS

* **Introduction to Spring Boot** (09:00 - 10:00)
  * Spring Framework vs Spring Boot
  * Spring Framework vs Java EE
  * A first tutorial with Spring Boot
  
* **Implement a REST API with Spring Boot, JAX-RS and MongoDB** (10:30 - 12:00)
  * Replace Spring MVC with JAX-RS and using Jackson
  * Use the provided skeleton as a starting point
  * Understand the role of maven
  * Implement a first resource of the project
  * Test the resource with [Postman](https://chrome.google.com/webstore/detail/postman-rest-client/fdmmgilgnpjigdojojpjoooidkmcomcm?hl=en)
  * Deploy the result on heroku
  
  
##### Day 2 (afternoon) : (Partial) implementation of the project API in Java

* Implement (some of) the resources as previously specified in RAML (13:00 - 16:30)
* Implement pagination, filtering and sorting for at least one resource (13:00 - 16:30)
* Deploy on heroku (13:00 - 16:30)
* Test the resources with Postman


##### Day 3 (morning) : Implementing REST APIs with Node.js

* **Introduction to Node.js** (08:30 - 09:00)
  * Overview of Node.js ecosystem
  * How to use npm
  * Asynchronous programming, the event loop and callbacks
  * A first example

* **Testing a REST API with Node.js** (09:00 - 09:30)
  * The [node-rest-client](https://www.npmjs.com/package/node-rest-client) module
  * Using the module to write a simple test client
  
* **The role of Express.js, mongoose, yeoman and Grunt.js** (09:30 - 10:00)
  * Continuous integration and build pipeline with Grunt.js
  * Scaffolding with yo
  * REST API with Express.js
  * Persistence with mongoose

* **First steps with the Node.js stack** (10:30 - 12:00)  
  * Scaffold the project with the [express](https://github.com/petecoop/generator-express) yeoman generator.
  * Implement a first RESTful endpoint
  * Understand how to use and configure Express.js
  * Understand how to use and configure Mongoose
  * Write a test client in Node.js to test the API (Java implementation previously deployed on heroku)
  * Deploy on heroku

##### Day 3 (afternoon) : (Partial) implementation of the project API in Javascript

* Implement (some of) the resources as previously specified in RAML (13:00 - 16:30)
* Implement pagination, filtering and sorting for at least one resource (13:00 - 16:30)
* Deploy on heroku (13:00 - 16:30)
* Test the resources with a Node.js test client


##### Day 4 (morning) : Finalize the implementation either in Java or Javascript

* Each group selects one the two technology platforms
* Each group completes the implementation, including the Node.js test client


##### Day 4 (afternoon) : Documentation, presentations and demonstrations

* Customize the apidoc-seed project to add information about the project (to make it a [landing page](http://www.impactbnd.com/blog/how-to-write-persuasive-landing-page-content))
* Present the results and make a demo.
* Bonus: implement a (simple) Web UI that provides a user interface for the REST API (for instance to let users create issues, for users to visualize issues on a map, for users to take actions on issues, etc.)




#### Functional scope of the API

Given the time constraints, the REST API should be simple. We are looking at something like 4 or 5 resources. The following features give an idea of the functional scope that we have in mind:

* Both **citizen** and **staff members** are **users** of the service. A user may have one or more of these two **roles**.
 
* Staff members can define **issue types**, which are used to categorize issues. Examples of issue types include *broken streetlight*, *dangerous crossroad* and *graffiti*. An issue type is defined by a short name and a description.

* All users can create **issues** to report problems and incidents. Every issue is initially defined by an author, an issue type, by a description and by geographic coordinates. Every issue also has a status (created, acknowledged, assigned, in_progress, solved, rejected). When an issue is assigned, it is associated to a staff member who is responsible for working on it.

* All users can add **comments** to issues. A comment is defined by its author, by a date and by textual content.

* All users can also **tag** issues with the keywords of their choices.

* Staff members can take **actions** on issues. When an action is taken on an issue, its status may change (for instance, if the user takes the action of *solving an issue*, its status is set to solved). 

* **Adding a comment** on an issue is one particular type of action (in other words, comments are not created directly).

* Users want to be able to **query** the information and in particular to:
	* Get the list of issues raised by a particular user.
	* Get the list of issues of a certain type.
	* Get the list of issues in a particular region.
	* Get the list of issues solved between two dates.
	* Get the list of issues created between two dates that are still unresolved.
	* Get the history of an issue (list of actions taken on the issue).
	* Get the list of users who have created most issues.
	* Get the list of users who have solved most issues.
	* Get the list of users who have the least assigned issues not yet solved or rejected.


#### Next steps

In an upcoming blog post, we will provide access to the course material. We will also report on what students have been able to achieve. Last but not least, we will show how the developed services can be easily integrated with the growing collection of iFLUX event sources.






