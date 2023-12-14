package com.amz.proxy.controller;

import com.amz.proxy.service.AsyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@RestController
@RequestMapping("/proxy")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProxyController {

    private final AsyncService asyncService;

    @GetMapping
    public ResponseEntity<?> handleGetRequest(@RequestParam String url) throws ExecutionException, InterruptedException {
        CompletableFuture<ResponseEntity<?>> futureResponse = asyncService.makeGetRequestAsync(url);
        return futureResponse.get();
    }

    @PostMapping
    public ResponseEntity<?> handlePostRequest(
            @RequestParam String url,
            @RequestParam(required = false, defaultValue = "application/x-www-form-urlencoded") String encoding,
            @RequestBody String data) throws ExecutionException, InterruptedException {
        CompletableFuture<ResponseEntity<?>> futureResponse = asyncService.makePostRequestAsync(url, data, encoding);
        return futureResponse.get();
    }
}