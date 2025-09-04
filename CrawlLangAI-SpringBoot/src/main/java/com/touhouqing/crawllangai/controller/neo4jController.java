package com.touhouqing.crawllangai.controller;

import com.touhouqing.crawllangai.common.Result;
import com.touhouqing.crawllangai.service.ParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/neo4j")
@RequiredArgsConstructor
public class neo4jController {

    private final ParticipantService participantService;

    @PostMapping
    public Result test() {
        participantService.saveParticipant();
        return Result.success();
    }
}
