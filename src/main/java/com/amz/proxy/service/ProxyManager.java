package com.amz.proxy.service;

import com.amz.proxy.model.ProxyServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ProxyManager {
    private final Map<String, ProxyServer> proxyConfigurations = new ConcurrentHashMap<>();
    private final Random random = new Random();

    @Value("${sockets.file.path}")
    private Resource resource;

    @Value("${sockets.delay}")
    private int delay;

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
                    proxyConfigurations.put(configuration.getHost(), configuration);
                }
            }
        }
    }

    public ProxyServer getFirstAvailableProxy() {
        long currentTime = System.currentTimeMillis();
        ProxyServer[] proxies = proxyConfigurations.values().toArray(new ProxyServer[0]);

        for (int i = 0; i < proxies.length; i++) {
            int randomIndex = i + random.nextInt(proxies.length - i);
            ProxyServer temp = proxies[randomIndex];
            proxies[randomIndex] = proxies[i];
            proxies[i] = temp;

            Long unavailableUntil = proxies[i].getUnavailableUntil();
            if (unavailableUntil == null || unavailableUntil <= currentTime) {
                return proxies[i];
            }
        }
        return null;
    }

    private ProxyServer findProxyByHost(String host) {
        return proxyConfigurations.get(host);
    }

    public void markHostAsUnavailableForOneHour(String host) {
        ProxyServer proxyServer = findProxyByHost(host);
        if (proxyServer != null) {
            log.info("Proxy {} marked as unvailable", proxyServer.getHost());
            proxyServer.setUnavailableUntil(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(delay));
//            proxyServer.setUnavailableUntil(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(delay));
        }
    }

}
