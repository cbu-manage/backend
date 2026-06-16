package com.example.cbumanage.global.setting.repository;

import com.example.cbumanage.global.setting.entity.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemSettingRepository extends JpaRepository<SystemSetting, String> {
}
