package com.touhouqing.crawllangai.model.neo4j.node;

import lombok.Data;
import org.springframework.data.annotation.Version;
import org.springframework.data.neo4j.core.schema.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Node("Participant")
public class ParticipantNode implements Serializable {

    /**
     * 企业名称
     */
    @Id
    private String name;

    @Version
    private Long version;

    @Relationship(type = "中标", direction = Relationship.Direction.OUTGOING)
    private List<ParticipantNode> bidParticipants = new ArrayList<>();

}
