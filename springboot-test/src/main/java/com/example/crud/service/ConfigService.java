package com.example.crud.service;

import com.example.crud.repository.ConfigRepository;
import org.springframework.stereotype.Service;

@Service
public class ConfigService {

    private final ConfigRepository configRepository;

    public ConfigService(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    public boolean isExcludeTestDataEnabled() {
        return configRepository.isEnabled("exclude_test_data_enabled");
    }
}
