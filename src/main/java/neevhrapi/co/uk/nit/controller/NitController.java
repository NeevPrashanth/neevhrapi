package neevhrapi.co.uk.nit.controller;

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
import java.util.Objects;

import static neevhrapi.co.uk.nit.constants.NitConstants.*;

@RestController
public class NitController {
    private static final Logger logger = LogManager.getLogger(NitController.class);

    private static List<Project> sentprojects = null;
    @Autowired
    CredentialStore credentialStore;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private AuthService authService;

    @GetMapping(value =TIMESHEET_BYWEEK_ENDPOINT)
    public List<WeekTimesheet> getCurrentWeekTimesheet( @RequestParam int weekFlag // 0 = current, 1 = previous, 2 = previous-previous
    ) {
        if (weekFlag < 0 || weekFlag > 2) {
            throw new IllegalArgumentException("Invalid weekFlag. Allowed values: 0, 1, 2");
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("username: {}", authentication.getName());
        List<TimesheetEntryDTO> flatList = projectService.getTimesheetEntriesForUser(authentication.getName(), weekFlag);
        return projectService.mapToWeekTimesheet(flatList,weekFlag);
    }

    @PostMapping(value =TIMESHEET_BYWEEK_ENDPOINT)
    public ResponseEntity<String> submitTimesheet(
                @RequestBody List<WeekTimesheet> weekTimesheetList
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            logger.info("username: {}", authentication.getName());
            projectService.saveTimesheet(authentication.getName(), weekTimesheetList);
            return ResponseEntity.ok("Timesheet submitted successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error while submitting timesheet: " + e.getMessage());
        }
    }

    @PostMapping(value = NitConstants.GET_TOKEN_ENDPOINT)
    public ResponseEntity<?> authenticate(@RequestBody AuthRequest request) {
        try {
            logger.info("received token request: {}", request);

            return ResponseEntity.ok(authService.authenticateAndGenerateToken(request));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials or application key.");
        }
    }

    @PostMapping(value = NitConstants.GET_REFERSH_TOKEN_ENDPOINT)
    public ResponseEntity<?> refeshtoken(@RequestBody RefreshTokenReq request) {
        try {
            logger.info("Received REFRESH token request: {}", request);

            CredentialStore.CredentialRecord record = credentialStore.getCredentialsByRefreshToken(request.getRefreshToken());

            if (record == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired refresh token.");
            }

            logger.info("received REFRESHtoken request: {}", request);
            AuthRequest authrequest = AuthRequest
                    .builder()
                    .password(record.getPassword())
                    .username(record.getUsername())
                    .applicationKey(record.getApplicationKey())
                    .build();
            // Invalidate old refresh token for security
            AuthResponse resposne = authService.authenticateAndGenerateToken(authrequest);
            credentialStore.invalidateRefreshToken(request.getRefreshToken());

            return ResponseEntity.ok(resposne);


        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials or application key.");
        }
    }

    @GetMapping(value = GET_RESOURCES_ENDPOINT)
    public List<UserDTO> getUsersExcludingManager() {
        return projectService.getUsersExcludingManager();
    }

    @GetMapping(value = GET_PROJ_STATUS_ENDPOINT)
    public List<ProjectStatusDTO> getProjectStatuses() {
        return projectService.getAllProjectStatuses();
    }

    @GetMapping(value = GET_PROJ_TASKS_ENDPOINT)
    public List<TaskResponseDTO> getTasksByProject(@PathVariable int projectId) {
        return projectService.getTasksByProjectId(projectId);
    }

    @PostMapping(value = GET_PROJ_TASKS_ENDPOINT)
    public ResponseEntity<?> createTask(@PathVariable int projectId,@RequestBody TaskRequest request) {
        int taskId = projectService.createTask(projectId,request);
        return ResponseEntity.ok("Task created successfully with ID: " + taskId);
    }

    //updateTask endpoint
    @PutMapping(TASK_ENDPOINT + "/{taskId}")
    public ResponseEntity<?> updateTask(@PathVariable int taskId, @RequestBody TaskRequest request) {
        projectService.updateTask(taskId, request);
        return ResponseEntity.ok("Task updated successfully with ID: " + taskId);
    }


    //add project endpoint
    @PostMapping(value = PROJECTS_ENDPOINT)
    public ResponseEntity<?> createProject(@RequestBody ProjectRequest request) {
        int projectId = projectService.createProject(request);
        return ResponseEntity.ok("Project created successfully with ID: " + projectId);
    }
    @PutMapping(PROJECTS_ENDPOINT + "/{projectId}")
    public ResponseEntity<?> updateProject(@PathVariable int projectId, @RequestBody ProjectRequest request) {
        projectService.updateProject(projectId, request);
        return ResponseEntity.ok("Project updated successfully with ID: " + projectId);
    }
    @GetMapping(value = PROJECTS_ENDPOINT)
    public Map<String, List<neevhrapi.co.uk.nit.domains.projectmgt.Project>> getAllProjects() {
        return projectService.getProjectsGroupedByStatus();
    }
    @GetMapping(value = EMPLOYEE_ENDPOINT)
    public List<User> getUsers() {
        return projectService.getAllUsers();
    }

    @GetMapping(GET_TIMESHEET_ENDPOINT+"/{userId}")
    public ResponseEntity<TimesheetData> getUserTimesheet(
            @PathVariable int userId,
            @RequestParam String endDate
    ) {
        TimesheetData result = projectService.getTimesheetByUser(userId, endDate);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping(GET_TIMESHEET_ENDPOINT)
    public ResponseEntity<String> approveTimesheets(@RequestBody TimesheetStatusUpdateRequest request) {
        int updated = projectService.updateStatusToApproved(request.getId());
        return ResponseEntity.ok(updated + " timesheet(s) approved.");
    }




}

