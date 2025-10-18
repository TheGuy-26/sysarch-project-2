# Step 1: Build the project
FROM ubuntu:latest AS builder

# Install Java 21 and sbt
RUN apt-get update && \
    apt-get install apt-transport-https curl gnupg -yqq && \
    echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | tee /etc/apt/sources.list.d/sbt.list && \
    echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | tee /etc/apt/sources.list.d/sbt_old.list && \
    curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | gpg --no-default-keyring --keyring gnupg-ring:/etc/apt/trusted.gpg.d/scalasbt-release.gpg --import && \
    chmod 644 /etc/apt/trusted.gpg.d/scalasbt-release.gpg && \
    apt-get update && \
    apt-get install -y openjdk-21-jdk unzip wget

# TODO: Currently, 1.4.9 is the latest version available via apt. This version is incompatible with JAVA 21. Remove this when a compatible version is available.
RUN wget https://scala.jfrog.io/artifactory/debian/sbt-1.11.2.deb && dpkg -i sbt-1.11.2.deb

WORKDIR /app

# Copy the build definition files
COPY sources/ .

# Download and cache the project dependencies
RUN sbt dist

RUN unzip target/universal/webui-1.1.zip -d /target

# Step 2: Create the final image
FROM ubuntu:latest

# Install Java 21 and Java 21 Debug and sbt and verilator
RUN apt-get update && \
    apt-get install apt-transport-https curl gnupg -yqq && \
    echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | tee /etc/apt/sources.list.d/sbt.list && \
    echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | tee /etc/apt/sources.list.d/sbt_old.list && \
    curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | gpg --no-default-keyring --keyring gnupg-ring:/etc/apt/trusted.gpg.d/scalasbt-release.gpg --import && \
    chmod 644 /etc/apt/trusted.gpg.d/scalasbt-release.gpg && \
    apt-get update && \
    apt-get install -y openjdk-21-jdk wget

# TODO: Currently, 1.4.9 is the latest version available via apt. This version is incompatible with JAVA 21. Remove this when a compatible version is available.    
RUN wget https://scala.jfrog.io/artifactory/debian/sbt-1.11.2.deb && dpkg -i sbt-1.11.2.deb

WORKDIR /app

# Copy the built artifacts from the previous step
COPY --from=builder /target/ .

# Set the entry point for the application
ENTRYPOINT ["/app/webui-1.1/bin/webui"]