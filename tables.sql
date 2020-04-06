CREATE DATABASE IF NOT EXISTS Project;
USE Project;
CREATE TABLE Facility(
    fid INT PRIMARY KEY AUTO_INCREMENT,
    address CHAR(100),
    name CHAR(30),
    description TEXT(500)
);
CREATE TABLE Room(
    rid INT,
    fid INT,
    PRIMARY KEY(rid, fid),
    FOREIGN KEY(fid) REFERENCES Facility(fid)
);
CREATE TABLE ClassType(
    title CHAR(20) PRIMARY KEY
);
CREATE TABLE Status(
    sType CHAR(20) PRIMARY KEY,
    cost REAL
);
CREATE TABLE Instructor(
    iid INT PRIMARY KEY AUTO_INCREMENT,
    name CHAR(20),
    salary REAL
);
CREATE TABLE ClassT(
    title CHAR(40) PRIMARY KEY,
    description TEXT(300),
    type CHAR(20) NOT NULL,
    FOREIGN KEY(type) REFERENCES ClassType(title)
);
CREATE TABLE Class(
    time TIMESTAMP,
    title CHAR(40) NOT NULL,
    capacity INT,
    rid INT,
    fid INT,
    iid INT NOT NULL,
    PRIMARY KEY(time, rid, fid),
    FOREIGN KEY(rid, fid) REFERENCES Room(rid, fid),
    FOREIGN KEY(iid) REFERENCES Instructor(iid),
    FOREIGN KEY(title) REFERENCES ClassT(title)
);
CREATE TABLE CreditCard (
    num BIGINT PRIMARY KEY,
    expiryDate DATE,
    csv INT,
    nameOnCard CHAR(20)
);
CREATE TABLE PAPAccount (
    accountNumber INT PRIMARY KEY,
    bankNumber INT,
    transitNumber INT
);
CREATE TABLE Payment(
    pid INT PRIMARY KEY AUTO_INCREMENT,
    frequency CHAR(10),
    creditCardNumber BIGINT,
    accountNumber INT,
    FOREIGN KEY(creditCardNumber) REFERENCES CreditCard(num),
    FOREIGN KEY(accountNumber) REFERENCES PAPAccount(accountNumber)
);
CREATE TABLE LetsYouTake(
    classType CHAR(20),
    sType CHAR(20),
    PRIMARY KEY(classType, sType),
    FOREIGN KEY(classType) REFERENCES ClassType(title),
    FOREIGN KEY(sType) REFERENCES Status(sType)
);
CREATE TABLE Member(
    mid INT PRIMARY KEY auto_increment,
    login CHAR(20) NOT NULL UNIQUE,
    password BINARY(16) NOT NULL,
    address CHAR(100),
    phoneNumber CHAR(10),
    email CHAR(30),
    name CHAR(20),
    birthDate DATE,
    sType CHAR(20) NOT NULL,
    FOREIGN KEY(stype) REFERENCES Status(sType)
);

CREATE TABLE Cleaner(
    staffID INT PRIMARY KEY,
    hourlySalary REAL,
    seniorityLevel INT,
    name CHAR(20)
);
CREATE TABLE Equipment(
    serialNumber INT PRIMARY KEY,
    itemName CHAR(20),
    quantity INT
);
CREATE TABLE Rates(
    mid INT,
    iid INT,
    rating INT,
    PRIMARY KEY(mid, iid),
    FOREIGN KEY(mid) REFERENCES Member(mid),
    FOREIGN KEY(iid) REFERENCES Instructor(iid),
    CHECK ( 1 <= rating <= 5 )
);

CREATE TABLE CanHave(
    rid INT,
    fid INT,
    classType CHAR(20),
    PRIMARY KEY(rid, fid, classType),
    FOREIGN KEY(rid, fid) REFERENCES Room(rid, fid),
    FOREIGN KEY(classType) REFERENCES ClassType(title)
);

CREATE VIEW InvalidClass as SELECT C.*
FROM Class C NATURAL JOIN ClassT CT
WHERE CT.type NOT IN(
    SELECT classType
    FROM CanHave CH
    WHERE CH.rid = C.rid AND CH.fid = C.fid
    );

CREATE TABLE Takes(
    mid INT,
    time TIMESTAMP,
    rid INT,
    fid INT,
    PRIMARY KEY(mid, time, rid, fid),
    FOREIGN KEY(mid) REFERENCES Member(mid),
    FOREIGN KEY(time, rid, fid) REFERENCES Class(time, rid, fid) ON DELETE CASCADE
);

CREATE TABLE MemberPayment(
    mid INT,
    pid INT,
    PRIMARY KEY(mid, pid),
    FOREIGN KEY(mid) REFERENCES Member(mid),
    FOREIGN KEY(pid) REFERENCES Payment(pid)
);
CREATE TABLE Cleans(
    fid INT,
    staffID INT,
    PRIMARY KEY(fid, staffID),
    FOREIGN KEY(fid) REFERENCES Facility(fid),
    FOREIGN KEY(staffID) REFERENCES Cleaner(staffID)
);
CREATE TABLE Provides(
    fid INT,
    serialNumber INT,
    PRIMARY KEY(fid, serialNumber),
    FOREIGN KEY(fid) REFERENCES Facility(fid),
    FOREIGN KEY(serialNumber) REFERENCES Equipment(serialNumber)
);
CREATE VIEW ratedInstructors AS
    SELECT i.*, (SELECT AVG(rating) from Rates r WHERE r.iid = i.iid) as avgRating
    FROM Instructor i;


CREATE TRIGGER mustBeAllowedToTakeI BEFORE INSERT ON Takes
    FOR EACH ROW
    IF NOT EXISTS(
            SELECT *
            FROM Class c NATURAL JOIN ClassT ct, Member m NATURAL JOIN LetsYouTake lyt
            WHERE c.time = NEW.time AND c.fid = NEW.fid AND c.rid = NEW.rid AND m.mid = NEW.mid AND lyt.classType = ct.type
        ) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'member cannot take this class';
    END IF;
CREATE TRIGGER mustBeAllowedToTakeU BEFORE UPDATE ON Takes
    FOR EACH ROW
    IF NOT EXISTS(
            SELECT *
            FROM Class c NATURAL JOIN ClassT ct, Member m NATURAL JOIN LetsYouTake lyt
            WHERE c.time = NEW.time AND c.fid = NEW.fid AND c.rid = NEW.rid AND m.mid = NEW.mid AND lyt.classType = ct.type
        ) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'member cannot take this class';
    END IF;

CREATE TRIGGER cantLowerStatusWhenTakingNotAllowedClasses BEFORE UPDATE ON Member
    FOR EACH ROW
    IF EXISTS(
            SELECT ct.title
            FROM Takes t NATURAL JOIN Class c NATURAL JOIN ClassT ct
            WHERE NEW.mid = t.mid AND c.time > CURRENT_TIMESTAMP AND ct.type NOT IN(
                SELECT l.classType
                FROM LetsYouTake l
                WHERE l.sType = NEW.sType
                )
        ) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'member cannot lower status while still taking courses that require the higher status';
    END IF;

CREATE TRIGGER classTypeInRooms_ClassT_UPDATE BEFORE UPDATE ON ClassT
    FOR EACH ROW IF NOT EXISTS(
        SELECT *
        FROM Class C NATURAL JOIN CanHave CH
        WHERE C.title = NEW.title AND CH.classType = NEW.type
        )
    THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'This Change will cause a room to have a class with invalid type in it';
    END IF;
CREATE TRIGGER classTypeInRooms_CanHave_UPDATE BEFORE UPDATE ON CanHave
    FOR EACH ROW IF EXISTS(
        SELECT *
        FROM Class C NATURAL JOIN ClassT CT
        WHERE OLD.rid = C.rid AND OLD.fid = OLD.fid AND OLD.classType = CT.type
        )
    THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'This Change will cause a room to have a class with invalid type in it';
    END IF;
CREATE TRIGGER classTypeInRooms_CanHave_DELETE BEFORE DELETE ON CanHave
    FOR EACH ROW IF EXISTS(
        SELECT *
        FROM Class C NATURAL JOIN ClassT CT
        WHERE OLD.rid = C.rid AND OLD.fid = OLD.fid AND OLD.classType = CT.type
        )
    THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'This Change will cause a room to have a class with invalid type in it';
    END IF;
CREATE TRIGGER classTypeInRooms_Class_Insert BEFORE INSERT ON Class
    FOR EACH ROW IF EXISTS(
        SELECT CT.type
        FROM ClassT CT
        WHERE NEW.title = CT.title AND CT.type NOT IN(
            SELECT CH.classType
            FROM CanHave CH
            WHERE CH.fid = NEW.fid AND CH.rid = NEW.rid
            )
        )
    THEN  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'This Change will cause a room to have a class with invalid type in it';
    END IF;
CREATE TRIGGER classTypeInRooms_Class_Update BEFORE INSERT ON Class
    FOR EACH ROW IF EXISTS(
        SELECT CT.type
        FROM ClassT CT
        WHERE NEW.title = CT.title AND CT.type NOT IN(
            SELECT CH.classType
            FROM CanHave CH
            WHERE CH.fid = NEW.fid AND CH.rid = NEW.rid
            )
        )
    THEN  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'This Change will cause a room to have a class with invalid type in it';
    END IF;

INSERT INTO classtype (title) VALUES ('Dance');
INSERT INTO classtype (title) VALUES ('BootCamp');
INSERT INTO classtype (title) VALUES ('Cardio');
INSERT INTO classtype (title) VALUES ('HIIT');
INSERT INTO classtype (title) VALUES ('GymSports');
INSERT INTO classtype (title) VALUES ('Swimming');
INSERT INTO classtype (title) VALUES ('Skating');
INSERT INTO classtype (title) VALUES ('Basketball');
INSERT INTO classtype (title) VALUES ('Tennis');

INSERT INTO instructor (name, salary) VALUES ('Carlos Hyde', 60000);
INSERT INTO instructor (name, salary) VALUES ('David ', 25000);
INSERT INTO instructor (name, salary) VALUES ('Sukh ', 25000);
INSERT INTO instructor (name, salary) VALUES ('Hastee', 25000);
INSERT INTO instructor (name, salary) VALUES ('Elon Musk', 90000);

INSERT INTO cleaner (staffID, hourlySalary, seniorityLevel, name) VALUES (1, 14, null, 'Ronald McDonald');
INSERT INTO cleaner (staffID, hourlySalary, seniorityLevel, name) VALUES (2, 12.75, null, 'Chuck E. Cheese');
INSERT INTO cleaner (staffID, hourlySalary, seniorityLevel, name) VALUES (3, 15, null, 'Tim Horton');
INSERT INTO cleaner (staffID, hourlySalary, seniorityLevel, name) VALUES (4, 13, null, 'Arnie McFly');
INSERT INTO cleaner (staffID, hourlySalary, seniorityLevel, name) VALUES (5, 22, null, 'Huncho Jack');

INSERT INTO equipment (serialNumber, itemName, quantity) VALUES (1, 'Skipping Rope', 30);
INSERT INTO equipment (serialNumber, itemName, quantity) VALUES (2, 'Resistance Bands', 30);
INSERT INTO equipment (serialNumber, itemName, quantity) VALUES (3, 'Yoga Mats', 25);
INSERT INTO equipment (serialNumber, itemName, quantity) VALUES (4, 'Speaker', 2);
INSERT INTO equipment (serialNumber, itemName, quantity) VALUES (5, 'Towels', 84);

INSERT INTO status (sType, cost) VALUES ('Bronze', 29);
INSERT INTO status (sType, cost) VALUES ('Silver', 39);
INSERT INTO status (sType, cost) VALUES ('Gold', 49);
INSERT INTO status (sType, cost) VALUES ('Platinum', 59);
INSERT INTO status (sType, cost) VALUES ('Student', 29);
INSERT INTO status (sType, cost) VALUES ('Senior', 35);
INSERT INTO status (sType, cost) VALUES ('Low-Income', 19);

INSERT INTO facility (address, name, description) VALUES ('8034 152 St, Surrey, BC', 'Surrey Athleisure Centre', 'The Langley Athleisure Centre is home to many public programs and private classes. We have been offering training programs and events at this facility since 2008 and are dedicated to create the best training experience in an indoor setting.');
INSERT INTO facility (address, name, description) VALUES ('2134 West 12 Ave, Vancouver BC', 'Vancouver Hockey Centre', 'The Vancouver Hockey Center is dedicated to creating opportunities for Canadians to play the national sport: Hockey. We were established in 2001 and offer public and private classes for all age ranges. Please check our schedule for drop-in classes.');
INSERT INTO facility (address, name, description) VALUES ('34-6454 Kingsqway, Burnaby, BC', 'Burnaby Aquatics Arena', 'Welcome to Burnaby Aquatics Arena. We have been offering private and public swimming classes at our facility since our establishment in 2010 for all  age range and drop-ins are always welcome. Our mission is to help our community to enhance their quality of life through aquatic exercises.');
INSERT INTO facility (address, name, description) VALUES ('3487 Haney Place, Coquitlam, BC', 'Best Training Facility', 'Welcome to Best Training Facility. Our facility was established in 2005 and has been offering various drop-in public and private training classes and fitness programs to enhance the quality of life of all of our members.');
INSERT INTO facility (address, name, description) VALUES ('2204 196 St, Langley, BC', 'Langley Athleisure Centre', 'The Langley Athleisure Centre is home to many public programs and private classes. We have been offering training programs and events at this facility since 2008 and are dedicated to create the best training experience in an indoor setting.');

INSERT INTO room (rid, fid) VALUES (100, 1);
INSERT INTO room (rid, fid) VALUES (101, 1);
INSERT INTO room (rid, fid) VALUES (102, 1);
INSERT INTO room (rid, fid) VALUES (103, 1);
INSERT INTO room (rid, fid) VALUES (104, 1);
INSERT INTO room (rid, fid) VALUES (105, 1);
INSERT INTO room (rid, fid) VALUES (100, 2);
INSERT INTO room (rid, fid) VALUES (101, 2);
INSERT INTO room (rid, fid) VALUES (102, 2);
INSERT INTO room (rid, fid) VALUES (101, 3);
INSERT INTO room (rid, fid) VALUES (102, 3);
INSERT INTO room (rid, fid) VALUES (103, 3);
INSERT INTO room (rid, fid) VALUES (100, 4);
INSERT INTO room (rid, fid) VALUES (101, 4);
INSERT INTO room (rid, fid) VALUES (102, 4);
INSERT INTO room (rid, fid) VALUES (103, 4);
INSERT INTO room (rid, fid) VALUES (104, 4);
INSERT INTO room (rid, fid) VALUES (100, 5);
INSERT INTO room (rid, fid) VALUES (101, 5);
INSERT INTO room (rid, fid) VALUES (102, 5);

INSERT INTO member(login, password, address, phoneNumber, email, name, birthDate, sType) VALUES ('imiller', X'a61a78e492ee60c63ed8f2bb3a6a0072','4444 128 St, Surrey, BC, V3X1T8', '6043442423', 'imiller@gmail.com', 'Ian Miller', '1978-12-12', 'Silver');
#password: "pa$$word"
INSERT INTO member(login, password, address, phoneNumber, email, name, birthDate, sType) VALUES ('jonh_w', X'd0763edaa9d9bd2a9516280e9044d885', '1243 Westwood Drive, Coquitlam, BC, V3B4S7', '7782443445', 'jonh.westbrrok@alumni.ubc.ca', 'John Westbrrok', '2001-08-04', 'Student');
#password: "monkey"
INSERT INTO member(login, password, address, phoneNumber, email, name, birthDate, sType) VALUES ('henry_cavill', X'2632a9189905c888ead002e11e5c4446', '12-3443 54 Ave, Vancouver, BC, V5X3B4', '6042557546', 'henry_cavill@shaw.ca', 'Henry Cavill', '1948-04-25', 'Senior');
#password: "asdfghjkl;'"
INSERT INTO member(login, password, address, phoneNumber, email, name, birthDate, sType) VALUES ('saqon_barkley', X'e10adc3949ba59abbe56e057f20f883e', '4534 12 Ave, Burnaby, BC, V3N2J1', '6041236533', 'sbarkley96@gmail.com', 'Saquon Barkley', '1996-01-19', 'Low-Income');
#password "123456"
INSERT INTO member(login, password, address, phoneNumber, email, name, birthDate, sType) VALUES ('russel_wesbrrok', X'35d4785ecc5e3dabfc2edf7542392837', '1243 Westwood Drive, Coquitlam, BC, V3B4S7', '7782347542', 'russel_wesbrrok@shaw.ca', 'Russel Westbrrok', '1970-04-15', 'Platinum');
#password "g0w0_3EWnf"
INSERT INTO member(login, password, address, phoneNumber, email, name, birthDate, sType) VALUES ('lana_smith', X'e99a18c428cb38d5f260853678922e03', '6237 134 St, Langley, BC, V3X1L7', '6044530927', 'lanasmith3@gmail.com', 'Lana Smith','1984-03-12', 'Silver');
#password "abc123"
INSERT INTO member(login, password, address, phoneNumber, email, name, birthDate, sType) VALUES ('jaebell', X'a50a12eee7caab7848728e0a59d96852', '7117 4th West St, Vancouver, BC, V761X3', '6044998076', 'jaebell@yahoo.ca', 'Jae Bell','1987-03-18', 'Gold');
#password "twenty20"
INSERT INTO member(login, password, address, phoneNumber, email, name, birthDate, sType) VALUES ('ben_carter', X'c4bb408471eb7727e59e11385b0a8c19', '4-44 Westbrook Mall, Vancouver, BC, V6S5U5', '7783353535', 'carterben@gmail.com', 'Ben Carter','1996-01-22', 'Student');
#password "hihihi"
INSERT INTO member(login, password, address, phoneNumber, email, name, birthDate, sType) VALUES ('hpotter', X'6403e55883a8a4abf62af4ef7cc85dc1', '545 Pemberton Ave, North Vancouver, BC, V3M0L7', '6049886151', 'hpotter00@gmail.com', 'Harry Potter','1980-08-31', 'Gold');
#password "hedwig"
INSERT INTO member(login, password, address, phoneNumber, email, name, birthDate, sType) VALUES ('estewart', X'4c2a4a7078da0ac6733464eacfd00f86', '777 152 St, Langley, BC, V8H3K7', '6045667656', 'estewart94@gmail.com', 'Elizabeth Stewart','1994-05-12', 'Low-income');
#password "magenta"
INSERT INTO member(login, password, address, phoneNumber, email, name, birthDate, sType) VALUES ('bellaswan', X'a53f3929621dba1306f8a61588f52f55', '15322 Buena Vista Ave, Surrey, BC, V7T6Y6', '6043456789', 'bellaswan@hotmail.net', 'Isabella Swan','1987-09-13', 'Bronze');
#password "edward"
INSERT INTO member(login, password, address, phoneNumber, email, name, birthDate, sType) VALUES ('samantha_r', X'5ed4b687561eea35f0f1f562fc38abf5', '34-62 1st St, Burnaby, BC, V7D5L5', '7781234567', 'samrobin@yahoo.com', 'Samantha Robin','1954-11-03', 'Senior');
#password "223344tt"
INSERT INTO member(login, password, address, phoneNumber, email, name, birthDate, sType) VALUES ('jills22', X'16af54f5d3a0b58c37f9a85d41c7795f', '3190 Parker Blvd, Vancouver, BC, V7P1Y7', '7780998765', 'jills22@gmail.com', 'Jill Steven','1976-10-29', 'Silver');
#password "55tg55"
INSERT INTO member(login, password, address, phoneNumber, email, name, birthDate, sType) VALUES ('willpark', X'aaab09a8fdd7d0ab9d2ff8f4563baa98', '25 111th St, Burnaby, BC, V7P0L0', '7786785474', 'willpark@shaw.ca', 'William Park','1975-05-05', 'Platinum');
#password "random9"


INSERT INTO classt (title, description, type) VALUES ('Abs of Steel', 'Have you ever wanted finely toned abs?  This is the class for you!  This class targets the various abdominal muscles through direct and indirect exercises, while also getting your heart racing!', 'BootCamp');
INSERT INTO classt (title, description, type) VALUES ('Running Machine', 'Do you get tired climbing the stairs?  Come train with us.  We’ll get you in shape and help[ you get that perfect runner body!  This class focuses on aerobic cardio exercises such as skipping rope, biking, and running on the treadmill.  Sign up now!', 'Cardio');
INSERT INTO classt (title, description, type) VALUES ('Shapetastic', 'This class is not for the faint of heart.  Come join us for interval exercises that will have you feeling the burn.  This is one of our hardest classes and will get you that beach body for the summer!', 'HIIT');
INSERT INTO classt (title, description, type) VALUES ('Basketball Skills I', 'Come improve your basketball skills, with a focus on dribbling ands shooting.  The second part of this class consists of scrimmage.  Open to adults only.  All skill levels welcome!', 'Basketball');
INSERT INTO classt (title, description, type) VALUES ('World Dance', 'Shake away your worries and have some fun learning various dances from across the world.  Each class will focus on a dance from a specific country, with guest instructors!  All dancers welcome regardless of experience!', 'Dance');
INSERT INTO classt (title, description, type) VALUES ('Learn to Skate', 'Come learn skating with us and build a foundation for figure skating, hockey, speed skating and ringette thouhg a series of balance, control and agility trainings!', 'Skating');
INSERT INTO classt (title, description, type) VALUES ('Personal Training Classes', 'Looking to start building a habit of working out? Or maybe you are looking for an intense training program? Regardless of how experienced you are, we are here to help you achieve your fitness dreams!', 'GymSports');
INSERT INTO classt (title, description, type) VALUES ('Lil Dippers Swimming Lessons', 'Want your little one to get a head start when it comes to pool play time? We have helped thousands of babies, AND adults all over Vancouver Greater Area learn water safety skills by pairing them with one of our seasoned swimming instructors!', 'Swimming');
INSERT INTO classt (title, description, type) VALUES ('World Tennis', 'Did you know that we are one of 18 tennis courts in the Greater Vancouver Area? We offer classes to all ages and skill levels all year. Take a lesson, register for a program, or join a drop-in class!', 'Tennis');

INSERT INTO canhave (rid, fid, classType) VALUES (100, 1, 'Cardio');
INSERT INTO canhave (rid, fid, classType) VALUES (100, 1, 'Dance');
INSERT INTO canhave (rid, fid, classType) VALUES (101, 1, 'Cardio');
INSERT INTO canhave (rid, fid, classType) VALUES (101, 1, 'Dance');
INSERT INTO canhave (rid, fid, classType) VALUES (101, 1, 'HIIT');
INSERT INTO canhave (rid, fid, classType) VALUES (101, 1, 'BootCamp');
INSERT INTO canhave (rid, fid, classType) VALUES (101, 1, 'GymSports');
INSERT INTO canhave (rid, fid, classType) VALUES (102, 1, 'Cardio');
INSERT INTO canhave (rid, fid, classType) VALUES (102, 1, 'Dance');
INSERT INTO canhave (rid, fid, classType) VALUES (102, 1, 'HIIT');
INSERT INTO canhave (rid, fid, classType) VALUES (102, 1, 'BootCamp');
INSERT INTO canhave (rid, fid, classType) VALUES (102, 1, 'GymSports');
INSERT INTO canhave (rid, fid, classType) VALUES (103, 1, 'Tennis');
INSERT INTO canhave (rid, fid, classType) VALUES (104, 1, 'Tennis');
INSERT INTO canhave (rid, fid, classType) VALUES (105, 1, 'Basketball');

INSERT INTO canhave (rid, fid, classType) VALUES (100, 2, 'Skating');
INSERT INTO canhave (rid, fid, classType) VALUES (101, 2, 'Skating');
INSERT INTO canhave (rid, fid, classType) VALUES (102, 2, 'Skating');

INSERT INTO canhave (rid, fid, classType) VALUES (101, 3, 'GymSports');
INSERT INTO canhave (rid, fid, classType) VALUES (101, 3, 'Dance');
INSERT INTO canhave (rid, fid, classType) VALUES (101, 3, 'Cardio');
INSERT INTO canhave (rid, fid, classType) VALUES (101, 3, 'HIIT');
INSERT INTO canhave (rid, fid, classType) VALUES (102, 3, 'GymSports');
INSERT INTO canhave (rid, fid, classType) VALUES (102, 3, 'Dance');
INSERT INTO canhave (rid, fid, classType) VALUES (102, 3, 'Cardio');
INSERT INTO canhave (rid, fid, classType) VALUES (102, 3, 'HIIT');
INSERT INTO canhave (rid, fid, classType) VALUES (103, 3, 'Swimming');

INSERT INTO canhave (rid, fid, classType) VALUES (100, 4, 'GymSports');
INSERT INTO canhave (rid, fid, classType) VALUES (100, 4, 'Dance');
INSERT INTO canhave (rid, fid, classType) VALUES (100, 4, 'Cardio');
INSERT INTO canhave (rid, fid, classType) VALUES (100, 4, 'HIIT');
INSERT INTO canhave (rid, fid, classType) VALUES (101, 4, 'GymSports');
INSERT INTO canhave (rid, fid, classType) VALUES (101, 4, 'Dance');
INSERT INTO canhave (rid, fid, classType) VALUES (101, 4, 'Cardio');
INSERT INTO canhave (rid, fid, classType) VALUES (101, 4, 'HIIT');
INSERT INTO canhave (rid, fid, classType) VALUES (102, 4, 'HIIT');
INSERT INTO canhave (rid, fid, classType) VALUES (102, 4, 'Dance');
INSERT INTO canhave (rid, fid, classType) VALUES (102, 4, 'BootCamp');
INSERT INTO canhave (rid, fid, classType) VALUES (102, 4, 'Cardio');
INSERT INTO canhave (rid, fid, classType) VALUES (103, 4, 'HIIT');
INSERT INTO canhave (rid, fid, classType) VALUES (103, 4, 'Dance');
INSERT INTO canhave (rid, fid, classType) VALUES (103, 4, 'BootCamp');
INSERT INTO canhave (rid, fid, classType) VALUES (103, 4, 'Cardio');
INSERT INTO canhave (rid, fid, classType) VALUES (104, 4, 'Cardio');
INSERT INTO canhave (rid, fid, classType) VALUES (104, 4, 'Dance');
INSERT INTO canhave (rid, fid, classType) VALUES (104, 4, 'BootCamp');
INSERT INTO canhave (rid, fid, classType) VALUES (104, 4, 'HIIT');

INSERT INTO canhave (rid, fid, classType) VALUES (100, 5, 'Cardio');
INSERT INTO canhave (rid, fid, classType) VALUES (100, 5, 'Dance');
INSERT INTO canhave (rid, fid, classType) VALUES (100, 5, 'HIIT');
INSERT INTO canhave (rid, fid, classType) VALUES (101, 5, 'Cardio');
INSERT INTO canhave (rid, fid, classType) VALUES (101, 5, 'Dance');
INSERT INTO canhave (rid, fid, classType) VALUES (101, 5, 'HIIT');
INSERT INTO canhave (rid, fid, classType) VALUES (101, 5, 'BootCamp');
INSERT INTO canhave (rid, fid, classType) VALUES (101, 5, 'GymSports');
INSERT INTO canhave (rid, fid, classType) VALUES (102, 5, 'Cardio');
INSERT INTO canhave (rid, fid, classType) VALUES (102, 5, 'Dance');
INSERT INTO canhave (rid, fid, classType) VALUES (102, 5, 'HIIT');
INSERT INTO canhave (rid, fid, classType) VALUES (102, 5, 'BootCamp');
INSERT INTO canhave (rid, fid, classType) VALUES (102, 5, 'GymSports');
INSERT INTO canhave (rid, fid, classType) VALUES (103, 5, 'Tennis');
INSERT INTO canhave (rid, fid, classType) VALUES (104, 5, 'Tennis');
INSERT INTO canhave (rid, fid, classType) VALUES (105, 5, 'Tennis');

INSERT INTO class (time, title, capacity, rid, fid, iid) VALUES ('2020-04-11 16:30:00', 'Abs of Steel', 30, 102, 4, 1);
INSERT INTO class (time, title, capacity, rid, fid, iid) VALUES ('2020-04-11 10:00:00', 'Running Machine', 15, 100, 1, 2);
INSERT INTO class (time, title, capacity, rid, fid, iid) VALUES ('2020-04-11 13:00:00', 'Shapetastic', 25, 103, 4, 3);
INSERT INTO class (time, title, capacity, rid, fid, iid) VALUES ('2020-04-11 20:00:00', 'Basketball Skills I', 40, 100, 4, 4);
INSERT INTO class (time, title, capacity, rid, fid, iid) VALUES ('2020-04-11 14:00:00', 'Running Machine', 20, 100, 5, 5);
INSERT INTO class (time, title, capacity, rid, fid, iid) VALUES ('2020-04-11 18:00:00', 'World Dance', 30, 100, 5, 5);

INSERT INTO creditcard (num, expiryDate, csv, nameOnCard) VALUES (1234120000000000, '2021-02-28', 123, 'Ian Miller');
INSERT INTO creditcard (num, expiryDate, csv, nameOnCard) VALUES (4567350000000000, '2020-07-31', 456, 'Lana Smith');
INSERT INTO creditcard (num, expiryDate, csv, nameOnCard) VALUES (6789680000000000, '2024-01-31', 789, 'Mary Cavill');
INSERT INTO creditcard (num, expiryDate, csv, nameOnCard) VALUES (1234100000000000, '2022-04-30', 987, 'Saquon Barkley');
INSERT INTO creditcard (num, expiryDate, csv, nameOnCard) VALUES (4321990000000000, '2025-06-30', 654, 'Russel Westbrrok');

INSERT INTO papaccount (accountNumber, bankNumber, transitNumber) VALUES (1234567, 4027, 1);
INSERT INTO papaccount (accountNumber, bankNumber, transitNumber) VALUES (4324324, 2334, 3);
INSERT INTO papaccount (accountNumber, bankNumber, transitNumber) VALUES (5673456, 5672, 1);
INSERT INTO papaccount (accountNumber, bankNumber, transitNumber) VALUES (9876543, 2342, 2);
INSERT INTO papaccount (accountNumber, bankNumber, transitNumber) VALUES (3498764, 6943, 4);

INSERT INTO payment (pid, frequency, creditCardNumber, accountNumber) VALUES (1, 'Monthly', 1234120000000000, null);
INSERT INTO payment (pid, frequency, creditCardNumber, accountNumber) VALUES (2, 'Bi-weekly', 4567350000000000, null);
INSERT INTO payment (pid, frequency, creditCardNumber, accountNumber) VALUES (3, 'Yearly', 6789680000000000, null);
INSERT INTO payment (pid, frequency, creditCardNumber, accountNumber) VALUES (4, 'Monthly', 1234100000000000, null);
INSERT INTO payment (pid, frequency, creditCardNumber, accountNumber) VALUES (5, 'Monthly', 4321990000000000, null);
INSERT INTO payment (pid, frequency, creditCardNumber, accountNumber) VALUES (6, 'Bi-weekly', null, 1234567);
INSERT INTO payment (pid, frequency, creditCardNumber, accountNumber) VALUES (7, 'Bi-monthly', null, 4324324);
INSERT INTO payment (pid, frequency, creditCardNumber, accountNumber) VALUES (8, 'Monthly', null, 5673456);
INSERT INTO payment (pid, frequency, creditCardNumber, accountNumber) VALUES (9, 'Monthly', null, 9876543);
INSERT INTO payment (pid, frequency, creditCardNumber, accountNumber) VALUES (10, 'Yearly', null, 3498764);

INSERT INTO rates (mid, iid, rating) VALUES (1, 1, 1);
INSERT INTO rates (mid, iid, rating) VALUES (2, 2, 3);
INSERT INTO rates (mid, iid, rating) VALUES (3, 3, 3);
INSERT INTO rates (mid, iid, rating) VALUES (4, 4, 3);
INSERT INTO rates (mid, iid, rating) VALUES (5, 5, 4);

INSERT INTO letsyoutake (classType, sType) VALUES ('Cardio', 'Bronze');
INSERT INTO letsyoutake (classType, sType) VALUES ('BootCamp', 'Silver');
INSERT INTO letsyoutake (classType, sType) VALUES ('Dance', 'Silver');
INSERT INTO letsyoutake (classType, sType) VALUES ('GymSports', 'Silver');
INSERT INTO letsyoutake (classType, sType) VALUES ('Cardio', 'Silver');
INSERT INTO letsyoutake (classType, sType) VALUES ('Cardio', 'Student');
INSERT INTO letsyoutake (classType, sType) VALUES ('HIIT', 'Student');
INSERT INTO letsyoutake (classType, sType) VALUES ('GymSports', 'Student');
INSERT INTO letsyoutake (classType, sType) VALUES ('Dance', 'Student');
INSERT INTO letsyoutake (classType, sType) VALUES ('BootCamp', 'Student');
INSERT INTO letsyoutake (classType, sType) VALUES ('Cardio', 'Senior');
INSERT INTO letsyoutake (classType, sType) VALUES ('HIIT', 'Senior');
INSERT INTO letsyoutake (classType, sType) VALUES ('GymSports', 'Senior');
INSERT INTO letsyoutake (classType, sType) VALUES ('Dance', 'Senior');
INSERT INTO letsyoutake (classType, sType) VALUES ('BootCamp', 'Senior');
INSERT INTO letsyoutake (classType, sType) VALUES ('Cardio', 'Low-Income');
INSERT INTO letsyoutake (classType, sType) VALUES ('HIIT', 'Low-Income');
INSERT INTO letsyoutake (classType, sType) VALUES ('GymSports', 'Low-Income');
INSERT INTO letsyoutake (classType, sType) VALUES ('Dance', 'Low-Income');
INSERT INTO letsyoutake (classType, sType) VALUES ('BootCamp', 'Low-Income');
INSERT INTO letsyoutake (classType, sType) VALUES ('Cardio', 'Gold');
INSERT INTO letsyoutake (classType, sType) VALUES ('HIIT', 'Gold');
INSERT INTO letsyoutake (classType, sType) VALUES ('GymSports', 'Gold');
INSERT INTO letsyoutake (classType, sType) VALUES ('Dance', 'Gold');
INSERT INTO letsyoutake (classType, sType) VALUES ('BootCamp', 'Gold');
INSERT INTO letsyoutake (classType, sType) VALUES ('Cardio', 'Platinum');
INSERT INTO letsyoutake (classType, sType) VALUES ('HIIT', 'Platinum');
INSERT INTO letsyoutake (classType, sType) VALUES ('GymSports', 'Platinum');
INSERT INTO letsyoutake (classType, sType) VALUES ('Dance', 'Platinum');
INSERT INTO letsyoutake (classType, sType) VALUES ('BootCamp', 'Platinum');
INSERT INTO letsyoutake (classType, sType) VALUES ('Swimming', 'Platinum');
INSERT INTO letsyoutake (classType, sType) VALUES ('Skating', 'Platinum');


INSERT INTO takes (mid, time, rid, fid) VALUES (1, '2020-04-11 16:30:00', 102, 4);
INSERT INTO takes (mid, time, rid, fid) VALUES (2, '2020-04-11 10:00:00', 100, 1);
INSERT INTO takes (mid, time, rid, fid) VALUES (3, '2020-04-11 13:00:00', 103, 4);
INSERT INTO takes (mid, time, rid, fid) VALUES (4, '2020-04-11 20:00:00', 100, 4);
INSERT INTO takes (mid, time, rid, fid) VALUES (5, '2020-04-11 10:00:00', 100, 1);
INSERT INTO takes (mid, time, rid, fid) VALUES (5, '2020-04-11 14:00:00', 100, 5);
INSERT INTO takes (mid, time, rid, fid) VALUES (6, '2020-04-11 18:00:00', 100, 5);

INSERT INTO memberpayment (mid, pid) VALUES (1, 1);
INSERT INTO memberpayment (mid, pid) VALUES (2, 5);
INSERT INTO memberpayment (mid, pid) VALUES (3, 3);
INSERT INTO memberpayment (mid, pid) VALUES (4, 4);
INSERT INTO memberpayment (mid, pid) VALUES (5, 5);
INSERT INTO memberpayment (mid, pid) VALUES (5, 6);
INSERT INTO memberpayment (mid, pid) VALUES (6, 2);

INSERT INTO cleans (fid, staffID) VALUES (1, 1);
INSERT INTO cleans (fid, staffID) VALUES (2, 2);
INSERT INTO cleans (fid, staffID) VALUES (3, 3);
INSERT INTO cleans (fid, staffID) VALUES (4, 4);
INSERT INTO cleans (fid, staffID) VALUES (5, 5);

INSERT INTO provides (fid, serialNumber) VALUES (1, 1);
INSERT INTO provides (fid, serialNumber) VALUES (1, 2);
INSERT INTO provides (fid, serialNumber) VALUES (1, 3);
INSERT INTO provides (fid, serialNumber) VALUES (1, 5);
INSERT INTO provides (fid, serialNumber) VALUES (3, 5);
INSERT INTO provides (fid, serialNumber) VALUES (4, 1);
INSERT INTO provides (fid, serialNumber) VALUES (4, 2);
INSERT INTO provides (fid, serialNumber) VALUES (4, 3);
INSERT INTO provides (fid, serialNumber) VALUES (4, 4);
INSERT INTO provides (fid, serialNumber) VALUES (4, 5);
INSERT INTO provides (fid, serialNumber) VALUES (5, 1);
INSERT INTO provides (fid, serialNumber) VALUES (5, 2);
INSERT INTO provides (fid, serialNumber) VALUES (5, 3);
INSERT INTO provides (fid, serialNumber) VALUES (5, 4);
INSERT INTO provides (fid, serialNumber) VALUES (5, 5);