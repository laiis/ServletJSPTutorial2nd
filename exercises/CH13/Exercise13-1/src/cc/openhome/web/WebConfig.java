package cc.openhome.web;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.extras.springsecurity5.dialect.SpringSecurityDialect;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;

@Configuration
@EnableWebMvc
@PropertySource("classpath:web.properties")
@EnableAspectJAutoProxy 
@ComponentScan(basePackages = {"cc.openhome.controller", "cc.openhome.aspect"})
public class WebConfig implements WebMvcConfigurer, ApplicationContextAware {
    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    } 
    
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
       return new PropertySourcesPlaceholderConfigurer();
    }  
    
    @Bean
    public PolicyFactory htmlPolicy() {
        return new HtmlPolicyBuilder()
                    .allowElements("a", "b", "i", "del", "pre", "code")
                    .allowUrlProtocols("http", "https")
                    .allowAttributes("href").onElements("a")
                    .requireRelNofollowOnLinks()
                    .toFactory();
    }

    private ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }    
    
    @Bean
    public ITemplateResolver templateResolver() {
        // 透過此實例進行相關設定，後續用來建立模版引擎物件
        var resolver = new SpringResourceTemplateResolver();
        resolver.setApplicationContext(applicationContext);

        // 開發階段可設定為不快取模版內容，修改模版才能即時反應變更
        resolver.setCacheable(false);
        // 搭配控制器傳回值的前置名稱
        resolver.setPrefix("/WEB-INF/templates/");
        // 搭配控制器傳回值的後置名稱
        resolver.setSuffix(".html");
        // HTML 頁面編碼
        resolver.setCharacterEncoding("UTF-8");
        // 這是一份 HTML 文件
        resolver.setTemplateMode(TemplateMode.HTML);
        return resolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine(ITemplateResolver templateResolver) {
        // 建立與設定模版引擎
        var engine = new SpringTemplateEngine();
        engine.setEnableSpringELCompiler(true);
        engine.setTemplateResolver(templateResolver);
        engine.addDialect(new SpringSecurityDialect()); 
        return engine;
    }

    @Bean
    public ViewResolver viewResolver(SpringTemplateEngine engine) {
        // 建立ViewResolver實作物件並設置模版引擎實例
        var resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(engine);
        // 回應內容編碼
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCache(false);
        return resolver;
    }    
}