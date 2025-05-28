@echo off
start powershell -NoExit -Command "cd server;npm start"
start powershell -NoExit -Command "Start-Sleep -Seconds 1;cd web-panel;npm start"
