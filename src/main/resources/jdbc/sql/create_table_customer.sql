

CREATE TABLE Customer (
  id INT NOT NULL AUTO_INCREMENT,
  firstName VARCHAR(45) NOT NULL,
  middleInitial VARCHAR(1) NULL,
  lastName VARCHAR(45) NOT NULL,
  address VARCHAR(45) NOT NULL,
  city VARCHAR(45) NOT NULL,
  state VARCHAR(45) NOT NULL,
  zipCode CHAR(5) NULL,
  PRIMARY KEY (id)
);
