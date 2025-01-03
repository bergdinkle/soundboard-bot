#!/bin/bash

# Install ffmpeg
apt update
apt install -y python3 python3-pip ffmpeg

# Install yt-dlp
curl -L https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp -o /usr/bin/yt-dlp
chmod a+rx /usr/bin/yt-dlp

# Set environment variables for devcontainer user
echo "export SOUNDBOARD_BOT_FFMPEG_BASE_PATH=/usr/bin" >> /home/vscode/.bashrc
echo "export SOUNDBOARD_BOT_YT_DLP_BASE_PATH=/usr/bin" >> /home/vscode/.bashrc
echo "export SOUNDBOARD_BOT_SOUND_FOLDER=/workspace/soundboard-bot/sounds" >> /home/vscode/.bashrc
echo "export SOUNDBOARD_BOT_TOKEN_SECRET=Zshe6CTBruBCocVkmjOnYFJlPJx8BBOMTyXTDt7liZ9kOG6M9p0Bx0DXUNYLp9Z4" >> /home/vscode/.bashrc
