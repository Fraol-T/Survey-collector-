package com.example.surveycollector;

import android.content.Context;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.surveycollector.model.AppDatabase;
import com.example.surveycollector.model.dao.AdminDao;
import com.example.surveycollector.model.dao.SurveyDao;
import com.example.surveycollector.model.entity.Admin;
import com.example.surveycollector.model.entity.Survey;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class DatabaseTest {
    private AppDatabase db;
    private AdminDao adminDao;
    private SurveyDao surveyDao;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        adminDao = db.adminDao();
        surveyDao = db.surveyDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void writeAdminAndSurveyAndReadInList() throws Exception {
        Admin admin = new Admin("admin@example.com", "admin123", "2026-06-09T09:00:00Z");
        adminDao.insert(admin);
        
        Admin fetchedAdmin = adminDao.getByEmail("admin@example.com");
        assertNotNull(fetchedAdmin);
        assertEquals("admin@example.com", fetchedAdmin.getEmail());
        
        // Since id is auto-generated and database is empty, the first admin's ID will be 1 (or we fetch it from DB)
        Survey survey = new Survey(fetchedAdmin.getId(), "Course Feedback", "Rate the course", 0, 1, "2026-06-09T09:05:00Z");
        surveyDao.insert(survey);
        
        Survey fetchedSurvey = surveyDao.getSurveyById(1);
        assertNotNull(fetchedSurvey);
        assertEquals("Course Feedback", fetchedSurvey.getTitle());
        assertEquals(fetchedAdmin.getId(), fetchedSurvey.getAdminId());
    }
}
