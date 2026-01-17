FROM osrm/osrm-backend:latest

# Ensure we have a downloader available for the entrypoint script.
# alpine-based images use apk; debian-based use apt-get. Handle both.
RUN set -eux; \
    if command -v apk >/dev/null 2>&1; then \
      apk add --no-cache curl wget ca-certificates; \
    elif command -v apt-get >/dev/null 2>&1; then \
      if grep -q "stretch" /etc/os-release; then \
        echo "deb http://archive.debian.org/debian stretch main" > /etc/apt/sources.list; \
        echo "deb http://archive.debian.org/debian-security stretch/updates main" >> /etc/apt/sources.list; \
      fi; \
      apt-get -o Acquire::Check-Valid-Until=false update; \
      apt-get install -y --no-install-recommends curl wget ca-certificates; \
      rm -rf /var/lib/apt/lists/*; \
    else \
      echo "Unsupported base image: no apk/apt-get" >&2; \
      exit 1; \
    fi

# Bake the entrypoint in the image (more reliable than bind-mounting on Windows).
COPY docker/osrm-entrypoint.sh /usr/local/bin/osrm-entrypoint.sh

# Guard against CRLF issues from Windows checkouts.
RUN set -eux; \
    sed -i 's/\r$//' /usr/local/bin/osrm-entrypoint.sh; \
    chmod +x /usr/local/bin/osrm-entrypoint.sh

VOLUME ["/data"]
EXPOSE 5000
ENTRYPOINT ["/usr/local/bin/osrm-entrypoint.sh"]
