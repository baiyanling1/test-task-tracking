package com.testtracking.service;

import com.testtracking.entity.SystemConfig;
import com.testtracking.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigService {

	private final SystemConfigRepository systemConfigRepository;

	@Transactional(readOnly = true)
	public Optional<String> getValue(String key) {
		return systemConfigRepository.findByConfigKey(key).map(SystemConfig::getConfigValue);
	}

	@Transactional
	public void setValue(String key, String value, String description) {
		SystemConfig config = systemConfigRepository.findByConfigKey(key).orElseGet(SystemConfig::new);
		config.setConfigKey(key);
		config.setConfigValue(value);
		if (description != null) {
			config.setDescription(description);
		}
		LocalDateTime now = LocalDateTime.now();
		if (config.getCreatedTime() == null) {
			config.setCreatedTime(now);
		}
		config.setUpdatedTime(now);
		systemConfigRepository.save(config);
		log.info("系统配置已保存: {}={}", key, value);
	}
}


