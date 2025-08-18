package IntegracionBackFront.backfront.Services.Cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryService {
    //Constante que define el tamaño máximo permitido para los archivos (5mb)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".gif", ".jpeg", ".png"};
    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /***
     *Subir imagenes a la Raiz de Cloudinary
     * @param file
     * @return URL de la imagen
     * @throws IOException
     */
    public String uploadImage(MultipartFile file) throws IOException{
        //1. Validamos el archivo
        validateImage(file);

        //Subimos el archivo a Cloudinary con configuraciones básicas
        //Tipo de recurso auto-detectado
        Map<?, ?>uploadResult = cloudinary.uploader()
                .upload(file.getBytes(), ObjectUtils.asMap(
                        "resource_type", "auto",
                        "quality", "auto:good"
                ));

        //Retorna la URL de la imagen
        return (String) uploadResult.get("secure_url");
    }

    /***
     * Sube una imagen a una carpeta en especificp
     * @param file
     * @param folder carpeta destino
     * @return
     * @throws IOException Si ocurre un error durante la subida
     */
    public String uploadImage(MultipartFile file, String folder) throws IOException{
        validateImage(file);
        //Generar un nombre unico para el archivo
        //Conservar la extension original
        //Agregar un prefijo y un UUID para evitar colisiones

        String originalFileName = file.getOriginalFilename();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String uniqueFileName = "img_" + UUID.randomUUID() + fileExtension;

        //Configuracion para subir imagen
        Map<String, Object> options = ObjectUtils.asMap(
                "folder", folder, //Carpeta d edestino
                "public_id", uniqueFileName, //Nombre unico para el archivo
                "use_filename", false,      //No usar el nombre original
                "unique_filename", false,   //No generar nombre unico (proceso heco anteriormente)
                "overwrite", false,      //No sobreescriir archivos
                "resource_type", "auto",    //Auto-detectar tipo de recurso
                "quality", "auto:good"  //Optimizacion de calidad automatica
        );

        //Subir el archivo
        Map<?,?>uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
        //Retornamos la URL segura
        return (String) uploadResult.get("secure_url");
    }

    /***
     *
     * @param file
     */
    private void validateImage(MultipartFile file){
        //1. Verificar si el archivo esta vacío
        if(file.isEmpty()){
            throw new IllegalArgumentException("El archivo no puede estar vacío.");
        }
        //2. Verificar el tamaño de la imagen
        if (file.getSize() > MAX_FILE_SIZE){
            throw new IllegalArgumentException("El archivo no puede ser mayor a 5MB");
        }

        //3. Obtener y validar el nombre original del archivo
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null){
            throw new IllegalArgumentException("Nombre de archivo invalido");
        }

        //4. Extraer y validar la extension
        String extension = originalFileName.substring(originalFileName.lastIndexOf(".")).toLowerCase();
        if (!Arrays.asList(ALLOWED_EXTENSIONS).contains(extension)){
            throw new IllegalArgumentException("Solo se permiten archivos JPG, JPEG, PNG y GIF");
        }

        //Verificar que el tipo de MINE sea una imagen
        if (!file.getContentType().startsWith("image")){
            throw new IllegalArgumentException("El archivo debe ser una imagen valida");
        }
    }
}
