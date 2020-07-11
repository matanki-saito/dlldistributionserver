FROM openjdk:14
VOLUME /tmp
ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
RUN ["chmod", "+x", "app/docker-entrypoint.sh"]
ENTRYPOINT ["app/docker-entrypoint.sh"]
