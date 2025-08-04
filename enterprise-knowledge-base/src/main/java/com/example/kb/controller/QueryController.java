package com.example.kb.controller;

import com.example.kb.model.QueryEntity;
import com.example.kb.model.Records.QueryRequest;
import com.example.kb.model.Records.QueryResult;
import com.example.kb.service.QueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/query")
public class QueryController {

    private final QueryService queryService;

    public QueryController(QueryService queryService) {
        this.queryService = queryService;
    }

    @PostMapping
    public ResponseEntity<QueryResult> query(@RequestBody QueryRequest request) {
        QueryResult result = queryService.query(request.question(), request.userId(), request.category());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public ResponseEntity<List<QueryEntity>> getHistory(
            @RequestParam(defaultValue = "10") int limit, @RequestParam(value = "userId", defaultValue = "admin") String userId) {
        List<QueryEntity> history = queryService.getQueryHistory(userId, limit);
        return ResponseEntity.ok(history);
    }
}