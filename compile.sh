#!/bin/bash
# 編譯所有 Java 原始碼
mkdir -p bin
find src -name "*.java" > sources.txt
javac -d bin -sourcepath src @sources.txt
if [ $? -eq 0 ]; then
    echo "=== 編譯成功 ==="
else
    echo "=== 編譯失敗 ==="
fi
rm -f sources.txt