FROM mcr.microsoft.com/azure-functions/powershell:3.0-powershell7-core-tools
WORKDIR /
RUN wget https://download.oracle.com/graalvm/17/latest/graalvm-jdk-17_linux-x64_bin.tar.gz
RUN tar -xzf graalvm-jdk-17_linux-x64_bin.tar.gz
RUN apt update && apt install -y build-essential zlib1g-dev
ENV JAVA_HOME="/graalvm-jdk-17.0.9+11.1"
RUN mkdir repo
WORKDIR /repo
ENTRYPOINT ["./gradlew", "app:packageDistribution"]