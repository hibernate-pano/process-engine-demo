package com.example.processengine.repository;

import com.example.processengine.model.ProcessInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 流程实例数据仓库
 */
@Repository
public interface ProcessInstanceRepository extends JpaRepository<ProcessInstance, String> {
}
