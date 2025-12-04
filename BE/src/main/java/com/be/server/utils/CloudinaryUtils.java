
    package com.be.server.utils;

    import com.cloudinary.Cloudinary;
    import com.cloudinary.utils.ObjectUtils;
    import org.springframework.scheduling.annotation.Async;
    import org.springframework.stereotype.Component;

    import java.util.Map;

    @Component
    public class CloudinaryUtils {

        private Cloudinary cloudinary;


        public CloudinaryUtils(Cloudinary cloudinary) {
            this.cloudinary = cloudinary;
        }

        @Async
        public String uploadImage(byte[] file, String id) {
            try {
                Map uploadResult = cloudinary.uploader().upload(file,
                        ObjectUtils.asMap("public_id", id ));
                return (String) uploadResult.get("url");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }
    }
