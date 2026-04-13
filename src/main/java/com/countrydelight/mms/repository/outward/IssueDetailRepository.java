package com.countrydelight.mms.repository.outward;

import com.countrydelight.mms.entity.outward.IssueDetail;
import com.countrydelight.mms.entity.outward.IssueDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueDetailRepository extends JpaRepository<IssueDetail, IssueDetailId> {
    List<IssueDetail> findByIssueId(Long issueId);
}
