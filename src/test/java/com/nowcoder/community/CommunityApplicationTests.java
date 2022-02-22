package com.nowcoder.community;

import com.nowcoder.community.dao.AlphaDao;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.service.AlphaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootTest
//this annotation means test method uses the same configurations as normal environment
@ContextConfiguration(classes = CommunityApplication.class)
//the class which wants to get spring containers should implements  ApplicationContextAware interface
class CommunityApplicationTests implements ApplicationContextAware {
	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		//Actually,applicationContext is the spring container,it is a interface which implements
		//HierarchicalBeanFactory interface which implements BeanFactory
		//BeanFactory is the spring top level interface
		//we usually use Application interface, which implements more methods

		//Here,we accept the applicationContext object for using it later
		this.applicationContext = applicationContext;
	}
	//test applicationContext get beans
	@Test
	public void testDao(){
		//get AlphaDao.class type beans ,there exists two impl classes,
		//@Primary annotation declare the class will be got primarily
		//And depend on AlphaDao interface , we can have plenty of impl classes, use @Primary annotation
		//to decide which bean will be got,this is convenient
		AlphaDao bean = applicationContext.getBean(AlphaDao.class);
		System.out.println(bean.select());

		//when we want to get the bean which is not primary ,we could use its name
		bean = applicationContext.getBean("alphaHibernate",AlphaDao.class);
		System.out.println(bean.select());
	}
	//test third party beans config
	@Test
	public void testBeanConfig(){
		SimpleDateFormat bean = applicationContext.getBean(SimpleDateFormat.class);
		System.out.println(bean.format(new Date()));
	}


	//this annotation will inject spring container into the fields,or set methods ,or constructors
	@Autowired
	@Qualifier("alphaHibernate")//hope inject the specified bean
	private AlphaDao alphaDao;
	@Autowired
	private AlphaService alphaService;
	@Autowired
	private SimpleDateFormat simpleDateFormat;
	@Test
	//test dependency injection
	public void testDI(){
		System.out.println(alphaDao);
		System.out.println(alphaService);
		System.out.println(simpleDateFormat);
	}

}
