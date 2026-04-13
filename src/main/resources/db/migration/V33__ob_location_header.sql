CREATE TABLE ob_location_header (
  ob_id       BIGINT NOT NULL AUTO_INCREMENT,
  branch_id   VARCHAR(20) NOT NULL,
  location_id VARCHAR(20) NOT NULL,
  cutoff_date DATE NOT NULL,
  created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (ob_id),
  UNIQUE KEY uq_ob_branch_location (branch_id, location_id),
  CONSTRAINT fk_ob_branch   FOREIGN KEY (branch_id)   REFERENCES branch_master(branch_id),
  CONSTRAINT fk_ob_location FOREIGN KEY (location_id) REFERENCES location_master(location_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
