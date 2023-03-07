<div align="center">
  <img width="13%" src="https://raw.githubusercontent.com/kkguan/php-accessor-idea-plugin/899502e6d1960921a8c4bf824bd8a75ed544bc8b/src/main/resources/META-INF/pluginIcon.svg">
</div>

# PHP Accessor IDEA Plugin

#### - 支持用户保存时自动生成访问器（Getter & Setter）

#### - 支持通过生成的访问器方法跳转至对应的类属性字段

#### - 支持类属性字段的"<a href="https://www.jetbrains.com/help/idea/find-highlight-usages.html">查找使用</a>"，插件将帮助找到访问器的所有引用位置

#### - 支持对类属性字段的"<a href="https://www.jetbrains.com/help/idea/refactoring-source-code.html">重命名重构</a>"，重构字段时将同步修改访问器及相关调用位置

快速入门
-----------

### 安装

1. phpstorm中检索并安装插件`PHP Accessor`
2. 确保phpstorm中已正确配置composer：`Settings -> PHP -> Composer -> Execution`

3. 通过composer安装 <a href="https://github.com/kkguan/php-accessor">PHP Accessor</a>
   （Hyperf框架可直接使用 <a href="https://github.com/kkguan/hyperf-php-accessor">Hyperf PHP Accessor</a>）

4. 项目`composer.json` 文件中配置以下信息信息

```json
{
  "scripts": {
    "php-accessor": "php-accessor generate $1"
  }
}
```

## 待补充......