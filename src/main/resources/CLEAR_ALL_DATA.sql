-- WARNING: Destructive script.
-- Clears all rows from core HR/project/timesheet/document tables while keeping table structures.
-- Run against the correct database (example: projectmgt).

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE timesheet;
TRUNCATE TABLE task_schedule;
TRUNCATE TABLE task_resource_history;
TRUNCATE TABLE task_resource;
TRUNCATE TABLE task_history;
TRUNCATE TABLE task;

TRUNCATE TABLE project_tasks;
TRUNCATE TABLE project_resource_history;
TRUNCATE TABLE project_resource;
TRUNCATE TABLE project_history;
TRUNCATE TABLE projects;

TRUNCATE TABLE employee_finance_document;
TRUNCATE TABLE employee_document_upload;
TRUNCATE TABLE document_request;

TRUNCATE TABLE employee_address_request;
TRUNCATE TABLE employee_address;
TRUNCATE TABLE employee_profile;

TRUNCATE TABLE user_roles;
TRUNCATE TABLE users;

SET FOREIGN_KEY_CHECKS = 1;
