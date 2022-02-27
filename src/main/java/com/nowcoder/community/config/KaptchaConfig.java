package com.nowcoder.community.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

//@Configuration annotation is a used to declare a config class
@Configuration
public class KaptchaConfig {
    //producer is a interface which has a implements class called DefaultKaptcha
    //here,we don't have springBoot automatic assembly(自动装配)
    //so we use @Bean to put it into spring container
    @Bean
    public Producer kaptchaProducer(){
        Properties properties = new Properties();
        //some configs about the verification image
        properties.setProperty("kaptcha.image.width", "100");
        properties.setProperty("kaptcha.image.height", "40");
        properties.setProperty("kaptcha.textproducer.font.size", "32");
        properties.setProperty("kaptcha.textproducer.font.color", "0,0,0");
        properties.setProperty("kaptcha.textproducer.char.string", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        properties.setProperty("kaptcha.noise.impl", "com.google.code.kaptcha.impl.NoNoise");
        //use the impl class
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        //set the config,needed to pass a properties into it
        Config config = new Config(properties);
        defaultKaptcha.setConfig(config);
        return defaultKaptcha;
    }

}
