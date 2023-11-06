FROM openjdk:11-jre-slim
LABEL MAINTAINER="sleefd@gmail.com"

USER root
WORKDIR  /opt/json2struct
ADD */opt/docker /opt/json2struct/
ENV JSON2STRUCT_HOME /opt/json2struct
VOLUME /opt/json2struct

expose 8080

ENTRYPOINT ["/opt/json2struct/bin/api"]
CMD []
