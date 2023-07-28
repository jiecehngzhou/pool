CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT '',
  `age` int(3) DEFAULT NULL,
  `sex` varchar(2) DEFAULT '',
  `crate_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

#注意$$后留空格
DELIMITER $$ 
DROP FUNCTION IF EXISTS excel_data $$
CREATE FUNCTION excel_data()
RETURNS INT
BEGIN
   DECLARE num INT DEFAULT 1000000;
   DECLARE i INT DEFAULT 0;
   WHILE i<num DO
    INSERT INTO user(`name`,`age`,`sex`) 
    VALUES(CONCAT('测试用户',i), 18,'男');
    SET i=i+1;
   END WHILE;
   RETURN i;
END

select excel_data();