# Zookeeper based bank app

To run the app, a zookeeper ensemble must be running within the specified IPs in RestAPI/RequestManager.java

Also, if you wish to add more bank instances, check RestAPI/RequestManager.java to specify the IPs of the rest of the instances.

## Parameters

- Param1: port to listen. Example: 80
- Param2: name of the host. The app has been implemented to run up to three instances: h1, h2 and h3

## Examples
 - java -jar BankApp.jar 81 h1
 - java -jar BankApp.jar 82 h2
 - java -jar BankApp.jar 83 h3

If any further instance is needed, RestAPI/RequestManager.java and ExploreZk.java need tweaking.

