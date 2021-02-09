package com.app.admin;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.HttpProtocol;
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

@SpringBootApplication
@EnableAdminServer
public class SpringBootAdminApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootAdminApplication.class, args);
	}

	@Bean
	public ClientHttpConnector customHttpClient() {
		return new ReactorClientHttpConnector(HttpClient.create().wiretap(true).protocol(HttpProtocol.H2).secure(ssl -> ssl.sslContext(getSslContext())));
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
