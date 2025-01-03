#!/bin/bash

# Install ffmpeg
apt update
apt install -y python3 python3-pip ffmpeg

# Install yt-dlp
curl -L https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp -o /usr/bin/yt-dlp
chmod a+rx /usr/bin/yt-dlp
