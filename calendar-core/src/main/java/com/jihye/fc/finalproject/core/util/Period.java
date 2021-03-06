package com.jihye.fc.finalproject.core.util;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
public class Period {
	
	private final LocalDateTime startAt;
	private final LocalDateTime endAt;
	
	public Period(LocalDateTime startAt, LocalDateTime endAt) {
		this.startAt = startAt;
		
		if(endAt == null){
			this.endAt = startAt;
		}else {
			this.endAt = endAt;
		}
	}
	
	public static Period of(LocalDateTime startAt, LocalDateTime endAt){
		return new Period(startAt, endAt);
	}
	
	public static Period of(LocalDate startDate, LocalDate endDate){
		return new Period(startDate.atStartOfDay(), endDate == null ? startDate.atStartOfDay() : endDate.atStartOfDay());
	}
	
	public boolean isOverlapped(LocalDateTime startAt, LocalDateTime endAt){
		return this.startAt.isBefore(endAt) && startAt.isBefore(this.endAt);
	}
	
	public boolean isOverlapped(Period period){
		return isOverlapped(period.startAt, period.endAt);
	}
}
