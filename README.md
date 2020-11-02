# 简介


#### Plugin-AnnotationValidate 能做什么？

AnnotationValidate基于Hibernate-Validator实现基于注解简化相关的代码校验

+ 一个注解完成方法参数上校验
+ 一个注解完成方法独立或特点参数校验

# 背景

 日常开发中基于Spring框架中的校验，在Controller提供了@Validated来完成入参校验工作。除了在Controller之外,
 包括未在Spring框架中或被RPC框架调用的时候也能实现与@Validated一样的校验。通常解决方案
 + AOP方式，这种方式只能应用于Spring中,如果是只是一个方法入参校验,就无法得到很好的应用
 + 方法入参数校验,通常的方案就是IF代码判断。
 + 通过类似guava包中的Preconditions做checkNotNull之类判断。
 + 直接使用HibernateValidator,校验一个对象，代码还是比较简单的。如果校验一个方法上的入参数还是有点繁琐。
 通过封装也能简化。当是还是有点不够简洁
 
 无论哪一种方案都可以实现,实现上面简化不简化的问题。基于上面的考量,封装了校验工具AnnotationValidate,
 只需要通过一个注解@Validated（com.linkkou.annotationvalidate包下）就可以完成方法上的参数校验
 
 
---
### 使用环境
    JAVA    >  1.8
    Maven   >  3.X
      
### Maven仓库
```xml
<dependency>
  <groupId>com.github.link-kou</groupId>
  <artifactId>annotation-validate</artifactId>
  <version>1.0.3</version>
</dependency>
```


### 示列代码

```java

import com.linkkou.annotationvalidate.Validated;
import javax.validation.constraints.NotBlank;

/**
 * 测试
 */
public class Test {

    /**
     * 用于方法上面。校验输入参数是否合理
     */
    @Validated
    public Boolean roleMeunsList(@NotBlank String id) {
        return true;
    }

    /**
     * 用于方法字段上面。校验输入参数是否合理
     */
    public Boolean roleMeunsList(@Validated User user) {
        return true;
    }

    public static class User {
        @NotBlank String id;
    }

}

```

# 原理

 JSR 269: Pluggable Annotation Processing API (https://www.jcp.org/en/jsr/detail?id=269) 。
 我们可以在Javac的编译期利用注解做这些事情。
 在Javac解析成抽象语法树之后(AST), AnnotationValidate根据自己的注解处理器，动态的修改 AST，增加新的节点(所谓代码)，最终通过分析和生成字节码。
 从而实现代码简化工作

#注意事项

 + 编译不生效，先查看一下是否开启了注解编译。无论是IDEA、Eclipse都必须开启注解编译,关于如何开启自行搜索。
 