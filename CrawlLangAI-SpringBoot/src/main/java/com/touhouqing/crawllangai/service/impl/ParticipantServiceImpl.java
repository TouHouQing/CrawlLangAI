package com.touhouqing.crawllangai.service.impl;

import com.touhouqing.crawllangai.model.neo4j.node.ParticipantNode;
import com.touhouqing.crawllangai.repository.ParticipantRepository;
import com.touhouqing.crawllangai.service.ParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParticipantServiceImpl implements ParticipantService {

    private final ParticipantRepository participantRepository;

    @Override
    public void saveParticipant() {
        ParticipantNode participantNode = new ParticipantNode();
        participantNode.setName("企业1");
        participantRepository.save(participantNode);
    }

    @Override
    public ParticipantNode upsertParticipantByName(String name) {
        ParticipantNode existing = participantRepository.findByName(name);
        if (existing != null) {
            return existing;
        }
        ParticipantNode node = new ParticipantNode();
        node.setName(name);
        return participantRepository.save(node);
    }

    @Override
    public void createBidRelation(String tenderer, String winner) {
        ParticipantNode tendererNode = upsertParticipantByName(tenderer);
        ParticipantNode winnerNode = upsertParticipantByName(winner);
        // 将中标者加入招标者的 outgoing "中标" 列表
        List<ParticipantNode> bidParticipants = tendererNode.getBidParticipants();
        if (bidParticipants.stream().noneMatch(p -> winner.equals(p.getName()))) {
            bidParticipants.add(winnerNode);
        }
        participantRepository.save(tendererNode);
    }
}
