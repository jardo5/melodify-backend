package com.melodify.Melodify.Repositories;

import com.melodify.Melodify.Models.Artist;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ArtistRepo extends MongoRepository<Artist, String> {
    
}
