#!/bin/sh -e

# install config
sudo install -m 600 github-runner.env /etc/

# systemd service
sudo install -m 644 github-runner.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable github-runner
