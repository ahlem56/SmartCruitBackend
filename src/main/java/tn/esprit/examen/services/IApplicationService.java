package tn.esprit.examen.services;

import org.springframework.web.multipart.MultipartFile;
import tn.esprit.examen.entities.Application;

import java.io.IOException;
import java.util.Map;

public interface IApplicationService {
    public Map<String, Object> applyToJob(Long candidateId, Long jobOfferId, MultipartFile cvFile, String firstName, String lastName, String email, String phone, String coverLetter) throws IOException;

}
