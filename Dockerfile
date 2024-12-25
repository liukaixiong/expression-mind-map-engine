FROM feifan-cont-registry-vpc.cn-shanghai.cr.aliyuncs.com/hub/openjdk:8-bundle-230315

COPY ./expression-mind-map-server/target/expression-mind-map-server.jar /app.jar
ENV LANG C.UTF-8
ENV TZ=Asia/Shanghai
ENV JAVA_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseContainerSupport -XX:-UseAdaptiveSizePolicy -XX:MaxRAMPercentage=75.00 -XX:InitialRAMPercentage=75.00 -XX:MinRAMPercentage=75.00 -XX:MetaspaceSize=512M -XX:+UseConcMarkSweepGC -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 -XX:+ExplicitGCInvokesConcurrentAndUnloadsClasses -XX:+CMSClassUnloadingEnabled -XX:+ParallelRefProcEnabled -XX:+CMSScavengeBeforeRemark -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/root/ -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintHeapAtGC -XX:+PrintGCApplicationStoppedTime -XX:ErrorFile=/root/hs_err_pid%p.log -Xloggc:/root/gc%t.log -Djava.security.egd=file:/dev/./urandom -Djava.awt.headless=true -Dfile.encoding=UTF-8"
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /

EXPOSE 8090

CMD java $IAST_OPTS $JACOCO_OPTS -server -Djava.security.egd=file:/dev/./urandom $JAVA_OPTS -jar app.jar
