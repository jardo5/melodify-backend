package com.melodify.Melodify.Repositories;

import com.melodify.Melodify.Models.Recommendation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RecommendationRepo extends MongoRepository<Recommendation, String> {
}
