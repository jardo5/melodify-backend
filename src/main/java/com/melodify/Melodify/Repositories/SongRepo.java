package com.melodify.Melodify.Repositories;

import com.melodify.Melodify.Models.Song;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Set;

public interface SongRepo extends MongoRepository<Song, String>{
    @Query("{ 'sentiment': ?0 }")
    List<Song> findSongsBySentiment(String sentiment);

    @Query("{ 'artist': ?0 }")
    List<Song> findSongsByArtist(String artist);

    @Query("{ 'album.name': ?0 }")
    List<Song> findSongsByAlbum(String albumName);

    @Query("{ '$or': [ {'artist': ?0}, {'album.name': ?1} ] }")
    List<Song> findSongsByArtistOrAlbum(String artist, String albumName);

    @Query("{ '_id': { '$in': ?0 } }")
    List<Song> findAllByIdIn(List<String> ids);

}
