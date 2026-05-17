package neevhrapi.co.uk.nit.controller.documents;

import neevhrapi.co.uk.nit.domains.documents.DocumentRequestCreate;
import neevhrapi.co.uk.nit.domains.documents.AddressUpsertRequest;
import neevhrapi.co.uk.nit.domains.documents.EmployeeCreateRequest;
import neevhrapi.co.uk.nit.service.documents.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/neevhrapi/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @GetMapping("/my-required/v1")
    public ResponseEntity<?> getMyRequiredDocuments() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(documentService.getMyRequiredDocuments(auth.getName()));
    }

    @GetMapping("/employee/my-details/v1")
    public ResponseEntity<?> getMyDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(documentService.getMyProfile(auth.getName()));
    }

    @PostMapping("/employee/my-address/upsert/v1")
    public ResponseEntity<?> upsertMyAddress(@RequestBody AddressUpsertRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        documentService.upsertMyAddress(auth.getName(), request);
        return ResponseEntity.ok("Address saved for approval");
    }

    @GetMapping("/employee/my-finance/v1")
    public ResponseEntity<?> getMyFinanceDocuments(@RequestParam Integer year) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(documentService.getMyFinanceDocuments(auth.getName(), year));
    }

    @PostMapping(value = "/upload/v1", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadDocument(@RequestParam("requestId") Integer requestId,
                                            @RequestParam("file") MultipartFile file) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        documentService.uploadEmployeeDocument(auth.getName(), requestId, file);
        return ResponseEntity.ok("Document uploaded and waiting for manager review");
    }

    @GetMapping("/download/v1/{uploadId}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Integer uploadId) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Resource resource = documentService.loadDocumentForDownload(auth.getName(), uploadId);
        String fileName = documentService.getOriginalFileName(uploadId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @GetMapping("/finance/download/v1/{financeDocId}")
    public ResponseEntity<Resource> downloadFinanceDocument(@PathVariable Integer financeDocId) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Resource resource = documentService.loadFinanceDocumentForDownload(auth.getName(), financeDocId);
        String fileName = documentService.getFinanceOriginalFileName(financeDocId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @PreAuthorize("hasRole('Manager')")
    @GetMapping("/manager/employees/v1")
    public ResponseEntity<?> getEmployeesForManager() {
        return ResponseEntity.ok(documentService.getAllEmployees());
    }

    @PreAuthorize("hasRole('Manager')")
    @PostMapping("/manager/employees/add/v1")
    public ResponseEntity<?> addEmployee(@RequestBody EmployeeCreateRequest request) {
        documentService.addEmployee(request);
        return ResponseEntity.ok("Employee created successfully");
    }

    @PreAuthorize("hasRole('Manager')")
    @GetMapping("/manager/employees/details/v1/{employeeId}")
    public ResponseEntity<?> getEmployeeDetails(@PathVariable Integer employeeId) {
        return ResponseEntity.ok(documentService.getEmployeeProfile(employeeId));
    }

    @PreAuthorize("hasRole('Manager')")
    @GetMapping("/manager/employees/address/pending/v1")
    public ResponseEntity<?> getPendingAddressRequests(@RequestParam(required = false) Integer employeeId) {
        return ResponseEntity.ok(documentService.getManagerPendingAddressRequests(employeeId));
    }

    @PreAuthorize("hasRole('Manager')")
    @PostMapping("/manager/employees/address/approve/v1/{requestId}")
    public ResponseEntity<?> approveAddressRequest(@PathVariable Integer requestId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        documentService.approveAddressRequest(auth.getName(), requestId);
        return ResponseEntity.ok("Address approved");
    }

    @PreAuthorize("hasRole('Manager')")
    @GetMapping("/manager/employees/hr/available/v1/{employeeId}")
    public ResponseEntity<?> getEmployeeHrDocuments(@PathVariable Integer employeeId) {
        return ResponseEntity.ok(documentService.getEmployeeHrDocuments(employeeId));
    }

    @PreAuthorize("hasRole('Manager')")
    @PostMapping("/manager/assign/v1")
    public ResponseEntity<?> assignDocument(@RequestBody DocumentRequestCreate request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        documentService.createDocumentRequest(auth.getName(), request);
        return ResponseEntity.ok("Document requested successfully");
    }

    @PreAuthorize("hasRole('Manager')")
    @PostMapping(value = "/manager/hr/upload-approved/v1", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadApprovedHrDocument(@RequestParam Integer employeeId,
                                                      @RequestParam String documentName,
                                                      @RequestParam("file") MultipartFile file) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        documentService.managerUploadApprovedHrDocument(auth.getName(), employeeId, documentName, file);
        return ResponseEntity.ok("HR document uploaded and approved successfully");
    }

    @PreAuthorize("hasRole('Manager')")
    @GetMapping("/manager/pending/v1")
    public ResponseEntity<?> getPendingDocuments(@RequestParam(required = false) Integer employeeId) {
        return ResponseEntity.ok(documentService.getManagerPendingDocuments(employeeId));
    }

    @PreAuthorize("hasRole('Manager')")
    @PostMapping("/manager/approve/v1/{uploadId}")
    public ResponseEntity<?> approveDocument(@PathVariable Integer uploadId) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        documentService.approveDocument(auth.getName(), uploadId);
        return ResponseEntity.ok("Document approved successfully");
    }

    @PreAuthorize("hasRole('Manager')")
    @PostMapping("/manager/reject/v1/{uploadId}")
    public ResponseEntity<?> rejectDocument(@PathVariable Integer uploadId) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        documentService.rejectDocument(auth.getName(), uploadId);
        return ResponseEntity.ok("Document rejected successfully");
    }

    @PreAuthorize("hasRole('Manager')")
    @GetMapping("/manager/approved/v1")
    public ResponseEntity<?> getApprovedDocuments(@RequestParam(required = false) Integer employeeId) {
        return ResponseEntity.ok(documentService.getManagerApprovedDocuments(employeeId));
    }

    @PreAuthorize("hasRole('Manager')")
    @PostMapping("/manager/archive/v1/{uploadId}")
    public ResponseEntity<?> archiveApprovedDocument(@PathVariable Integer uploadId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        documentService.archiveApprovedDocument(auth.getName(), uploadId);
        return ResponseEntity.ok("Document archived successfully");
    }

    @PreAuthorize("hasRole('Manager')")
    @PostMapping(value = "/manager/employees/finance/upload/v1", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFinanceDocument(@RequestParam Integer employeeId,
                                                   @RequestParam Integer year,
                                                   @RequestParam String docType,
                                                   @RequestParam("file") MultipartFile file) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        documentService.uploadFinanceDocument(auth.getName(), employeeId, year, docType, file);
        return ResponseEntity.ok("Finance document uploaded successfully");
    }

    @PreAuthorize("hasRole('Manager')")
    @GetMapping("/manager/employees/finance/list/v1/{employeeId}")
    public ResponseEntity<?> getFinanceDocuments(@PathVariable Integer employeeId, @RequestParam Integer year) {
        return ResponseEntity.ok(documentService.getFinanceDocuments(employeeId, year));
    }
}
