##Kepler Distributed Service Framework
- - -

###服务面临的问题及我们的解决方案
######`撸个可控的微服务`
<img src="https://raw.githubusercontent.com/KimShen/Images/master/target.png" width="100%"/>

- - -

###设计思路
#####基础结构
<img src="https://raw.githubusercontent.com/KimShen/Images/master/overview.png" width="100%"/>
* <a href="http://zookeeper.apache.org">`关于ZooKeeper`</a>
* <a href="https://www.mongodb.org/">`关于MongoDB(可选)`</a>

- - -

#####角色关系
<img src="https://raw.githubusercontent.com/KimShen/Images/master/workflow.png" width="100%"/>  
######角色说明:
  * Service:服务提供者
	* Client: 服务调用者
	* Registry: 注册中心
	* Monitor: 数据收集服务, 收集Service/Client运行时状态
	* Admin: 服务管理中心, 提供服务治理统一入口(API) 

- - -

###准备工作
  * JDK依赖
    * JDK1.7及以上

  * 构建依赖
    * <a href="http://maven.apache.org">Maven</a>  

  * 运行依赖  
    * <a href="http://spring.io">Spring</a>`我们推(强)荐(制)使用Spring托管服务组件.优质的服务需要优质的容器.`  
    * <a href="http://netty.io/">Netty</a>`网络传输`  
    * <a href="https://github.com/cglib/cglib">Cglib</a>`动态代理`  
    * Zookeeper(Client)  
    * <a href="http://hessian.caucho.com/">Hessian(内嵌)</a>/<a href="http://wiki.fasterxml.com/JacksonHome">Jackson(可选)</a>`报文解析`   
  
  * 中间件依赖
    * <a href="http://zookeeper.apache.org/doc/r3.5.1-alpha/zookeeperStarted.html">部署ZooKeeper</a>: `强制使用`
    * 部署MongoDB: 可选,仅当开启Monitor/Admin时
    
- - -

###构建第一个服务
  * 构建约定: `标准Maven目录结构`
  * <a href="https://github.com/KimShen/Kepler_Example/tree/master/start">示例下载</a>, 可用于对照以下流程
  * 目录结构: 
	  * src/main/java
		  * 示例代码
		  * kepler-service.xml
		  `服务端配置`
		  * kepler-client.xml
		  `客户端配置`
		  * kepler.conf
		  `框架配置文件`
		  * log4j.properties
		  `可选`
	  * <a href="https://github.com/KimShen/Kepler_Example/blob/master/start/pom.xml">pom.xml</a>
	  
- - -

######<a href="https://github.com/KimShen/Kepler_Example/tree/master/start/src/main/java/com/kepler/demo/start/QuickStart.java">定义服务接口</a>

```
package com.kepler.demo.start;
import com.kepler.annotation.Service;
@Service(version = "0.0.1")
public interface QuickStart {		
    public HelloWorld hi(String name);		
}
```
`@Service(version = "0.0.1")标记该接口为服务接口,版本为0.0.1`  

######<a href="https://github.com/KimShen/Kepler_Example/tree/master/start/src/main/java/com/kepler/demo/start/HelloWorld.java">定义数据对象</a>
```
package com.kepler.demo.start;
public interface HelloWorld {  			
    public String hello();  		
}
``` 

######<a href="https://github.com/KimShen/Kepler_Example/tree/master/start/src/main/java/com/kepler/demo/start/impl/QuickStartImpl.java">定义服务实现</a>
```
package com.kepler.demo.start.impl;
import com.kepler.annotation.Service;
import com.kepler.demo.start.HelloWorld;
import com.kepler.demo.start.QuickStart;
@Autowired
public class QuickStartImpl implements QuickStart {
    public HelloWorld hi(String name) {	
        return new HelloWorldImpl(name);
    }
}
```
`@Autowired表示将自动发布服务`

######<a href="https://github.com/KimShen/Kepler_Example/tree/master/start/src/main/java/com/kepler/demo/start/impl/HelloWorldImpl.java">定义数据对象实现</a>
```
package com.kepler.demo.start.impl;
import java.io.Serializable;
import com.kepler.demo.start.HelloWorld;
public class HelloWorldImpl implements Serializable, HelloWorld {
    private static final long serialVersionUID = 1L;
    private String name;
    public HelloWorldImpl(String name) {
        super();
        this.name = name;
    }
    
    public String hello() {
        return "Hello " + this.name;
    }
}
```

######<a href="https://github.com/KimShen/Kepler_Example/tree/master/start/src/main/java/com/kepler/demo/main/Server.java">定义服务端启动类</a>
```
package com.kepler.demo.main;
import org.springframework.context.support.ClassPathXmlApplicationContext;
public class Server {
    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("kepler-service.xml");
        context.start();
        System.in.read();
        context.close();
    }
}
```

######<a href="https://github.com/KimShen/Kepler_Example/tree/master/start/src/main/java/com/kepler/demo/main/Client.java">定义客户端启动类</a>
```
package com.kepler.demo.main;
import junit.framework.Assert;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.kepler.demo.start.QuickStart;
public class Client {
    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("kepler-client.xml");
        QuickStart start = context.getBean(QuickStart.class);
        Assert.assertEquals("Hello Kepler", start.hi("Kepler").hello());
        context.close();
    }
}
```

######<a href="https://github.com/KimShen/Kepler_Example/tree/master/start/src/main/java/kepler-service.xml">定义服务端配置(kepler-service.xml)</a>
```	
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans" 
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:dubbo="http://code.alibabatech.com/schema/dubbo" 
    xsi:schemaLocation="http://www.springframework.org/schema/beans        
    http://www.springframework.org/schema/beans/spring-beans.xsd        
    http://code.alibabatech.com/schema/dubbo        
    http://code.alibabatech.com/schema/dubbo/dubbo.xsd">
	
    <import resource="classpath:kepler-server.xml" />
    <bean class="com.kepler.demo.start.impl.QuickStartImpl" />

</beans>
```

######<a href="https://github.com/KimShen/Kepler_Example/tree/master/start/src/main/java/kepler-client.xml">定义客户端配置(kepler-client.xml)</a>
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans" 
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:dubbo="http://code.alibabatech.com/schema/dubbo" 
    xsi:schemaLocation="http://www.springframework.org/schema/beans        
    http://www.springframework.org/schema/beans/spring-beans.xsd        
    http://code.alibabatech.com/schema/dubbo        
    http://code.alibabatech.com/schema/dubbo/dubbo.xsd">
	
    <import resource="classpath:kepler-core.xml" />
    <bean class="com.kepler.service.imported.ImportedServiceFactory" parent="kepler.service.imported.abstract">
      <constructor-arg index="0" value="com.kepler.demo.start.QuickStart" />
    </bean>
	
</beans>
```

######<a href="https://github.com/KimShen/Kepler_Example/tree/master/start/src/main/java/kepler.conf">kepler.conf</a>(需修改为实际ZooKeeper地址)
```
com.kepler.zookeeper.zkfactory.host=127.0.0.1:2781			
```

######<a href="https://github.com/KimShen/Kepler_Example/tree/master/start/src/main/java/kepler-client.xml">log4j.properties(可选)</a>
```
log4j.rootLogger=INFO,CONSOLE
log4j.appender.CONSOLE=org.apache.log4j.ConsoleAppender
log4j.appender.CONSOLE.layout=org.apache.log4j.PatternLayout
log4j.appender.CONSOLE.layout.ConversionPattern=%d{yyyy-MM-dd HH\:mm\:ss,SSS} %5p{%F\:%L}-%m%n  		
```	

###如何从Dubbo迁移
* <a href="http://dubbo.io/User+Guide-zh.htm#UserGuide-zh-%E5%BF%AB%E9%80%9F%E5%90%AF%E5%8A%A8">关于Dubbo</a>
* <a href="https://github.com/KimShen/Kepler_Example/tree/master/migrate">示例下载</a>, 可用于对照以下流程
* 目录结构: 
	* src/main/java
		* <a href="https://github.com/KimShen/Kepler_Example/tree/master/migrate/src/main/java/com/alibaba/dubbo/demo">com.alibaba.dubbo.demo.provider</a>
		`迁移服务`
		* <a href="https://github.com/KimShen/Kepler_Example/tree/master/migrate/src/main/java/main/dubbo">main.dubbo</a>
		`Dubbo启动入口`
		* <a href="https://github.com/KimShen/Kepler_Example/tree/master/migrate/src/main/java/main/kepler">main.kepler</a>
		`Kepler启动入口`
		* <a href="https://github.com/KimShen/Kepler_Example/tree/master/migrate/src/main/java/dubbo-provider.xml">dubbo-provider.xml</a>
		`Dubbo服务端配置`
		* <a href="https://github.com/KimShen/Kepler_Example/tree/master/migrate/src/main/java/dubbo-consumer.xml">dubbo-consumer.xml</a>
		`Dubbo客户端配置`
		* <a href="https://github.com/KimShen/Kepler_Example/tree/master/migrate/src/main/java/kepler-service.xml">kepler-service.xml</a>:`Kepler服务端配置`
		* <a href="https://github.com/KimShen/Kepler_Example/tree/master/migrate/src/main/java/kepler-client.xml">kepler-client.xml</a>:`Kepler客户端配置`
		* <a href="https://github.com/KimShen/Kepler_Example/tree/master/migrate/src/main/java/kepler.conf">kepler.conf</a>:`框架配置文件`
		* <a href="https://github.com/KimShen/Kepler_Example/tree/master/migrate/src/main/java/log4j.properties">log4j.properties</a>:`可选`
		* <a href="https://github.com/KimShen/Kepler_Example/tree/master/migrate/pom.xml">pom.xml</a>
* 推荐开启`-XX:+PrintGCDetails`

- - -
##更多功能
