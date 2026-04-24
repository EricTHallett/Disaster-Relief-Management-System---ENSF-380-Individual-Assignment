-- Each time this file is executed, it will reset the database to the original state defined below.  You can import this directly in your database by \i 'path_to_file\project.sql' (with appropriate use of \ or / based on OS). Alternately, you can just paste these commands in.

-- During grading, the TAs will create a database with the same tables as shown here, but different data. Thus you cannot assume any data provided here exists. You may assume that we will only use valid data in our grading file. 

-- Connect outside database first (as postgres user)
\connect postgres

DROP DATABASE IF EXISTS ensf380project;
CREATE DATABASE ensf380project;
\c ensf380project

CREATE TABLE Person (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),
    comments TEXT
);

CREATE TABLE Location (
    id SERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    address TEXT NOT NULL
);

CREATE TABLE DisasterVictim (
    person_id INTEGER PRIMARY KEY REFERENCES Person(id) ON DELETE CASCADE,
    date_of_birth DATE,
    approximate_age INTEGER,
    gender VARCHAR(50),
    entry_date DATE NOT NULL DEFAULT CURRENT_DATE,
    location_id INTEGER REFERENCES Location(id),
    is_soft_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE MedicalRecord (
    id SERIAL PRIMARY KEY,
    victim_id INTEGER NOT NULL REFERENCES DisasterVictim(person_id) ON DELETE CASCADE,
    treatment_details TEXT NOT NULL,
    treatment_date DATE NOT NULL DEFAULT CURRENT_DATE,
    location_id INTEGER REFERENCES Location(id)
);

CREATE TABLE Supply (
    id SERIAL PRIMARY KEY,
    supply_type VARCHAR(100) NOT NULL,
    location_id INTEGER REFERENCES Location(id),
    victim_id INTEGER REFERENCES DisasterVictim(person_id),
    expiry_date DATE,
    allocation_date DATE,
    description TEXT
);

CREATE TABLE FamilyRelationship (
    id SERIAL PRIMARY KEY,
    person_one_id INTEGER NOT NULL REFERENCES Person(id) ON DELETE CASCADE,
    person_two_id INTEGER NOT NULL REFERENCES Person(id) ON DELETE CASCADE,
    relationship_type VARCHAR(50) NOT NULL
);

CREATE TABLE Inquiry (
    id SERIAL PRIMARY KEY,
    inquirer_id INTEGER NOT NULL REFERENCES Person(id),
    subject_person_id INTEGER REFERENCES Person(id),
    inquiry_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    details TEXT NOT NULL
);

CREATE TABLE CulturalRequirement (
    id SERIAL PRIMARY KEY,
    victim_id INTEGER NOT NULL REFERENCES DisasterVictim(person_id) ON DELETE CASCADE,
    requirement_category VARCHAR(100) NOT NULL,
    requirement_option VARCHAR(100) NOT NULL
);

CREATE TABLE Skill (
    id SERIAL PRIMARY KEY,
    skill_name VARCHAR(100) NOT NULL,
    category VARCHAR(20) NOT NULL CHECK (category IN ('medical', 'language', 'trade')),
    CONSTRAINT unique_skill_name_category UNIQUE (skill_name, category)
);

CREATE TABLE VictimSkill (
    id SERIAL PRIMARY KEY,
    victim_id INTEGER NOT NULL REFERENCES DisasterVictim(person_id) ON DELETE CASCADE,
    skill_id INTEGER NOT NULL REFERENCES Skill(id) ON DELETE CASCADE,
    details VARCHAR(50),
    language_capabilities VARCHAR(100),
    certification_expiry DATE,
    proficiency_level VARCHAR(20) NOT NULL CHECK (proficiency_level IN ('beginner', 'intermediate', 'advanced'))
);

-- Grant permissions to oop user
ALTER DATABASE ensf380project OWNER TO oop;
GRANT ALL PRIVILEGES ON DATABASE ensf380project TO oop;
GRANT ALL ON ALL TABLES IN SCHEMA public TO oop;
GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO oop;

-- LOCATIONS
INSERT INTO Location (name, address) VALUES
('TELUS Convention Centre', '136 8 Ave SE, Calgary, AB'),
('University of Calgary', '2500 University Dr NW, Calgary, AB'),
('Pantheon Shelter', '456 Shelter Rd, Calgary, AB'),
('Dispatch Centre', '123 Main St, Calgary, AB');

-- PEOPLE (comments only for unstructured info)
INSERT INTO Person (first_name, last_name, comments) VALUES
-- Disaster Victims
('Teruya', 'Bouillon', 'Alert but quiet'),
('Freda', 'McDonald', NULL),
('Elisabeth', 'ten Boom', NULL),
('Amina', 'Al-Farsi', NULL),
('Carlos', 'Mendez', NULL),
-- Inquirer
('Joseph', 'Bouillon', 'Phoned from Paris'),
-- Additional victim for soft delete example
('Chris', 'Far', 'Chris case for soft deletion');

-- DISASTER VICTIMS
INSERT INTO DisasterVictim (person_id, date_of_birth, approximate_age, gender, entry_date, location_id, is_soft_deleted) VALUES
((SELECT id FROM Person WHERE first_name = 'Teruya'), NULL, 5, 'boy', '2025-01-18', 
 (SELECT id FROM Location WHERE name = 'TELUS Convention Centre'), FALSE),
((SELECT id FROM Person WHERE first_name = 'Freda'), '1986-06-03', NULL, 'woman', '2025-01-18', 
 (SELECT id FROM Location WHERE name = 'University of Calgary'), FALSE),
((SELECT id FROM Person WHERE first_name = 'Elisabeth'), '1965-08-19', NULL, 'woman', '2025-01-18', 
 (SELECT id FROM Location WHERE name = 'Pantheon Shelter'), FALSE),
((SELECT id FROM Person WHERE first_name = 'Amina'), '1990-08-22', NULL, 'woman', '2025-01-20', 
 (SELECT id FROM Location WHERE name = 'University of Calgary'), FALSE),
((SELECT id FROM Person WHERE first_name = 'Carlos'), '1985-11-30', NULL, 'man', '2025-01-20', 
 (SELECT id FROM Location WHERE name = 'Pantheon Shelter'), FALSE),
((SELECT id FROM Person WHERE first_name = 'Chris'), NULL, 30, 'non-binary person', '2025-01-21', 
 (SELECT id FROM Location WHERE name = 'TELUS Convention Centre'), TRUE);  -- SOFT DELETED

-- MEDICAL RECORDS
INSERT INTO MedicalRecord (victim_id, treatment_details, treatment_date, location_id) VALUES
((SELECT person_id FROM DisasterVictim JOIN Person ON person_id = Person.id WHERE first_name = 'Teruya'),
 'Broken arm treated with cast.', '2025-01-18',
 (SELECT id FROM Location WHERE name = 'TELUS Convention Centre')),
((SELECT person_id FROM DisasterVictim JOIN Person ON person_id = Person.id WHERE first_name = 'Freda'),
 'Twisted ankle and light burns treated.', '2025-01-18',
 (SELECT id FROM Location WHERE name = 'University of Calgary')),
((SELECT person_id FROM DisasterVictim JOIN Person ON person_id = Person.id WHERE first_name = 'Elisabeth'),
 'Pernicious anemia monitoring and B12 supplement.', '2025-01-18',
 (SELECT id FROM Location WHERE name = 'Pantheon Shelter'));

-- SUPPLIES (including expired perishable)
INSERT INTO Supply (supply_type, location_id, victim_id, expiry_date, allocation_date, description) VALUES
-- Non-perishable supplies
('personal toiletries kit', (SELECT id FROM Location WHERE name = 'University of Calgary'),
 (SELECT person_id FROM DisasterVictim JOIN Person ON person_id = Person.id WHERE first_name = 'Freda'), NULL, '2025-01-18', 'Basic hygiene'),
('clothing set', (SELECT id FROM Location WHERE name = 'University of Calgary'),
 (SELECT person_id FROM DisasterVictim JOIN Person ON person_id = Person.id WHERE first_name = 'Freda'), NULL, '2025-01-18', 'Size M'),
-- Perishable supplies
('water', (SELECT id FROM Location WHERE name = 'TELUS Convention Centre'), NULL, '2025-12-31', NULL, 'Case of 24 bottles'),
('food ration', (SELECT id FROM Location WHERE name = 'TELUS Convention Centre'), NULL, '2024-11-15', NULL, 'canned milk'), -- expired
('food ration', (SELECT id FROM Location WHERE name = 'Pantheon Shelter'), NULL, '2025-08-20', NULL, 'canned milk'), -- not expired
-- Supply at location (not allocated to victim)
('blanket', (SELECT id FROM Location WHERE name = 'University of Calgary'), NULL, NULL, NULL, 'Wool blanket');

-- FAMILY RELATIONSHIPS
INSERT INTO FamilyRelationship (person_one_id, person_two_id, relationship_type) VALUES
((SELECT id FROM Person WHERE first_name = 'Freda'), (SELECT id FROM Person WHERE first_name = 'Joseph'), 'spouse'),
((SELECT id FROM Person WHERE first_name = 'Freda'), (SELECT id FROM Person WHERE first_name = 'Teruya'), 'parent');

-- INQUIRY
INSERT INTO Inquiry (inquirer_id, subject_person_id, details) VALUES
((SELECT id FROM Person WHERE first_name = 'Joseph'),
 (SELECT id FROM Person WHERE first_name = 'Freda'),
 'Seeking information on wife Freda McDonald and children.');

-- CULTURAL REQUIREMENTS
INSERT INTO CulturalRequirement (victim_id, requirement_category, requirement_option) VALUES
((SELECT person_id FROM DisasterVictim JOIN Person ON person_id = Person.id WHERE first_name = 'Freda'),
 'dietary restrictions', 'vegetarian'),
((SELECT person_id FROM DisasterVictim JOIN Person ON person_id = Person.id WHERE first_name = 'Amina'),
 'dietary restrictions', 'halal'),
((SELECT person_id FROM DisasterVictim JOIN Person ON person_id = Person.id WHERE first_name = 'Carlos'),
 'safe-space requirements', 'LGBTQIA+ affirming');

-- SKILLS (all categories)
INSERT INTO Skill (skill_name, category) VALUES
-- Medical
('first-aid', 'medical'),
('nursing', 'medical'),
('counseling', 'medical'),
('doctor', 'medical'),
-- Language
('French', 'language'),
('Japanese', 'language'),
('Spanish', 'language'),
-- Trade
('carpentry', 'trade'),
('electricity', 'trade'),
('plumbing', 'trade')
ON CONFLICT (skill_name, category) DO NOTHING;

-- VICTIM SKILLS (with certifications, language capabilities, proficiencies)
INSERT INTO VictimSkill (victim_id, skill_id, details, language_capabilities, certification_expiry, proficiency_level) VALUES
-- Teruya: Language skills
((SELECT person_id FROM DisasterVictim JOIN Person ON person_id = Person.id WHERE first_name = 'Teruya'),
 (SELECT id FROM Skill WHERE skill_name = 'French' AND category = 'language'), NULL, 'speak/listen', NULL, 'intermediate'),
((SELECT person_id FROM DisasterVictim JOIN Person ON person_id = Person.id WHERE first_name = 'Teruya'),
 (SELECT id FROM Skill WHERE skill_name = 'Japanese' AND category = 'language'), NULL, 'speak/listen', NULL, 'beginner'),
-- Amina: Medical skills with certification
((SELECT person_id FROM DisasterVictim JOIN Person ON person_id = Person.id WHERE first_name = 'Amina'),
 (SELECT id FROM Skill WHERE skill_name = 'nursing' AND category = 'medical'), 'Registered Nurse', NULL, '2026-12-31', 'intermediate'),
((SELECT person_id FROM DisasterVictim JOIN Person ON person_id = Person.id WHERE first_name = 'Amina'),
 (SELECT id FROM Skill WHERE skill_name = 'counseling' AND category = 'medical'), 'Trauma Counselor', NULL, '2025-10-15', 'advanced'),
-- Carlos: Trade skills
((SELECT person_id FROM DisasterVictim JOIN Person ON person_id = Person.id WHERE first_name = 'Carlos'),
 (SELECT id FROM Skill WHERE skill_name = 'carpentry' AND category = 'trade'), NULL, NULL, NULL, 'advanced'),
((SELECT person_id FROM DisasterVictim JOIN Person ON person_id = Person.id WHERE first_name = 'Carlos'),
 (SELECT id FROM Skill WHERE skill_name = 'electricity' AND category = 'trade'), 'Electrician License', NULL, '2027-05-20', 'advanced');

-- ============================================================================
-- COMPREHENSIVE EXAMPLE PERSON (has all cultural requirements and all skills)
-- ============================================================================

-- Add the comprehensive person
INSERT INTO Person (first_name, last_name, comments) VALUES
('Alex', 'Comprehensive', 'Example person with all cultural requirements and skills');

-- Add as DisasterVictim
INSERT INTO DisasterVictim (person_id, date_of_birth, approximate_age, gender, entry_date, location_id, is_soft_deleted)
SELECT 
    p.id,
    '1995-07-15',
    NULL,
    'non-binary person',
    '2025-01-22',
    (SELECT id FROM Location WHERE name = 'University of Calgary' LIMIT 1),
    FALSE
FROM Person p
WHERE p.first_name = 'Alex' AND p.last_name = 'Comprehensive';

-- Add ALL cultural requirements common to both files
INSERT INTO CulturalRequirement (victim_id, requirement_category, requirement_option)
SELECT 
    dv.person_id,
    cr.requirement_category,
    cr.requirement_option
FROM DisasterVictim dv
CROSS JOIN (VALUES 
    ('dietary restrictions', 'vegetarian'),
    ('safe-space requirements', 'LGBTQIA+ affirming'),
    ('language support', 'translation services')
) AS cr(requirement_category, requirement_option)
WHERE dv.person_id = (SELECT person_id FROM DisasterVictim dv2 
                      JOIN Person p ON dv2.person_id = p.id 
                      WHERE p.first_name = 'Alex' AND p.last_name = 'Comprehensive');

-- Add ALL skills to Alex with various proficiency levels and details
INSERT INTO VictimSkill (victim_id, skill_id, details, language_capabilities, certification_expiry, proficiency_level)
SELECT 
    dv.person_id AS victim_id,
    s.id AS skill_id,
    CASE 
        WHEN s.skill_name = 'first-aid' THEN 'First Aid Certified'
        WHEN s.skill_name = 'nursing' THEN 'RN License'
        WHEN s.skill_name = 'counseling' THEN 'Trauma Specialist'
        WHEN s.skill_name = 'doctor' THEN 'MD License'
        WHEN s.skill_name = 'carpentry' THEN 'Carpenter License'
        WHEN s.skill_name = 'electricity' THEN 'Electrician License'
        WHEN s.skill_name = 'plumbing' THEN 'Plumber License'
        ELSE NULL
    END AS details,
    CASE 
        WHEN s.category = 'language' THEN
            CASE 
                WHEN s.skill_name = 'French' THEN 'read/write, speak/listen'
                WHEN s.skill_name = 'Japanese' THEN 'speak/listen'
                WHEN s.skill_name = 'Spanish' THEN 'read/write'
                ELSE NULL
            END
        ELSE NULL
    END AS language_capabilities,
    CASE 
        WHEN s.skill_name = 'first-aid' THEN '2025-12-31'::DATE
        WHEN s.skill_name = 'nursing' THEN '2026-06-30'::DATE
        WHEN s.skill_name = 'counseling' THEN '2025-09-15'::DATE
        WHEN s.skill_name = 'doctor' THEN '2027-03-01'::DATE
        WHEN s.skill_name = 'electricity' THEN '2026-08-20'::DATE
        WHEN s.skill_name = 'plumbing' THEN '2027-05-15'::DATE
        ELSE NULL
    END AS certification_expiry,
    CASE 
        WHEN s.skill_name IN ('first-aid', 'Japanese', 'plumbing') THEN 'beginner'
        WHEN s.skill_name IN ('nursing', 'French', 'carpentry') THEN 'intermediate'
        WHEN s.skill_name IN ('counseling', 'doctor', 'Spanish', 'electricity') THEN 'advanced'
        ELSE 'intermediate'
    END AS proficiency_level
FROM DisasterVictim dv
CROSS JOIN Skill s
WHERE dv.person_id = (SELECT person_id FROM DisasterVictim dv2 
                      JOIN Person p ON dv2.person_id = p.id 
                      WHERE p.first_name = 'Alex' AND p.last_name = 'Comprehensive')
AND s.skill_name IN ('first-aid', 'nursing', 'counseling', 'doctor', 'French', 'Japanese', 'Spanish', 'carpentry', 'electricity', 'plumbing');
