# hface

Hazelcast cluster monitoring and debugging app

## What do you mean?

![hface braindump](https://github.com/tolitius/hface/blob/master/doc/hface-braindump.jpg?raw=true)

## The Gist

`hface` monitors a Hazelcast cluster in real time. It currently supports maps, multimaps and queues.
Support for other distributed data structures is coming.

In order to monitor a remote Hazelcast cluster add an `8K` jar dependency to it:

```clojure
[org.hface/hface-client "0.1.6"]
```

or

```xml
<dependency>
  <groupId>org.hface</groupId>
  <artifactId>hface-client</artifactId>
  <version>0.1.6</version>
</dependency>
```

it will be collecting the stats from _all_ the nodes and will be sending these stats to `hface` for aggregation and visual pleasure. Here is [an example](https://github.com/tolitius/hface-server) of a simple Hazelcast server node with an hface client dependency.

## Visual

This is what hface dash currently looks like as it monitors the cluster:

![hface dash](https://github.com/tolitius/hface/blob/master/doc/hface-dash.png?raw=true)

### ASCII

All the stats are also available in JSON via `/stats`:

<img alt="Hazelcast cluster stats" width="35%" src="https://raw.githubusercontent.com/tolitius/hface/master/doc/stats.json.png?raw=true"/>

## Run it

### hface config

hface dashboard relies on a small configuraion file that can be pointed to by `-Dconf=path-to-config`. Here is a sample config:

```clojure
{:collector {:refresh-interval 4}                       ;; refresh cluster stats every 4 seconds
 :hz-client {:hosts ["127.0.0.1" "127.0.0.2"]           ;; hazelcast cluster hosts/ips
             :retry-ms 5000                             ;; retry to reconnect in 5 seconds
             :retry-max 720000                          ;; 720000 * 5000 = one hour
             :group-name "dev"                          ;; creds to the cluster (dev/dev-pass are hz defaults)
             :group-password "dev-pass"}}
```
### from releases

download the hface release from [releases](https://github.com/tolitius/hface/releases) i.e. let's say the version is `0.1.0`:

```
$ wget https://github.com/tolitius/hface/releases/download/v0.1.0/hface-dash-0.1.0.jar
```

run it:

```
$ java -jar -Dconf=/opt/app/hface/hface.conf hface-dash-0.1.0.jar
```

then, for joy, go to [http://localhost:3000/](http://localhost:3000/)

###### _`/opt/app/hface/hface.conf` is a sample path to the config file, replace it with your path_

### from sources


#### get ready

you would need [lein](https://leiningen.org) (if you come from Java think maven) to compile and build a single runnable jar, a.k.a. an uberjar.

lein is straighforward to install: just [download the script](https://leiningen.org/#install) and run it

#### build and run

* clone the repo (e.g. `git clone https://github.com/tolitius/hface.git`)
* `cd hface/dash`
* `lein ring uberjar`
* `java -jar -Dconf=/path-to/hface.conf target/hface-dash.jar`

then, for joy, go to [http://localhost:3000/](http://localhost:3000/)

##### lein version

starting from 2.9.0+ lein brings newer deps that break backwards compatibility as discussed in [this](https://github.com/oakes/Nightlight/issues/53) and [this](https://github.com/tolitius/hface/issues/11) issues. If you experience a problem building hface from sources:

```
$ lein downgrade 2.8.3
$ lein do clean, ring uberjar
```
which will work, and then:
```
$ lein upgrade
```
to bring you back to the latest `lein` version.

## License

Copyright © 2019 tolitius

Distributed under the Eclipse Public License, the same as Clojure.
