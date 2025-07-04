package tn.esprit.examen.services;

import org.springframework.web.multipart.MultipartFile;
import tn.esprit.examen.entities.Application;

import java.io.IOException;

public interface IApplicationService {
    public Application applyToJob(Long candidateId, Long jobOfferId, MultipartFile cvFile, String firstName, String lastName, String email, String phone, String coverLetter) throws IOException;

}
