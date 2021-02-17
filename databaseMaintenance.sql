SELECT * FROM BasicUsers
SELECT * FROM Projects
SELECT * FROM UserProjectAssociations
SELECT * FROM Bugs


--CREATE TABLE UserProjectAssociations
--(
--	username varchar(50),
--	project_id int,

--	CONSTRAINT FK_username FOREIGN KEY (username) REFERENCES BasicUsers(username) ON DELETE CASCADE ON UPDATE CASCADE,
--	CONSTRAINT FK_project_id FOREIGN KEY (project_id) REFERENCES Projects(id) ON DELETE CASCADE ON UPDATE CASCADE
--)

--CREATE TABLE Projects
--(
--	id int PRIMARY KEY,
--	leader varchar(30) NOT NULL,
--	[name] varchar(30) NOT NULL,
--	[description] varchar(200)
--);

--CREATE TABLE Bugs
--(
--	id int PRIMARY KEY,
--	project_id int NOT NULL,
--	[priority] int DEFAULT 10,
--	complexity int DEFAULT 0,
--	[description] varchar(400),
--	username varchar(50),
--	date_issued datetime,
--	[status] varchar(20) 

--	CONSTRAINT FK__username_Bugs FOREIGN KEY (username) REFERENCES BasicUsers(username) ON DELETE CASCADE ON UPDATE CASCADE,
--	CONSTRAINT FK_project_id_Bugs FOREIGN KEY (project_id) REFERENCES Projects(id) ON DELETE CASCADE ON UPDATE CASCADE
--);

INSERT INTO Projects VALUES(1,'user1','fistProjectinho','nothing specific');

INSERT INTO UserProjectAssociations VALUES('user2',1);
INSERT INTO UserProjectAssociations VALUES('user1',1);

DROP TABLE Projects;
DROP TABLE UserProjectAssociations;

