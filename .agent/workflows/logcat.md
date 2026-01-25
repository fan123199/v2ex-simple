---
description: 抓取应用实时日志 (Logcat)
---

执行以下步骤来获取当前运行应用的 Logcat 日志：

1. **获取 PID 并查看实时日志**
// turbo
```zbash
adb logcat -d --pid=$(adb shell pidof -s im.fdx.v2ex.debug) | tail -n 100
```

2. **仅查看错误日志 (Errors)**
// turbo
```zbash
adb logcat -d *:E --pid=$(adb shell pidof -s im.fdx.v2ex.debug)
```

3. **根据关键字过滤 (例如: Parsing)**
// turbo
```zbash
adb logcat -d --pid=$(adb shell pidof -s im.fdx.v2ex.debug) | grep "Parsing"
```

4. **持续监控日志 (手动停止)**
```zbash
adb logcat --pid=$(adb shell pidof -s im.fdx.v2ex.debug)
```
