#!/bin/bash
set -x  # Enable debug output

export HOME=/home/mediauser

cleanup() {
    echo "Cleaning up..."
    if mount | grep -q "/mnt/zurg"; then
        fusermount3 -u /mnt/zurg || umount -f /mnt/zurg || true
    fi
    if mount | grep -q "/mnt/media"; then
        fusermount3 -u /mnt/media || umount -f /mnt/media || true
    fi
    kill $(jobs -p) 2>/dev/null || true
    exit
}

trap "cleanup" TERM INT

# Check Zurg binary
if [ ! -x /usr/local/bin/zurg ]; then
    echo "ERROR: zurg binary not found or not executable"
    ls -la /usr/local/bin/zurg || true
    exit 1
fi

PUID=${PUID:-1500}
PGID=${PGID:-1500}

# Check for FileBot license mount
if [ -f /data/filebot/filebot.psm ]; then
    echo "FileBot license detected, enabling FileBot processing"
    export HAS_FILEBOT=1
else
    echo "No FileBot license detected, disabling FileBot processing"
    export HAS_FILEBOT=0
fi

# Ensure /app/logs exists and is writable
echo "Preparing Zurg directories..."
mkdir -p /app/logs
chown ${PUID}:${PGID} /app /app/logs
chmod 775 /app /app/logs
ls -ld /app /app/logs

echo "Starting Zurg..."
cp /app/config.yml.template /app/config.yml
sed -i "s/\${RD_TOKEN}/${RD_TOKEN}/g" /app/config.yml
whoami  # Debug: Confirm user
cd /app && /usr/local/bin/zurg --config /app/config.yml &
ZURG_PID=$!

echo "Waiting for Zurg..."
for i in $(seq 1 30); do
    if nc -z localhost 9999; then
        echo "Zurg is available"
        break
    fi
    echo "Waiting for Zurg... attempt $i/30"
    sleep 1
done
if ! nc -z localhost 9999; then
    echo "ERROR: Zurg failed to start"
    exit 1
fi

echo "Mounting Zurg with rclone..."
/usr/local/bin/rclone mount zurg:/dav/ /mnt/zurg \
    --allow-other \
    --allow-non-empty \
    --dir-cache-time 10s \
    --vfs-cache-mode full \
    --log-level DEBUG \
    --log-file /tmp/rclone.log \
    --daemon &
RCLONE_PID=$!

echo "Waiting for rclone mount..."
for i in $(seq 1 30); do
    if ls /mnt/zurg >/dev/null 2>&1 && [ -n "$(ls -A /mnt/zurg)" ]; then
        echo "rclone mount verified"
        ls -l /mnt/zurg
        break
    fi
    if ! kill -0 $RCLONE_PID 2>/dev/null; then
        echo "ERROR: rclone process died"
        cat /tmp/rclone.log
        exit 1
    fi
    echo "Waiting for rclone... attempt $i/30"
    sleep 1
done
if ! ls /mnt/zurg >/dev/null 2>&1 || [ -z "$(ls -A /mnt/zurg)" ]; then
    echo "ERROR: rclone mount failed or empty"
    cat /tmp/rclone.log
    exit 1
fi

echo "Starting VMapFS..."
/usr/local/bin/vmapfs -mount /mnt/media -source /mnt/zurg/__all__/ -state /data/virtualfs-state.json &
VMAPFS_PID=$!

echo "Waiting for VMapFS mount..."
for i in $(seq 1 30); do
    if ls /mnt/media >/dev/null 2>&1 && [ -n "$(ls -A /mnt/media)" ]; then
        echo "vmapfs mount verified"
        ls -l /mnt/media
        break
    fi
    if ! kill -0 $VMAPFS_PID 2>/dev/null; then
        echo "ERROR: vmapfs process died"
        exit 1
    fi
    echo "Waiting for vmapfs... attempt $i/30"
    sleep 1
done
if ! ls /mnt/media >/dev/null 2>&1 || [ -z "$(ls -A /mnt/media)" ]; then
    echo "ERROR: vmapfs mount failed or empty"
    exit 1
fi

echo "Starting Jellyfin..."
exec /jellyfin/jellyfin
