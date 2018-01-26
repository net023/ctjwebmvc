# ctjwebmvc

~~~
<build>
  	<plugins>
  		<plugin>
  			<groupId>org.apache.maven.plugins</groupId>
  			<artifactId>maven-compiler-plugin</artifactId>
  			<version>3.5.1</version>
  			<configuration>
  				<source>1.7</source>
  				<target>1.7</target>
  			</configuration>
  		</plugin>
  		<plugin>
  			<groupId>org.apache.tomcat.maven</groupId>
  			<artifactId>tomcat7-maven-plugin</artifactId>
  			<version>2.2</version>
  			<configuration>
  				<port>8080</port>
  				<path>/ctjwebmvc</path>
  			</configuration>
  		</plugin>
  	</plugins>
  </build>
  <dependencies>
  	<dependency>
  		<groupId>javax.servlet</groupId>
  		<artifactId>servlet-api</artifactId>
  		<version>2.5</version>
  		<scope>provided</scope>
  	</dependency>
  	<!-- ctjwebmvc核心 -->
  	<dependency>
  		<groupId>com.caitaojun.ctjwebmvc</groupId>
  		<artifactId>ctjwebmvc</artifactId>
  		<version>0.0.2</version>
  	</dependency>
    <dependency>
  		<groupId>jstl</groupId>
  		<artifactId>jstl</artifactId>
  		<version>1.2</version>
  	</dependency>
  	<dependency>
  		<groupId>taglibs</groupId>
  		<artifactId>standard</artifactId>
  		<version>1.1.2</version>
  	</dependency>
  </dependencies>
~~~


## 使用教程
https://www.kancloud.cn/net023/handbook/514152
