#!/bin/bash
# 自动编译、安装并启动调试版应用

echo "==> 执行 gradlew installDebug..."
./gradlew installDebug

if [ $? -eq 0 ]; then
    echo "==> 安装成功，正在启动应用..."
    adb shell am start -n im.fdx.v2ex.debug/im.fdx.v2ex.ui.main.MainActivity
else
    echo "==> 安装失败，取消启动。"
    exit 1
fi
