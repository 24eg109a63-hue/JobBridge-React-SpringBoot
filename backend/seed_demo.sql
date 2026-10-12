USE jobbridge;
INSERT IGNORE INTO users(name,email,password_hash,role,company_name) VALUES
('Techspire Recruiting','recruitment@techspire.demo','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','company','Techspire'),
('CloudNest Careers','careers@cloudnest.demo','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','company','CloudNest'),
('BrightPath Hiring','jobs@brightpath.demo','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','company','BrightPath');
INSERT INTO jobs(company_id,title,location,job_type,description,requirements)
SELECT u.id,x.title,x.location,x.job_type,x.description,x.requirements FROM users u JOIN (
 SELECT 'Junior React Developer' title,'Hyderabad, Telangana' location,'Full-time' job_type,'Build responsive user interfaces and collaborate with product and API teams.' description,'JavaScript, React fundamentals, HTML and CSS.' requirements,'recruitment@techspire.demo' email UNION ALL
 SELECT 'Java Backend Intern','Hybrid - Hyderabad','Internship','Help build REST APIs and write maintainable backend code.','Core Java, OOP, SQL basics.','recruitment@techspire.demo' UNION ALL
 SELECT 'Cybersecurity Analyst Trainee','Hyderabad, Telangana','Full-time','Assist with security monitoring, incident triage and vulnerability reviews.','Networking basics, Linux and security fundamentals.','careers@cloudnest.demo' UNION ALL
 SELECT 'Data Analyst Intern','Remote, India','Internship','Prepare reports and discover insights from business datasets.','SQL, Excel and basic Python.','careers@cloudnest.demo' UNION ALL
 SELECT 'UI/UX Design Intern','Hyderabad, Telangana','Internship','Create wireframes and polished experiences for web products.','Figma, design fundamentals and portfolio.','jobs@brightpath.demo' UNION ALL
 SELECT 'Python Developer','Remote, India','Full-time','Develop Python services, integrations and automated tests.','Python, REST APIs, Git and SQL.','jobs@brightpath.demo' UNION ALL
 SELECT 'QA Automation Engineer','Hyderabad, Telangana','Full-time','Create test plans and automate regression testing for web applications.','Testing basics, JavaScript or Python, API testing.','recruitment@techspire.demo' UNION ALL
 SELECT 'Cloud Support Associate','Hybrid - Hyderabad','Full-time','Support cloud deployments, monitor service health and troubleshoot issues.','Linux, networking and cloud fundamentals.','careers@cloudnest.demo'
) x ON x.email=u.email WHERE NOT EXISTS(SELECT 1 FROM jobs j WHERE j.title=x.title);
