#!/bin/bash
# Firewall rules for Claude Code devcontainer
# Only allows connections to services Claude Code needs

set -e

# Flush existing rules
iptables -F OUTPUT 2>/dev/null || true

# Allow loopback
iptables -A OUTPUT -o lo -j ACCEPT

# Allow established connections
iptables -A OUTPUT -m state --state ESTABLISHED,RELATED -j ACCEPT

# Allow DNS
iptables -A OUTPUT -p udp --dport 53 -j ACCEPT
iptables -A OUTPUT -p tcp --dport 53 -j ACCEPT

# Allow HTTPS (443) - needed for Anthropic API, npm, GitHub
iptables -A OUTPUT -p tcp --dport 443 -j ACCEPT

# Allow HTTP (for package managers)
iptables -A OUTPUT -p tcp --dport 80 -j ACCEPT

# Allow SSH (for git)
iptables -A OUTPUT -p tcp --dport 22 -j ACCEPT

echo "Firewall rules applied successfully"
