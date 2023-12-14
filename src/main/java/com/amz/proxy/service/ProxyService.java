package com.amz.proxy.service;

import com.amz.proxy.model.ProxyServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Random;


@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProxyService {
    private final ProxyManager proxyManager;
    private static final String[] USER_AGENTS = {
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3",
        "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3",
    };
    private HttpClient createProxyClient(String username, char[] password, String host, int port) {
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope(host, port), new UsernamePasswordCredentials(username, password));
        HttpHost myProxy = new HttpHost(host, port);
        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        clientBuilder.setProxy(myProxy).setDefaultCredentialsProvider(credentialsProvider).disableCookieManagement();
        return clientBuilder.build();
    }

    private RestTemplate createProxyTemplate(HttpClient httpClient) {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(httpClient);
        return new RestTemplate(factory);
    }

    private ResponseEntity<?> handleProxyErrors(
            ResponseEntity<?> responseEntity,
            String host
    ) {
        HttpStatusCode statusCode = responseEntity.getStatusCode();
        if (statusCode.is5xxServerError()) {
            proxyManager.markHostAsUnavailableForOneHour(host);
            return makeGetRequest(responseEntity.getHeaders().getLocation().toString());
        }
        return responseEntity;
    }

    private ResponseEntity<?> handleProxyErrors(
            ResponseEntity<?> responseEntity,
            String host,
            String data,
            String encoding
    ) {
        HttpStatusCode statusCode = responseEntity.getStatusCode();
        if (statusCode.is5xxServerError() && data != null && encoding != null) {
            proxyManager.markHostAsUnavailableForOneHour(host);
            return makePostRequest(
                    responseEntity.getHeaders().getLocation().toString(),
                    data,
                    encoding
            );
        }
        return responseEntity;
    }


    public ResponseEntity<?> makeGetRequest(String url) {
        try {
            ProxyServer proxy = proxyManager.getFirstAvailableProxy();
            if (proxy == null) return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).build();

            final HttpClient httpClient = createProxyClient(proxy.getUsername(), proxy.getPassword(), proxy.getHost(), proxy.getPort());
            final RestTemplate restTemplate = createProxyTemplate(httpClient);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.USER_AGENT, USER_AGENTS[new Random().nextInt(USER_AGENTS.length)]);
            HttpEntity<?> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            return handleProxyErrors(responseEntity, proxy.getHost());
        } catch (Exception err) {
            log.error("Error making GET request through proxy: {}", err.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<?> makePostRequest(String url, String data, String encoding) {
        try {
            ProxyServer proxy = proxyManager.getFirstAvailableProxy();
            if (proxy == null) return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).build();
//            Please uncommit for testing
//            proxyManager.markHostAsUnavailableForOneHour(proxy.getHost());

            final HttpClient httpClient = createProxyClient(proxy.getUsername(), proxy.getPassword(), proxy.getHost(), proxy.getPort());
            final RestTemplate restTemplate = createProxyTemplate(httpClient);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.USER_AGENT, USER_AGENTS[new Random().nextInt(USER_AGENTS.length)]);
            headers.setContentType(MediaType.parseMediaType(encoding));
            String jsonString = data.replaceAll("\\\\", "");
            JSONObject jsonObject = new JSONObject(jsonString);

            HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    String.class
            );
            return handleProxyErrors(responseEntity, proxy.getHost(), data, encoding);
        } catch (Exception err) {
            log.error("Error making POST request through proxy: {}", err.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
