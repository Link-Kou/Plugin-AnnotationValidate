# Plugin-AnnotationValidate

### Plugin-AnnotationValidate 能做什么？

> 基于Plugin-Spring实现以下功能
- 基于Hibernate-Validator实现基于注解

---
### 使用环境

    JAVA    >  1.8
    Maven   >  3.X
    
### Maven仓库
    
```xml

<dependency>
  <groupId>com.github.link-kou</groupId>
  <artifactId>annotation-validate</artifactId>
  <version>1.0.2</version>
</dependency>

```


### 示列

```java

     /**
      * 用于方法上面。校验输入参数是否合理
      */
     @Validated
     @Override
     public Boolean roleMeunsList(@NotBlank String id) {
         
     }   

    /**
      * 用于方法字段上面。校验输入参数是否合理
      */
     @Override
     public Boolean roleMeunsList(@Validated User user) {
         
     }  

```

