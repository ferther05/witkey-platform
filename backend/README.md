# 后端开发环境与 IDEA 2021 导入说明

本后端使用 Spring Boot 3.2.5、Java 17、Spring Security 6、Jakarta JPA 和 JJWT 0.12.5。项目 SDK、Maven 运行 JDK 均应使用 JDK 17。

## IDEA 2021.2.1 的配置

打开本目录的 `pom.xml`，作为 Maven 项目导入。在「文件 → 设置 → 构建、执行、部署 → 构建工具 → Maven」中配置：

- Maven 主路径：`Bundled (Maven 3)`，本机 IDEA 2021.2.1 自带版本为 **3.6.3**。
- 用户设置文件：本机现有的 `E:\apache-maven-3.9.16\conf\settings.xml`。
- 本地仓库：`E:\apache-maven-3.9.16\repository`。
- 「正在导入 → 导入程序的 JDK」：`17`。
- 「运行程序 → JRE」：使用项目 SDK（17）。
- 「运行程序 → 将 IDE 构建/运行操作委托给 Maven」：勾选。

在「文件 → 项目结构 → 项目」中确认项目 SDK 和语言级别为 17。保存设置后点击 Maven 工具窗口的「重新加载所有 Maven 项目」，等待同步和索引完成。

以上设置文件和仓库路径是本机路径；换电脑时选择对应的本地配置。仓库目录名称包含 `3.9.16` 不影响兼容性，关键是 Maven **主程序**使用 3.6.3。本机命令行 Maven 3.9.16 可以正常构建；此次故障发生在旧 IDEA 的导入器与新版 Maven 的接口之间。

## 大量“无法解析符号”的原因

2026-11-20 排查发现，IDEA 2021.2.1 使用 Maven 3.9.16 导入本项目时，IDEA 日志出现：

```text
java.lang.NoSuchMethodError:
org.apache.maven.model.validation.DefaultModelValidator: method 'void <init>()' not found
```

Maven 模型导入失败后，模块没有加载第三方依赖，产生了 `springframework`、`JpaRepository`、`Page`、`Jwts` 等大量连锁错误；编译目录和字节码版本还残留 `${project.build.directory}`、`${java.version}`。无需逐个修改 Java 文件或降级 Spring Boot。

切换到内置 Maven 后重新导入，IDEA 自动恢复了依赖、`target/classes` 编译目录、Java 17 语言级别和字节码版本。

本机的 `WitkeyApplication` 运行配置还缺少 classpath 所属模块，现已关联 `backend` 模块，并使用 Java 参数文件缩短命令行、以项目目录作为工作目录，便于 Windows 下运行和定位 `uploads`。

## 验证记录

- JDK 17 + 命令行 Maven 3.9.16 执行 `mvn -B -ntp clean verify`：111 个 Java 文件重新编译，JAR 打包成功。
- JDK 17 + IDEA 自带 Maven 3.6.3 使用现有 settings 执行 `verify`：成功。
- IDEA 内构建（委托 Maven 的 `install --non-recursive`）：`BUILD SUCCESS`。
- IDEA「问题 → 项目错误」显示「IDE 没有发现错误」，截图涉及的 Spring Security、JPA 依赖已正常解析。
- 当前 `src/test` 下没有测试用例，Maven 构建成功不代表执行了业务自动化测试。

命令行构建可在本目录运行：

```powershell
mvn -B -ntp clean verify
```

启动前确认本机 MySQL 和 `witkey_platform` 数据库可用，之后在 IDEA 运行 `com.psc.witkey.WitkeyApplication`，或使用 JDK 17 启动 `target/witkey-server-1.0.0.jar`。后端默认端口为 8082。
