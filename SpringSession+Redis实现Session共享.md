1.首先是pom.xml文件的maven依赖:

```xml
		<dependency>
			<groupId>junit</groupId>
			<artifactId>junit</artifactId>
			<version>3.8.1</version>
			<scope>test</scope>
		</dependency>
		<dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>javax.servlet-api</artifactId>
            <version>3.0.1</version>
            <scope>provided</scope>
        </dependency>
		session和Redis的依赖
		<dependency>
            <groupId>org.springframework.session</groupId>
            <artifactId>spring-session-data-redis</artifactId>
            <version>1.3.1.RELEASE</version>
            <type>pom</type>
        </dependency>
       <dependency>
            <groupId>biz.paluch.redis</groupId>
            <artifactId>lettuce</artifactId>
            <version>3.5.0.Final</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-web</artifactId>
            <version>4.3.4.RELEASE</version>
        </dependency>
```

2.在web.xml文件中配置:

```xml
<context-param>
		<param-name>contextConfigLocation</param-name>
		<param-value>classpath*:spring/*xml</param-value>
	</context-param>

	<listener>
		<listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
	</listener>

	<!--DelegatingFilterProxy将查找一个Bean的名字springSessionRepositoryFilter丢给一个过滤器。为每个请求 
		调用DelegatingFilterProxy, springSessionRepositoryFilter将被调用 -->
	<filter>
		<filter-name>springSessionRepositoryFilter</filter-name>
		<filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
	</filter>

	<filter-mapping>
		<filter-name>springSessionRepositoryFilter</filter-name>
		<url-pattern>/*</url-pattern>
		<dispatcher>REQUEST</dispatcher>
		<dispatcher>ERROR</dispatcher>
	</filter-mapping>
```

3.配置application-session.xml文件:

```xml
<context:annotation-config />

	<!--创建一个Spring Bean的名称springSessionRepositoryFilter实现过滤器。 筛选器负责将HttpSession实现替换为Spring会话支持。在这个实例中，Spring会话得到了Redis的支持。 -->
	<bean
		class="org.springframework.session.data.redis.config.annotation.web.http.RedisHttpSessionConfiguration" />
	<!--创建了一个RedisConnectionFactory，它将Spring会话连接到Redis服务器。我们配置连接到默认端口(6379)上的本地主机！ -->
	<bean
		class="org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory" />
```

4.写LoginServlet和SessionServlet两个类测试:

```java
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        request.getSession().setAttribute("testKey", "742981086@qq.com");

        //这里设置Session的过期时间没有用，在application-session.xml配置过期时间
        request.getSession().setMaxInactiveInterval(10*1000);

        response.sendRedirect(request.getContextPath() + "/session");

    }

}

```

```java
@WebServlet("/session")
public class SessionServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		System.out.println(request.getRemoteAddr());
		System.out.print(request.getRemoteHost() + " : " + request.getRemotePort());

		String sesssionID = request.getSession().getId();
		System.out.println("-----------tomcat1---sesssionID-------" + sesssionID);

		String testKey = (String) request.getSession().getAttribute("testKey");
		System.out.println("-----------tomcat1-testKey-------" + testKey);

		PrintWriter out = null;
		try {
			out = response.getWriter();
			out.append("tomcat1 ---- sesssionID : " + sesssionID);
			out.append("{\"name\":\"dufy2\"}" + "\n");
			out.append("tomcat1 ----- testKey : " + testKey + "\n");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				out.close();
			}
		}

	}

}
```

5.打包部署到两个Tomcat中进行测试即可

参考博客地址:https://blog.csdn.net/u010648555/article/details/79459953