package com.melodify.Melodify.Repositories;

import com.melodify.Melodify.Models.Artist;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArtistRepo extends MongoRepository<Artist, String> {
    
}
