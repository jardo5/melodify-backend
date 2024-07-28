package com.melodify.Melodify.Repositories;

import com.melodify.Melodify.Models.TopTrack;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TopTrackRepo extends MongoRepository<TopTrack, String> {
}
