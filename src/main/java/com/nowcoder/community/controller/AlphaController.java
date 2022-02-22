package com.nowcoder.community.controller;

import com.nowcoder.community.service.AlphaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
}
