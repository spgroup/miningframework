package org.springframework.boot.web.client;

import java.util.function.Supplier;
import org.springframework.http.client.ClientHttpRequestFactory;

@Deprecated(since = "3.0.0", forRemoval = true)
public class ClientHttpRequestFactorySupplier implements Supplier<ClientHttpRequestFactory> {

    @Override
    public ClientHttpRequestFactory get() {
        return ClientHttpRequestFactories.get(ClientHttpRequestFactorySettings.DEFAULTS);
    }
}