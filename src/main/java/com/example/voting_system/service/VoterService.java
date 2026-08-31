package com.example.voting_system.service;

import com.example.voting_system.model.Voter;
import com.example.voting_system.repository.VoterRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

@Service
public class VoterService {

    private final VoterRepository voterRepository;

    public VoterService(VoterRepository voterRepository) {
        this.voterRepository = voterRepository;
    }

    @PostConstruct
    public void loadVoters() {
        if (voterRepository.count() == 0) {
            var resourceStream = getClass().getClassLoader().getResourceAsStream("voters.csv");
            if (resourceStream == null) {
                System.out.println("voters.csv not found in classpath. Skipping CSV seeding...");
                return;
            }
            try (CSVReader reader = new CSVReader(new InputStreamReader(resourceStream))) {
                List<String[]> rows = reader.readAll();
                // Skip header
                for (int i = 1; i < rows.size(); i++) {
                    String[] row = rows.get(i);
                    if (row.length >= 10) {
                        Voter voter = new Voter(
                                row[0].toUpperCase(), // VoterID
                                row[1], // FirstName
                                row[2], // LastName
                                row[3], // DOB
                                row[9] // Email
                        );
                        voterRepository.save(voter);
                    }
                }
                System.out.println("Loaded voters from CSV to MySQL Database.");
            } catch (IOException | CsvException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Voters already exist in the MySQL database. Skipping CSV seeding...");
        }
    }

    public Voter getVoter(String voterId) {
        return voterRepository.findByVoterId(voterId.toUpperCase()).orElse(null);
    }
}
