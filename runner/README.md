# Self-Hosted GitHub Actions Runner

We use a self-hosted runner for GitHub Actions. The runner uses an
Ubuntu Docker container that’s controlled via a Systemd service.

The setup, like GitHub’s runners, is ephemeral and doesn’t depend on the
state of the host.

## Installation

- Install Docker
- Install service and config: `./install.sh`

## Usage

- Start service: `sudo systemctl start github-runner`
- Stop service: `sudo systemctl stop github-runner`
- Logs: `journalctl -f -u github-runner.service`
