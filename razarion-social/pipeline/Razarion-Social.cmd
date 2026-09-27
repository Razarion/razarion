@echo off
rem Razarion Social - a run started by hand. A double-click is enough.
rem
rem 1. scheduled\run.ps1: check the token, fill the queue (plan.mjs), upload media, and publish
rem    one approved post per network - exactly what the schedule does.
rem 2. Then the review page opens for whatever the run made.
rem    Keep this window open while you need the page; closing it stops the page.

cd /d "%~dp0"
title Razarion Social
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scheduled\run.ps1"
echo.
echo Review page: http://127.0.0.1:4711   (closing this window stops it)
node review.mjs --open
echo.
pause
