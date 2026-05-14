package com.techmatch.resume.service.embedding;

import java.util.List;

public interface EmbeddingService {

    List<Double> embed(String text);
}
