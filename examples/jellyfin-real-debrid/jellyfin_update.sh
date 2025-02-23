#!/bin/bash

set -x  # Enable debug output

jellyfin_url="http://localhost:8096"
jellyfin_token="${JELLYFIN_TOKEN:-}"  # Optional, from environment
media_mount="/mnt/media"

if [ -n "$jellyfin_token" ]; then
    # Token provided, update specific paths
    for arg in "$@"; do
        parsed_arg="${arg//\\}"
        absolute_path="$media_mount/$parsed_arg"
        echo "Updating Jellyfin for: $absolute_path"
        curl -X POST "$jellyfin_url/Library/Refresh" \
            -H "Authorization: MediaBrowser Token=$jellyfin_token" \
            -H "Content-Type: application/json" \
            -d "{\"Path\": \"$absolute_path\"}"
    done
else
    # No token, trigger full library scan (works without auth during initial setup)
    echo "No Jellyfin token provided, triggering full library scan..."
    curl -X POST "$jellyfin_url/Library/Refresh" \
        -H "Content-Type: application/json"
fi
