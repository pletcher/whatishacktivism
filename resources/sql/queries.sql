-- :name create-hn-story! :<! :1
-- :doc creates a new Hacker News story record
INSERT INTO hn_stories
(hn_id, url)
VALUES (:hn_id, :url)
RETURNING *

-- :name find-hn-story-by-id :? :1
-- :doc finds a Hacker News story record with the given id
SELECT * from hn_stories WHERE id = :id LIMIT 1

-- :name create-description! :<! :1
-- :doc creates a new description record of a Hacker News story
INSERT INTO descriptions
(hn_story_id, body)
VALUES (:hn_story_id, :body)
RETURNING *

-- :name find-descriptions-of-story :? :*
-- :doc finds all the descriptions of a Hacker News story
SELECT * from descriptions
WHERE hn_story_id = :hn_story_id LIMIT :limit OFFSET :offset
