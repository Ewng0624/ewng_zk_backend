package cn.zhuhai.usercenter;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("cn.zhuhai.usercenter.mapper")
public class EwngInitProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(EwngInitProjectApplication.class, args);
    }

}
