Project Loom Experiment using Spring Boot, Spring WebMVC, and Postgres
======================================================================

This repository contains an experiment that uses a Spring Boot application with [Virtual Threads](https://wiki.openjdk.java.net/display/loom/Main).

Involved components:

* Spring Framework 6.0 M5
* Spring Boot 3.0 M4
* Apache Tomcat 10.1.0 M17
* HikariCP 5.0.1 (Loom issue: https://github.com/brettwooldridge/HikariCP/issues/1463)
* PGJDBC 42.4.0 (PR that turns `synchronized` into Loom-friendly Locks: https://github.com/pgjdbc/pgjdbc/issues/1951)
 
This experiment evolves incrementally, find the previous state at https://github.com/mp911de/spring-boot-virtual-threads-experiment/tree/boot-2.4. 

You need Java 19 (EAP) with `--enable-preview` to run the example. 

Customization of a vanilla Spring Boot with Tomcat application:
                                                              
```java
@Bean
AsyncTaskExecutor applicationTaskExecutor() {
    // enable async servlet support
    ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
    return new TaskExecutorAdapter(executorService::execute);
}

@Bean
TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {

    return protocolHandler -> {
        protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
    };
}
```

After starting the application, run `GET http://localhpst:8080/where-am-i` to verify you're running on a virtual thread:

```
$ http :8080/where-am-i                                      
HTTP/1.1 200 
Connection: keep-alive
Content-Length: 51
Content-Type: text/plain;charset=UTF-8
Date: Wed, 27 Jul 2022 09:34:23 GMT
Keep-Alive: timeout=60

VirtualThread[#82]/runnable@ForkJoinPool-1-worker-1
```

License
-------

* [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
