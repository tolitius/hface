package org.hface;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;

import java.io.Serializable;
import java.util.concurrent.Callable;

public class InstanceStatsTask
    implements Callable<Object>, Serializable, HazelcastInstanceAware {

  private transient HazelcastInstance hazelcastInstance;

  private final static IFn gatherInstanceStats;

  static {
    IFn require = Clojure.var( "clojure.core", "require" );
    require.invoke( Clojure.read( "hface.hz" ) );
    gatherInstanceStats = Clojure.var( "hface.hz", "instance-stats" );
  }

  public void setHazelcastInstance( HazelcastInstance hazelcastInstance ) {
    this.hazelcastInstance = hazelcastInstance;
  }

  public Object call() throws Exception {

    return gatherInstanceStats.invoke( hazelcastInstance );
  }


//  public static void main( String[] args ) throws Exception {
//
//    HazelcastInstance hz = Hazelcast.newHazelcastInstance();
//
//    InstanceStatsTask ist = new InstanceStatsTask();
//    ist.setHazelcastInstance( hz );
//
//    System.out.println( ist.call() );
//  }
}
