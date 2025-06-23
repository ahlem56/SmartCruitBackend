package tn.esprit.examen.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprit.examen.entities.Application;
import tn.esprit.examen.repositories.ApplicationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class ApplicationService {
    private final ApplicationRepository applicationRepository;

    public Application create(Application application) {
        application.setAppliedAt(LocalDateTime.now());
        return applicationRepository.save(application);
    }

    public List<Application> getAll() {
        return applicationRepository.findAll();
    }

    public Optional<Application> getById(Long id) {
        return applicationRepository.findById(id);
    }

    public void delete(Long id) {
        applicationRepository.deleteById(id);
    }
}
