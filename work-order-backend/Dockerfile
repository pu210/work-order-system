# ---- Stage 1：build（用 Maven + JDK21 編出 WAR）----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# 先只複製 pom.xml 抓依賴，善用 Docker layer cache：
# 之後只改 Java 原始碼、沒改 pom.xml 時，這一層可以直接重用快取，不用重抓一次全部依賴
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src src
RUN mvn clean package -DskipTests -B

# ---- Stage 2：runtime（丟進 Tomcat 跑）----
# pom.xml 是 packaging=war 且 spring-boot-starter-tomcat 是 provided，
# 代表設計上就是打包成 WAR、部署進外部 Tomcat 容器，不是用內建嵌入式伺服器直接 java -jar
FROM tomcat:10.1-jdk21
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/work-order-backend-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]
