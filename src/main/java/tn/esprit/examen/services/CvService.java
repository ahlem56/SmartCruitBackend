package tn.esprit.examen.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class CvService implements ICvService {
    private final Cloudinary cloudinary;

    public String uploadCvToCloudinary(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "auto",
                        "folder", "cv_storage"  // nom du dossier dans Cloudinary
                ));
        return (String) uploadResult.get("secure_url");  // Lien du fichier
    }
}
