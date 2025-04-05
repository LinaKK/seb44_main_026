package greenNare.product.repository;

import greenNare.product.dto.ProductImageDto;
import greenNare.product.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Integer> {
    List<Image> findImagesUriByProductProductId(int productId);
    //Image findImageUriByProductProductId(int productId);

    List<Image> findImagesUriByReviewReviewId(int reviewId);

    Optional<Image> findImageUriByImageUri(String ImageUri);

    List<Image> findByReviewReviewId(int reviewId);

    @Query("SELECT new greenNare.product.dto.ProductImageDto(img.imageUri, img.product.productId) FROM Image img WHERE img.product.productId IN :productIds")
    List<ProductImageDto> findByProductIds(@Param("productIds") List<Integer> productIds);

}
