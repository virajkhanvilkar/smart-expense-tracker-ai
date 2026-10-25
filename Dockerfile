FROM tomcat:11-jdk21-temurin

RUN rm -rf /usr/local/tomcat/webapps/*

COPY SmartExpenseTracker.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

CMD ["catalina.sh", "run"]