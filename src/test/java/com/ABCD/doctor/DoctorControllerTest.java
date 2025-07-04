package com.ABCD.doctor;

import com.ABCD.doctor.model.Doctor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testng.annotations.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testRegisterDoctor() throws Exception {
        Doctor doctor = new Doctor();
        doctor.setRegNo("D100");
        doctor.setName("Hari Krishna");
        doctor.setSpecialization("Cardiology");

        mockMvc.perform(post("/api/registerDoctor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(doctor)))
                .andExpect(status().isOk());
    }

    @Test
    public void testSearchDoctor() throws Exception {
        mockMvc.perform(get("/api/searchDoctor/Hari Krishna"))
               .andExpect(status().isOk());
    }
}
