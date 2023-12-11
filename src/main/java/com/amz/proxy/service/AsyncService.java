package com.amz.proxy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AsyncService {
    private final ProxyService proxyService;
    @Async
    public CompletableFuture<ResponseEntity<?>> makeGetRequestAsync(String url) {
        try {
            ResponseEntity<?> responseEntity = proxyService.makeGetRequest(url);
            return CompletableFuture.completedFuture(responseEntity);
        } catch (Exception err) {
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        }
    }

    @Async
    public CompletableFuture<ResponseEntity<?>> makePostRequestAsync(String url, String data, String encoding) {
        try {
            ResponseEntity<?> responseEntity = proxyService.makePostRequest(url, data, encoding);
            return CompletableFuture.completedFuture(responseEntity);
        } catch (Exception err) {
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        }
    }
}
