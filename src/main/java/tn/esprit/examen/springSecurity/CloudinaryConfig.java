package tn.esprit.examen.springSecurity;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dybcu2sd6",
                "api_key", "892443697242913",
                "api_secret", "XdGveqAUJuxCEJbWjeU1lPJUsAc",
                "secure", true
        ));
    }
}