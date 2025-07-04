package com.ABCD.doctor.controller;

import com.ABCD.doctor.model.Doctor;
import com.ABCD.doctor.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DoctorController {

    @Autowired
    private DoctorRepository repository;

    @PostMapping("/registerDoctor")
    public ResponseEntity<String> register(@RequestBody Doctor doctor) {
        repository.save(doctor);
        return ResponseEntity.ok("Doctor registered successfully");
    }

    @PutMapping("/updateDoctor/{regNo}")
    public ResponseEntity<String> update(@PathVariable String regNo, @RequestBody Doctor doctor) {
        if (!repository.existsById(regNo)) return ResponseEntity.notFound().build();
        doctor.setRegNo(regNo);
        repository.save(doctor);
        return ResponseEntity.ok("Doctor updated");
    }

    @GetMapping("/searchDoctor/{name}")
    public List<Doctor> search(@PathVariable String name) {
        return repository.findByName(name);
    }

    @DeleteMapping("/deletePolicy/{regNo}")
    public ResponseEntity<String> delete(@PathVariable String regNo) {
        if (!repository.existsById(regNo)) return ResponseEntity.notFound().build();
        repository.deleteById(regNo);
        return ResponseEntity.ok("Doctor deleted");
    }
}
