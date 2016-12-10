# website service 

Serves files from **website/** directory in the server root.

sample configuration 

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
