# Mockability Clients - Java

These clients are strictly unnecessary, because the Mockability server is easily accessible via its REST interface.
However, these clients encapsulate the REST interface and allow you to make method calls instead of HTTP requests,
and to represent HTTP requests and responses as Java objects instead of JSON strings.

There are two clients now available, and another one to become available very soon.

## Common Functionality
All the Java clients have the functionality given below, although they use different data structures to represent
requests and responses.

### Construction
Create a client object by passing the base URL of the Mockability server to its constructor.

### `response = clear(method, uri)`
Remove everything the Mockability server is remembering about requests from your IP address to the specified `uri`
with the specified `method`.  If the response is not successful, its body may be of interest.

### `response = prepare(method, uri, expectedResponse)`
Instruct the Mockability server to respond with `expectedResponse` when it sees a request from your IP address to
the specified `uri` with the specified `method`.  Call this as many times as you like, and the Mockability server
will remember the `expectedResponse`s and send them back in the order they were received.

### `requests = report(method, uri)`
Instruct the Mockability server to send you all the requests it remembers from your IP address to the specified `uri`
with the specified `method`.  The Mockability server has no persistence, so its memory will be empty when you
start it up; also, you can use `clear` to make it forget any recorded requests that it might be remembering.  Calling
`report()` does *not* clear its memory.

### Other HTTP Requests
The Mockability server will respond only as directed to HTTP requests other than those to the `/mockability/...` URLs
that are targeted by `clear()`, `prepare()`, and `report()` calls.  If a request is received for which the Mockability
server has not been prepared, its response will have a 499 status code and a `text/plain` body telling what it *was*
prepared for.

## `HttpClientMockabilityClient` - Apache/Android `HttpClient`
If you're using Apache's HttpClient in the code you're testing anyway (for example, native Android code), and you're
familiar with the library and want to keep using it, you should access the Mockability server with an object of type
`HttpClientMockabilityClient`, which will allow you to represent HTTP requests as `HttpRequestBase`s and HTTP responses
as `HttpResponse` objects.

## `HttpServletMockabilityClient` - Sun Servlets
If you like `HttpServletRequest`s and `HttpServletResponse`s, you can access the Mockability server with an object of
type `HttpServletMockabilityClient`.  The particular request and response objects used will be the impressive static
mocks provided by Spring (`MockHttpServletRequest` and `MockHttpServletResponse`), which are very easy to populate and
examine.

## Coming Soon - `SimpleMockabilityClient`
Internally, the `MockabilityClient` uses simple, dumb data structures that do almost nothing for you but which are
very easy to understand.  If you'd like to operate on a level lower than that of the popular HTTP libraries but higher
than that of raw REST HTTP, you might look into using a `SimpleMockabilityClient` to interact with the Mockability
server.

### Custom Client Classes
There are many, many representations of HTTP requests and responses available in the Java world.  If we didn't hit
your favorites, and you'd like to use a `MockabilityClient` that understands them, all you need to do is write a
class that implements `LibraryAdapter`.  This class will act as a translator between your favorite request/response
classes and the simple ones that `MockabilityClient` uses directly.  Once you've got your class written, just
create a raw `MockabilityClient` object with an instance of your class along with the target base URL, and you're
home free.

If you think it's a little sloppy to have to provide an instance of your class on construction, and you're unhappy
that your client objects are of class `MockabilityClient` instead of something with a more exotic and
intention-revealing name, just take a look at the `HttpClientMockabilityClient` or `HttpServletMockabilityClient`
source code and see if something occurs to you.
