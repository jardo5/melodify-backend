package com.melodify.Melodify.Repositories;

import com.melodify.Melodify.Models.Song;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SongRepo extends MongoRepository<Song, String>{
}
