FROM jellyfin/jellyfin:latest AS base

# Stage 1: Extract Zurg binary
FROM ghcr.io/debridmediamanager/zurg-testing:latest AS zurg-source

# Stage 2: Build VMapFS and assemble final image
FROM base

# Install dependencies (including FileBot requirements, xz-utils, and libmediainfo)
RUN apt-get update && apt-get install -y \
    fuse3 \
    curl \
    unzip \
    bash \
    golang \
    git \
    make \
    netcat-openbsd \
    musl \
    openjdk-17-jre-headless \
    xz-utils \
    libmediainfo0v5 \
    && ln -s /lib/x86_64-linux-musl/libc.so /lib/libc.musl-x86_64.so.1 \
    && rm -rf /var/lib/apt/lists/*

# Install rclone
RUN curl -O https://downloads.rclone.org/rclone-current-linux-amd64.zip && \
    unzip rclone-current-linux-amd64.zip && \
    cd rclone-*-linux-amd64 && \
    cp rclone /usr/local/bin/ && \
    chown root:root /usr/local/bin/rclone && \
    chmod 755 /usr/local/bin/rclone && \
    cd .. && \
    rm -rf rclone-*-linux-amd64 rclone-current-linux-amd64.zip

# Verify rclone installation
RUN rclone --version

# Copy Zurg binary from zurg-source
COPY --from=zurg-source /app/zurg /usr/local/bin/zurg
RUN chmod +x /usr/local/bin/zurg

# Install FileBot to /opt/filebot
RUN curl -L https://get.filebot.net/filebot/FileBot_5.1.6/FileBot_5.1.6-portable.tar.xz -o /tmp/filebot.tar.xz && \
    mkdir -p /tmp/filebot-extract && \
    tar -xJf /tmp/filebot.tar.xz -C /tmp/filebot-extract && \
    mkdir -p /opt/filebot/data && \
    mv /tmp/filebot-extract/* /opt/filebot/ && \
    chmod +x /opt/filebot/filebot.sh && \
    ln -s /opt/filebot/filebot.sh /usr/local/bin/filebot && \
    chown -R 1500:1500 /opt/filebot && \
    chmod -R 775 /opt/filebot && \
    rm -rf /tmp/filebot.tar.xz /tmp/filebot-extract

# Add user for PUID:PGID (e.g., 1500:1500)
ARG PUID=1500
ARG PGID=1500
RUN groupadd -g ${PGID} mediauser && \
    useradd -u ${PUID} -g ${PGID} -m -s /bin/bash mediauser

# Configure FUSE and directories
RUN echo "user_allow_other" >> /etc/fuse.conf && \
    mkdir -p /mnt/zurg /mnt/media /data /app /app/logs /data/filebot && \
    chown ${PUID}:${PGID} /mnt/zurg /mnt/media /data /app /app/logs /data/filebot && \
    chmod 775 /mnt/zurg /mnt/media /data /app /app/logs /data/filebot

# Configure rclone
RUN mkdir -p /home/mediauser/.config/rclone && \
    printf "[zurg]\ntype = webdav\nurl = http://localhost:9999\nvendor = other\n" > /home/mediauser/.config/rclone/rclone.conf && \
    chown -R mediauser:mediauser /home/mediauser/.config

# Build VMapFS from GitHub using make
RUN git clone https://github.com/LachlanBridges/VMapFS.git /usr/src/vmapfs && \
    cd /usr/src/vmapfs && \
    make build && \
    cp bin/vmapfs /usr/local/bin/vmapfs && \
    chmod +x /usr/local/bin/vmapfs

COPY start.sh /start.sh
COPY config.yml.template /app/config.yml.template
COPY update_library.sh /app/update_library.sh
COPY jellyfin_update.sh /app/jellyfin_update.sh
COPY filebot/ /app/filebot/
RUN chmod +x /start.sh /app/update_library.sh /app/jellyfin_update.sh /app/filebot/filebot_process.sh

# Set FileBot data directory (kept as backup)
ENV FILEBOT_DATA=/data/filebot

ENTRYPOINT ["/bin/bash"]
CMD ["/start.sh"]
