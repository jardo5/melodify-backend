package com.melodify.Melodify.Repositories;

import com.melodify.Melodify.Models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    // Find users who have liked at least one song from a list of song IDs
    @Query("{ 'likedSongs': { '$in': ?0 } }")
    List<User> findUsersWhoLikedSongs(List<String> songIds);

    // Find users who liked a specific song
    @Query("{ 'likedSongs': ?0 }")
    List<User> findUsersByLikedSong(String songId);

    // Find all users except the current user, ordered by a custom field
    @Query("{ '_id': { '$ne': ?0 } }")
    List<User> findAllExceptUser(String userId);
}
