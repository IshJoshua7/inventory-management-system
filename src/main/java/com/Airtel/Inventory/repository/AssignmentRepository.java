package com.Airtel.Inventory.repository;

import com.Airtel.Inventory.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByStatus(String status);
    List<Assignment> findByDeviceId(Long deviceId);
    List<Assignment> findByEmployeeId(Long employeeId);
}