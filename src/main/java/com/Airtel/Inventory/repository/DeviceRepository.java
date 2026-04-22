package com.Airtel.Inventory.repository;

import com.Airtel.Inventory.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    List<Device> findByStatus(String status);
    List<Device> findByDeviceType(String deviceType);
    List<Device> findByDeviceCondition(String deviceCondition);
    long countByStatus(String status);
}