# servlet_async_graceful_stop

This package describes Servlet 3.0's async and graceful stop.

 * Jetty needs custom handler to support async + graceful stop
 * undertow needs ...
  * io.undertow.Undertow shutdown connections immediately

## tomcat

https://github.com/spring-projects/spring-boot/issues/4657
This code doesn't work. because it shutdown threads.
