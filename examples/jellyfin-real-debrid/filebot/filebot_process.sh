#!/bin/bash
set -x  # Enable debug output

media_mount="/mnt/media"
unsorted_dir="$media_mount/_UNSORTED"

export FILEBOT_DATA="/data/filebot"

for arg in "$@"; do
    input_path="${arg//\\}"
    echo "Processing update: $input_path"
    absolute_input="$unsorted_dir/$input_path"

    if [ -n "$(filebot -mediainfo "$absolute_input" --format "{media.Episode}")" ]; then
        script="/app/filebot/series.groovy"
    elif [ -n "$(filebot -mediainfo "$absolute_input" --format "{anime}")" ]; then
        script="/app/filebot/anime.groovy"
    else
        script="/app/filebot/movie.groovy"
    fi

    /opt/filebot/filebot.sh -rename "$absolute_input" \
        --output "$media_mount" \
        --action move \
        --conflict auto \
        -non-strict \
        --log-file /app/logs/filebot.log \
        --license /data/filebot/filebot.psm \
        -script "$script"
done
