package com.example;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ImageBanner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.ReflectionUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

@SpringBootApplication
public class FilesIntegrationApplication {

	@Bean
	RouteBuilder fileRoute(
			@Value("${endpoint-in}") String inEndpoint,
			@Value("${endpoint-out}") String outEndpoint,
			Environment environment) {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {

				from(inEndpoint).
						process(exchange -> {
							try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream printStream = new PrintStream(baos)) {
								ImageBanner imageBanner = new ImageBanner(new FileSystemResource(exchange.getIn().getBody(File.class)));
								imageBanner.printBanner(environment, getClass(), printStream);
								String fileName = exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);
								exchange.getIn().setHeader(Exchange.FILE_NAME, fileName.split("\\.")[0] + ".txt");
								exchange.getIn().setBody(new String(baos.toByteArray()));
							} catch (IOException e) {
								ReflectionUtils.rethrowRuntimeException(e);
							}
                        }).
						to(outEndpoint);

			}
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(FilesIntegrationApplication.class, args);
	}
}
