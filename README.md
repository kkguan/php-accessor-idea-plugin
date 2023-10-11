<div align="center">
  <img width="13%" src="https://raw.githubusercontent.com/kkguan/php-accessor-idea-plugin/899502e6d1960921a8c4bf824bd8a75ed544bc8b/src/main/resources/META-INF/pluginIcon.svg">
</div>

# PHP Accessor IDEA Plugin

#### - 保存时自动生成访问器（Getter & Setter）

#### - 支持通过生成的访问器方法跳转至对应的类属性字段

#### - 支持类属性字段的"<a href="https://www.jetbrains.com/help/idea/find-highlight-usages.html">查找使用</a>"，插件将帮助找到访问器的所有引用位置

#### - 支持对类属性字段的"<a href="https://www.jetbrains.com/help/idea/refactoring-source-code.html">重命名重构</a>"，重构字段时将同步修改访问器及相关调用位置

#### - 一键生成对象的所有Setter调用语句

插件地址：https://plugins.jetbrains.com/plugin/21172-php-accessor

## 快速入门

### 安装

1. phpstorm中检索并安装插件`PHP Accessor`

2. 确保phpstorm中已正确配置composer：`Settings -> PHP -> Composer -> Execution`

3. 通过composer安装 <a href="https://github.com/kkguan/php-accessor">PHP Accessor</a>
   （Hyperf框架 <a href="https://github.com/kkguan/hyperf-php-accessor">Hyperf PHP Accessor</a> |
   Laravel框架 <a href="https://github.com/kkguan/laravel-php-accessor">Laravel PHP Accessor</a>）

4. 项目`composer.json` 文件中配置以下信息信息
   ```json
   {
     "scripts": {
       "php-accessor": "@php vendor/bin/php-accessor generate"
     }
   }
   ```

### 通过`#[Data]`注解原始类

```php
<?php
namespace App;

use PhpAccessor\Attribute\Data;

#[Data]
class Entity
{
    private int $id;

    private int $sex;
}
```

更多注解的使用说明,详见<a href="https://github.com/kkguan/php-accessor">PHP Accessor</a>.

## 相关资源

#### <a href="https://github.com/kkguan/php-accessor">PHP Accessor</a>: 访问器生成器

#### <a href="https://github.com/kkguan/php-accessor-idea-plugin">PHP Accessor IDEA Plugin</a>: Phpstorm插件,文件保存时自动生成访问器.支持访问器的跳转,代码提示,查找及类字段重构等.

#### <a href="https://github.com/kkguan/hyperf-php-accessor">Hyperf PHP Accessor</a>: Hyperf框架SDK

#### <a href="https://github.com/kkguan/laravel-php-accessor">Laravel PHP Accessor</a>: Laravel框架SDK



