package com.example.processengine.repository;

import com.example.processengine.model.ProcessDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 流程定义数据仓库
 */
@Repository
public interface ProcessDefinitionRepository extends JpaRepository<ProcessDefinition, String> {
}
