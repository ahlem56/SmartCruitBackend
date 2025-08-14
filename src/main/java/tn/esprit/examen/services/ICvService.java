package tn.esprit.examen.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ICvService {
    String uploadCvToCloudinary(MultipartFile file) throws IOException ;
}
