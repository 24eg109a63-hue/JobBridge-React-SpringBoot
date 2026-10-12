package com.jobbridge.api;

import java.util.*;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DuplicateKeyException;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class ApiController {
    private final JdbcTemplate db;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    public ApiController(JdbcTemplate db) { this.db = db; }

    @GetMapping("/health") public Map<String,Object> health() {
        Map<String,Object> r = new LinkedHashMap<>(); r.put("status","ok"); r.put("service","JobBridge Spring Boot API"); return r;
    }

    @GetMapping("/jobs") public List<Map<String,Object>> jobs(@RequestParam(required=false) String q) {
        String term = q == null ? "" : q.trim();
        return db.queryForList("SELECT j.id,j.title,j.location,j.job_type AS jobType,j.description,j.requirements,j.created_at AS createdAt,u.company_name AS companyName,u.name AS companyContact FROM jobs j JOIN users u ON u.id=j.company_id WHERE (?='' OR j.title LIKE CONCAT('%',?,'%') OR j.location LIKE CONCAT('%',?,'%') OR u.company_name LIKE CONCAT('%',?,'%')) ORDER BY j.created_at DESC", term,term,term,term);
    }

    @PostMapping("/register") public ResponseEntity<?> register(@RequestBody Map<String,String> body) {
        String name = val(body,"name"), email = val(body,"email").toLowerCase(), password = val(body,"password");
        String role = val(body,"role").isBlank() ? "seeker" : val(body,"role");
        String company = val(body,"companyName");
        if(name.isBlank() || email.isBlank() || password.length()<8) return ResponseEntity.badRequest().body(Map.of("error","Name, email and a password of at least 8 characters are required."));
        if(!List.of("seeker","company").contains(role)) return ResponseEntity.badRequest().body(Map.of("error","Role must be seeker or company."));
        try {
            db.update("INSERT INTO users(name,email,password_hash,role,company_name) VALUES(?,?,?,?,?)",name,email,encoder.encode(password),role,company.isBlank()?null:company);
            Map<String,Object> user = db.queryForMap("SELECT id,name,email,role,company_name AS companyName FROM users WHERE email=?",email);
            return ResponseEntity.status(201).body(user);
        } catch(DuplicateKeyException ex) { return ResponseEntity.status(409).body(Map.of("error","That email is already registered.")); }
    }

    @PostMapping("/login") public ResponseEntity<?> login(@RequestBody Map<String,String> body) {
        String email=val(body,"email").toLowerCase(), password=val(body,"password");
        List<Map<String,Object>> rows=db.queryForList("SELECT id,name,email,password_hash,role,company_name AS companyName FROM users WHERE email=?",email);
        if(rows.isEmpty() || !encoder.matches(password, String.valueOf(rows.get(0).get("password_hash")))) return ResponseEntity.status(401).body(Map.of("error","Invalid email or password."));
        Map<String,Object> user=new LinkedHashMap<>(rows.get(0)); user.remove("password_hash"); return ResponseEntity.ok(user);
    }

    @PostMapping("/jobs") public ResponseEntity<?> createJob(@RequestBody Map<String,Object> b) {
        Long companyId=number(b.get("companyId")); String title=str(b,"title"), location=str(b,"location"), type=str(b,"jobType"), description=str(b,"description"), requirements=str(b,"requirements");
        if(companyId==null || title.isBlank() || location.isBlank() || description.isBlank()) return ResponseEntity.badRequest().body(Map.of("error","Company ID, title, location and description are required."));
        List<Map<String,Object>> company=db.queryForList("SELECT id FROM users WHERE id=? AND role='company'",companyId);
        if(company.isEmpty()) return ResponseEntity.status(403).body(Map.of("error","Register or log in with a company account before posting."));
        db.update("INSERT INTO jobs(company_id,title,location,job_type,description,requirements) VALUES(?,?,?,?,?,?)",companyId,title,location,type.isBlank()?"Full-time":type,description,requirements);
        return ResponseEntity.status(201).body(Map.of("message","Job posted successfully."));
    }

    @PostMapping("/applications") public ResponseEntity<?> apply(@RequestBody Map<String,Object> b) {
        Long jobId=number(b.get("jobId")), seekerId=number(b.get("seekerId")); String cover=str(b,"coverLetter");
        if(jobId==null || seekerId==null) return ResponseEntity.badRequest().body(Map.of("error","Job ID and seeker ID are required."));
        if(db.queryForList("SELECT id FROM users WHERE id=? AND role='seeker'",seekerId).isEmpty()) return ResponseEntity.status(403).body(Map.of("error","Please register or log in as a job seeker."));
        try { db.update("INSERT INTO applications(job_id,seeker_id,cover_letter) VALUES(?,?,?)",jobId,seekerId,cover); return ResponseEntity.status(201).body(Map.of("message","Application submitted.")); }
        catch(DuplicateKeyException ex) { return ResponseEntity.status(409).body(Map.of("error","You have already applied for this job.")); }
    }

    @GetMapping("/applications") public ResponseEntity<?> applications(@RequestParam Long userId, @RequestParam String role) {
        if("seeker".equals(role)) return ResponseEntity.ok(db.queryForList("SELECT a.id,a.status,a.applied_at AS appliedAt,a.cover_letter AS coverLetter,j.id AS jobId,j.title,j.location,j.job_type AS jobType,u.company_name AS companyName FROM applications a JOIN jobs j ON j.id=a.job_id JOIN users u ON u.id=j.company_id WHERE a.seeker_id=? ORDER BY a.applied_at DESC",userId));
        if("company".equals(role)) return ResponseEntity.ok(db.queryForList("SELECT a.id,a.status,a.applied_at AS appliedAt,a.cover_letter AS coverLetter,j.title,j.id AS jobId,s.name AS applicantName,s.email AS applicantEmail FROM applications a JOIN jobs j ON j.id=a.job_id JOIN users s ON s.id=a.seeker_id WHERE j.company_id=? ORDER BY a.applied_at DESC",userId));
        return ResponseEntity.badRequest().body(Map.of("error","Unsupported role."));
    }

    @PatchMapping("/applications/{id}/status") public ResponseEntity<?> status(@PathVariable Long id,@RequestBody Map<String,String> b) {
        String status=val(b,"status"); if(!List.of("Submitted","Under Review","Shortlisted","Rejected","Hired").contains(status)) return ResponseEntity.badRequest().body(Map.of("error","Invalid application status."));
        int n=db.update("UPDATE applications SET status=? WHERE id=?",status,id); return n==0?ResponseEntity.notFound().build():ResponseEntity.ok(Map.of("message","Status updated."));
    }
    private String val(Map<String,String> b,String k){return b.getOrDefault(k,"").trim();}
    private String str(Map<String,Object> b,String k){Object x=b.get(k);return x==null?"":String.valueOf(x).trim();}
    private Long number(Object x){try{return x==null?null:Long.valueOf(String.valueOf(x));}catch(Exception e){return null;}}
}
