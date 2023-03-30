# Changelog

## 0.3.3

* Feature 抑制"方法未找到"弱警告

## 0.3.2

* Fix 修复用户删除文件可能导致的报错
* Fix 修复生成访问器后无法处理`Add method`的问题

## 0.3.1

* Feature 升级依赖包版本及增加对Hyperf框架的依赖包安装提示
* Feature 对已生成访问器的类成员，抑制其产生"只读取，未写入"及"仅写入，未读取"的弱警告

## 0.3.0

* Fix 修复默认路径WIN下可能导致的异常
* Refactor 去除对Phpstorm内置PhpAccessorsGenerator的依赖

## 0.2.5

* Feature 依赖环境检测及引导
* Feature 中文支持
* Refactor 优化生成逻辑

## 0.2.4

* Fix 解决遗留Bug

## 0.2.3

* Feature 对已生成访问器的类成员，抑制其产生"未使用"的弱警告
* Feature 对已生成访问器的类，抑制其产生"其他"声明的弱警告

## 0.2.2

* Feature 类字段支持查找及重构
* Fix 修复vendor目录下会生成代理类的问题
