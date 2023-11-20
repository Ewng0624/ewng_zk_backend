package cn.zhuhai.usercenter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("cn.zhuhai.usercenter.mapper")
@EnableScheduling
public class EwngInitProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(EwngInitProjectApplication.class, args);
    }

}
