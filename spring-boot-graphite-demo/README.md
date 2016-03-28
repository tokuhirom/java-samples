# spring-boot + graphite demo

This demo sends spring-boot metrics to graphite via dropwizard metrics.

Graph Tree:
<img src="https://www.evernote.com/shard/s2/sh/dc19c395-38b1-4e96-acd9-d70030b6fd44/c1cc6c25dd759f42/res/22b4aae2-7beb-4051-869d-7d0f264444d2/skitch.png">

Dashboard:
<img src="https://www.evernote.com/shard/s2/sh/5b7ddf9b-c038-4179-a386-898889b2ba49/f99f08277f8236f9/res/98e42f26-2d4c-4878-b6ce-7eed4b7ddddf/skitch.png">

## Graphite tips

### How do I install graphite on my mac?

There's a great tutorial.
https://gist.github.com/relaxdiego/7539911#prerequisites

### How do I send metrics to graphite server?

You can send a value via nc.

    PORT=2003
    SERVER=graphite.your.org
    echo "local.random.diceroll 4 `date +%s`" | nc -q0 ${SERVER} ${PORT}
