package at.jku.isse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import org.springframework.context.ApplicationListener;

@SpringBootApplication(exclude = ErrorMvcAutoConfiguration.class)
public class PPE3Webfrontend extends SpringBootServletInitializer implements ApplicationListener<ApplicationReadyEvent>  {

	static public void main(String[] args) {
        SpringApplication application = new SpringApplication(PPE3Webfrontend.class);        
        application.run(args);
    }        

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
       
    }
}
