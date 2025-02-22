# VMapFS with Jellyfin and RealDebrid

This example demonstrates how to use VMapFS with Zurg, rclone, and Jellyfin to create a virtual filesystem for RealDebrid content, served through Jellyfin. VMapFS maps files from a flat RealDebrid structure (via Zurg and rclone) into a hierarchical filesystem, making it ideal for media servers like Jellyfin or Plex.

## Features
- **RealDebrid Integration**: Streams content directly from RealDebrid using Zurg’s WebDAV server.
- **Hierarchical Filesystem**: VMapFS reorganizes files from Zurg’s flat `__all__` directory into virtual paths (e.g., `/movies`, `/tv`, `_UNSORTED`).
- **Jellyfin Support**: Runs Jellyfin to serve the virtual filesystem over HTTP.
- **Dockerized**: Everything runs in a single container for easy deployment and segregation.

## Prerequisites
- **Docker**: Installed with `docker-compose`.
- **RealDebrid Token**: Obtain an API token from [RealDebrid](https://real-debrid.com/apitoken).

## Setup
### Clone the Repository
Clone the VMapFS repo to a temporary directory for testing:
```bash
git clone https://github.com/LachlanBridges/VMapFS.git /tmp/vmapfs
cd /tmp/vmapfs/examples/jellyfin-real-debrid

### Prepare Environment
1. Create Directories:
```bash
mkdir -p config data cache logs zurg
chown -R 1500:1500 config data cache logs zurg
chmod -R 775 config data cache logs zurg
```
2. Set Environment Variables:

Copy the example environment file and customize it:
```bash
cp .env.example .env
nano .env
```
Edit .env to include your RealDebrid token:
```bash
RD_TOKEN=your_real_debrid_token
TZ=Australia/Sydney  # Optional, defaults to UTC
PUID=1500           # Optional, adjust to your user ID
PGID=1500           # Optional, adjust to your group ID
```
3. Build and Run

Build and start the container:

```bash
docker-compose up -d --build
```
The container will:
- Start Zurg to serve RealDebrid content via WebDAV.
- Mount Zurg with rclone at /mnt/zurg.
- Mount VMapFS at /mnt/media, mapping files from /mnt/zurg/\_\_all\_\_.
- Run Jellyfin on ports 8096 and 8920.

## Usage
1. Access Jellyfin:
- Open http://localhost:8096 in your browser.
- Complete the Jellyfin setup wizard.
2. Add Libraries:
- Point libraries to /mnt/media/movies, /mnt/media/tv, etc.
- Files in _UNSORTED can be renamed/moved within /mnt/media using tools like filebot.
3. Verify Direct Streaming:
- Play a file in Jellyfin and monitor network traffic (e.g., nload or iftop). Minimal traffic indicates direct streaming from RealDebrid.

## Customization
- VMapFS Mappings: Edit ./data/virtualfs-state.json to adjust virtual paths (e.g., move files from _UNSORTED to /movies).
- Ports: Change 8096:8096 and 8920:8920 in docker-compose.yml if needed.
- User IDs: Adjust PUID and PGID in .env to match your system user.
- Timezone: Set TZ in .env or uncomment /etc/localtime:/etc/localtime:ro in docker-compose.yml to use the host’s timezone.

## Top-Level Wrapper (Optional)
For a custom setup, clone the repo to /opt/mediaserver/vmapfs/ and create /opt/mediaserver/docker-compose.yml:

```yaml
version: "3.5"
services:
  mediaserver:
    extends:
      file: ./vmapfs/examples/jellyfin-real-debrid/docker-compose.yml
      service: mediaserver
```
Run from /opt/mediaserver/:

```bash
docker-compose up -d --build
```

## Troubleshooting
- Build Fails:
    - Check docker-compose logs mediaserver for errors (e.g., missing dependencies).
    - Ensure RD_TOKEN is set in .env.
- Mounts Empty:
    - Verify Zurg: docker exec -it mediaserver nc -z localhost 9999.
    - Check rclone logs: docker exec -it mediaserver cat /tmp/rclone.log.
- _UNSORTED Errors:
    - Ensure /mnt/zurg/\_\_all\_\_ has files: docker exec -it mediaserver ls -l /mnt/zurg.
    - Inspect /data/virtualfs-state.json.
## Notes
- Direct Streaming: Zurg’s WebDAV enables direct streaming; VMapFS should preserve this (test with network monitoring).
- Filebot: Use /mnt/media with tools like filebot for renaming/moving files, leveraging VMapFS’s virtual structure.

For more details on VMapFS, see the main [README](../../README).
