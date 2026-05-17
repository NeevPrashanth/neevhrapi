package neevhrapi.co.uk.nit.service.documents;

import neevhrapi.co.uk.nit.domains.documents.DocumentRequestCreate;
import neevhrapi.co.uk.nit.domains.documents.AddressItem;
import neevhrapi.co.uk.nit.domains.documents.AddressUpsertRequest;
import neevhrapi.co.uk.nit.domains.documents.EmployeeCreateRequest;
import neevhrapi.co.uk.nit.domains.documents.EmployeeProfileItem;
import neevhrapi.co.uk.nit.domains.documents.FinanceDocumentItem;
import neevhrapi.co.uk.nit.domains.documents.ManagerDocumentItem;
import neevhrapi.co.uk.nit.domains.documents.MyDocumentItem;
import neevhrapi.co.uk.nit.domains.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class DocumentService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.documents.base-dir:uploads/documents}")
    private String baseDir;

    private Path pendingDir() throws IOException {
        Path dir = Paths.get(baseDir, "pending").toAbsolutePath().normalize();
        Files.createDirectories(dir);
        return dir;
    }

    private Path approvedDir() throws IOException {
        Path dir = Paths.get(baseDir, "approved").toAbsolutePath().normalize();
        Files.createDirectories(dir);
        return dir;
    }

    private Path rejectedDir() throws IOException {
        Path dir = Paths.get(baseDir, "rejected").toAbsolutePath().normalize();
        Files.createDirectories(dir);
        return dir;
    }

    private Path financeDir(Integer year) throws IOException {
        Path dir = Paths.get(baseDir, "finance", String.valueOf(year)).toAbsolutePath().normalize();
        Files.createDirectories(dir);
        return dir;
    }

    public Integer getUserIdByUsername(String username) {
        try {
            return jdbcTemplate.queryForObject("SELECT id FROM users WHERE username = ?", Integer.class, username);
        } catch (EmptyResultDataAccessException ex) {
            return 0;
        }
    }

    public List<User> getAllEmployees() {
        String sql = "SELECT u.id, u.username FROM users u JOIN user_roles ur ON ur.user_id = u.id WHERE ur.role = 'Employee' ORDER BY u.username";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            User user = new User();
            user.setEmpId(rs.getInt("id"));
            user.setEmpName(rs.getString("username"));
            return user;
        });
    }

    public int addEmployee(EmployeeCreateRequest request) {
        String email = Objects.requireNonNullElse(request.getEmailId(), "").trim();
        String first = Objects.requireNonNullElse(request.getFirstName(), "").trim();
        String last = Objects.requireNonNullElse(request.getLastName(), "").trim();
        String dob = Objects.requireNonNullElse(request.getDob(), "").trim();
        if (email.isEmpty() || first.isEmpty() || last.isEmpty() || dob.isEmpty()) {
            throw new IllegalArgumentException("All employee fields are required");
        }

        Integer userExists = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE username = ?", Integer.class, email);
        if (userExists != null && userExists > 0) {
            throw new IllegalArgumentException("Email already exists");
        }

        jdbcTemplate.update("INSERT INTO users (username, password) VALUES (?, ?)", email, passwordEncoder.encode("welcome123"));
        Integer userId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE username = ?", Integer.class, email);
        jdbcTemplate.update("INSERT INTO user_roles (user_id, role) VALUES (?, 'Employee')", userId);
        jdbcTemplate.update(
                "INSERT INTO employee_profile (user_id, first_name, last_name, dob, email_id, created_at) VALUES (?, ?, ?, ?, ?, NOW())",
                userId, first, last, dob, email
        );
        return 1;
    }

    public EmployeeProfileItem getEmployeeProfile(Integer employeeId) {
        String sql = "SELECT user_id, first_name, last_name, dob, email_id FROM employee_profile WHERE user_id = ?";
        List<EmployeeProfileItem> list = jdbcTemplate.query(sql, (rs, rowNum) -> EmployeeProfileItem.builder()
                .userId(rs.getInt("user_id"))
                .firstName(rs.getString("first_name"))
                .lastName(rs.getString("last_name"))
                .dob(rs.getString("dob"))
                .emailId(rs.getString("email_id"))
                .build(), employeeId);
        if (list.isEmpty()) {
            return null;
        }
        EmployeeProfileItem profile = list.get(0);
        profile.setApprovedAddresses(getApprovedAddresses(employeeId));
        profile.setPendingAddresses(getPendingAddressRequests(employeeId));
        return profile;
    }

    public EmployeeProfileItem getMyProfile(String username) {
        Integer userId = getUserIdByUsername(username);
        return getEmployeeProfile(userId);
    }

    private List<AddressItem> getApprovedAddresses(Integer userId) {
        String sql = "SELECT id, user_id, address_type, line1, line2, city, postal_code, country FROM employee_address WHERE user_id = ? ORDER BY address_type";
        return jdbcTemplate.query(sql, (rs, rowNum) -> AddressItem.builder()
                .id(rs.getInt("id"))
                .userId(rs.getInt("user_id"))
                .addressType(rs.getString("address_type"))
                .line1(rs.getString("line1"))
                .line2(rs.getString("line2"))
                .city(rs.getString("city"))
                .postalCode(rs.getString("postal_code"))
                .country(rs.getString("country"))
                .status("APPROVED")
                .build(), userId);
    }

    public List<AddressItem> getPendingAddressRequests(Integer userId) {
        String sql = "SELECT id, user_id, address_type, line1, line2, city, postal_code, country, status FROM employee_address_request WHERE user_id = ? AND status = 'PENDING' ORDER BY address_type";
        return jdbcTemplate.query(sql, (rs, rowNum) -> AddressItem.builder()
                .id(rs.getInt("id"))
                .userId(rs.getInt("user_id"))
                .addressType(rs.getString("address_type"))
                .line1(rs.getString("line1"))
                .line2(rs.getString("line2"))
                .city(rs.getString("city"))
                .postalCode(rs.getString("postal_code"))
                .country(rs.getString("country"))
                .status(rs.getString("status"))
                .build(), userId);
    }

    public int upsertMyAddress(String username, AddressUpsertRequest request) {
        Integer userId = getUserIdByUsername(username);
        String type = Objects.requireNonNullElse(request.getAddressType(), "").trim().toUpperCase();
        if (!"PRIMARY".equals(type) && !"SECONDARY".equals(type)) {
            throw new IllegalArgumentException("addressType must be PRIMARY or SECONDARY");
        }

        jdbcTemplate.update("DELETE FROM employee_address_request WHERE user_id = ? AND address_type = ? AND status = 'PENDING'", userId, type);
        return jdbcTemplate.update(
                "INSERT INTO employee_address_request (user_id, address_type, line1, line2, city, postal_code, country, status, requested_at) VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING', NOW())",
                userId, type, request.getLine1(), request.getLine2(), request.getCity(), request.getPostalCode(), request.getCountry()
        );
    }

    public List<AddressItem> getManagerPendingAddressRequests(Integer employeeId) {
        String sql = "SELECT id, user_id, address_type, line1, line2, city, postal_code, country, status FROM employee_address_request WHERE status = 'PENDING' AND (? IS NULL OR user_id = ?) ORDER BY requested_at DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> AddressItem.builder()
                .id(rs.getInt("id"))
                .userId(rs.getInt("user_id"))
                .addressType(rs.getString("address_type"))
                .line1(rs.getString("line1"))
                .line2(rs.getString("line2"))
                .city(rs.getString("city"))
                .postalCode(rs.getString("postal_code"))
                .country(rs.getString("country"))
                .status(rs.getString("status"))
                .build(), employeeId, employeeId);
    }

    public int approveAddressRequest(String managerUsername, Integer requestId) {
        List<AddressItem> rows = jdbcTemplate.query(
                "SELECT id, user_id, address_type, line1, line2, city, postal_code, country, status FROM employee_address_request WHERE id = ? AND status = 'PENDING'",
                (rs, rowNum) -> AddressItem.builder()
                        .id(rs.getInt("id"))
                        .userId(rs.getInt("user_id"))
                        .addressType(rs.getString("address_type"))
                        .line1(rs.getString("line1"))
                        .line2(rs.getString("line2"))
                        .city(rs.getString("city"))
                        .postalCode(rs.getString("postal_code"))
                        .country(rs.getString("country"))
                        .status(rs.getString("status"))
                        .build(), requestId
        );
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Pending address request not found");
        }
        AddressItem req = rows.get(0);
        Integer managerId = getUserIdByUsername(managerUsername);

        Integer exists = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM employee_address WHERE user_id = ? AND address_type = ?", Integer.class, req.getUserId(), req.getAddressType());
        if (exists != null && exists > 0) {
            jdbcTemplate.update(
                    "UPDATE employee_address SET line1 = ?, line2 = ?, city = ?, postal_code = ?, country = ?, updated_at = NOW(), approved_by = ? WHERE user_id = ? AND address_type = ?",
                    req.getLine1(), req.getLine2(), req.getCity(), req.getPostalCode(), req.getCountry(), managerId, req.getUserId(), req.getAddressType()
            );
        } else {
            jdbcTemplate.update(
                    "INSERT INTO employee_address (user_id, address_type, line1, line2, city, postal_code, country, approved_at, approved_by) VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), ?)",
                    req.getUserId(), req.getAddressType(), req.getLine1(), req.getLine2(), req.getCity(), req.getPostalCode(), req.getCountry(), managerId
            );
        }

        return jdbcTemplate.update("UPDATE employee_address_request SET status = 'APPROVED', reviewed_at = NOW(), reviewed_by = ? WHERE id = ?", managerId, requestId);
    }

    public int createDocumentRequest(String managerUsername, DocumentRequestCreate request) {
        Integer managerId = getUserIdByUsername(managerUsername);
        return jdbcTemplate.update(
                "INSERT INTO document_request (employee_id, document_name, requested_by, created_at, active) VALUES (?, ?, ?, NOW(), 1)",
                request.getEmployeeId(), request.getDocumentName(), managerId
        );
    }

    public int managerUploadApprovedHrDocument(String managerUsername, Integer employeeId, String documentName, MultipartFile file) throws IOException {
        Integer managerId = getUserIdByUsername(managerUsername);
        String cleanDocName = Objects.requireNonNullElse(documentName, "").trim();
        if (cleanDocName.isEmpty()) {
            throw new IllegalArgumentException("documentName is required");
        }

        jdbcTemplate.update(
                "INSERT INTO document_request (employee_id, document_name, requested_by, created_at, active) VALUES (?, ?, ?, NOW(), 1)",
                employeeId, cleanDocName, managerId
        );
        Integer requestId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM document_request WHERE employee_id = ? AND document_name = ?", Integer.class, employeeId, cleanDocName);

        String originalFileName = Objects.requireNonNullElse(file.getOriginalFilename(), "upload.bin");
        String safeFileName = UUID.randomUUID() + "_" + originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        Path approvedPath = approvedDir().resolve(ts + "_" + safeFileName);
        Files.copy(file.getInputStream(), approvedPath, StandardCopyOption.REPLACE_EXISTING);

        return jdbcTemplate.update(
                "INSERT INTO employee_document_upload (request_id, employee_id, original_file_name, stored_file_name, pending_file_path, approved_file_path, status, uploaded_at, approved_at, approved_by) VALUES (?, ?, ?, ?, NULL, ?, 'APPROVED', NOW(), NOW(), ?)",
                requestId, employeeId, originalFileName, safeFileName, approvedPath.toString(), managerId
        );
    }

    public List<MyDocumentItem> getEmployeeHrDocuments(Integer employeeId) {
        String sql =
                "SELECT dr.id AS request_id, dr.document_name, COALESCE(edu.status, 'NOT_UPLOADED') AS status, edu.id AS upload_id, " +
                "edu.original_file_name, CASE WHEN edu.status = 'APPROVED' THEN edu.original_file_name ELSE NULL END AS approved_file_name " +
                "FROM document_request dr " +
                "LEFT JOIN employee_document_upload edu ON edu.id = ( " +
                "  SELECT e2.id FROM employee_document_upload e2 WHERE e2.request_id = dr.id ORDER BY e2.uploaded_at DESC LIMIT 1 " +
                ") " +
                "WHERE dr.employee_id = ? AND dr.active = 1 ORDER BY dr.created_at DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> MyDocumentItem.builder()
                .requestId(rs.getInt("request_id"))
                .documentName(rs.getString("document_name"))
                .status(rs.getString("status"))
                .uploadId((Integer) rs.getObject("upload_id"))
                .uploadedFileName(rs.getString("original_file_name"))
                .approvedFileName(rs.getString("approved_file_name"))
                .build(), employeeId);
    }

    public List<MyDocumentItem> getMyRequiredDocuments(String username) {
        Integer userId = getUserIdByUsername(username);
        String sql =
                "SELECT dr.id AS request_id, " +
                "dr.document_name, " +
                "COALESCE(edu.status, 'NOT_UPLOADED') AS status, " +
                "edu.id AS upload_id, " +
                "edu.original_file_name, " +
                "CASE WHEN edu.status = 'APPROVED' THEN edu.original_file_name ELSE NULL END AS approved_file_name " +
                "FROM document_request dr " +
                "LEFT JOIN employee_document_upload edu ON edu.id = ( " +
                "  SELECT e2.id FROM employee_document_upload e2 WHERE e2.request_id = dr.id " +
                "  ORDER BY e2.uploaded_at DESC LIMIT 1 " +
                ") " +
                "WHERE dr.employee_id = ? AND dr.active = 1 " +
                "ORDER BY dr.created_at DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> MyDocumentItem.builder()
                .requestId(rs.getInt("request_id"))
                .documentName(rs.getString("document_name"))
                .status(rs.getString("status"))
                .uploadId((Integer) rs.getObject("upload_id"))
                .uploadedFileName(rs.getString("original_file_name"))
                .approvedFileName(rs.getString("approved_file_name"))
                .build(), userId);
    }

    public int uploadEmployeeDocument(String username, Integer requestId, MultipartFile file) throws IOException {
        Integer userId = getUserIdByUsername(username);
        String sqlCheck = "SELECT COUNT(*) FROM document_request WHERE id = ? AND employee_id = ? AND active = 1";
        Integer count = jdbcTemplate.queryForObject(sqlCheck, Integer.class, requestId, userId);
        if (count == null || count == 0) {
            throw new IllegalArgumentException("Invalid document request for this employee");
        }

        String originalFileName = Objects.requireNonNullElse(file.getOriginalFilename(), "upload.bin");
        String safeFileName = UUID.randomUUID() + "_" + originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        Path target = pendingDir().resolve(safeFileName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        String insertSql = "INSERT INTO employee_document_upload (request_id, employee_id, original_file_name, stored_file_name, pending_file_path, status, uploaded_at) VALUES (?, ?, ?, ?, ?, 'PENDING', NOW())";
        return jdbcTemplate.update(insertSql, requestId, userId, originalFileName, safeFileName, target.toString());
    }

    public List<ManagerDocumentItem> getManagerPendingDocuments(Integer employeeId) {
        String sql =
                "SELECT edu.id AS upload_id, dr.id AS request_id, dr.employee_id, " +
                "u.username AS employee_name, dr.document_name, edu.original_file_name, edu.status " +
                "FROM employee_document_upload edu " +
                "JOIN document_request dr ON dr.id = edu.request_id " +
                "JOIN users u ON u.id = dr.employee_id " +
                "WHERE edu.status = 'PENDING' AND dr.active = 1 AND (? IS NULL OR dr.employee_id = ?) " +
                "ORDER BY edu.uploaded_at DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> ManagerDocumentItem.builder()
                .uploadId(rs.getInt("upload_id"))
                .requestId(rs.getInt("request_id"))
                .employeeId(rs.getInt("employee_id"))
                .employeeName(rs.getString("employee_name"))
                .documentName(rs.getString("document_name"))
                .originalFileName(rs.getString("original_file_name"))
                .status(rs.getString("status"))
                .build(), employeeId, employeeId);
    }

    public List<ManagerDocumentItem> getManagerApprovedDocuments(Integer employeeId) {
        String sql =
                "SELECT edu.id AS upload_id, dr.id AS request_id, dr.employee_id, " +
                "u.username AS employee_name, dr.document_name, edu.original_file_name, edu.status " +
                "FROM employee_document_upload edu " +
                "JOIN document_request dr ON dr.id = edu.request_id " +
                "JOIN users u ON u.id = dr.employee_id " +
                "WHERE edu.status = 'APPROVED' " +
                "AND dr.active = 1 " +
                "AND edu.id = ( " +
                "  SELECT MAX(e2.id) " +
                "  FROM employee_document_upload e2 " +
                "  JOIN document_request dr2 ON dr2.id = e2.request_id " +
                "  WHERE e2.status = 'APPROVED' " +
                "    AND dr2.employee_id = dr.employee_id " +
                "    AND dr2.document_name = dr.document_name " +
                ") " +
                "AND (? IS NULL OR dr.employee_id = ?) " +
                "ORDER BY edu.approved_at DESC, edu.uploaded_at DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> ManagerDocumentItem.builder()
                .uploadId(rs.getInt("upload_id"))
                .requestId(rs.getInt("request_id"))
                .employeeId(rs.getInt("employee_id"))
                .employeeName(rs.getString("employee_name"))
                .documentName(rs.getString("document_name"))
                .originalFileName(rs.getString("original_file_name"))
                .status(rs.getString("status"))
                .build(), employeeId, employeeId);
    }

    public int approveDocument(String managerUsername, Integer uploadId) throws IOException {
        String sql = "SELECT pending_file_path, stored_file_name FROM employee_document_upload WHERE id = ? AND status = 'PENDING'";
        var row = jdbcTemplate.queryForMap(sql, uploadId);
        if (row == null || row.isEmpty()) {
            throw new IllegalArgumentException("Pending document not found");
        }

        Path pendingPath = Paths.get(String.valueOf(row.get("pending_file_path"))).toAbsolutePath().normalize();
        String storedFileName = String.valueOf(row.get("stored_file_name"));
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        Path approvedPath = approvedDir().resolve(ts + "_" + storedFileName);
        Files.createDirectories(approvedPath.getParent());
        Files.move(pendingPath, approvedPath, StandardCopyOption.REPLACE_EXISTING);

        Integer managerId = getUserIdByUsername(managerUsername);
        String updateSql = "UPDATE employee_document_upload SET status = 'APPROVED', approved_at = NOW(), approved_by = ?, approved_file_path = ? WHERE id = ?";
        return jdbcTemplate.update(updateSql, managerId, approvedPath.toString(), uploadId);
    }

    public int archiveApprovedDocument(String managerUsername, Integer uploadId) {
        String fetchSql =
                "SELECT dr.id AS request_id, dr.document_name " +
                "FROM employee_document_upload edu " +
                "JOIN document_request dr ON dr.id = edu.request_id " +
                "WHERE edu.id = ? AND edu.status = 'APPROVED' AND dr.active = 1";
        List<java.util.Map<String, Object>> rows = jdbcTemplate.queryForList(fetchSql, uploadId);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Approved active document not found");
        }

        Integer requestId = ((Number) rows.get(0).get("request_id")).intValue();
        String currentName = String.valueOf(rows.get(0).get("document_name"));
        String archivedSuffix = " (Archived " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ")";
        String archivedName = currentName + archivedSuffix;

        Integer managerId = getUserIdByUsername(managerUsername);

        jdbcTemplate.update(
                "UPDATE document_request SET active = 0, document_name = ? WHERE id = ?",
                archivedName, requestId
        );

        return jdbcTemplate.update(
                "UPDATE employee_document_upload SET status = 'ARCHIVED', approved_by = ? WHERE id = ?",
                managerId, uploadId
        );
    }

    public int rejectDocument(String managerUsername, Integer uploadId) throws IOException {
        String sql = "SELECT pending_file_path, stored_file_name FROM employee_document_upload WHERE id = ? AND status = 'PENDING'";
        var row = jdbcTemplate.queryForMap(sql, uploadId);
        if (row == null || row.isEmpty()) {
            throw new IllegalArgumentException("Pending document not found");
        }

        Path pendingPath = Paths.get(String.valueOf(row.get("pending_file_path"))).toAbsolutePath().normalize();
        String storedFileName = String.valueOf(row.get("stored_file_name"));
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        Path rejectedPath = rejectedDir().resolve(ts + "_" + storedFileName);
        Files.createDirectories(rejectedPath.getParent());
        Files.move(pendingPath, rejectedPath, StandardCopyOption.REPLACE_EXISTING);

        Integer managerId = getUserIdByUsername(managerUsername);
        String updateSql =
                "UPDATE employee_document_upload " +
                "SET status = 'REJECTED', approved_by = ?, rejected_at = NOW(), rejected_file_path = ?, pending_file_path = NULL " +
                "WHERE id = ?";
        return jdbcTemplate.update(updateSql, managerId, rejectedPath.toString(), uploadId);
    }

    public Resource loadDocumentForDownload(String username, Integer uploadId) throws MalformedURLException {
        Integer userId = getUserIdByUsername(username);
        boolean isManager = isManager(userId);

        String sql;
        Object[] params;
        if (isManager) {
            sql = "SELECT approved_file_path, pending_file_path, status FROM employee_document_upload WHERE id = ?";
            params = new Object[]{uploadId};
        } else {
            sql = "SELECT approved_file_path, pending_file_path, status FROM employee_document_upload WHERE id = ? AND employee_id = ?";
            params = new Object[]{uploadId, userId};
        }

        var rows = jdbcTemplate.queryForList(sql, params);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Document not found");
        }

        String status = String.valueOf(rows.get(0).get("status"));
        String pathToUse;

        if ("APPROVED".equalsIgnoreCase(status)) {
            pathToUse = (String) rows.get(0).get("approved_file_path");
        } else if (isManager) {
            pathToUse = (String) rows.get(0).get("pending_file_path");
        } else {
            throw new IllegalArgumentException("Document is not approved yet");
        }

        Path filePath = Paths.get(pathToUse).toAbsolutePath().normalize();
        return new UrlResource(filePath.toUri());
    }

    public String getOriginalFileName(Integer uploadId) {
        return jdbcTemplate.queryForObject("SELECT original_file_name FROM employee_document_upload WHERE id = ?", String.class, uploadId);
    }

    private boolean isManager(Integer userId) {
        String roleSql = "SELECT role FROM user_roles WHERE user_id = ?";
        List<String> roles = jdbcTemplate.query(roleSql, (rs, rowNum) -> rs.getString("role"), userId);
        return roles.stream().anyMatch(r -> "Manager".equalsIgnoreCase(r));
    }

    public int uploadFinanceDocument(String managerUsername, Integer employeeId, Integer year, String docType, MultipartFile file) throws IOException {
        String originalFileName = Objects.requireNonNullElse(file.getOriginalFilename(), "upload.bin");
        String safeFileName = UUID.randomUUID() + "_" + originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        Path target = financeDir(year).resolve(safeFileName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        Integer managerId = getUserIdByUsername(managerUsername);
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM employee_finance_document WHERE employee_id = ? AND finance_year = ? AND doc_type = ?",
                Integer.class, employeeId, year, docType
        );

        if (count != null && count > 0) {
            return jdbcTemplate.update(
                    "UPDATE employee_finance_document SET original_file_name = ?, stored_file_name = ?, file_path = ?, uploaded_at = NOW(), uploaded_by = ? WHERE employee_id = ? AND finance_year = ? AND doc_type = ?",
                    originalFileName, safeFileName, target.toString(), managerId, employeeId, year, docType
            );
        }

        return jdbcTemplate.update(
                "INSERT INTO employee_finance_document (employee_id, finance_year, doc_type, original_file_name, stored_file_name, file_path, uploaded_at, uploaded_by) VALUES (?, ?, ?, ?, ?, ?, NOW(), ?)",
                employeeId, year, docType, originalFileName, safeFileName, target.toString(), managerId
        );
    }

    public List<FinanceDocumentItem> getFinanceDocuments(Integer employeeId, Integer year) {
        String sql = "SELECT id, employee_id, finance_year, doc_type, original_file_name FROM employee_finance_document WHERE employee_id = ? AND finance_year = ? ORDER BY doc_type";
        return jdbcTemplate.query(sql, (rs, rowNum) -> FinanceDocumentItem.builder()
                .id(rs.getInt("id"))
                .employeeId(rs.getInt("employee_id"))
                .year(rs.getInt("finance_year"))
                .docType(rs.getString("doc_type"))
                .originalFileName(rs.getString("original_file_name"))
                .build(), employeeId, year);
    }

    public List<FinanceDocumentItem> getMyFinanceDocuments(String username, Integer year) {
        Integer userId = getUserIdByUsername(username);
        return getFinanceDocuments(userId, year);
    }

    public Resource loadFinanceDocumentForDownload(String username, Integer financeDocId) throws MalformedURLException {
        Integer userId = getUserIdByUsername(username);
        boolean manager = isManager(userId);
        String sql = manager
                ? "SELECT file_path FROM employee_finance_document WHERE id = ?"
                : "SELECT file_path FROM employee_finance_document WHERE id = ? AND employee_id = ?";
        List<String> paths = manager
                ? jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("file_path"), financeDocId)
                : jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("file_path"), financeDocId, userId);
        if (paths.isEmpty()) {
            throw new IllegalArgumentException("Finance document not found");
        }
        Path filePath = Paths.get(paths.get(0)).toAbsolutePath().normalize();
        return new UrlResource(filePath.toUri());
    }

    public String getFinanceOriginalFileName(Integer financeDocId) {
        return jdbcTemplate.queryForObject("SELECT original_file_name FROM employee_finance_document WHERE id = ?", String.class, financeDocId);
    }
}
