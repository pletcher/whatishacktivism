FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/whatishacktivism.jar /whatishacktivism/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/whatishacktivism/app.jar"]
