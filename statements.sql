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
PREPARE updatePayment FROM 'INSERT INTO memberpayment(mid, pid) VALUES (?, ?)';

PREPARE getFacilities FROM 'SELECT * FROM facility';
PREPARE getClasses FROM
    'SELECT *,
        (SELECT COUNT(t.mid) FROM takes t WHERE c.time = t.time AND c.rid = t.rid AND c.fid = t.fid) as taking,
        ? IN (SELECT mid FROM takes t2 WHERE c.time = t2.time AND c.rid = t2.rid AND c.fid = t2.fid) as isMemberTaking
    FROM instructor i NATURAL JOIN class c NATURAL JOIN classt
    WHERE fid = ? AND time > CURRENT_TIMESTAMP';
PREPARE getRegisteredClasses FROM
    'SELECT *,
        (SELECT COUNT(t.mid) FROM takes t WHERE c.time = t.time AND c.rid = t.rid AND c.fid = t.fid) as taking
    FROM instructor i NATURAL JOIN class c NATURAL JOIN classt
    WHERE fid = ? AND time > CURRENT_TIMESTAMP AND ? IN (SELECT mid FROM takes t2 WHERE c.time = t2.time AND c.rid = t2.rid AND c.fid = t2.fid)';
PREPARE getInstructor FROM 'SELECT * FROM ratedinstructors i WHERE i.iid = ?';
PREPARE getInstructorsInFacility FROM 'SELECT i.* FROM ratedinstructors i WHERE
    i.iid IN (SELECT c.iid FROM class c WHERE c.time > CURRENT_TIMESTAMP AND c.iid = iid AND c.fid = ?)';

PREPARE registerForClass FROM 'INSERT INTO takes(mid, time, rid, fid) VALUES (?, ?, ?, ?)'
PREPARE deregisterForClass FROM 'DELETE FROM takes WHERE mid = ? AND time = ? AND rid = ? AND fid = ?';