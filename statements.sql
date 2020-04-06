USE project;

PREPARE getMember FROM 'SELECT * FROM member NATURAL JOIN status WHERE login = ? AND password = ?';
PREPARE getStatuses FROM 'SELECT sType FROM status';
PREPARE createMember FROM 'INSERT INTO member(login, password, address, phoneNumber, email, name, birthDate, sType) VALUES (?, ?, ?, ?, ?, ?, ?, ?)';
PREPARE addPayment FROM 'INSERT INTO memberpayment(mid, pid) VALUES (?,?)';
PREPARE getClassTypesFromStatus FROM 'SELECT classType FROM letsyoutake WHERE sType = ?';
PREPARE getStatusCost FROM 'SELECT cost FROM status WHERE sType = ?';
PREPARE createCreditCard FROM 'INSERT INTO creditcard(num, expiryDate, csv, nameOnCard) VALUES (?,?,?,?)';
PREPARE createPayment FROM 'INSERT INTO payment(frequency, creditCardNumber, accountNumber) VALUES (?, ?, NULL)';
PREPARE getPayments FROM 'SELECT * FROM payment NATURAL JOIN memberpayment WHERE mid = ?';
PREPARE getCreditCard FROM 'SELECT * FROM creditcard WHERE num = ?';
PREPARE getPap FROM 'SELECT * FROM PAPAccount WHERE accountNumber = ?';
PREPARE updatePersonal FROM 'SELECT * FROM MEMBER WHERE mid = ?' --then update result set
PREPARE updatePayment FROM 'INSERT INTO memberpayment(mid, pid) VALUES (?, ?)';
PREPARE updatePassword FROM 'SELECT * FROM MEMBER WHERE mid = ?' --then update result set

PREPARE getFacilities FROM 'SELECT * FROM facility';
PREPARE getClasses FROM
    'SELECT *, COUNT(t.mid) as taking,
        ? IN (SELECT mid FROM takes t2 WHERE c.time = t2.time AND c.rid = t2.rid AND c.fid = t2.fid) as isMemberTaking
    FROM takes t NATURAL RIGHT OUTER JOIN class c NATURAL JOIN classt ct NATURAL JOIN instructor i
    WHERE fid = ? AND time > CURRENT_TIMESTAMP AND '/*Uses selected column*/' = ?
    GROUP BY c.time, c.rid, c.fid';
PREPARE getRegisteredClasses FROM
    'SELECT *, COUNT(t.mid) as taking
    FROM  takes t NATURAL RIGHT OUTER JOIN class c NATURAL JOIN classt NATURAL JOIN instructor i
    WHERE fid = ? AND time > CURRENT_TIMESTAMP AND ? IN (SELECT mid FROM takes t2 WHERE c.time = t2.time AND c.rid = t2.rid AND c.fid = t2.fid)
    GROUP BY c.time, c.rid, c.fid';
PREPARE getInstructor FROM 'SELECT * FROM ratedinstructors i WHERE i.iid = ?';
PREPARE getInstructorsInFacility FROM 'SELECT i.*, r.rating FROM ratedinstructors i NATURAL LEFT OUTER JOIN
    (SELECT * FROM rates WHERE mid = ?) r
WHERE i.iid IN (SELECT c.iid FROM class c WHERE c.time > CURRENT_TIMESTAMP AND c.iid = iid AND c.fid = ?)';
PREPARE rateInstructor FROM 'INSERT INTO rates (mid, iid, rating) VALUES (? ,?, ?)';
PREPARE rateInstructorAlt FROM 'SELECT * FROM rates WHERE (mid = ? AND iid = ?)'; --then update result set
PREPARE bestInstructor FROM 'SELECT * FROM ratedinstructors i WHERE i.avgRating =
                                (SELECT MAX(i2.avgRating) FROM ratedinstructors i2)';

PREPARE registerForClass FROM 'INSERT INTO takes(mid, time, rid, fid) VALUES (?, ?, ?, ?)'
PREPARE deregisterForClass FROM 'DELETE FROM takes WHERE mid = ? AND time = ? AND rid = ? AND fid = ?';