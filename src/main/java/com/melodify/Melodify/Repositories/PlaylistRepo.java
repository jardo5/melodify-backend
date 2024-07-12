package com.melodify.Melodify.Repositories;

import com.melodify.Melodify.Models.Playlist;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlaylistRepo extends MongoRepository<Playlist, String> {
}
