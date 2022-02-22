package com.nowcoder.community.controller;

import com.nowcoder.community.service.AlphaService;
import org.apache.coyote.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

@Controller
@RequestMapping("/alpha")
public class AlphaController {
    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello(){
        return "Hello spring boot!";
    }


    @Autowired
    private AlphaService alphaService;

    @RequestMapping("/data")
    @ResponseBody
    public String getData(){
        return alphaService.find();
    }

    @RequestMapping("/http")
    //response obj can output any value to WebBrowser
    //DispatcherServlet calls this method will automatically
    //pass these two obj(Response , Request)
    public void http(HttpServletRequest request, HttpServletResponse response){
        //get request data
        //get request method
        System.out.println(request.getMethod());
        //get the path
        System.out.println(request.getServletPath());
        //get the header,the return value is an ancient iterator
        Enumeration<String> headerNames = request.getHeaderNames();
        //traverse the iterator
        while(headerNames.hasMoreElements()){
            //get the name of httpHeader
            String name = headerNames.nextElement();
            //by the name of headers get header
            String header = request.getHeader(name);
            System.out.println(name+":"+header);
        }
        //get parameters
        //in browser , we can transfer a parameter use name "code"
        //such as localhost:8081/community/alpha/hello?code=1
        System.out.println(request.getParameter("code"));


        //Response : return response data to browser
        //set response content type
        //"text/html;"-->page type text
        response.setContentType("text/html;charset=utf-8");
        //use packaged stream to output
        //new feature: write Stream in try(),in the bracket,it will close automatically,
        //no need finally{}
        try (PrintWriter writer = response.getWriter() ){
            //write pages here
            writer.write("<h1>NowCoder community</h1>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


     //get request(default)
    //1.first pass value way
    // /students?current= &limit=
     @RequestMapping(path = "/students",method = RequestMethod.GET)
     @ResponseBody
     //usually, it can match param,but if there isn't pass the param ,we should handle it use annotation
     public String getStudents(
             //required,是可以为空，默认值是1
             @RequestParam(name = "current",required = false,defaultValue = "1") int current,
             @RequestParam(name = "limit",required = false,defaultValue = "10") int limit){
         System.out.println(current);
         System.out.println(limit);
         return "some students";
     }
    //2. second way
    // /student/123
    @RequestMapping(path = "/student/{id}",method = RequestMethod.GET)
    @ResponseBody
    //@PathVariable:Variable from path
    public String getAStudent(@PathVariable("id") int id){
        System.out.println(id);
        return "a student";
    }

    //post request
    @RequestMapping(path = "/student",method = RequestMethod.POST)
    @ResponseBody
    public String saveStudent(String name,int age){
        System.out.println(name);
        System.out.println(age);
        return "save success";
    }
}
