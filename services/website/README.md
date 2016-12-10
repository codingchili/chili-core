# website service 

Serves files from **website/** directory in the server root.

##### Featureset
- gzip support
- file caching
- hot reload of files
- :x: support for templating with Jade
- :x: support for 0-copy

##### Configuration in conf/service/website.json

```
{
  "identity" : {
    "node" : "website.node",
    "host" : "server.1"
  },
  "startPage" : "/index.html",
  "missingPage" : "/404.html",
  "gzip" : false
}
```
