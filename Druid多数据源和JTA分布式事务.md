Druid分布式事务的数据源: com.alibaba.druid.pool.xa.DruidXADataSource

一、方法动态切换数据源

首先引入Atomikos的maven依赖:

```xml
		<dependency>
			<groupId>javax.transaction</groupId>
			<artifactId>jta</artifactId>
			<version>1.1</version>
		</dependency>
		
		<dependency>
			<groupId>com.atomikos</groupId>
			<artifactId>transactions-api</artifactId>
			<version>3.9.3</version>
		</dependency>
		
		<dependency>
			<groupId>com.atomikos</groupId>
			<artifactId>atomikos-util</artifactId>
			<version>3.9.3</version>
		</dependency>
		
		<dependency>
			<groupId>com.atomikos</groupId>
			<artifactId>transactions</artifactId>
			<version>3.9.3</version>
		</dependency>

		<dependency>
			<groupId>com.atomikos</groupId>
			<artifactId>transactions-jta</artifactId>
			<version>3.9.3</version>
		</dependency>
		
		<dependency>
			<groupId>com.atomikos</groupId>
			<artifactId>transactions-jdbc</artifactId>
			<version>3.9.3</version>
		</dependency>
```

1.创建动态切换数据源的自定义注解:

```java
/**
 * 注解式数据源，用来进行数据源切换
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ChooseDataSource {
    String value() default "";
}
```

2.创建对应的切面类:

```java
/**
 * 类描述：完成数据源的切换，抽类切面，具体项目继承一下，不需要重写即可使用
 */
@Aspect
public class ChooseDataSourceAspect {

    protected static final ThreadLocal<String> preDatasourceHolder = new ThreadLocal<String>();

    /**
     * 提取出公用的方法
     */
    @Pointcut("@annotation(com.amos.spring.annotation.ChooseDataSource)")
    public void methodWithChooseAnnotation() {

    }

    /**
     * 根据@ChooseDataSource的属性值设置不同的dataSourceKey,以供DynamicDataSource
     */
    @Before("methodWithChooseAnnotation()")
    public void changeDataSourceBeforeMethodExecution(JoinPoint jp) {
        //拿到anotation中配置的数据源
        String resultDS = determineDatasource(jp);
        //没有配置实用默认数据源
        if (resultDS == null) {
            DataSourceKeyHolder.setDataSourceKey(null);
            return;
        }
        preDatasourceHolder.set(DataSourceKeyHolder.getDataSourceKey());
        //将数据源设置到数据源持有者
        DataSourceKeyHolder.setDataSourceKey(resultDS);
    }

    /**
     * 如果需要修改获取数据源的逻辑，请重写此方法
     */
    @SuppressWarnings("rawtypes")
    protected String determineDatasource(JoinPoint jp) {
        String methodName = jp.getSignature().getName();
        Class targetClass = jp.getSignature().getDeclaringType();
        String dataSourceForTargetClass = resolveDataSourceFromClass(targetClass);
        String dataSourceForTargetMethod = resolveDataSourceFromMethod(
                targetClass, methodName);
        String resultDS = determinateDataSource(dataSourceForTargetClass,
                dataSourceForTargetMethod);
        return resultDS;
    }

    /**
     * 方法执行完毕以后，数据源切换回之前的数据源。
     * 比如foo()方法里面调用bar()，但是bar()另外一个数据源，
     * bar()执行时，切换到自己数据源，执行完以后，要切换到foo()所需要的数据源，以供
     * foo()继续执行。
     */
    @After("methodWithChooseAnnotation()")
    public void restoreDataSourceAfterMethodExecution() {
        DataSourceKeyHolder.setDataSourceKey(preDatasourceHolder.get());
        preDatasourceHolder.remove();
    }


    /**
     * @param targetClass
     * @param methodName
     * @return
     */
    @SuppressWarnings("rawtypes")
    private String resolveDataSourceFromMethod(Class targetClass,
                                               String methodName) {
        Method m = ReflectUtil.findUniqueMethod(targetClass, methodName);
        if (m != null) {
            ChooseDataSource choDs = m.getAnnotation(ChooseDataSource.class);
            return resolveDataSourcename(choDs);
        }
        return null;
    }

    /**
     * 最终数据源，如果方法上设置有数据源，则以方法上的为准，如果方法上没有设置，则以类上的为准，如果类上没有设置，则使用默认数据源</li>
     * @param classDS
     * @param methodDS
     * @return
     */
    private String determinateDataSource(String classDS, String methodDS) {
        return methodDS == null ? classDS : methodDS;
    }

    /**
     * @param targetClass
     * @return
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private String resolveDataSourceFromClass(Class targetClass) {
        ChooseDataSource classAnnotation = (ChooseDataSource) targetClass
                .getAnnotation(ChooseDataSource.class);
        // 直接为整个类进行设置
        return null != classAnnotation ? resolveDataSourcename(classAnnotation)
                : null;
    }

    /**
     * 组装DataSource的名字
     * @param ds
     * @return
     */
    private String resolveDataSourcename(ChooseDataSource ds) {
        return ds == null ? null : ds.value();
    }

}
```

3.创建自定义数据源配置类:

```java
/**
 * 自定义数据源配置
 */
public class CustomerDatasource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        return DataSourceKeyHolder.getDataSourceKey();
    }
}
```

4.工具类:

```java
/**
 * 将数据源的键存入ThreadLocal中
 */
public class DataSourceKeyHolder {
    private static final ThreadLocal<String> contextHolder = new ThreadLocal<String>();
    private static Logger logger = LoggerFactory.getLogger(DataSourceKeyHolder.class);

    public static void setDataSourceKey(String dataSourceKey) {
        contextHolder.set(dataSourceKey);
    }

    public static String getDataSourceKey() {
        String key = contextHolder.get();
        logger.info("Thread:"+Thread.currentThread().getName()+"dataSource key is "+ key);
        return key;
    }

    public static void clearDataSourceKey() {
        contextHolder.remove();
    }
}
```

```java
public class ReflectUtil {

	/**
	 * 方法描述 : 寻找方法名唯 一的方法
	 * @param clazz
	 * @param name
	 * @return
	 */
	public static Method findUniqueMethod(Class<?> clazz, String name) {
		Assert.notNull(clazz, "Class must not be null");
		Assert.notNull(name, "Method name must not be null");
		Class<?> searchType = clazz;
		while (searchType != null) {
			Method[] methods = (searchType.isInterface() ? searchType.getMethods() : searchType.getDeclaredMethods());
			for (Method method : methods) {
				if (name.equals(method.getName())) {
					return method;
				}
			}
			searchType = searchType.getSuperclass();
		}
		return null;
	}
}
```

5.自定义sqlSessionTemplate模版:

```java
/**
 * TODO: 增加描述
 */
public class CustomSqlSessionTemplate extends SqlSessionTemplate {
	 
    private final SqlSessionFactory sqlSessionFactory;
    private final ExecutorType executorType;
    private final SqlSession sqlSessionProxy;
    private final PersistenceExceptionTranslator exceptionTranslator;
 
    private Map<Object, SqlSessionFactory> targetSqlSessionFactorys;
    private SqlSessionFactory defaultTargetSqlSessionFactory;
 
    public void setTargetSqlSessionFactorys(Map<Object, SqlSessionFactory> targetSqlSessionFactorys) {
        this.targetSqlSessionFactorys = targetSqlSessionFactorys;
    }
 
    public void setDefaultTargetSqlSessionFactory(SqlSessionFactory defaultTargetSqlSessionFactory) {
        this.defaultTargetSqlSessionFactory = defaultTargetSqlSessionFactory;
    }
 
    public CustomSqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        this(sqlSessionFactory, sqlSessionFactory.getConfiguration().getDefaultExecutorType());
    }
 
    public CustomSqlSessionTemplate(SqlSessionFactory sqlSessionFactory, ExecutorType executorType) {
        this(sqlSessionFactory, executorType, new MyBatisExceptionTranslator(sqlSessionFactory.getConfiguration()
                .getEnvironment().getDataSource(), true));
    }
 
    public CustomSqlSessionTemplate(SqlSessionFactory sqlSessionFactory, ExecutorType executorType,
            PersistenceExceptionTranslator exceptionTranslator) {
 
        super(sqlSessionFactory, executorType, exceptionTranslator);
 
        this.sqlSessionFactory = sqlSessionFactory;
        this.executorType = executorType;
        this.exceptionTranslator = exceptionTranslator;
        
        this.sqlSessionProxy = (SqlSession) newProxyInstance(
                SqlSessionFactory.class.getClassLoader(),
                new Class[] { SqlSession.class }, 
                new SqlSessionInterceptor());
 
        this.defaultTargetSqlSessionFactory = sqlSessionFactory;
    }
 
    @Override
    public SqlSessionFactory getSqlSessionFactory() {
 
        SqlSessionFactory targetSqlSessionFactory = targetSqlSessionFactorys.get(DataSourceKeyHolder.getDataSourceKey());
        if (targetSqlSessionFactory != null) {
            return targetSqlSessionFactory;
        } else if (defaultTargetSqlSessionFactory != null) {
            return defaultTargetSqlSessionFactory;
        } else {
            Assert.notNull(targetSqlSessionFactorys, "Property 'targetSqlSessionFactorys' or 'defaultTargetSqlSessionFactory' are required");
            Assert.notNull(defaultTargetSqlSessionFactory, "Property 'defaultTargetSqlSessionFactory' or 'targetSqlSessionFactorys' are required");
        }
        return this.sqlSessionFactory;
    }
 
    @Override
    public Configuration getConfiguration() {
        return this.getSqlSessionFactory().getConfiguration();
    }
 
    public ExecutorType getExecutorType() {
        return this.executorType;
    }
 
    public PersistenceExceptionTranslator getPersistenceExceptionTranslator() {
        return this.exceptionTranslator;
    }
 
    /**
     * {@inheritDoc}
     */
    public <T> T selectOne(String statement) {
        return this.sqlSessionProxy.<T> selectOne(statement);
    }
 
    /**
     * {@inheritDoc}
     */
    public <T> T selectOne(String statement, Object parameter) {
        return this.sqlSessionProxy.<T> selectOne(statement, parameter);
    }
 
    /**
     * {@inheritDoc}
     */
    public <K, V> Map<K, V> selectMap(String statement, String mapKey) {
        return this.sqlSessionProxy.<K, V> selectMap(statement, mapKey);
    }
 
    /**
     * {@inheritDoc}
     */
    public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey) {
        return this.sqlSessionProxy.<K, V> selectMap(statement, parameter, mapKey);
    }
 
    /**
     * {@inheritDoc}
     */
    public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey, RowBounds rowBounds) {
        return this.sqlSessionProxy.<K, V> selectMap(statement, parameter, mapKey, rowBounds);
    }
 
    /**
     * {@inheritDoc}
     */
    public <E> List<E> selectList(String statement) {
        return this.sqlSessionProxy.<E> selectList(statement);
    }
 
    /**
     * {@inheritDoc}
     */
    public <E> List<E> selectList(String statement, Object parameter) {
        return this.sqlSessionProxy.<E> selectList(statement, parameter);
    }
 
    /**
     * {@inheritDoc}
     */
    public <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
        return this.sqlSessionProxy.<E> selectList(statement, parameter, rowBounds);
    }
 
    /**
     * {@inheritDoc}
     */
    public void select(String statement, ResultHandler handler) {
        this.sqlSessionProxy.select(statement, handler);
    }
 
    /**
     * {@inheritDoc}
     */
    public void select(String statement, Object parameter, ResultHandler handler) {
        this.sqlSessionProxy.select(statement, parameter, handler);
    }
 
    /**
     * {@inheritDoc}
     */
    public void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler) {
        this.sqlSessionProxy.select(statement, parameter, rowBounds, handler);
    }
 
    /**
     * {@inheritDoc}
     */
    public int insert(String statement) {
        return this.sqlSessionProxy.insert(statement);
    }
 
    /**
     * {@inheritDoc}
     */
    public int insert(String statement, Object parameter) {
        return this.sqlSessionProxy.insert(statement, parameter);
    }
 
    /**
     * {@inheritDoc}
     */
    public int update(String statement) {
        return this.sqlSessionProxy.update(statement);
    }
 
    /**
     * {@inheritDoc}
     */
    public int update(String statement, Object parameter) {
        return this.sqlSessionProxy.update(statement, parameter);
    }
 
    /**
     * {@inheritDoc}
     */
    public int delete(String statement) {
        return this.sqlSessionProxy.delete(statement);
    }
 
    /**
     * {@inheritDoc}
     */
    public int delete(String statement, Object parameter) {
        return this.sqlSessionProxy.delete(statement, parameter);
    }
 
    /**
     * {@inheritDoc}
     */
    public <T> T getMapper(Class<T> type) {
        return getConfiguration().getMapper(type, this);
    }
 
    /**
     * {@inheritDoc}
     */
    public void commit() {
        throw new UnsupportedOperationException("Manual commit is not allowed over a Spring managed SqlSession");
    }
 
    /**
     * {@inheritDoc}
     */
    public void commit(boolean force) {
        throw new UnsupportedOperationException("Manual commit is not allowed over a Spring managed SqlSession");
    }
 
    /**
     * {@inheritDoc}
     */
    public void rollback() {
        throw new UnsupportedOperationException("Manual rollback is not allowed over a Spring managed SqlSession");
    }
 
    /**
     * {@inheritDoc}
     */
    public void rollback(boolean force) {
        throw new UnsupportedOperationException("Manual rollback is not allowed over a Spring managed SqlSession");
    }
 
    /**
     * {@inheritDoc}
     */
    public void close() {
        throw new UnsupportedOperationException("Manual close is not allowed over a Spring managed SqlSession");
    }
 
    /**
     * {@inheritDoc}
     */
    public void clearCache() {
        this.sqlSessionProxy.clearCache();
    }
 
    /**
     * {@inheritDoc}
     */
    public Connection getConnection() {
        return this.sqlSessionProxy.getConnection();
    }
 
    /**
     * {@inheritDoc}
     * @since 1.0.2
     */
    public List<BatchResult> flushStatements() {
        return this.sqlSessionProxy.flushStatements();
    }
 
    /**
     * Proxy needed to route MyBatis method calls to the proper SqlSession got from Spring's Transaction Manager It also
     * unwraps exceptions thrown by {@code Method#invoke(Object, Object...)} to pass a {@code PersistenceException} to
     * the {@code PersistenceExceptionTranslator}.
     */
    private class SqlSessionInterceptor implements InvocationHandler {
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            final SqlSession sqlSession = getSqlSession(
                    CustomSqlSessionTemplate.this.getSqlSessionFactory(),
                    CustomSqlSessionTemplate.this.executorType, 
                    CustomSqlSessionTemplate.this.exceptionTranslator);
            try {
                Object result = method.invoke(sqlSession, args);
                if (!isSqlSessionTransactional(sqlSession, CustomSqlSessionTemplate.this.getSqlSessionFactory())) {
                    // force commit even on non-dirty sessions because some databases require
                    // a commit/rollback before calling close()
                    sqlSession.commit(true);
                }
                return result;
            } catch (Throwable t) {
                Throwable unwrapped = unwrapThrowable(t);
                if (CustomSqlSessionTemplate.this.exceptionTranslator != null && unwrapped instanceof PersistenceException) {
                    Throwable translated = CustomSqlSessionTemplate.this.exceptionTranslator
                        .translateExceptionIfPossible((PersistenceException) unwrapped);
                    if (translated != null) {
                        unwrapped = translated;
                    }
                }
                throw unwrapped;
            } finally {
                closeSqlSession(sqlSession, CustomSqlSessionTemplate.this.getSqlSessionFactory());
            }
        }
    }
 
}
```

6.**最重要的是配置文件的配置**:

```xml
<?xml version="1.0" encoding="UTF-8"?>

<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd
		http://www.springframework.org/schema/tx  
        http://www.springframework.org/schema/tx/spring-tx.xsd 
        http://www.springframework.org/schema/aop
        http://www.springframework.org/schema/aop/spring-aop.xsd">

    <!-- 启动扫描 -->
    <context:component-scan base-package="com.amos.spring">
        <!-- <context:exclude-filter type="annotation" expression="org.springframework.stereotype.Controller" /> -->
    </context:component-scan>

    <!-- 启用CGliB -->
    <aop:aspectj-autoproxy  />


    <bean class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
        <property name="locations">
            <list>
                <value>classpath*:jdbc.properties</value>
            </list>
        </property>
    </bean>

	<!-- 两个数据源的公用配置，方便下面直接引用 -->
     <bean id="abstractXADataSource" class="com.atomikos.jdbc.AtomikosDataSourceBean" init-method="init"
             destroy-method="close" abstract="true">
        <property name="xaDataSourceClassName" value="com.mysql.jdbc.jdbc2.optional.MysqlXADataSource"/>
        <property name="poolSize" value="10" />
        <property name="minPoolSize" value="10"/>
        <property name="maxPoolSize" value="30"/>
        <property name="borrowConnectionTimeout" value="60"/>
        <property name="reapTimeout" value="20"/>
        <!-- 最大空闲时间 -->
        <property name="maxIdleTime" value="60"/>
        <property name="maintenanceInterval" value="60" />
        <property name="loginTimeout" value="60"/>
        <property name="logWriter" value="60"/>
        <property name="testQuery">
            <value>select 1</value>
        </property>
        
    </bean> 
    
    <!-- 配置第一个数据源 -->
    <bean id="dataSource_a" parent="abstractXADataSource">
    	<!-- value只要两个数据源不同就行，随便取名 -->
        <property name="uniqueResourceName" value="mysql/sitestone" />
        <property name="xaDataSourceClassName"
            value="com.mysql.jdbc.jdbc2.optional.MysqlXADataSource" />
        <property name="xaProperties">
            <props>
                <prop key="URL">${jdbc.url.spider}</prop>
                <prop key="user">${jdbc.username}</prop>
                <prop key="password">${jdbc.password}</prop>
            </props>
        </property>
    </bean>

    <!-- 配置第二个数据源-->
    <bean id="dataSource_b" parent="abstractXADataSource">
		<!-- value只要两个数据源不同就行，随便取名 -->
        <property name="uniqueResourceName" value="mysql/sitesttwo" />
        <property name="xaDataSourceClassName"
            value="com.mysql.jdbc.jdbc2.optional.MysqlXADataSource" />
        <property name="xaProperties">
            <props>
               <prop key="URL">${jdbc_tb.url.spider}</prop>
                <prop key="user">${jdbc_tb.username}</prop>
                <prop key="password">${jdbc_tb.password}</prop>
            </props>
        </property>
    </bean>
  

    <bean name="dynamicDatasource" class="com.amos.spring.datasource.CustomerDatasource">
        <property name="targetDataSources">
            <map>
            	<!-- key表示方法中需要引用的名称  -->
                <entry key="ds_1" value-ref="dataSource_a"/>
                <entry key="ds_2" value-ref="dataSource_b"/>
            </map>
        </property>
        <property name="defaultTargetDataSource" ref="dataSource_a"    />
    </bean>
    
    
    <!-- 为业务逻辑层的方法解析@DataSource注解  为当前线程的routeholder注入数据源key -->
    
    <bean id="sqlSessionFactorya" class="org.mybatis.spring.SqlSessionFactoryBean">
        <property name="dataSource" ref="dataSource_a"/>
         <property name="typeAliasesPackage" value="com.amos.spring.dschange.bean" />
         <!-- mapper和resultmap配置路径 --> 
        <property name="mapperLocations">
            <list>
                <!-- 表示在com.amos目录下的任意包下的resultmap包目录中，以-resultmap.xml或-mapper.xml结尾所有文件 --> 
                <value>classpath:com/amos/spring/dschange/mapper/ShopMapper.xml</value>
            </list>
        </property>
    </bean>
    
    <bean id="sqlSessionFactoryb" class="org.mybatis.spring.SqlSessionFactoryBean">
        <property name="dataSource" ref="dataSource_b"/>
         <property name="typeAliasesPackage" value="com.amos.spring.dschange.bean" />
          <!-- mapper和resultmap配置路径 --> 
        <property name="mapperLocations">
            <list>
                <!-- 表示在com.amos目录下的任意包下的resultmap包目录中，以-resultmap.xml或-mapper.xml结尾所有文件 --> 
                <value>classpath:com/amos/spring/dschange/mapper/ShopMapper.xml</value>
            </list>
        </property>
    </bean>
    
    <!-- 配置自定义的SqlSessionTemplate模板，注入相关配置 -->
    <bean id="sqlSessionTemplate" class="com.amos.spring.mybatis.CustomSqlSessionTemplate" scope="prototype">
        <constructor-arg ref="sqlSessionFactorya" />
        <property name="targetSqlSessionFactorys">
            <map>     
                <entry value-ref="sqlSessionFactorya" key="ds_1"/>
                <entry value-ref="sqlSessionFactoryb" key="ds_2"/>
            </map> 
        </property>
    </bean>

    <bean id="mapperScannerConfigurer" class="org.mybatis.spring.mapper.MapperScannerConfigurer">
        <property name="basePackage" value="com.amos.spring.dschange.mapper" />
        <property name="sqlSessionTemplateBeanName" value="sqlSessionTemplate"/>
        <property name="markerInterface" value="com.amos.spring.dschange.mapper.SqlMapper"/>
    </bean>
    
    <!-- JTA配置开始 -->
    <bean id="atomikosTransactionManager" class="com.atomikos.icatch.jta.UserTransactionManager"
        init-method="init" destroy-method="close">
        <property name="forceShutdown">
            <value>true</value>
        </property>
    </bean>
    <bean id="atomikosUserTransaction" class="com.atomikos.icatch.jta.UserTransactionImp">
        <property name="transactionTimeout" value="300" />
    </bean>
    <bean id="springTransactionManager"
        class="org.springframework.transaction.jta.JtaTransactionManager">
        <property name="transactionManager">
            <ref bean="atomikosTransactionManager" />
        </property>
        <property name="userTransaction">
            <ref bean="atomikosUserTransaction" />
        </property>
        <!-- 必须设置，否则程序出现异常 JtaTransactionManager does not support custom isolation levels by default -->
        <property name="allowCustomIsolationLevels" value="true" />
    </bean>
    <!-- JTA配置结束 -->
    
    <!-- 配置事务管理 -->
 	<tx:annotation-driven transaction-manager="springTransactionManager" proxy-target-class="true" />
 	
</beans>
```

参考博客地址:

http://www.cnblogs.com/LiQ0116/p/7027049.html

https://www.jianshu.com/p/c36451774b65

https://blog.csdn.net/u013310119/article/details/84280930