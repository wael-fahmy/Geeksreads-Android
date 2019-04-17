package com.example.geeksreads;

import android.support.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class ProfileTest {

    @Rule
    public ActivityTestRule<Profile> menuActivityTestRule =
            new ActivityTestRule<>(Profile.class, true, true);

    @Test
    public void Test_1() {


        //Test Profile Picture:
        assertEquals("http://geeksreads.000webhostapp.com/LoggedInUser/PP.jpg", Profile.ForTestProfilePicture);

        //Test Followers Count:
        assertEquals("2", Profile.ForTestFollowersCount);

        //Test Following Count:
        assertEquals("2", Profile.ForTestFollowingCount);
    }
}