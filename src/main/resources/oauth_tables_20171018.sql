/*
* Table oauth_access_token
*/
CREATE TABLE IF NOT EXISTS `oauth_access_token` (
`token_id` varchar(256) DEFAULT NULL,
`token` blob,
`authentication_id` varchar(256) DEFAULT NULL,
`user_name` varchar(256) DEFAULT NULL,
`client_id` varchar(256) DEFAULT NULL,
`authentication` blob,
`refresh_token` varchar(256) DEFAULT NULL
);
 
/*
* Table oauth_client_details
*/
CREATE TABLE IF NOT EXISTS `oauth_client_details` (
`client_id` varchar(256) NOT NULL,
`resource_ids` varchar(256) DEFAULT NULL,
`client_secret` varchar(256) DEFAULT NULL,
`scope` varchar(256) DEFAULT NULL,
`authorized_grant_types` varchar(256) DEFAULT NULL,
`web_server_redirect_uri` varchar(256) DEFAULT NULL,
`authorities` varchar(256) DEFAULT NULL,
`access_token_validity` int(11) DEFAULT NULL,
`refresh_token_validity` int(11) DEFAULT NULL,
`additional_information` varchar(4096) DEFAULT NULL,
`autoapprove` varchar(256) DEFAULT NULL,
PRIMARY KEY (`client_id`)
);
 
/*
* Table oauth_code
*/
CREATE TABLE IF NOT EXISTS `oauth_code` (
`code` varchar(256) DEFAULT NULL,
`authentication` blob
);
 
/*
* Table oauth_refresh_token
*/
CREATE TABLE IF NOT EXISTS `oauth_refresh_token` (
`token_id` varchar(256) DEFAULT NULL,
`token` blob,
`authentication` blob
);

REPLACE INTO `oauth_client_details` VALUES ('web', 'pacex', '$2a$10$Poie7MAHyYPoLIuq7RMWkO8b5tfO1gl3Fl4A9VKZi8f1dKqQ3zZ7W', 'read', 'password', NULL, 'ROLE_USER', NULL, NULL, NULL, NULL);
REPLACE INTO `oauth_client_details` VALUES  ('webTwo', 'pacex', '$2a$10$N/8HjHH.5dgo3V6A9iu5jebUwYyB5lYzRzAKdyrMikeXHK42VbV5q', 'read,write,trust', 'refresh_token,password,client_credentials', '', '', NULL, NULL, '{}', '');

