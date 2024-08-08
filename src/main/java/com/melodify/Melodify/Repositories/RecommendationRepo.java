package com.melodify.Melodify.Repositories;

import com.melodify.Melodify.Models.Recommendation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RecommendationRepo extends MongoRepository<Recommendation, String> {
    Optional<Recommendation> findByUserId(String userId);

}
