FROM alpine:3.2
MAINTAINER Black Duck Hub Team

ENV JAVA_VERSION_MAJOR=7 \
    JAVA_VERSION_MINOR=80 \
    JAVA_VERSION_BUILD=15 \
    JAVA_HOME=/opt/jre \
    APP_HOME=/opt/blackduck/hub/scanner \
    BDS_SCANNER_BASE_DIR="/tmp/bds-scanner" \
    SCAN_CLI_JAR_NAME=scan.cli-3.1.0-SNAPSHOT-standalone.jar \
    LANG=C.UTF-8
ENV PATH=${APP_HOME}/bin:${JAVA_HOME}/bin:${PATH}

# Java on Alpine
#  https://github.com/gliderlabs/docker-alpine/issues/11
#  https://hub.docker.com/r/frolvlad/alpine-glibc/
#  https://hub.docker.com/r/frolvlad/alpine-oraclejdk8/
#  https://developer.atlassian.com/blog/2015/08/minimal-java-docker-containers/
#  https://github.com/anapsix/docker-alpine-java/blob/master/Dockerfile.tpl

RUN mkdir /opt && \
    apk --update add curl && \
    curl -Lso "/tmp/glibc-2.21-r2.apk" "https://circle-artifacts.com/gh/andyshinn/alpine-pkg-glibc/6/artifacts/0/home/ubuntu/alpine-pkg-glibc/packages/x86_64/glibc-2.21-r2.apk" && \
    curl -Lso "/tmp/glibc-bin-2.21-r2.apk" "https://circle-artifacts.com/gh/andyshinn/alpine-pkg-glibc/6/artifacts/0/home/ubuntu/alpine-pkg-glibc/packages/x86_64/glibc-bin-2.21-r2.apk" && \
    apk add --allow-untrusted "/tmp/glibc-2.21-r2.apk" "/tmp/glibc-bin-2.21-r2.apk" && \
    /usr/glibc/usr/bin/ldconfig "/lib" "/usr/glibc/usr/lib" && \
    echo 'hosts: files mdns4_minimal [NOTFOUND=return] dns mdns4' >> /etc/nsswitch.conf && \
    curl -jksSLH "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/${JAVA_VERSION_MAJOR}u${JAVA_VERSION_MINOR}-b${JAVA_VERSION_BUILD}/jre-${JAVA_VERSION_MAJOR}u${JAVA_VERSION_MINOR}-linux-x64.tar.gz \
      | tar -xzf - -C /opt \
            --exclude "*/bin/javaws" \
            --exclude "*/plugin" \
            --exclude "*/man" \
            --exclude "*/lib/deploy*" \
            --exclude "*/lib/desktop" \
            --exclude "*/lib/plugin.jar" \
            --exclude "*/lib/javaws.jar" \
            --exclude "*/lib/javafx.properties" \
            --exclude "*/lib/jfxrt.jar" \
            --exclude "*/lib/amd64/libgstreamer-lite.so" \
            --exclude "*/lib/amd64/fx*.so" \
            --exclude "*/lib/amd64/libjavafx*.so" \
            --exclude "*/lib/amd64/libjfx*.so" && \
    apk del curl && \
    ln -s /opt/jre1.${JAVA_VERSION_MAJOR}.0_${JAVA_VERSION_MINOR} ${JAVA_HOME} && \
    rm -rf /tmp/* /var/cache/apk/*

# Install the Hub Scan CLI Client and Container Scanner

COPY bin ${APP_HOME}/bin/
COPY lib ${APP_HOME}/lib/
COPY libexec ${APP_HOME}/bin/

# Labels can change even when nothing else does, keep them at the end
LABEL vendor="Black Duck Software" \
      com.blackducksoftware.version="3.1.0-SNAPSHOT"

ENTRYPOINT [ "scanner" ]
CMD [ "-?" ]
