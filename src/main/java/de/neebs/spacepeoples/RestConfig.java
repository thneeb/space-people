package de.neebs.spacepeoples;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.ForwardedHeaderFilter;

import java.text.SimpleDateFormat;

/**
 * @author XNEEBT
 * @version 1.0
 * @since 10.05.2017.
 */
@Configuration
public class RestConfig {
    @Bean
    public Jackson2ObjectMapperBuilder jacksonBuilder() {
        Jackson2ObjectMapperBuilder b = new Jackson2ObjectMapperBuilder();
        b.indentOutput(true).dateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX"));
        b.serializationInclusion(JsonInclude.Include.NON_NULL);
        return b;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }
}
