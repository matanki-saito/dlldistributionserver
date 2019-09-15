# Dll Distribution server
 
 # sequence
 
 ![img1](Resource/README.img1.png)
 
# component
 ![img2](Resource/system.png)
 
## infra
  - AWS: https://aws.amazon.com/jp/
    - CloudFront
    - EC2
    - RDS
    - Route53
    - S3
    - ECS+Secrets Manager+IAM
  - GitHub: https://github.com/
    - this repository
    - private repository
  - DockerHub: https://hub.docker.com/
    - gnagaoka/triela-app:latest (public)
    - gnagaoka/triela-private:latest (private)
  - circleci: https://circleci.com/
  - DotTK: http://www.dot.tk/en/index.html?lang=en
  - ZeroSSL: https://zerossl.com/
  - Discord: https://discordapp.com/

## component 
  - Java 11 later: https://openjdk.java.net/projects/jdk/11/
  - MySQL 8 later: https://www.mysql.com/jp/
  - cURL: https://curl.haxx.se/
  - Docker: https://www.docker.com/
  
## app
  - lombok: https://projectlombok.org/
  - gradle: https://gradle.org/
  - Spring boot 2 later: http://spring.io/projects/spring-boot
  - Spring security oauth2 autoconfig: https://docs.spring.io/spring-security-oauth2-boot/docs/current/reference/htmlsingle/
  - OkHttp3: http://square.github.io/okhttp/
  - retrofit: https://square.github.io/retrofit/
  - MyBatis + annotation: http://blog.mybatis.org/
  - thymeleaf: https://www.thymeleaf.org/index.html
  - caffeine: https://github.com/ben-manes/caffeine
  - swagger: https://swagger.io
  - logback
  - Slf4j
  - schemaspy

## IDE and doc
  - IntelliJ IDEA: https://www.jetbrains.com/idea/
  - swagger: https://swagger.io
  - markdown
  - PlantUML: http://plantuml.com/
 
## local dev

```
$ docker-compose down && docker-compose up -d
```

## documents
 - [Schemaspy](https://matanki-saito.github.io/dlldistributionserver/schemadoc/)
 - [Javadoc](https://matanki-saito.github.io/dlldistributionserver/javadoc/)
