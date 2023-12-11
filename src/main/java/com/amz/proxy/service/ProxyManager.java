package com.amz.proxy.service;

import com.amz.proxy.model.ProxyServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ProxyManager {

    private final List<ProxyServer> proxyConfigurations = new ArrayList<>();

    @Value("${sockets.file.path}")
    private Resource resource;

    @Value("${sockets.delay}")
    private String delay;

    @PostConstruct
    public void init() {
        try {
            loadProxyConfigurations();
            log.info("Reading configuration file...");
        } catch (IOException e) {
            log.error("Error reading file: {}", e.getMessage());
            throw new RuntimeException("Error reading file!", e);
        }
    }

    private void loadProxyConfigurations() throws IOException {
        InputStream inputStream = resource.getInputStream();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 4) {
                    ProxyServer configuration = new ProxyServer();
                    configuration.setHost(parts[0]);
                    configuration.setPort(Integer.parseInt(parts[1]));
                    configuration.setUsername(parts[2]);
                    configuration.setPassword(parts[3].toCharArray());
                    proxyConfigurations.add(configuration);
                }
            }
        }
    }

    public ProxyServer getFirstAvailableProxy() {
        return proxyConfigurations.stream()
                .filter(proxy -> proxy.getUnavailableUntil() == null || proxy.getUnavailableUntil() <= System.currentTimeMillis())
                .findFirst()
                .orElse(null);
    }

    private ProxyServer findProxyByHost(String host) {
        return proxyConfigurations.stream()
                .filter(proxy -> proxy.getHost().equals(host))
                .findFirst()
                .orElse(null);
    }

    public void markHostAsUnavailableForOneHour(String host) {
        ProxyServer proxyServer = findProxyByHost(host);
        if (proxyServer != null) {
            log.info("Proxy {} marked as unvailable", proxyServer.getHost());
            proxyServer.setUnavailableUntil(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(Long.parseLong(delay)));
//            proxyServer.setUnavailableUntil(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(Long.parseLong(delay)));
        }
    }

    @Scheduled(fixedRate = 60000)
    public void checkUnavailableProxies() {
        long currentTime = System.currentTimeMillis();
        for (ProxyServer proxyServer : proxyConfigurations) {
            Long unavailableUntil = proxyServer.getUnavailableUntil();
            if (unavailableUntil != null && unavailableUntil <= currentTime) {
                proxyServer.setUnavailableUntil(null);
            }
        }
    }
}
