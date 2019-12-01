# Docker deployment of a zookeeper bank app
The app can be found here: https://github.com/sergiosheypol/BankZookeeper


## Deployment 1
``docker-compose -f config1.yaml up``

At this point, Zookeeper is alive with a 3-quorum ensemble using 3 Docker containers using the IP addresses shown in docker-compose.
Furthermore, there are another 3 bank-containers running the App.

Assigned IPs:
- zoo1: exposed through localhost:2181
- zoo2: exposed through localhost:2182
- zoo3: exposed through localhost:2183
- bank1: exposed through localhost:81
- bank2: exposed through localhost:82
- bank3: exposed through localhost:83


## Deployment 2
``docker-compose -f config2.yaml up``

At this point, Zookeeper is alive with a 3-quorum ensemble using 3 Docker containers using the IP addresses shown in docker-compose.
Furthermore, there are another 3 bank-containers running the App.

Since it's using "link", reaching a container from another one can be done by simply using the name. For example: ping bank2

Assigned IPs:
- zoo1: 10.0.0.2
- zoo2: 10.0.0.3
- zoo3: 10.0.0.4
- bank1: 10.0.0.5
- bank2: 10.0.0.6
- bank3: 10.0.0.7

The app wrapped in the container is listening in port 80


## Launch user interface

Lunch one or more web clients by typing the following command line (Port Example: 8000, 8001, 8002). Please make sure you are in the "frontEnd" directory.

``python -m SimpleHTTPServer [port]``

At this point a web client to interact with the bank application can be foun at http://localhost:8000/



