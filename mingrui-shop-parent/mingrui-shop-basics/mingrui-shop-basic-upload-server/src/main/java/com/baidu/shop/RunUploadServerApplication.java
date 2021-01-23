package com.baidu.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @ClassName RunUploadServerApplication
 * @Description: TODO
 * @Author wangjing
 * @Date 2021/01/23
 * @Version V1.0
 **/

@SpringBootApplication
@EnableEurekaClient
public class RunUploadServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(RunUploadServerApplication.class);
    }
}
