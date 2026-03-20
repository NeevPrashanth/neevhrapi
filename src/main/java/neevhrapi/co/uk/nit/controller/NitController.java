package neevhrapi.co.uk.nit.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import neevhrapi.co.uk.nit.constants.NitConstants;
import neevhrapi.co.uk.nit.domains.Project;
import neevhrapi.co.uk.nit.domains.jwtauth.AuthRequest;
import neevhrapi.co.uk.nit.domains.jwtauth.AuthResponse;
import neevhrapi.co.uk.nit.domains.jwtauth.RefreshTokenReq;
import neevhrapi.co.uk.nit.domains.projectmgt.*;
import neevhrapi.co.uk.nit.domains.timesheet.TimesheetData;
import neevhrapi.co.uk.nit.domains.timesheet.TimesheetStatusUpdateRequest;
import neevhrapi.co.uk.nit.domains.user.User;
import neevhrapi.co.uk.nit.service.CredentialStore;
import neevhrapi.co.uk.nit.service.ProjectService;
import neevhrapi.co.uk.nit.service.jwtauth.AuthService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static neevhrapi.co.uk.nit.constants.NitConstants.*;

@RestController
public class NitController {
    private static final Logger logger = LogManager.getLogger(NitController.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static List<Project> sentprojects = null;
    @Autowired
    CredentialStore credentialStore;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private AuthService authService;

    @GetMapping(value = TIMESHEET_BYWEEK_ENDPOINT)
    public List<WeekTimesheet> getCurrentWeekTimesheet(@RequestParam int weekFlag // 0 = current, 1 = previous, 2 =
                                                                                  // previous-previous
    ) {
        if (weekFlag < 0 || weekFlag > 2) {
            throw new IllegalArgumentException("Invalid weekFlag. Allowed values: 0, 1, 2");
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("getCurrentWeekTimesheet payload - weekFlag: {}, username: {}", weekFlag, authentication.getName());
        List<TimesheetEntryDTO> flatList = projectService.getTimesheetEntriesForUser(authentication.getName(),
                weekFlag);
        List<WeekTimesheet> response = projectService.mapToWeekTimesheet(flatList, weekFlag);
        logger.info("getCurrentWeekTimesheet response JSON: {}", toJson(response));
        return response;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            logger.warn("Unable to serialize response to JSON", e);
            return String.valueOf(value);
        }
    }

    @PostMapping(value = TIMESHEET_BYWEEK_ENDPOINT)
    public ResponseEntity<String> submitTimesheet(
            @RequestBody List<WeekTimesheet> weekTimesheetList) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            logger.info("submitTimesheet payload - username: {}, entries: {}", authentication.getName(),
                    weekTimesheetList == null ? 0 : weekTimesheetList.size());
            projectService.saveTimesheet(authentication.getName(), weekTimesheetList);
            String response = "Timesheet submitted successfully.";
            logger.info("submitTimesheet response: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            String response = "Error while submitting timesheet: " + e.getMessage();
            logger.error("submitTimesheet response: {}", response, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping(value = NitConstants.GET_TOKEN_ENDPOINT)
    public ResponseEntity<?> authenticate(@RequestBody AuthRequest request) {
        try {
            logger.info("authenticate payload - username: {}, applicationKey: {}", request.getUsername(),
                    request.getApplicationKey());
            AuthResponse authResponse = authService.authenticateAndGenerateToken(request);
            logger.info("authenticate response: token issued for username {}", request.getUsername());
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            String response = "Invalid credentials or application key.";
            logger.error("authenticate response: {}", response, e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping(value = NitConstants.GET_REFERSH_TOKEN_ENDPOINT)
    public ResponseEntity<?> refeshtoken(@RequestBody RefreshTokenReq request) {
        try {
            String refreshToken = request.getRefreshToken();
            logger.info("refeshtoken payload - refresh token length: {}",
                    refreshToken == null ? 0 : refreshToken.length());

            CredentialStore.CredentialRecord record = credentialStore
                    .getCredentialsByRefreshToken(request.getRefreshToken());

            if (record == null) {
                String response = "Invalid or expired refresh token.";
                logger.info("refeshtoken response: {}", response);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            AuthRequest authrequest = AuthRequest
                    .builder()
                    .password(record.getPassword())
                    .username(record.getUsername())
                    .applicationKey(record.getApplicationKey())
                    .build();
            AuthResponse response = authService.authenticateAndGenerateToken(authrequest);
            credentialStore.invalidateRefreshToken(request.getRefreshToken());

            logger.info("refeshtoken response: new token issued for username {}", record.getUsername());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            String response = "Invalid credentials or application key.";
            logger.error("refeshtoken response: {}", response, e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @GetMapping(value = GET_RESOURCES_ENDPOINT)
    public List<UserDTO> getUsersExcludingManager() {
        logger.info("getUsersExcludingManager payload: none");
        List<UserDTO> response = projectService.getUsersExcludingManager();
        logger.info("getUsersExcludingManager response: {} user(s)", response.size());
        return response;
    }

    @GetMapping(value = GET_PROJ_STATUS_ENDPOINT)
    public List<ProjectStatusDTO> getProjectStatuses() {
        logger.info("getProjectStatuses payload: none");
        List<ProjectStatusDTO> response = projectService.getAllProjectStatuses();
        logger.info("getProjectStatuses response: {} status record(s)", response.size());
        return response;
    }

    @GetMapping(value = GET_PROJ_TASKS_ENDPOINT)
    public List<TaskResponseDTO> getTasksByProject(@PathVariable int projectId) {
        logger.info("getTasksByProject payload - projectId: {}", projectId);
        List<TaskResponseDTO> response = projectService.getTasksByProjectId(projectId);
        logger.info("getTasksByProject response: {} task(s)", response.size());
        return response;
    }

    @PostMapping(value = GET_PROJ_TASKS_ENDPOINT)
    public ResponseEntity<?> createTask(@PathVariable int projectId, @RequestBody TaskRequest request) {
        logger.info("createTask payload - projectId: {}, request: {}", projectId, request);
        int taskId = projectService.createTask(projectId, request);
        String response = "Task created successfully with ID: " + taskId;
        logger.info("createTask response: {}", response);
        return ResponseEntity.ok(response);
    }

    @PutMapping(TASK_ENDPOINT + "/{taskId}")
    public ResponseEntity<?> updateTask(@PathVariable int taskId, @RequestBody TaskRequest request) {
        logger.info("updateTask payload - taskId: {}, request: {}", taskId, request);
        projectService.updateTask(taskId, request);
        String response = "Task updated successfully with ID: " + taskId;
        logger.info("updateTask response: {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = PROJECTS_ENDPOINT)
    public ResponseEntity<?> createProject(@RequestBody ProjectRequest request) {
        logger.info("createProject payload: {}", request);
        int projectId = projectService.createProject(request);
        String response = "Project created successfully with ID: " + projectId;
        logger.info("createProject response: {}", response);
        return ResponseEntity.ok(response);
    }

    @PutMapping(PROJECTS_ENDPOINT + "/{projectId}")
    public ResponseEntity<?> updateProject(@PathVariable int projectId, @RequestBody ProjectRequest request) {
        logger.info("updateProject payload - projectId: {}, request: {}", projectId, request);
        projectService.updateProject(projectId, request);
        String response = "Project updated successfully with ID: " + projectId;
        logger.info("updateProject response: {}", response);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = PROJECTS_ENDPOINT)
    public Map<String, List<neevhrapi.co.uk.nit.domains.projectmgt.Project>> getAllProjects() {
        logger.info("getAllProjects payload: none");
        Map<String, List<neevhrapi.co.uk.nit.domains.projectmgt.Project>> response = projectService
                .getProjectsGroupedByStatus();
        logger.info("getAllProjects response groups: {}", response.keySet());
        return response;
    }

    @GetMapping(value = EMPLOYEE_ENDPOINT)
    public List<User> getUsers() {
        logger.info("getUsers payload: none");
        List<User> response = projectService.getAllUsers();
        logger.info("getUsers response: {} user(s)", response.size());
        return response;
    }

    @GetMapping(GET_TIMESHEET_ENDPOINT + "/{userId}")
    public ResponseEntity<TimesheetData> getUserTimesheet(
            @PathVariable int userId,
            @RequestParam String endDate) {
        logger.info("getUserTimesheet payload - userId: {}, endDate: {}", userId, endDate);
        TimesheetData result = projectService.getTimesheetByUser(userId, endDate);
        if (result == null) {
            logger.info("getUserTimesheet response: 404 Not Found");
            return ResponseEntity.notFound().build();
        }
        logger.info("getUserTimesheet response: {}", result);
        return ResponseEntity.ok(result);
    }

    @PostMapping(GET_TIMESHEET_ENDPOINT)
    public ResponseEntity<String> approveTimesheets(@RequestBody TimesheetStatusUpdateRequest request) {
        logger.info("approveTimesheets payload: {}", request);
        int updated = projectService.updateStatusToApproved(request.getId());
        String response = updated + " timesheet(s) approved.";
        logger.info("approveTimesheets response: {}", response);
        return ResponseEntity.ok(response);
    }
}
