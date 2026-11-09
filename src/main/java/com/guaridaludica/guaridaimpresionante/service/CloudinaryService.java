package com.guaridaludica.guaridaimpresionante.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryService.class);

    public record ImagenSubida(String url, String publicId) {}

    private final Cloudinary cloudinary;
    private final String carpeta;

    public CloudinaryService(Cloudinary cloudinary,
                             @Value("${app.cloudinary.folder}") String carpeta) {
        this.cloudinary = cloudinary;
        this.carpeta = carpeta;
    }

    public ImagenSubida subir(MultipartFile archivo) throws IOException {
        Map<?, ?> r = cloudinary.uploader().upload(archivo.getBytes(), ObjectUtils.asMap(
                "asset_folder", carpeta,
                "use_asset_folder_as_public_id_prefix", true,
                "resource_type", "image"));
        return new ImagenSubida((String) r.get("secure_url"), (String) r.get("public_id"));
    }

    public void borrar(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            log.warn("No se pudo borrar la imagen {} de Cloudinary", publicId, e);
        }
    }
}