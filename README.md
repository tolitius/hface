# hface

Hazelcast cluster monitoring and debugging app

## What do you mean?

![hface braindump](https://github.com/tolitius/hface/blob/master/doc/hface-braindump.jpg?raw=true)

## The Gist

`hface` monitors a Hazelcast cluster in real time. It currently supports maps, multimaps and queues. 
Support for other distributed data structures is coming.

In order to monitor a remote Hazelcast cluster add an `8K` jar dependency to it:

[![Clojars Project](http://clojars.org/org.hface/hface-client/latest-version.svg)](http://clojars.org/org.hface/hface-client)

or

```xml
<dependency>
  <groupId>org.hface</groupId>
  <artifactId>hface-client</artifactId>
  <version>0.1.5</version>
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

## License

Copyright © 2018 tolitius

Distributed under the Eclipse Public License, the same as Clojure.
