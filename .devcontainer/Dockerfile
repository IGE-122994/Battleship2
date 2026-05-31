FROM eclipse-temurin:17-jdk

# Install Maven
RUN apt-get update && apt-get install -y \
    maven \
    git \
    curl \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /workspace

# Verify versions
RUN java -version && mvn -version && git --version