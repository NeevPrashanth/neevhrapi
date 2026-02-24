package neevhrapi.co.uk.nit.dao;

import neevhrapi.co.uk.nit.domains.Issue;
import neevhrapi.co.uk.nit.domains.IssueRequest;
import neevhrapi.co.uk.nit.domains.InterfaceRecord;

import java.util.Date;
import java.util.List;

public interface IssueDao {
    public int insertIssue(String issue, String action, Date date, Integer interfaceId, String username) ;

    List<Issue> fetchIssues(IssueRequest issueRequest);

    public List<InterfaceRecord> fetchRecords(String interfaceGroupName, String type);
}

