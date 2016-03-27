# spring-boot + graphite demo

This demo sends spring-boot metrics to graphite via dropwizard metrics.

Graph Tree:
<img src="https://www.evernote.com/l/AALcGcOVOLFOlqzZ1wAwtv1EwcxsJd11n0I">

Dashboard:
<img src="https://www.evernote.com/l/AAJbfd-bwDhBeaOGiYiJsrpJ-Z8IJ3-CNvk">

## Graphite tips

### How do I install graphite on my mac?

There's a great tutorial.
https://gist.github.com/relaxdiego/7539911#prerequisites

### How do I send metrics to graphite server?

You can send a value via nc.

    PORT=2003
    SERVER=graphite.your.org
    echo "local.random.diceroll 4 `date +%s`" | nc -q0 ${SERVER} ${PORT}
