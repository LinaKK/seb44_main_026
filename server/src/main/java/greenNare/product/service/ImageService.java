package greenNare.product.service;

import greenNare.product.entity.Image;
import greenNare.product.entity.Product;
import greenNare.product.repository.ImageRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ImageService {

    private final ImageRepository imageRepository;

    public ImageService (ImageRepository imageRepository){
        this.imageRepository = imageRepository;
    }


    @Cacheable(value = "productImageLinks", key = "#product.productId")
    public List<String> getImageLinks(Product product){
        System.out.println("getImage");
        List<Image> images = imageRepository.findImagesUriByProductProductId(product.getProductId());
        List<String> imageLinks = images.stream()
                .map(image -> image.getImageUri())
                .collect(Collectors.toList());

        return imageLinks;
    }

}
