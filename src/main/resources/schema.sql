CREATE TABLE IF NOT EXISTS films (
        id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
        name varchar(40) NOT NULL,
        description varchar(200) NOT NULL,
        release_date date,
        duration_min integer
); 

CREATE TABLE IF NOT EXISTS genre_assignments (
        id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
        film_id integer,
        genre_id integer
);

CREATE TABLE IF NOT EXISTS genres (
        id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
		name varchar(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS MPA_assignments (
        id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
        film_id integer,
        mpa_id integer
);

CREATE TABLE IF NOT EXISTS MPAS (
        id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
		name varchar(20) NOT NULL
);


CREATE TABLE IF NOT EXISTS users (
        id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
        login varchar(30) NOT NULL,
        name varchar(30) NOT NULL,
        birthday date,
		email varchar(70) NOT NULL
);

CREATE TABLE IF NOT EXISTS user_friendlists (
        id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
        user_id integer NOT NULL
);

CREATE TABLE IF NOT EXISTS user_friendlist_entries (
        id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
        friendlist_id integer NOT NULL,
		associated_user_id integer NOT NULL,
		status varchar(30) NOT NULL
);

CREATE TABLE IF NOT EXISTS film_likes (
        id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
        film_id integer NOT NULL,
		user_id integer NOT NULL
);

ALTER TABLE genre_assignments
ADD CONSTRAINT IF NOT EXISTS fk_film_id_films
FOREIGN KEY (film_id) REFERENCES films ON DELETE CASCADE;

ALTER TABLE genre_assignments
ADD CONSTRAINT IF NOT EXISTS fk_genre_id_genres
FOREIGN KEY (genre_id) REFERENCES genres ON DELETE CASCADE;

ALTER TABLE MPA_assignments
ADD CONSTRAINT IF NOT EXISTS fk_film_id_films
FOREIGN KEY (film_id) REFERENCES films ON DELETE CASCADE;

ALTER TABLE MPA_assignments
ADD CONSTRAINT IF NOT EXISTS fk_mpa_id_mpas
FOREIGN KEY (mpa_id) REFERENCES MPAS ON DELETE CASCADE;

ALTER TABLE film_likes
ADD CONSTRAINT IF NOT EXISTS fk_film_id_films
FOREIGN KEY (film_id) REFERENCES films ON DELETE CASCADE;

ALTER TABLE film_likes
ADD CONSTRAINT IF NOT EXISTS fk_user_id_users
FOREIGN KEY (user_id) REFERENCES users ON DELETE CASCADE;

ALTER TABLE user_friendlists
ADD CONSTRAINT IF NOT EXISTS fk_user_id_users
FOREIGN KEY (user_id) REFERENCES users ON DELETE CASCADE;

ALTER TABLE user_friendlist_entries
ADD CONSTRAINT IF NOT EXISTS fk_friendlist_id_user_friendlists
FOREIGN KEY (friendlist_id) REFERENCES user_friendlists ON DELETE CASCADE;

ALTER TABLE USERS 
ADD CONSTRAINT IF NOT EXISTS unique_login UNIQUE(login);

ALTER TABLE GENRES
ADD CONSTRAINT IF NOT EXISTS unique_name UNIQUE(name);
