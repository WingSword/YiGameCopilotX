# Wasm 版本修复 - 概述

## 问题

Kotlin/Wasm 链接器报错 `IrTypeAliasSymbolImpl is already bound: kotlinx.datetime/Instant`，阻止 Web
端运行。

## 根因分析

1. 项目 Kotlin 版本 2.0.0，但 Compose 1.9.3 + kotlinx-serialization + Ktor 的 Wasm klib 全部用 Kotlin
   2.2.21 编译
2. Gradle 依赖解析将所有 `kotlin-stdlib` 统一到 2.2.21
3. kotlinx-datetime 0.6.x 的 Wasm klib（Kotlin 1.9.21 编译）中的 `Instant` type alias 与 stdlib 2.2+ 的
   `kotlin.time.Instant` 冲突
4. `kotlin.time.Clock`/`kotlin.time.Instant` 仅在 Kotlin 2.3+ 标准库中可用

## 解决方案

- **Kotlin**: 2.0.0 → **2.3.20**
- **kotlinx-datetime**: 0.6.1 → **0.7.1**
- 3 个 shared 模块文件的 import 迁移：`kotlinx.datetime.Clock/Instant` → `kotlin.time.Clock/Instant`

## 修改文件

1. `gradle/libs.versions.toml` — Kotlin 2.3.20, kotlinx-datetime 0.7.1
2. `shared/.../utils/DateTimeUtils.kt` — import 迁移
3. `shared/.../lan/data/LANModels.kt` — import 迁移
4. `shared/.../lan/LANRoomManager.kt` — import 迁移
5. `gradle.properties` — 无变更（保持 android.builtInKotlin=false）
6. yarn lock 更新

## 验证结果

- Wasm 编译 + 链接 + Webpack dev server 成功运行在 http://localhost:8082
- Android assembleDebug 成功
- 所有编译警告均为 deprecation 类（非致命）
