package com.flacofitness.app.service;

import com.flacofitness.app.exception.BusinessValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class UserPhotoStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    private final Path usersUploadPath;
    private final DataSize maxImageSize;
    private final String uploadDir;

    public UserPhotoStorageService(@Value("${upload.dir}") String uploadDir,
                                   @Value("${upload.max-image-size:5MB}") DataSize maxImageSize) {
        this.uploadDir = uploadDir;
        this.maxImageSize = maxImageSize;
        this.usersUploadPath = Paths.get(uploadDir, "users").toAbsolutePath().normalize();
    }

    public String guardarFotoUsuario(Long usuarioId, MultipartFile foto, String fotoActualPath) {
        validarFoto(foto);

        try {
            Files.createDirectories(usersUploadPath);

            String extension = resolverExtension(foto);
            String fileName = "user-" + usuarioId + "-" + UUID.randomUUID() + extension;
            Path destino = usersUploadPath.resolve(fileName).normalize();

            if (!destino.startsWith(usersUploadPath)) {
                throw new BusinessValidationException("La ruta de destino para la foto no es valida");
            }

            try (InputStream inputStream = foto.getInputStream()) {
                Files.copy(inputStream, destino, StandardCopyOption.REPLACE_EXISTING);
            }

            eliminarFotoAnterior(fotoActualPath, destino);
            return Paths.get(uploadDir, "users", fileName).toString().replace("\\", "/");
        } catch (IOException ex) {
            throw new BusinessValidationException("No se pudo almacenar la imagen del usuario");
        }
    }

    public void validarFotoUsuario(MultipartFile foto) {
        if (foto == null || foto.isEmpty()) {
            return;
        }
        validarFoto(foto);
    }

    private void validarFoto(MultipartFile foto) {
        if (foto == null || foto.isEmpty()) {
            throw new BusinessValidationException("Debes seleccionar una imagen para subir");
        }

        if (foto.getSize() > maxImageSize.toBytes()) {
            throw new BusinessValidationException("La imagen supera el tamano maximo permitido de " + maxImageSize.toMegabytes() + " MB");
        }

        String contentType = foto.getContentType();
        if (!StringUtils.hasText(contentType) || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BusinessValidationException("Solo se permiten imagenes JPG, PNG, GIF o WEBP");
        }
    }

    private String resolverExtension(MultipartFile foto) {
        String originalFilename = StringUtils.cleanPath(foto.getOriginalFilename() == null ? "" : foto.getOriginalFilename());
        String extension = StringUtils.getFilenameExtension(originalFilename);

        if (StringUtils.hasText(extension)) {
            return "." + extension.toLowerCase(Locale.ROOT);
        }

        return switch (foto.getContentType()) {
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }

    private void eliminarFotoAnterior(String fotoActualPath, Path nuevaFoto) throws IOException {
        if (!StringUtils.hasText(fotoActualPath)) {
            return;
        }

        String normalizedPath = fotoActualPath.trim().replace("\\", "/");
        if (normalizedPath.startsWith("/")) {
            normalizedPath = normalizedPath.substring(1);
        }

        Path fotoAnterior = Paths.get(normalizedPath).toAbsolutePath().normalize();

        if (fotoAnterior.startsWith(usersUploadPath) && !fotoAnterior.equals(nuevaFoto)) {
            Files.deleteIfExists(fotoAnterior);
        }
    }
}
