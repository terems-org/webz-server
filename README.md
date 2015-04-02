# WebZ Server v0.9 beta ([Pedesis](https://www.pinterest.com/terems_org/pedesis-from-ancient-greek-a-leaping/))

[**WebZ Server**](server-jar.md) is the core part of so called WebZ Platform -
a Git-based web hosting platform. (The page you are looking at right now is being served to you by WebZ Platform "directy" from
[**this**](https://github.com/terems-org/webz-server#webz-server-v09-beta-pedesis) GitHub repo).  
WebZ Server is an open source project available under
[**GNU Affero GPL v3.0**](http://www.gnu.org/licenses/agpl-3.0) - it is a web server that supports
[**Markdown**](http://daringfireball.net/projects/markdown/).
It's written in Java and is designed to serve web pages from various local and remote file sources as well as from so called
file source "hybrids". It has an API that allows custom server-side
[**WebZ Filters**](https://github.com/terems-org/webz-api/blob/master/src/main/java/org/terems/webz/WebzFilter.java)
to be implemented in Java.  
This API is called [**WebZ API**](https://github.com/terems-org/webz-api) and is available under
[**Apache License v2.0**](http://www.apache.org/licenses/LICENSE-2.0). WebZ Filter injection mechanism itself is not dynamic yet - currently all WebZ Filters need to be packaged together with the Server, but later it will become possible to load them dynamically as part of WebZ site deployment.

> ***NOTE:*** *Some time later a* ***contributor license agreement*** *will be applied to this particular repo.*
> [***This article***](https://julien.ponge.org/blog/in-defense-of-contributor-license-agreements/)
> *explains why it is a good idea to consider using contributor license agreements for certain open source projects.*
