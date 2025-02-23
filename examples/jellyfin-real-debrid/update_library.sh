#!/bin/bash

# Wrapper script for Zurg on_library_update
# Conditionally runs FileBot if available, then updates Jellyfin library

set -x  # Enable debug output

# Run FileBot processing if license is present
if [ "$HAS_FILEBOT" = "1" ]; then
    echo "FileBot enabled, processing updates..."
    /app/filebot_process.sh "$@"
else
    echo "FileBot disabled, skipping processing..."
fi

# Update Jellyfin library
/app/jellyfin_update.sh "$@"

echo "Library update complete"
