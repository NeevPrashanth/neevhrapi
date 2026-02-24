package neevhrapi.co.uk.nit.controller;

import neevhrapi.co.uk.nit.domains.tasktracker.Task;
import neevhrapi.co.uk.nit.domains.tasktracker.TaskResponse;
import neevhrapi.co.uk.nit.domains.tasktracker.User;
import neevhrapi.co.uk.nit.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import static neevhrapi.co.uk.nit.constants.NitConstants.ADDUSER_ENDPOINT;
import static neevhrapi.co.uk.nit.constants.NitConstants.TASKTRACKER_ENDPOINT;

@RestController
public class TaskController {
    @Autowired
    private TaskService taskService;
    @PostMapping(value =TASKTRACKER_ENDPOINT)
    public ResponseEntity<String> createTask(@RequestBody Task task) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("username :"+authentication.getName());
        task.setUserid(taskService.getUserIdByUsername(authentication.getName()));
        System.out.println(new Date() +" : Create tasktracker api"+task.toString());
        int result = taskService.save(task);
        String res= result > 0 ? "Task inserted successfully" : "Failed to insert task";
        return ResponseEntity.ok(res);
    }

    @GetMapping(value =TASKTRACKER_ENDPOINT)
    public ResponseEntity<List<TaskResponse>> getTasksByUserAndDate(
                     @RequestParam("date") String dateStr) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("username :"+authentication.getName());
     int userId=taskService.getUserIdByUsername(authentication.getName());

        LocalDate date = LocalDate.parse(dateStr);
        List<TaskResponse> tasks = taskService.findByUserIdAndDate(userId, date);
        return ResponseEntity.ok(tasks);
    }
    @PostMapping(value =ADDUSER_ENDPOINT)
    public ResponseEntity<String> addUser(@RequestBody User user) {
        String result = taskService.addUser(user);
        return ResponseEntity.ok(result);
    }

}
