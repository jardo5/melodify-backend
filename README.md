<h1 align="center">
  <img src="https://i.imgur.com/PrndwKF.png" alt="Melodify" width="300" height="300">
</h1>

<h1 align="center">
Melodify - Spring Boot Backend
</h1>

## Main Tools Used

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Apache Maven](https://img.shields.io/badge/Apache%20Maven-C71A36?style=for-the-badge&logo=Apache%20Maven&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-%234ea94b.svg?style=for-the-badge&logo=mongodb&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens)
![Spotify](https://img.shields.io/badge/Spotify-1ED760?style=for-the-badge&logo=spotify&logoColor=white)
![ChatGPT](https://img.shields.io/badge/chatGPT-74aa9c?style=for-the-badge&logo=openai&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-%23FF9900.svg?style=for-the-badge&logo=amazon-aws&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/github%20actions-%232671E5.svg?style=for-the-badge&logo=githubactions&logoColor=white)



## Introduction
Melodify is a Spring Boot backend service designed to power a music-related application. It interacts with various external APIs, such as Spotify and Genius, to fetch, analyze, and manage song data. The backend provides several endpoints for managing user accounts, songs, artists, and other related resources. This README will guide you through the setup, usage, and functionalities provided by Melodify.

## Table of Contents ## 
- [Introduction](#introduction)
- [Installation](#installation)
- [Usage](#usage)
- [Endpoints](#endpoints)
  - [Songs](#songs)
  - [Users](#users)
  - [Admin](#admin)
  - [Artists](#artists)
  - [Authentication](#authentication)
  - [Recommendations](#recommendations)
- [External API Integrations](#external-api-integrations)
- [Dependencies](#dependencies)
- [Configuration](#configuration)
- [Be Aware](#be-aware)
- [License](#license)

## Installation
To set up the Melodify backend, follow these steps:

1.**Clone the repository:**    
```bash
    git clone https://github.com/your-repo/melodify-backend.git
    cd melodify-backend
```
2.**Configure environment variables:**
```
Add a .env file to the root of the project. With the following keys:
    GENIUS_API_KEY=
    SPOTIFY_CLIENT_ID=
    SPOTIFY_CLIENT_SECRET=
    SPOTIFY_REDIRECT_URI=
    LYRICS_API_URL= (lyricsovh API Key)
    SENTIMENT_API_KEY= (GPT API Key)
    SENTIMENT_PROMPT= (GPT Prompt return JSON format)
    MONGODB_URI=
    MONGODB_DATABASE_NAME==
    JWT_KEY=

```
3.**Build the project using Maven:**    
```bash
mvn clean install
```

4.**Run the application:**    
```bash
mvn spring-boot:run
```
## Usage
Once the application is running, you can interact with the various endpoints to manage songs, users, and other resources. You can use tools like Postman or curl to test the endpoints.

## Endpoints ##
## Songs ##

-**POST `/songs/batch`**  
  Process and store a batch of songs.

-**GET `/songs/search`**  
  Search for songs based Title and Artist(s).

-**GET `/songs/top`**  
  Retrieve Top 50 USA songs based on Spotify playlist.

-**GET `/songs/{id}`**  
  Get detailed information about a specific song.

-**GET `/songs/fetch-persist-songs`**  
  Fetch and persist songs to DB. (**DO NOT ABUSE THIS ENDPOINT. TESTING ONLY**)

-**GET `/songs/lyrics`**  
  Retrieve lyrics for a specific song. (Testing purposes)

-**GET `/songs/sentiment`**  
  Analyze and return the sentiment of a song's lyrics. (Testing purposes)

## Users ##

-**GET `/users/info`**  
  Retrieve information about the authenticated user.

-**POST `/users/login`**  
  Authenticate a user and return a JWT token.

-**POST `/users/signup`**  
  Register a new user.

-**POST `/users/{userId}/dislike`**  
  Dislike a song for a user.

-**DELETE `/users/{userId}/disliked/{songId}`**  
  Remove a song from the user's disliked list.

-**POST `/users/{userId}/like`**  
  Like a song for a user.

-**DELETE `/users/{userId}/liked/{songId}`**  
  Remove a song from the user's liked list.

-**POST `/users/{userId}/save`**  
  Save a song for a user.

-**DELETE `/users/{userId}/saved/{songId}`**  
  Remove a song from the user's saved list.

## Admin ##
**GET `/admin/dashboard`**  
  Access the admin dashboard. (**Not Implemented Yet**)

## Artists ##
-**GET `/artists/{artistId}`**  
  Retrieve information about a specific artist.

## Authentication ##
-**GET `/auth/providers/spotify/callback`**  
  Handle Spotify OAuth callback.

-**GET `/auth/providers/spotify/login`**  
  Initiate Spotify OAuth login.

## Recommendations ##
-**GET `/recommendations`**  
  Retrieve song recommendations for the user.

-**POST `/recommendations/refresh`**  
  Refresh and update song recommendations.

## External API Integrations ##
-**POST**`https://accounts.spotify.com/api/token`  
  Obtain a token from Spotify for authentication.

-**GET**`https://api.spotify.com/v1/playlists/{playlistId}/tracks`  
  Fetch tracks from a Spotify playlist.

-**POST**`https://api.openai.com/v1/chat/completions`  
  Interact with OpenAI's API for various purposes.

-**GET**`https://api.genius.com/artists/{artistId}`  
  Retrieve artist information from Genius.

-**GET**`https://api.genius.com/search?q={query}`  
  Search for songs or artists on Genius.

-**GET**`https://api.genius.com/songs/{songId}`  
  Get details about a song from Genius.

## Dependencies ##
Melodify relies on the following key dependencies:

**Spring Boot Starters:**  
    `spring-boot-starter-data-mongodb`  
    `spring-boot-starter-oauth2-client`  
    `spring-boot-starter-web`  
    `spring-boot-starter-actuator`  
    `spring-boot-starter-test`

**Development Tools:**  
    `spring-boot-devtools` (optional)

**Security:**  
    `spring-security-test`\
    `jjwt-api`\
    `jjwt-impl`\
    `jjwt-jackson`

**Libraries:**\
    `org.json:json` (for JSON processing)\
    `org.mongodb:mongodb-driver-sync` (for MongoDB interactions)\
    `io.github.cdimascio:dotenv-java` (for loading environment variables)

**Testing:**  
    `de.flapdoodle.embed:de.flapdoodle.embed.mongo` (embedded MongoDB for tests)

**Utilities:**  
    -`org.projectlombok:lombok`

## Configuration
If needed, edit the `application.properties`:
  
```
spring.application.name=Melodify
spring.data.mongodb.uri=${MONGODB_URI}
spring.data.mongodb.database=${MONGODB_DATABASE_NAME}
rateLimiter.permitsPerSecond=10
server.port=80
```
## Be Aware

**API rate limits:**  
  Be aware of the rate limits imposed by external APIs (Spotify, Genius, OpenAI, Lyricsovh)


## License
This project is licensed under the MIT License. See the LICENSE file for more details.
