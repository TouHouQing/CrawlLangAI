package com.touhouqing.crawllangai.service;

import com.touhouqing.crawllangai.model.neo4j.node.ParticipantNode;

public interface ParticipantService {

    void saveParticipant();

    /**
     * 根据名称创建或更新参与方节点
     * @param name 企业名称
     * @return 持久化后的节点
     */
    ParticipantNode upsertParticipantByName(String name);

    /**
     * 创建招标者到中标者的“中标”关系
     * @param tenderer 招标者名称
     * @param winner 中标者名称
     */
    void createBidRelation(String tenderer, String winner);
}
