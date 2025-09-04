package com.touhouqing.crawllangai.repository;

import com.touhouqing.crawllangai.model.neo4j.node.ParticipantNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticipantRepository extends Neo4jRepository<ParticipantNode, String> {
    /**
     * 根据企业名称查询企业节点
     *
     * @param name 企业名称
     * @return 企业节点
     */
    ParticipantNode findByName(String name);


}
