# AndroidUtil

[![JitPack](https://jitpack.io/v/AndroidUtil/AndroidUtil.svg)](https://jitpack.io/#AndroidUtil/AndroidUtil)
[![CI](https://github.com/AndroidUtil/AndroidUtil/workflows/Android%20CI/badge.svg)](https://github.com/AndroidUtil/AndroidUtil/actions)
[![API](https://img.shields.io/badge/API-21%2B-blue.svg?style=flat)](https://android-arsenal.com/api?level=21)
[![License](https://img.shields.io/badge/license-Apche%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Blog](https://img.shields.io/badge/blog-Jenly-9933CC.svg)](https://jenly1314.github.io/)
[![QQGroup](https://img.shields.io/badge/QQGroup-20867961-blue.svg)](http://shang.qq.com/wpa/qunwpa?idkey=8fcc6a2f88552ea44b1.1.982c94fd124f7bb3ec227e2a400dbbfaad3dc2f5ad)

AndroidUtil 是一个整理了Android常用工具类集合，平时在开发的过程中可能会经常用到。

主要来源：
* [org.apache.commons](https://commons.apache.org/)
* [base-util](https://github.com/jenly1314/Base)


## 引入

### Gradle

1. 在Project的 **build.gradle** 里面添加远程仓库  
          
```gradle
allprojects {
    repositories {
        //...
        maven { url 'https://jitpack.io' }
    }
}
```

2. 在Module的 **build.gradle** 里面添加引入依赖项

```gradle
//AndroidX 版本
implementation 'com.github.AndroidUtil:AndroidUtil:1.0.0'

```



