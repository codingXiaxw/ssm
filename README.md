## 一个案例带你快速入门SSM开发

**写在前面的话:**关于SSM框架的工程搭建请点击这里前往我的博客[SSM整合工程的搭建](http://codingxiaxw.cn/2016/11/15/44-ssm%E7%9A%84%E6%95%B4%E5%90%88/)

## 开发环境
IDEA Spring3.x+SpringMVC+Mybatis  
没有用到maven管理工具。

## 1.实现商品的列表展示

### 1.1提出需求
功能描述:在页面中展示商品列表。

### 1.2编写表

sql语句见github中.sql文件。

### 1.3持久层mapper的编写
编写好数据库后我们便可以通过MyBatis逆向工程快速生成对单表映射的sql，包括mapper.java、mapper.xml和pojo类。

根据逆向工程生成的这三个文件与单表都是一对一的关系，例如通过Items表会生成ItemsMapper.java、ItemsMapper.xml和Items.java的pojo类，这里我们为了便于需求的扩展，所以另外自己编写一个ItemsCustom.java并继承Items.java和Items.java的包装类ItemsQueryVo.java，代码如下:
```java
public class ItemsQueryVo {
	//商品信息
	private ItemsCustom itemsCustom;

	public ItemsCustom getItemsCustom() {
		return itemsCustom;
	}

	public void setItemsCustom(ItemsCustom itemsCustom) 	{
		this.itemsCustom = itemsCustom;
	}	
}
```

然后自己编写一个ItemsCustomerMapper.xml:
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
"http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="mapper.ItemsMapperCustom">

	<!-- 商品查询的sql片段
	建议是以单表为单位定义查询条件
	建议将常用的查询条件都写出来
	 -->
	<sql id="query_items_where">
		<if test="itemsCustom!=null">
			<if test="itemsCustom.name!=null and itemsCustom.name!=''">
				and  name like '%${itemsCustom.name}%'
			</if>
			<if test="itemsCustom.id!=null">
				and  id = #{itemsCustom.id}
			</if>
		
		</if>
		
	</sql>
	
	<!-- 商品查询 
	parameterType：输入 查询条件
	-->
	
	<select id="findItemsList" parameterType="po.ItemsQueryVo"
			resultType="po.ItemsCustom">
		SELECT * FROM items 
		<where>
			<include refid="query_items_where"/>
		</where>
	</select>
</mapper>
```
与ItemsCustomMapper.java:
```java
public interface ItemsMapperCustom {
	// 商品查询列表
	List<ItemsCustom> findItemsList(ItemsQueryVo itemsQueryVo)
			throws Exception;
}
```

至于Mapper的配置我们已经在springmvc.xml中通过spring组件扫描器
```xml

	<!--
	MapperScannerConfigurer：mapper的扫描器，将包下边的mapper接口自动创建代理对象，
	自动创建到spring容器中，bean的id是mapper的类名（首字母小写）
	 -->
	<bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
		<!-- 配置扫描包的路径
		如果要扫描多个包，中间使用半角逗号分隔
		要求mapper.xml和mapper.java同名且在同一个目录
		 -->
		<property name="basePackage" value="mapper"/>
		<!-- 使用sqlSessionFactoryBeanName -->
		<property name="sqlSessionFactoryBeanName" value="sqlSessionFactory"/>
	</bean>
```
进行了统一的配置。


接口里面调用xml文件中查询表中所有商品列表信息的sql语句，然后我们便可以进行业务逻辑层的代码编写.


### 1.4业务逻辑层service的编写
首先我们在service包下创建一个商品的service接口ItemsService.java文件，里面编写的方法和ItemsCustomMapper.java中的方法对应以实现商品列表的查询:
```java
public interface ItemsService {

    //商品的查询列表
    public List<ItemsCustom> findItemsList(ItemsQueryVo itemsQueryVo)
            throws Exception;
}
```

然后编写其实现类ItemsServiceImpl.java:
```java
public class ItemsServiceImpl implements ItemsService {

    //注入mapper
    @Autowired
    private ItemsMapperCustom itemsMapperCustom;

    //商品的查询列表
    @Override
    public List<ItemsCustom> findItemsList(ItemsQueryVo itemsQueryVo) throws Exception {

        return itemsMapperCustom.findItemsList(itemsQueryVo);
    }
}
```

代码中通过Spring框架的DI注入依赖对象mapper即itemsMapperCustom对象，然后调用itemsMapperCustom的findItemsList方法实现商品列表查询,然后在spring配置文件applicationContext-service.xml中要进行service的配置，添加如下标签:
```xml
<!--商品配置的service-->
	<bean id="itemsService" class="service.impl.ItemsServiceImpl"/>
```
便可。接下来便应该完成控制层Controller.java的代码编写了。

### 1.5控制层Controller的编写
在controller包下创建一个ItemsController.java，里面编写代码:
```java
@Controller
public class ItemsController {

    //注入service
    @Autowired
    private ItemsService itemsService;

    @RequestMapping("/queryItems")
    public ModelAndView queryItems() throws Exception {
        //调用servie来查询商品列表
        List<ItemsCustom> itemsList=itemsService.findItemsList(null);

        ModelAndView modelAndView=new ModelAndView();
        modelAndView.addObject("itemsList",itemsList);
        //指定逻辑视图名itemsList
        modelAndView.setViewName("itemsList");

        return modelAndView;
    }
}
```
通过@Autowired注解完成service的依赖注入，通过@Controller注解将Controller自动添加到spring容器IOC中，通过@RequestMapping("/queryItems")注解指明访问该Controller的url。  

至于itemsList.jsp的页面编写代码如下:  
```xml
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>查询商品列表</title>
</head>
<body> 
<form action="${pageContext.request.contextPath }/items/queryItem.action" method="post">
查询条件：
<table width="100%" border=1>
<tr>
<td><input type="submit" value="查询"/></td>
</tr>
</table>
商品列表：
<table width="100%" border=1>
<tr>
	<td>商品名称</td>
	<td>商品价格</td>
	<td>生产日期</td>
	<td>商品描述</td>
	<td>操作</td>
</tr>
<c:forEach items="${itemsList }" var="item">
<tr>
	<td>${item.name }</td>
	<td>${item.price }</td>
	<td><fmt:formatDate value="${item.createtime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
	<td>${item.detail }</td>
	
	<td><a href="${pageContext.request.contextPath }/items/editItems.action?id=${item.id}">修改</a></td>

</tr>
</c:forEach>

</table>
</form>
</body>

</html>
```
然后我们运行服务器，输入网址`http://localhost:8080/SpringMvcMybatis/queryItems.action`，发现无法看到页面，这是因为我们的spring配置文件没有得到加载，需要在web.xml文件中加入如下内容进行spring容器的配置:  
```xml
    <!--配置spring容器监听器-->
    <context-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>/WEB-INF/classes/config/spring/applicationContext-*.xml</param-value>
    </context-param>
    <listener>
        <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
    </listener>
```
然后重新运行服务器并输入网址，看到如下页面，说明成功使用SSM框架完成开发显示商品列表的项目:
![](http://od2xrf8gr.bkt.clouddn.com/%E5%B1%8F%E5%B9%95%E5%BF%AB%E7%85%A7%202016-11-16%20%E4%B8%8B%E5%8D%888.00.34.png)


到此，我们便通过SSM的整合工程项目，完成了对商品列表的查询。接下来我们再实现对商品的另一个功能:修改商品信息。

## 2.实现商品信息的修改
### 2.1需求
功能描述:商品信息修改。操作流程：1.在商品列表页面点击修改连接。2.打开商品修改页面，显示了当前商品的信息(根据商品id查询商品信息)。3.修改商品信息，点击提交(更新商品信息)。  

通过此案例，我们也会穿插用SSM进行注解开发的基础知识如: @RequestMapping注解的改善、controller方法返回值、Controller方法中的参数与页面参数的绑定。

### 2.2mapper的编写
此功能涉及到的mapper为ItemsMapper.java与ItemsMapper.xml，已使用逆向工程为我们生成。

### 2.3service的编写
在ItemsService接口中添加方法:
```java
   //根据商品id查询商品信息
    public ItemsCustom findItemsById(int id) throws Exception;
    
      //更新商品信息
    /**
     * 定义service接口，遵循单一职责，将业务参数细化(不要使用包装类型，比如map)
     * @param id 修改商品的id
     * @param itemsCustom 修改商品的信息
     * @throws Exception
     */
    public void updateItems(Integer id,ItemsCustom itemsCustom) throws Exception;
```

然后是实现类ItemsServiceImpl.java:
```java
 
 	//注入依赖对象itemsMapper
 	 @Autowired
    private ItemsMapper itemsMapper;

    @Override
    public ItemsCustom findItemsById(int id) throws Exception {

        Items items=itemsMapper.selectByPrimaryKey(id);

        //在这里以后随着需求的变化，需要查询商品的其它相关信息，返回到controller
        //所以这个时候用到扩展类更好，如下
        ItemsCustom itemsCustom=new ItemsCustom();
        //将items的属性拷贝到itemsCustom
        BeanUtils.copyProperties(items,itemsCustom);

        return itemsCustom;
    }

    @Override
    public void updateItems(Integer id,ItemsCustom itemsCustom) throws Exception {

        //在service中一定要写业务代码




        //对于关键业务数据的非空校验
        if (id==null)
        {
            //抛出异常，提示调用接口的用户，id不能唯恐
            //...
        }

        itemsMapper.updateByPrimaryKeyWithBLOBs(itemsCustom);
    }
```

说一句:对service的开发是整个系统中开发最重要的部分，所以你要把service的开发放在学习的重点上。接下来就要写Controller的代码了，然而写Controller的过程中会学到很多注解开发的基础知识。


### 2.4Controller的编写之@RequestMapping的特性学习

#### 2.4.1窄化请求映射
我们除了在Controller方法的上面加上一个@RequestMapping的注解指定url外(完成url映射)，还可以在Controller类的上面指定一个@RequestMapping注解指定访问路径的根url，如这里我们是对商品的操作，所以可以在Controller类上面加上一个@RequestMapping的注解指定访问商品信息的根路径(叫“窄化请求映射”):
```java
@Controller
//定义url的根路径，访问时根路径+方法名的url
@RequestMapping("/items")
public class ItemsController {
}
```
使用窄化请求映射的好处:更新规范系统的url，避免url冲突。  

然后继续我们的Controller开发，添加方法:
```java
    @RequestMapping(value = "/editItems",method = RequestMethod.GET)
    public ModelAndView editItems() throws Exception
    {
        ModelAndView modelAndView=new ModelAndView();

        //调用service查询商品的信息
        ItemsCustom itemsCustom=itemsService.findItemsById(1);
        //将模型数据传到jsp
        modelAndView.addObject("item",itemsCustom);
        //指定逻辑视图名
        modelAndView.setViewName("editItem");

        return modelAndView;
    }
```

编写editItem.jsp页面:
```xml
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>修改商品信息</title>

</head>
<body> 
<form id="itemForm" action="${pageContext.request.contextPath }/items/editItemSubmit.action" method="post" >
<input type="hidden" name="id" value="${id }"/>
修改商品信息：
<table width="100%" border=1>
<tr>
	<td>商品名称</td>
	<td><input type="text" name="name" value="${itemsCustom.name }"/></td>
</tr>
<tr>
	<td>商品价格</td>
	<td><input type="text" name="price" value="${itemsCustom.price }"/></td>
</tr>
<tr>
	<td>商品简介</td>
	<td>
	<textarea rows="3" cols="30" name="detail">${itemsCustom.detail }</textarea>
	</td>
</tr>
<tr>
<td colspan="2" align="center"><input type="submit" value="提交"/>
</td>
</tr>
</table>
</form>
</body>
</html>
```
然后运行服务器，此时应该输入网址`http://localhost:8080/SpringMvcMybatis/items/queryItems.action`而不是`http://localhost:8080/SpringMvcMybatis/queryItems.action`,然后点击右边的修改链接便可以进去相应的修改页面:![](http://od2xrf8gr.bkt.clouddn.com/%E5%B1%8F%E5%B9%95%E5%BF%AB%E7%85%A7%202016-11-16%20%E4%B8%8B%E5%8D%888.55.33.png)  

#### 2.4.2限制http请求的方法
不知道你发现没有，我们在Controller的editItems()方法上的注解中加入的是`value = "/editItems",method = RequestMethod.GET`参数而不再是单单的`"/editItems"`参数了，这里我们便用到了使用@RequestMapping注解限制http请求的方法。如果你将这里的`method = RequestMethod.GET`改为`method = RequestMethod.POST`，然后在页面中再点击修改链接时就会报错。 

另外method属性的属性值为数组，我们也可以将注解中的参数改为`ethod = {RequestMethod.GET,RequestMethod.POST}`表示请求既可以为POST请求又可以为GET请求。 


### 2.5Controller的编写之Controller方法返回值学习
#### 2.5.1返回ModerAndView

目前我们使用的方式都是返回的ModerAndView对象，例如我们已经编写的Controller中的queryItems()方法和editItems()方法。接下来我们看看返回字符串的方法编写。

#### 2.5.2返回字符串
首先注释掉我们返回值为ModerAndView类型的editItems()方法。如果controller方法返回jsp页面，可以简单将方法返回值类型定义 为字符串，最终返回逻辑视图名。编写返回值为String类型的editItems()方法，代码如下:
```java
    //方法返回字符串，字符串就是逻辑视图名，Model作用是将数据填充到request域，在页面显示
    @RequestMapping(value = "/editItems",method = RequestMethod.GET)
    public String editItems(Model model) throws Exception
    {

    
        //调用service查询商品的信息
        ItemsCustom itemsCustom=itemsService.findItemsById(1);

        model.addAttribute("itemsCustom",itemsCustom);

        return "editItem";
    }
```
方法中我们需要传入一个Model对象，作用是将数据填充到request域，在页面显示。然后运行服务器，输入`http://localhost:8080/SpringMvcMybatis/items/queryItems.action`照常正确访问该网站。再来介绍返回值为void的方法。

#### 2.5.3返回void
同样注释掉返回值为String类型的editItems()方法，然后加入返回值为void的editItems()方法:
```java
    @RequestMapping(value = "/editItems",method = RequestMethod.GET)
    public void editItems(HttpServletRequest request, HttpServletResponse response) throws Exception
    {

        //调用service查询商品的信息
        ItemsCustom itemsCustom=itemsService.findItemsById(id);

        request.setAttribute("item",itemsCustom);

        //注意如果使用request转向页面，这里需要指定页面的完整路径
        request.getRequestDispatcher("/WEB-INF/jsp/editItem.jsp").forward(request,response);
    }
```

其实这里就是运用的原生态的Servlet的开发方式，运行服务器，输入`http://localhost:8080/SpringMvcMybatis/items/queryItems.action`仍照常正确访问该网站。  


通过这种返回值为void的方法我们容易输出json、xml格式的数据，即通过response指定响应结果，例如响应json数据如下：
```java
response.setCharacterEncoding("utf-8");
response.setContentType("application/json;charset=utf-8");
response.getWriter().write("json串");
```

上面我们就通过完成商品信息的编辑功能介绍了Controller中三种返回值类型的方法。而通过返回字符串的方法，有时候会返回一些特殊的字符串(例如返回`return "forward:url路径"`或`return "redirect:url路径"`)。分别代表请求转发和请求冲定向，下面我们通过完善编辑商品信息后进行提交的功能来讲解这两种返回特殊字符串类型的方法。在Controller中添加editItemSubmit()方法:
```java
 //商品提交页面
    //itemsQueryVo是包装类型的pojo
    @RequestMapping("/editItemSubmit")
    public String editItemSubmit() throws Exception
    {
        //请求转发,使用forward进行请求转发，request数据可以共享，url地址栏不会
//        return "forward:queryItems.action";

        //使用redirect进行重定向，request数据无法共享，url地址栏会发生变化的。由于我们重定向的页面queryItems.action与本页面editItemSubmit.action在同一根目录下，所以不需要加入根路径
       return "redirect:queryItems.action";
    }
```

运行服务器，然后我们便可以在editItems.jsp页面通过点击"提交"按钮请求转发或者请求重定向到我们的`queryItems.action`页面。如上，我便介绍完Contoller方法返回值的知识。接下来介绍Controller方法中的参数与页面参数绑定的知识。  

### 2.6Controller的编写之方法参数与页面参数的绑定
不知你注意到没有，在Controller的方法中我们传入的参数都是我们自己根据需求手动传入的参数，而真正的需求中我们是需要将页面中的参数传递到Controller的方法中的，那如何将页面的参数绑定到Controller的方法中呢？看下方参数绑定的过程图解:  

![](http://od2xrf8gr.bkt.clouddn.com/%E5%B1%8F%E5%B9%95%E5%BF%AB%E7%85%A7%202016-11-16%20%E4%B8%8B%E5%8D%889.33.44.png)

首先我们看看Controller的方法中默认支持的形参(即之前我们根据需求手动传入的参数，这些参数处理适配器会默认识别并进行赋值)有:1.HttpServletRequest:通过request对象获取请求信息。 2.HttpServletResponse:通过response处理响应信息。3.HttpSession:通过session对象得到session中存放的对象。4.Model/modelmap/map:通过model向页面传递数据，页面通过${item.XXXX}获取item对象的属性值,如下:
```java
//调用service查询商品信息
Items item = itemService.findItemById(id);
model.addAttribute("item", item);
```
但是值得我们关心的不是这些默认的参数，而是我们自定义参数传入Controller方法的形参中，继续往下面看。

#### 2.6.1@RequestParam
如果request请求的参数名和controller方法的形参数名称一致，适配器自动进行参数绑定。如果不一致可以通过
@RequestParam 指定request请求的参数名绑定到哪个方法形参上。

对于必须要传的参数，通过@RequestParam中属性required设置为true，如果不传此参数则报错。
 
对于有些参数如果不传入，还需要设置默认值，使用@RequestParam中属性defaultvalue设置默认值。

例如Controller中的方法:
```java
    @RequestMapping(value = "/editItems",method = RequestMethod.GET)
    public void editItems(HttpServletRequest request, HttpServletResponse response,@RequestParam(value = "item_id",required = false,defaultValue = "1") Integer id) throws Exception
    {

        //调用service查询商品的信息
        ItemsCustom itemsCustom=itemsService.findItemsById(id);

        request.setAttribute("item",itemsCustom);

        //zhuyi如果使用request转向页面，这里需要指定页面的完整路径
        request.getRequestDispatcher("/WEB-INF/jsp/editItem.jsp").forward(request,response);
    }
```

没对形参id加上@RequestParam注解时，当我们从页面进入到editItems.action时，只有从页面传入的参数名为id时该id参数值才会传到editItems()方法的id参数值上，如果从页面传入的参数明不为id而为其他参数名时例如`http://localhost:8080/SpringMvcMybatis/items/editItems.action?item_id=1`，此时通过调试会发现editItems()方法中的id属性值为null;而当我们为形参id加上了@RequestParam注解并指定了其属性`value = "item_id"`后，若从页面传入的参数名为item_id，则该参数值会因为添加了`value = "item_id"`该属性而被赋值给id属性。`required`属性若设置为true，则如果从页面进入到editItem.action时没有传入此参数则会报错。`defaultvalue`属性值表示为该参数赋默认值。

#### 2.6.2绑定简单类型
上述那个editItem()方法时原始的servlet开发方法，接下来我们用返回值为String 类型的方法进行注解开发的基础知识讲解。

可以绑定整型、字符串、单精/双精度、日期、布尔型，很简单处理，我不进行讲解，通过下面绑定pojo类型你就会清楚了。

#### 2.6.3绑定pojo类型
绑定pojo类型又可以分为绑定简单pojo类型和绑定包装pojo类型。

##### 2.6.3.1绑定简单pojo类型
简单pojo类型只包括简单类型的属性。绑定过程:request请求的参数名称和pojo的属性名一致，就可以绑定成功。  

修改Controller中的editItemSubmit()方法:
```java
//商品提交页面
    //itemsQueryVo是包装类型的pojo
    @RequestMapping("/editItemSubmit")
    public String editItemSubmit(Integer id,ItemsCustom itemsCustom) throws Exception
    {
        //进行数据回显
        model.addAttribute("id",id);
//        model.addAttribute("item",itemsCustom);


        itemsService.updateItems(id,itemsCustom);
        //请求转发
//        return "forward:queryItems.action";



        //重定向
       return "redirect:queryItems.action";
    }
```
点击提交按钮，从editItem.jsp页面进入editItemSubmit.action时，就会将编辑页面的参数都映射到该方法的id形参和ItemsCustom对象中，此时我们修改商品的信息，然后点击提交按钮，服务器反应过程如下:点击提交按钮，页面从editItem.jsp进入到editItemSubmit.action并将修改后的商品信息提交到数据库并将这些参数传入到ItemsCustom对象的属性中，然后重定向到queryItems.action进行商品的列表信息展示。  

**问题:**如果controller方法形参中有多个pojo且pojo中有重复的属性，使用简单pojo绑定无法有针对性的绑定，比如:方法形参有items和User，pojo同时存在name属性，从http请求过程的name无法有针对性的绑定到items或user。要解决此种方法我们就需要用到下面的绑定包装的pojo类型。

##### 2.6.3.2绑定包装的pojo类型
这里我们复制editItem.jsp页面粘贴出一个editItem2.jsp页面，染护修改editItem2.jsp中的参数名为itemsCustom.name、itemsCustom.price、itemsCustom.detail，修改Controller中的editItemSubmit方法中的形参为`public String editItemSubmit(Integer id,ItemsCustom itemsCustom,ItemsQueryVo itemsQueryVo) throws Exception{...}
`修改editItems的返回值类型为`editItems2`。运行程序，点击提交按钮，页面信息成功传入到itemsQueryVo的属性中。成功运行后我们还是将信息改回成原来的模样，方便后面的测试。  

#### 2.6.4使用属性编辑器完成自定义绑定

此时我们在editItem.jsp中添加上日期的信息展示:
```xml
<tr>
	<td>商品生产日期</td>
	<td><input type="text" name="createtime" value="<fmt:formatDate value="${itemsCustom.createtime}" pattern="yyyy-MM-dd HH-mm-ss"/>"/></td>
</tr>
```

然后运行程序，当点击提交按钮时会报错，你知道为什么吗？原因是因为通过点击提交按钮，页面中参数名为"createtime"的参数名由于跟Controller方法中的形参ItemsCustom有相同的属性名createtime，所以此时页面中的日期会映射到ItemsCustom的Date属性中,但是从页面传过来的日期是字符串类型，而ItemsCustom的属性是java.util.Date类型，所以当然会报错。这样的话，我们就必须完成日期字符串向java类型日期的转换。此时我们就需要自定义日期类型的绑定，即使用属性编辑器来完成自定义的绑定。有如下两种方法:1.使用WebDataBinder（了解），在Controller中添加如下代码:
```java
    //自定义属性编辑器
    @InitBinder
    public void initBinder(WebDataBinder binder) throws  Exception{

        //Date.class必须是与controller方法形参pojo属性一致的date类型，这里是java.util.Date
        binder.registerCustomEditor(Date.class,new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd HH-mm-ss"),true));
    }
```
运行程序，点击提交按钮后不会再出现报错信息，且editItem.jsp页面的createtime参数也成功传入到了ItemsCustom的createtime属性中。使用这种方法的问题是无法在多个controller共用。那我们就来介绍第二种方法:使用WebBindingInitializer（了解）。首先我们需要编写一个自定义属性编辑器CustomPropertyEditor.java，代码如下:
```java
public class CustomPropertyEditor implements PropertyEditorRegistrar
{

    @Override
    public void registerCustomEditors(PropertyEditorRegistry binder) {
        binder.registerCustomEditor(Date.class,new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd HH-mm-ss"),true));

    }
}
```

然后要在springmvc.xml文件中加入对它的配置:
```xml
<!-- 注册属性编辑器 -->
	<bean id="customPropertyEditor" class="cn.itcast.ssm.propertyeditor.CustomPropertyEditor"></bean> 
<!-- 自定义webBinder -->
	<bean id="customBinder"
		class="org.springframework.web.bind.support.ConfigurableWebBindingInitializer">
		<property name="propertyEditorRegistrars">
			<list>
				<ref bean="customPropertyEditor"/>
			</list>
		</property>
	</bean>
```

然后要在注解适配器的配置标签中加入如下属性:
```xml
<bean class="org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter">
	<property name="webBindingInitializer" ref="customBinder"></property> 
</bean>
```

这样我们便可以注释掉第一种属性编辑器的代码了，使用第二种方式虽然配置很繁琐，但是很适用。运行程序，也成功将editItem.jsp页面的createtime参数映射到ItemsCustom的createtime属性中。下面我再讲一种自定义绑定参数的方法。

#### 2.6.5使用转换器完成自定义参数绑定(想往架构师方向发展的要掌握这种方法)
首先要定义一个转换器CustomDateConverter.java完成日期的转换，代码如下:
```java
public class CustomDateConverter implements Converter<String,Date> {

    @Override
    public Date convert(String source) {

        try{
            return new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").parse(source);
        }catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }
}
```
在定义一个StringTrimConverter.java用于去除日期字符串两边的空格,代码如下:
```java
public class StringTrimConverter implements Converter<String,String> {

    @Override
    public String convert(String source) {

        try{
            //去掉字符串两边的空格，如果去除后为空则返回null
            if (source!=null)
            {
                source=source.trim();
                if (source.equals(""))
                    return null;
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }

        return source;
    }
}
```

定义好后就需要对转换器进行配置:思路就是先定义一个转换器然后注入到适配器中。而对于转换器在springmvc.xml中的配置有两种方式，第一种方式针对不使用`<mvc:annotation-driven>`,第二种方式针对使用`<mvc:annotation-driven>`,我们就来讲讲第二种方式。在springmvc.xml中添加如下配置:
```xml
    <!--mvc的注解驱动器，通过它可以替代下边的处理器映射器和适配器-->
    <mvc:annotation-driven conversion-service="conversionService">
    </mvc:annotation-driven>

    <!--转换器-->
    <!-- conversionService -->
    <bean id="conversionService"
          class="org.springframework.format.support.FormattingConversionServiceFactoryBean">
        <!-- 转换器 -->
        <property name="converters">
            <list>
                <bean class="controller.converter.CustomDateConverter"/>
                <bean class="controller.converter.StringTrimConverter"/>
            </list>
        </property>
    </bean>
```

使用了注解驱动的配置后，我们就可以注释掉处理器映射器与处理器适配器了。运行程序，也成功将editItem.jsp页面的createtime参数映射到ItemsCustom的createtime属性中。

由于往后我们还要进行json数据的开发，所以这里我们还是不采用使用注解驱动的方式，还是采用注解映射器与注解适配器的方式进行开发。修改后的最后的springmvc.xml配置信息如下:
```xml
  <!--使用spring组件扫描
    一次性配置此包下所有的Handler-->
    <context:component-scan base-package="controller"/>

    <!--mvc的注解驱动器，通过它可以替代下边的处理器映射器和适配器-->
    <!--<mvc:annotation-driven></mvc:annotation-driven>-->

    <!--注解处理器映射器-->
    <bean class="org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping"/>

    <!--注解的适配器-->
    <bean class="org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter">
        <property name="webBindingInitializer" ref="customBinder"></property>
    </bean>

    <!--配置视图解析器
    要求将jstl的包加到classpath-->
    <bean class="org.springframework.web.servlet.view.InternalResourceViewResolver">
        <property name="prefix" value="/WEB-INF/jsp/" />
        <property name="suffix" value=".jsp" />
    </bean>



      <!-- 自定义webBinder -->
    <bean id="customBinder"
          class="org.springframework.web.bind.support.ConfigurableWebBindingInitializer">
        <property name="conversionService" ref="conversionService"/>
        <!--早期的自定义属性编辑器-->
        <!--<property name="propertyEditorRegistrars">-->
            <!--<list>-->
                <!--<ref bean="customPropertyEditor"/>-->
            <!--</list>-->
        <!--</property>-->
    </bean>

    <!-- 注册属性编辑器 -->
    <bean id="customPropertyEditor" class="controller.propertyeditor.CustomPropertyEditor"></bean>



    <!--mvc的注解驱动器，通过它可以替代下边的处理器映射器和适配器-->
    <!--<mvc:annotation-driven conversion-service="conversionService">-->
    <!--</mvc:annotation-driven>-->

    <!--转换器-->
    <!-- conversionService -->
    <bean id="conversionService"
          class="org.springframework.format.support.FormattingConversionServiceFactoryBean">
        <!-- 转换器 -->
        <property name="converters">
            <list>
                <bean class="controller.converter.CustomDateConverter"/>
                <bean class="controller.converter.StringTrimConverter"/>
            </list>
        </property>
    </bean>
```

这个converter的配置是一劳永逸的配置，也就是系统架构级别的配置，希望你能成功掌握。

好了，通过上述的案例，便成功的使用了SSM框架对对商品信息的三个功能。希望通过这个案例，你能成功掌握SSM框架。接下来我将讲解使用SSM进行注解开发的高级知识。博客链接[SSM注解开发的高级知识讲解](http://codingxiaxw.cn/2016/11/19/46-ssm%E9%AB%98%E7%BA%A7%E5%BC%80%E5%8F%91/),源码链接[点击这里前往我的github](https://github.com/codingXiaxw/ssm2)

## 3.联系

  If you have some questions after you see this article,you can tell your doubts in the comments area or you can find some info by  clicking these links.


- [Blog@codingXiaxw's blog](http://codingxiaxw.cn)

- [Weibo@codingXiaxw](http://weibo.com/u/5023661572?from=hissimilar_home&refer_flag=1005050003_)

- [Zhihu@codingXiaxw](http://www.zhihu.com/people/e9f78fa34b8002652811ac348da3f671)  
- [Github@codingXiaxw](https://github.com/codingXiaxw)
