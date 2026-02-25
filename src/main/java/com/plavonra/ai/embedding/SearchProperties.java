package com.plavonra.ai.embedding;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.search")
public record SearchProperties(int topK, double similarityThreshold) {}
