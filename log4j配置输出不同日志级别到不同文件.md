```wiki
log4j配置输出不同级别的日志到不同的文件中：
DEBUG级别写到debug对应的文件中
INFO级别写到info对应的文件中
WARN级别写到warn对应的文件中
ERROR级别写到error对应的文件中
```

```java
log4j.properties配置文件信息如下：

log4j.rootLogger=DEBUG,CONSOLE,AllFile,DebugFile,InfoFile,WarnFile,ErrorFile
log4j.addivity.org.apache=false

log4j.appender.CONSOLE=org.apache.log4j.ConsoleAppender
log4j.appender.CONSOLE.Threshold=DEBUG
log4j.appender.CONSOLE.layout.ConversionPattern=%d{yyyy-MM-dd HH\:mm\:ss} %5p %c{1}\:[%L] \: %m%n
log4j.appender.CONSOLE.Target=System.out
log4j.appender.CONSOLE.Encoding=UTF-8
log4j.appender.CONSOLE.layout=org.apache.log4j.PatternLayout

log4j.appender.AllFile=org.apache.log4j.DailyRollingFileAppender 
log4j.appender.AllFile.File=logs/AllLevel/record
log4j.appender.AllFile.DatePattern='_'yyyy-MM-dd'.log'
log4j.appender.AllFile.layout=org.apache.log4j.PatternLayout  
log4j.appender.AllFile.layout.ConversionPattern=%d{yyyy-MM-dd HH\:mm\:ss} %5p %c{1}\:[%L] \: %m%n

log4j.appender.DebugFile=com.sunkaisens.nms.log4j.CustomizeDailyRollingFileAppender
log4j.appender.DebugFile.File=logs/DebugLevel/debug
log4j.appender.DebugFile.Threshold=DEBUG
log4j.appender.DebugFile.DatePattern='.'yyyy-MM-dd'.log'
log4j.appender.DebugFile.layout=org.apache.log4j.PatternLayout
log4j.appender.DebugFile.layout.ConversionPattern=%d{yyyy-MM-dd HH\:mm\:ss} %5p %c{1}\:[%L] \: %m%n

log4j.appender.InfoFile=com.sunkaisens.nms.log4j.CustomizeDailyRollingFileAppender
log4j.appender.InfoFile.File=logs/InfoLevel/info
log4j.appender.InfoFile.Threshold=INFO
log4j.appender.InfoFile.DatePattern='.'yyyy-MM-dd'.log'
log4j.appender.InfoFile.layout=org.apache.log4j.PatternLayout
log4j.appender.InfoFile.layout.ConversionPattern=%d{yyyy-MM-dd HH\:mm\:ss} %5p %c{1}\:[%L] \: %m%n

log4j.appender.WarnFile=com.sunkaisens.nms.log4j.CustomizeDailyRollingFileAppender
log4j.appender.WarnFile.File=logs/WarnLevel/warn
log4j.appender.WarnFile.Threshold=WARN
log4j.appender.WarnFile.DatePattern='.'yyyy-MM-dd'.log'
log4j.appender.WarnFile.layout=org.apache.log4j.PatternLayout
log4j.appender.WarnFile.layout.ConversionPattern=%d{yyyy-MM-dd HH\:mm\:ss} %5p %c{1}\:[%L] \: %m%n

log4j.appender.ErrorFile=com.sunkaisens.nms.log4j.CustomizeDailyRollingFileAppender
log4j.appender.ErrorFile.File=logs/ErrorLevel/error
log4j.appender.ErrorFile.Threshold=ERROR
log4j.appender.ErrorFile.DatePattern='.'yyyy-MM-dd'.log'
log4j.appender.ErrorFile.layout=org.apache.log4j.PatternLayout
log4j.appender.ErrorFile.layout.ConversionPattern=%d{yyyy-MM-dd HH\:mm\:ss} %5p %c{1}\:[%L] \: %m%n

com.sunkaisens.nms.log4j.CustomizeDailyRollingFileAppender是自定义的类:

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Priority;

public class CustomizeDailyRollingFileAppender extends DailyRollingFileAppender {
	
	@Override  
    public boolean isAsSevereAsThreshold(Priority priority) {  
        return this.getThreshold().equals(priority);  
    }
}
通过这个类判断级别是否相等

然后在需要打印日志的地方声明:
import org.apache.log4j.Logger; 导入的包
private static Logger logger = Logger.getLogger(MBServer.class);

```

