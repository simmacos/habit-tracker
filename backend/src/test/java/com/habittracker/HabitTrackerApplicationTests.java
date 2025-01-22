package com.habittracker;

import com.habittracker.model.Habit;
import com.habittracker.service.HabitService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class HabitTrackerApplicationTests {

	@Autowired
	HabitService habitService;

	@Test
	void contextLoads() {
	}

	
	void testLoadHabits(){
		Long idUtente = 2L;
		List<Habit> habitsList = habitService.getUserHabits(idUtente, true);

		habitsList.forEach(x -> System.out.println("id> " + x.getId() + "name>> " + x.getName() + "Active?>" + x.getIsActive()));
	}
}
