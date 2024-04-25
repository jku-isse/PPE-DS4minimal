package at.jku.isse.designspace;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import at.jku.isse.designspace.artifactconnector.core.monitoring.IProgressObserver;
import at.jku.isse.designspace.jama.service.NoOpProgressObserver;


@Configuration
public class JamaConnSpringConfig {

        @Bean
        public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
                return new PropertySourcesPlaceholderConfigurer();
        }

        @Bean
        public static  IProgressObserver getProgressObserver() {
        	return new NoOpProgressObserver();
        }
        
}
