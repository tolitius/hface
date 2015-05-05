#hface

look your Hazelcast cluster in the face!

## What do you mean?

![hface braindump](https://github.com/tolitius/hface/blob/master/doc/hface-braindump.jpg?raw=true)

## Gist

Until the full docs arrive here is the gist.

hface will monitor a distributed Hazelcast cluster in real time. It currently supports maps, multimaps and queues. 
Support for other distributed data structures is coming.

In order to monitor a remote Hazelcast cluster, a 10Kb dependency (soon in maven central) should be added:
```shell
$ ll -h target/hface-client.jar
-rw-r--r--  1 tolitius  staff   9.9K May  4 23:11 target/hface-client.jar
```

it will be collecting the stats from all the nodes and will be sending them to hface for a visual pleasure.

## Visual

Initial thinking:

![hface dash](https://github.com/tolitius/hface/blob/master/doc/hface-dash.png?raw=true)

## License

Copyright © 2015 tolitius

Distributed under the Eclipse Public License, the same as Clojure.
