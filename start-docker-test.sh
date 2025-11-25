#!/bin/bash

# Quick Start Script for 2-Player Poker Game Testing

set -e

GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${BLUE}═══════════════════════════════════════════════════${NC}"
echo -e "${BLUE}   🎴 Poker Game - Quick Test Setup${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════${NC}"
echo ""

# Check dependencies
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker not found. Install: https://docs.docker.com/get-docker/${NC}"
    exit 1
fi

if ! docker compose version &> /dev/null; then
    echo -e "${RED}❌ Docker Compose not found.${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Dependencies OK${NC}"
echo ""

# Check server status
if docker compose ps | grep -q "Up"; then
    echo -e "${YELLOW}⚠️  Server already running${NC}"
    echo "1) Restart  2) Logs  3) Stop  4) Continue"
    read -p "Choose (1-4): " choice
    case $choice in
        1) docker compose restart ;;
        2) docker compose logs -f; exit 0 ;;
        3) docker compose down; exit 0 ;;
        4) ;;
        *) exit 1 ;;
    esac
else
    echo -e "${GREEN}🚀 Starting server...${NC}"
    docker compose up -d
    sleep 2
fi

echo ""
docker compose ps
echo ""
echo -e "${GREEN}✅ Server: ws://localhost:8081/ws/poker${NC}"
echo ""

echo -e "${BLUE}═══════════════════════════════════════════════════${NC}"
echo -e "${BLUE}   🎮 Connect 2 Players${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════${NC}"
echo ""
echo -e "${YELLOW}Option 1: Python Clients${NC}"
echo "  Terminal 2: ${BLUE}python3 poker-client.py Alice${NC}"
echo "  Terminal 3: ${BLUE}python3 poker-client.py Bob${NC}"
echo ""
echo -e "${YELLOW}Option 2: Browser${NC}"
echo "  Open websocket-client.html in 2 tabs"
echo ""

echo -e "${BLUE}═══════════════════════════════════════════════════${NC}"
echo -e "${BLUE}   🎯 Game Flow${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════${NC}"
echo ""
echo "1. REGISTER Alice 1000"
echo "2. REGISTER Bob 1000"
echo "3. START_GAME <alice-id> <bob-id> 10 20"
echo "4. GET_MY_CARDS <game-id> <player-id>"
echo "5. CALL/CHECK/RAISE/FOLD"
echo "6. DEAL_FLOP/TURN/RIVER <game-id>"
echo "7. DETERMINE_WINNER <game-id>"
echo ""
echo -e "${YELLOW}Tip: Use 'help' and 'quick' commands in client${NC}"
echo ""

echo -e "${BLUE}═══════════════════════════════════════════════════${NC}"
echo "View logs? (y/n)"
read -p "> " view_logs

if [[ $view_logs == "y" || $view_logs == "Y" ]]; then
    echo ""
    echo -e "${GREEN}Server logs (Ctrl+C to exit):${NC}"
    echo ""
    docker compose logs -f
fi
