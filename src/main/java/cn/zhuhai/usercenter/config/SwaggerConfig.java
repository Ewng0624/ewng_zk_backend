package cn.zhuhai.usercenter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;


@Configuration
@EnableSwagger2WebMvc
public class SwaggerConfig {
    // 创建Docket存入容器，Docket代表一个接口文档
    @Bean(value = "webApiConfig")
    public Docket defaultApi2(){
        return new Docket(DocumentationType.SWAGGER_2)
                // 创建接口文档的具体信息
                .apiInfo(apiInfo())
                // 创建选择器，控制哪些接口被加入文档
                .select()
                // 指定@ApiOperation标注的接口被加入文档
                .apis(RequestHandlerSelectors.basePackage("cn.zhuhai.usercenter.controller"))
                // 路径选择 线上环境不要全部暴露
                .paths(PathSelectors.any())
                .build();
    }

    // 创建接口文档的具体信息，会显示在接口文档页面中
    private ApiInfo apiInfo(){
        return new ApiInfoBuilder()
                // 文档标题
                .title("标题：用户中心系统接口文档")
                // 文档描述
                .description("描述：用户中心文档接口")
                // 版本
                .version("1.0")
                // 版权
                .license("")
                // 版权地址
                .licenseUrl("")
                .build();
    }
}
