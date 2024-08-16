# Changelog

## 0.6.5

* Feature 支持 242.* 版本

## 0.6.4

* Fix 修复生成对象的Setter调用时，出现重复变量的问题

## 0.6.3

* Fix 注释内的类型声明现在可以正确跳转
* Fix 对已生成访问器的类，抑制其在注释中产生存在多个定义的弱警告

## 0.6.2

* Fix 修复`Add method`时类型声明包含代理抽象类的问题
* Fix 对已生成访问器的类，抑制其在注释中产生存在多个定义的弱警告

## 0.6.1

* Feature 优化配置界面
* Feature 识别Hyperf框架的配置文件并同步到IDEA的插件配置中

## 0.6.0

* Feature 代理IDE内置的访问器生成动作(`Getter`、`Getters and Setters`及`Setter`)，相关动作生成的访问器命名将以PHPAccessor为准。

## 0.5.3

* Feature 增加对Laravel框架的依赖包安装提示

## 0.5.2

* Feature 支持 233.* 版本

## 0.5.1

* Fix 修复新版本IDEA中，无法正确对this完成代码提示

## 0.5.0

* Feature 支持生成对象的Setter调用

## 0.4.2

* Fix 修复部分场景下类型推断错误

## 0.4.1

* Fix 修复新版本IDEA中类查找引用报错的问题

## 0.4.0

* Feature 支持通过右键菜单生成访问器
* Fix 修复了在类上查找引用时展示代理类的问题

## 0.3.5

* Feature 支持 232.* 版本

## 0.3.4

* Fix 修复PhpTypeProvider4可能导致的异常

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
