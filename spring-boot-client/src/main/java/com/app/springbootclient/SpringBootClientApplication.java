package com.app.springbootclient;

import de.codecentric.boot.admin.client.config.ClientProperties;
import de.codecentric.boot.admin.client.registration.ReactiveRegistrationClient;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

@RestController
@SpringBootApplication
public class SpringBootClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootClientApplication.class, args);
	}

	@GetMapping("/getMeOverHttpTwo")
	public Mono<String> getMeOverHttpTwo() {
		return Mono.just("This is over http2. Curling this endpoint will get a HTTP2 response");
	}

	@Bean
	public ReactiveRegistrationClient registrationClient(ClientProperties clientProperties) {
		final WebClient.Builder webClient = getWebClient().mutate();
		return new ReactiveRegistrationClient(webClient.build(), clientProperties.getReadTimeout());
	}

	@Bean
	@Primary
	public WebClient getWebClient() {
		return WebClient.create().mutate().defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).clientConnector(new ReactorClientHttpConnector(HttpClient.create().wiretap(true).secure(sslContextSpec -> sslContextSpec.sslContext(getSslContext())))).build();
	}

	public SslContext getSslContext() {
		try {
			final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			try (InputStream file = new FileInputStream("/client-keystore.p12")) {
				final KeyStore keyStore = KeyStore.getInstance("JKS");
				keyStore.load(file, "secret".toCharArray());
				keyManagerFactory.init(keyStore, "secret".toCharArray());
			}
			return SslContextBuilder.forClient().keyManager(keyManagerFactory).trustManager(InsecureTrustManagerFactory.INSTANCE).build();
		} catch (CertificateException | NoSuchAlgorithmException | IOException | KeyStoreException | UnrecoverableKeyException e) {
			e.printStackTrace();
			return null;
		}
	}

}
