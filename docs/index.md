---
title: 首页
nav_order: 1
permalink: /
---

# ATFO OpenAdSDK

ATFO OpenAdSDK（`com.atfo.ad:core`）是 Android 广告变现 SDK，覆盖**开屏、信息流（模板渲染 / 宿主自渲染）、插屏、激励视频、通知广告**五类广告。

本站在线文档包含两篇接入指南，按接入模式选择：

| 接入模式 | 说明 | 文档 |
|---|---|---|
| **非聚合** | 仅依赖 `com.atfo.ad:core`，ATFO 自有广告源开箱即用，无需三方 adapter | [非聚合接入指南](standalone.md) |
| **聚合** | 集成 GroMore、优量汇、百度、快手等三方广告网络的聚合模式 | [聚合接入指南](aggregation.md) |

## 示例 Demo

仓库 [atfoenginex/ATFOAndroidSdkDemo](https://github.com/atfoenginex/ATFOAndroidSdkDemo) 提供了完整可运行工程，主页六个按钮各对应一个接入示例，每个示例是一个独立 Activity（配套同名布局），可直接整文件复制到自己的工程：

- 开屏 · 信息流（模板渲染）· 信息流（宿主自渲染）· 插屏 · 激励视频 · 通知广告

## 快速开始

1. 联系商务人员获取媒体账户、应用 ID、广告位 ID、安全密钥。
2. 按接入模式选择 [非聚合接入指南](standalone.md) 或 [聚合接入指南](aggregation.md) 完成依赖配置与 SDK 初始化。
3. 在主页面配置参数后运行 Demo，参考各广告类型的接入代码。