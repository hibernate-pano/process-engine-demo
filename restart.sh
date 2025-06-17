#!/bin/bash
# 一键重启前后端脚本

DIR=$(cd "$(dirname "$0")" && pwd)
$DIR/stop.sh
sleep 2
$DIR/start.sh
