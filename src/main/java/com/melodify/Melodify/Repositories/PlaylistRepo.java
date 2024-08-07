package com.melodify.Melodify.Repositories;

import com.melodify.Melodify.Models.Playlist;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaylistRepo extends MongoRepository<Playlist, String> {
}
