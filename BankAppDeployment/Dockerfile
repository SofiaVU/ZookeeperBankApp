FROM openjdk

ADD zookeeper.tar.gz /
WORKDIR /zookeeper-3.4.14

COPY docker-entrypoint.sh /

RUN cd /
RUN chmod +x /docker-entrypoint.sh
