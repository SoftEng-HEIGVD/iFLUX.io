---
layout: post
title:  "Docker Swarm"
author: Laurent Prevost
categories: docker
---

> Docker Swarm is native clustering for Docker. It turns a pool of Docker hosts into a single, virtual host.

During the work to make iFLUX deployable with Docker, we investigated around [Docker Swarm][docker-swarm]. For that, we used [CoreOS][coreos]
with [Vagrant][vagrant]. We used the CoreOS Vagrant [example][coreos-vagrant].

{% highlight bash %}
{% raw %}
$> git clone https://github.com/coreos/coreos-vagrant.git
{% endraw %}
{% endhighlight %}

To be able to test the clustering with Docker Swarm, we firstly modified the `Vagrantfile` present in the CoreOS repository we just cloned.
We set the number of instances to be equal to two. This allows us having two nodes to test the Docker Swarm mechanisms.

{% highlight bash %}
{% raw %}
# Defaults for config options defined in CONFIG
$num_instances = 2
{% endraw %}
{% endhighlight %}

By default on CoreOS, the Docker socket is not exposed. To be able to use Docker Swarm we can choose two different approaches:

1. Using `etcd` through Docker Swarm
2. Using Docker Swarm without using `etcd`

In this example, we chosen to test only Docker Swarm. Then we have chosen the first way to configure the cluster. Then, we faced
some troubles as CoreOS does not use Docker socket directly.

### Node 1

To enable the socket, we have to modify the `systemd` configuration on our two CoreOS nodes. We did the following modifications and commands:

{% highlight bash %}
{% raw %}
# Connecting to the first node
localhost$> vagrant ssh core-01

# Copy a default configuration file. Use sudo to perform the following commands.
core@core-01$> cp /usr/lib/systemd/system/docker.service /etc/systemd/system/
core@core-01$> vi /etc/systemd/system/docker.service
{% endraw %}
{% endhighlight %}

We placed the following configuration in the file `/etc/systemd/system/docker.service`. You can notice that we added `--label sotrage=ssd` on the node 1.
This value will be used by Docker Swarm later.

{% highlight bash %}
{% raw %}
[Unit]
Description=Docker Application Container Engine
Documentation=http://docs.docker.com
After=docker.socket early-docker.target network.target
Requires=docker.socket early-docker.target

[Service]
Environment=TMPDIR=/var/tmp
EnvironmentFile=-/run/flannel_docker_opts.env
MountFlags=slave
LimitNOFILE=1048576
LimitNPROC=1048576
ExecStart=/usr/lib/coreos/dockerd --daemon --host=fd:// $DOCKER_OPTS $DOCKER_OPT_BIP $DOCKER_OPT_MTU $DOCKER_OPT_IPMASQ --label storage=ssd

[Install]
WantedBy=multi-user.target
{% endraw %}
{% endhighlight %}

We also created the filest `/etc/systemd/system/docker-tcp.socket` with the following content:

{% highlight bash %}
{% raw %}
[Unit]
Description=Docker Socket for the API

[Socket]
ListenStream=2375
BindIPv6Only=both
Service=docker.service

[Install]
WantedBy=sockets.target
{% endraw %}
{% endhighlight %}

Once the setup is done, we can stop the Docker service and then start the new service and restart the Docker service.

{% highlight bash %}
{% raw %}
# Use sudo to perform the following commands
core@core-01$> systemctl stop docker
core@core-01$> systemctl enable docker-tcp.socket
core@core-01$> systemctl start docker-tcp.socket
core@core-01$> systemctl start docker
{% endraw %}
{% endhighlight %}

### Node 2

In the following file, the main difference is the label we added to the Docker start command. We added `--label storage=disk`

{% highlight bash %}
{% raw %}
[Unit]
Description=Docker Application Container Engine
Documentation=http://docs.docker.com
After=docker.socket early-docker.target network.target
Requires=docker.socket early-docker.target

[Service]
Environment=TMPDIR=/var/tmp
EnvironmentFile=-/run/flannel_docker_opts.env
MountFlags=slave
LimitNOFILE=1048576
LimitNPROC=1048576
ExecStart=/usr/lib/coreos/dockerd --daemon --host=fd:// $DOCKER_OPTS $DOCKER_OPT_BIP $DOCKER_OPT_MTU $DOCKER_OPT_IPMASQ --label storage=disk

[Install]
WantedBy=multi-user.target
{% endraw %}
{% endhighlight %}

We also created the filest `/etc/systemd/system/docker-tcp.socket` with the same content than Node 1.

And finally, we run the same commands to start the services than the Node 1.

### Intermediate result

At this stage, you should get the following result on Node 1:

{% highlight bash %}
{% raw %}
core@core-01$> docker info
Containers: 0
Images: 0
Storage Driver: overlay
 Backing Filesystem: extfs
Execution Driver: native-0.2
Kernel Version: 3.18.6
Operating System: CoreOS 598.0.0
CPUs: 1
Total Memory: 998 MiB
Name: core-01
ID: O7FZ:AE46:YRFU:5P4Z:BZRB:DUFZ:QGL6:MWZP:IULQ:YYAT:EULQ:MTA3
Labels:
 storage=ssd
{% endraw %}
{% endhighlight %}

and Node 2:

{% highlight bash %}
{% raw %}
core@core-02$> docker info
Containers: 0
Images: 0
Storage Driver: overlay
 Backing Filesystem: extfs
Execution Driver: native-0.2
Kernel Version: 3.18.6
Operating System: CoreOS 598.0.0
CPUs: 1
Total Memory: 998 MiB
Name: core-02
ID: PPXX:EJSO:77AZ:A5TO:KFUF:R42J:BWUP:O6TI:7MQR:XYQP:P5LJ:KQY2
Labels:
 storage=disk
{% endraw %}
{% endhighlight %}

## Docker Swarm Setup

From now, we can start to setup Docker Swarm on our two nodes. First, we can create the Docker Swarm Manager. You can do that
on one of the two nodes. We have chosen the Node 1.

### Cluster creation

{% highlight bash %}
{% raw %}
core@core-01$> docker run --rm swarm create
6856663cdefdec325839a4b7e1de38e8
{% endraw %}
{% endhighlight %}

**Remark**: Keep the unique identifier generated for later use.

### Setup the Swarm master

There is just a command to generate the Swarm manager. The command needs to know:

| name       | description |
|------------|-------------|
| swarm_port | The Swarm TCP port exposed to the host.
| cluster_id | The cluster identifier. The value we generated in the previous command where we created the cluster.

{% highlight bash %}
{% raw %}
core@core-01$> docker run -d -p <swarm_port>:2375 swarm manage token://<cluster_id>
{% endraw %}
{% endhighlight %}

### Create the Swarm agent on all nodes

It's quite simple to create the agents. We have just to run a command on each cluster node.

| name       | description |
|------------|-------------|
| node_ip    | The IP of the node where the container is deployed. It must be an accessible IP and not necessary public IP.
| cluster_id | The cluster identifier. The value we generated in the previous command where we created the cluster.

{% highlight bash %}
{% raw %}
# Node 1
core@core-01$> docker run -d swarm join --addr=<node_ip:2375> token://<cluster_id>

# Node 2
core@core-02$> docker run -d swarm join --addr=<node_ip:2375> token://<cluster_id>
{% endraw %}
{% endhighlight %}

To ensure that everything is working properly, we can run the following command:

| name       | description |
|------------|-------------|
| swarm_ip   | The IP of the host where the Swarm manager is deployed. In our case, the IP of Node 1.
| swarm_port | The Swarm TCP port exposed on the target host.

{% highlight bash %}
{% raw %}
core@core-01$> docker -H tcp://<swarm_ip:swarm_port> info
Containers: 3
Nodes: 2
 core-01: 172.17.8.101:2375
  └ Containers: 2
  └ Reserved CPUs: 0 / 1
  └ Reserved Memory: 0 B / 998 MiB
 core-02: 172.17.8.102:2375
  └ Containers: 1
  └ Reserved CPUs: 0 / 1
  └ Reserved Memory: 0 B / 998 MiB
{% endraw %}
{% endhighlight %}

We can run the following command to see the current containers deployed in the cluster. Notice that we do the command
from the Node 2 but we specify the IP of the Node 1.

{% highlight bash %}
{% raw %}
core@core-02$> docker -H tcp://<swarm_ip:swarm_port> ps
CONTAINER ID        IMAGE               COMMAND             CREATED             STATUS              PORTS               NAMES
{% endraw %}
{% endhighlight %}

There is nothing relevant at the moment. There is no data about the Swarm Manager and the Swarm Agents. They are not part of the
containers deployed in the cluster directly. They participate in the infra for the other container.

We can also list the nodes of our cluster as Swarm saw them:

{% highlight bash %}
{% raw %}
core@core-01$> docker run --rm swarm list token://388c00a60491681dfb098b1d9f91bbe7
172.17.8.102:2375
172.17.8.101:2375
{% endraw %}
{% endhighlight %}

## Running containers

Ok, everything is ready. We can deploy `MySQL` and `Nginx` on our cluster. For the example, we will use only standar images and
we will not do relevant stuff with the containers themselves. They will illustrate how easy it is to deploy stuff in the cluster.

First, we should come back on the `--label storage=disk|ssd` that we put in the `systemd` configuration earlier in this article.
Setting up those labels on the different nodes of our CoreOS cluster have a meanings for Swarm. With that labels, we are now able,
when we deploy a new container, to specify preferences on where we want to deploy our container.

### MySQL deployment on Node 1

{% highlight bash %}
{% raw %}
core@core-02$> docker -H tcp://<swarm_ip>:<swarm_port> run -d -P -e constraint:storage==ssd -e MYSQL_ROOT_PASSWORD=toto --name db mysql
{% endraw %}
{% endhighlight %}

The interresting aspect of the command is the `-e constraint:storage==ssd`. With this argument, we specify to Docker Swarm that we want
to deploy our `MySQL` image on a node that have `ssd storage`. Remember that we have the Node 1 with the label specifying `ssd` as the
storage.

{% highlight bash %}
{% raw %}
core@core-02$> docker ps -a
CONTAINER ID        IMAGE               COMMAND      CREATED             STATUS              PORTS               NAMES
33b64da531ae        swarm:latest        "/swarm j"   4 days ago          Up 4 days           2375/tcp            high_leakey
{% endraw %}
{% endhighlight %}

We wanted to list all the containers running on the Node 2 but we cannot see any `MySQL` on this node. We just have the Swarm agent
running on this node. In fact, that's the expected behavior. When we run the same command on Node 1, we can see:

{% highlight bash %}
{% raw %}
core@core-01$> docker ps -a
CONTAINER ID        IMAGE               COMMAND      CREATED              STATUS              PORTS                     NAMES
cceaabe37fe3        mysql:latest        "/entrypo"   About a minute ago   Up About a minute   0.0.0.0:49157->3306/tcp   db
b23dd312fdde        swarm:latest        "/swarm j"   4 days ago           Up 4 days           2375/tcp                  lonely_franklin
b7dcad4e898b        swarm:latest        "/swarm m"   4 days ago           Up 4 days           0.0.0.0:2376->2375/tcp    jolly_cori
{% endraw %}
{% endhighlight %}

Yeah ! We have our `MySQL` image up and running in the Node 1. In addition, we see the Swarm Manager and Agent present on this node.
We can also run the following command on any node to see the images running in the cluster.

{% highlight bash %}
{% raw %}
core@core-02$> docker -H tcp://<swarm_ip>:<swarm_port> ps
CONTAINER ID        IMAGE               COMMAND      CREATED             STATUS              PORTS                          NAMES
cceaabe37fe3        mysql:latest        "/entrypo"   4 minutes ago       Up 3 minutes        172.17.8.101:49157->3306/tcp   core-01/db
{% endraw %}
{% endhighlight %}

This time, the command give us more info. We see that we have a `MySQL` image running in the cluster and then we can also
see that the image is running on Node 1 (`core-01/db`).

Let's try to start a second `MySQL` with the same constraint (just the name of the container is changed).

{% highlight bash %}
{% raw %}
core@core-02$> docker -H tcp://<swarm_ip>:<swarm_port> run -d -P -e constraint:storage==ssd -e MYSQL_ROOT_PASSWORD=toto --name db-02 mysql

core@core-02$> docker -H tcp://<swarm_ip>:<swarm_port> ps
CONTAINER ID        IMAGE               COMMAND      CREATED             STATUS              PORTS                          NAMES
03b4c941cf84        mysql:latest        "/entrypo"   4 seconds ago       Up 2 seconds        172.17.8.101:49158->3306/tcp   core-01/db-02
cceaabe37fe3        mysql:latest        "/entrypo"   6 minutes ago       Up 6 minutes        172.17.8.101:49157->3306/tcp   core-01/db
{% endraw %}
{% endhighlight %}

And on Node 1, we can see:

{% highlight bash %}
{% raw %}
core@core-01$> docker ps -a
CONTAINER ID        IMAGE               COMMAND      CREATED             STATUS              PORTS                     NAMES
03b4c941cf84        mysql:latest        "/entrypo"   4 seconds ago       Up 3 seconds        0.0.0.0:49160->3306/tcp   db-02
cceaabe37fe3        mysql:latest        "/entrypo"   10 minutes ago      Up 10 minutes       0.0.0.0:49157->3306/tcp   db
b23dd312fdde        swarm:latest        "/swarm j"   4 days ago          Up 4 days           2375/tcp                  lonely_franklin
b7dcad4e898b        swarm:latest        "/swarm m"   4 days ago          Up 4 days           0.0.0.0:2376->2375/tcp    jolly_cori
{% endraw %}
{% endhighlight %}

Ok, we have a second container deployed on Node 1. Great, we can now go for `Nginx`.

### Nginx deployment on Node 2

For `Nginx`, we do not require the same kind of resource. We can easily run it on a standard disk. Then, we will start two
containers on Node 2. This time, we specify the constraint to be `disk`.

{% highlight bash %}
{% raw %}
core@core-01$> docker -H tcp://<swarm_ip>:<swarm_port> run -d -P -e constraint:storage==disk --name f1 nginx
core@core-01$> docker -H tcp://<swarm_ip>:<swarm_port> run -d -P -e constraint:storage==disk --name f2 nginx
{% endraw %}
{% endhighlight %}

Now we can check like we did for `MySQL`.


{% highlight bash %}
{% raw %}
core@core-01$> docker -H tcp://<swarm_ip>:<swarm_port> ps
CONTAINER ID        IMAGE               COMMAND      CREATED             STATUS              PORTS                          NAMES
b75cd8f9be4a        nginx:1             "nginx -g"   2 minutes ago       Up 2 minutes        172.17.8.102:49156->80/tcp     core-02/f2
ab1b0065c64c        nginx:1             "nginx -g"   2 minutes ago       Up 2 minutes        172.17.8.102:49154->80/tcp     core-02/f1
03b4c941cf84        mysql:latest        "/entrypo"   16 minutes ago      Up 16 minutes       172.17.8.101:49160->3306/tcp   core-01/db-02
cceaabe37fe3        mysql:latest        "/entrypo"   26 minutes ago      Up 26 minutes       172.17.8.101:49157->3306/tcp   core-01/db

core@core-02$> docker ps
CONTAINER ID        IMAGE               COMMAND      CREATED             STATUS              PORTS                   NAMES
b75cd8f9be4a        nginx:1             "nginx -g"   3 seconds ago       Up 2 seconds        0.0.0.0:49156->80/tcp   f2
ab1b0065c64c        nginx:1             "nginx -g"   21 seconds ago      Up 20 seconds       0.0.0.0:49154->80/tcp   f1
33b64da531ae        swarm:latest        "/swarm j"   4 days ago          Up 43 seconds       2375/tcp                high_leakey
{% endraw %}
{% endhighlight %}

## Conclusion

With this simple example, we demonstrated how to use Docker Swarm. It is a great tool that you can use with `etcd`, `consul` and many
other solutions to manage a cluster.

[coreos]: http://www.coreos.org
[coreos-vagrant]: https://github.com/coreos/coreos-vagrant.git
[docker-swarm]: https://github.com/docker/swarm
[vagrant]: http://www.vagrantup.com

