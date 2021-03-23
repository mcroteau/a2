![](http://104.156.252.167/aspen/a2.png)

a2 is a throwback using Sun's HttpServer & HttpExchange
while leveraging dependency injection. It is a full framework 
built to give tribute to Sun. All requests are mapped to a 
DispatcherHandler which dispatches the incoming request to mapped
uri, just like Spring.

Nothing beats the Servlet spec + Tomcat, but if you want something
quick to setup and don't plan heavy lifting, this framework 
might be good for you.

Take a look at `src/main/spike` as an example Todo app with 
full CRUD operations.
  

To run simply execute the following command:

`./runme.sh`



### M1/ Todos
add camel casing to underscore
check if column exists
remove first on context http dispatcher