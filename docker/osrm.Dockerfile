FROM osrm/osrm-backend:latest

# Install wget and curl to ensure data download capability
# osrm-backend is typically Debian-based
RUN if [ -f /etc/alpine-release ]; then \
        apk add --no-cache curl wget ca-certificates; \
    else \
        # Handle EOL Debian Stretch repositories \
        if grep -q "stretch" /etc/os-release; then \
            echo "deb http://archive.debian.org/debian stretch main" > /etc/apt/sources.list; \
            echo "deb http://archive.debian.org/debian-security stretch/updates main" >> /etc/apt/sources.list; \
            apt-get -o Acquire::Check-Valid-Until=false update; \
        else \
            apt-get update; \
        fi && \
        apt-get install -y --no-install-recommends curl wget ca-certificates && \
        rm -rf /var/lib/apt/lists/*; \
    fi

COPY docker/osrm-entrypoint.sh /usr/local/bin/osrm-entrypoint.sh
RUN chmod +x /usr/local/bin/osrm-entrypoint.sh

VOLUME ["/data"]
EXPOSE 5000
