package com.solver.db.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class PaidSolver {
	@Id
	@ManyToOne
	@JoinColumn(name="userId")
	private User user;
	
	private Date startTime;
	private Date endTime;
}
