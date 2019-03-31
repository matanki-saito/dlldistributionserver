CREATE TABLE IF NOT EXISTS exe
(
   id INT(11) NOT NULL AUTO_INCREMENT,
   md5 VARCHAR(32) NOT NULL,
   version VARCHAR(100) NOT NULL,
   description VARCHAR(1000) NULL DEFAULT NULL,
   phase VARCHAR(45) NOT NULL DEFAULT 'prod',
   distribution_asset_id INT(11) NULL DEFAULT NULL,
   github_repo_id INT(11) NOT NULL,
   PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS `file`
(
  `md5` VARCHAR(32) NOT NULL,
  `asset_id` INT(11) NOT NULL,
  `data` MEDIUMBLOB NULL DEFAULT NULL,
  `data_url` VARCHAR(512) NULL DEFAULT NULL,
  `data_size` INT(11) NULL DEFAULT NULL,
  PRIMARY KEY (`asset_id`)
);