package greenNare.product.service;


import greenNare.cache.CacheService;
import greenNare.exception.BusinessLogicException;
import greenNare.exception.ExceptionCode;
import greenNare.product.dto.GetProductWithImageDto;
import greenNare.product.entity.Image;
import greenNare.product.entity.Product;
import greenNare.product.repository.ImageRepository;
import greenNare.product.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductService {
    private ProductRepository productRepository;
    private ImageRepository imageRepository;

    private ImageService imageService;
    private CacheService cacheService;


    public ProductService (ProductRepository productRepository,
                           ImageRepository imageRepository,
                           CacheService cacheService,
                           ImageService imageService) {
        this.productRepository = productRepository;
        this.imageRepository = imageRepository;
        this.imageService = imageService;
    }



    //DB에서 카테고리별 상품 조회
    @Cacheable(value = "products", key = "#category+'_'+#page+'_'+#size")
    public Page<Product> getProducts(int page, int size, String category) {
        PageRequest pageRequest = PageRequest.of(page, size);
        if(category.equals("all")) {
            Page<Product> products = productRepository.findAll(pageRequest);
            System.out.println("find products from DB (all category)");
            return products;
        }
        else {
            Page<Product> products = productRepository.findByCategory(pageRequest, category);
            System.out.println("find products from DB (" + category + ")");
            return products;
        }
    }



    public Page<Product> getProducts(Pageable pageRequest, List<Integer> productIds) {
        Page<Product> products = productRepository.findByProductIds(productIds, pageRequest);

        return  products;
    }



    //DB에서 상품, 상품별 이미지 조회하여 리스트로 반환(사용자 cart상품 리스트 매개변수로 받지 않음)
    public List<GetProductWithImageDto> getProductsWithImage(Page<Product> products, boolean existCart) {

        List<GetProductWithImageDto> getProductWithImageDtos = products.getContent().stream()
                .map(product -> {

                    GetProductWithImageDto resultDto = new GetProductWithImageDto(
                            product.getProductId(),
                            product.getProductName(),
                            product.getDetail(),
                            product.getPrice(),
                            product.getCategory(),
                            product.getPoint(),
                            product.getStoreLink(),
                            imageService.getImageLinks(product), //여기서 db조회 발생
                            existCart

                    );

                    return resultDto;
                })
                .collect(Collectors.toList());

        return getProductWithImageDtos;

    }



    //DB에서 상품, 상품별 이미지 조회하여 리스트로 반환(사용자 cart상품 리스트 매개변수로 받음)
    public List<GetProductWithImageDto> getProductsWithImage(Page<Product> products, List<Integer> cartProductId){

        List<GetProductWithImageDto> getProductWithImageDtos = products.getContent().stream()
                .map(product -> {

                    log.info("finding cartproducrt");

                    GetProductWithImageDto resultDto = new GetProductWithImageDto(
                            product.getProductId(),
                            product.getProductName(),
                            product.getDetail(),
                            product.getPrice(),
                            product.getCategory(),
                            product.getPoint(),
                            product.getStoreLink(),
                            imageService.getImageLinks(product),
                            existInMyCart(cartProductId, product.getProductId())

                    );

                    return resultDto;
                })
                .collect(Collectors.toList());

        return getProductWithImageDtos;
    }



    //DB에서 특정상품 상세정보 조회하여 반환
    @Cacheable(value = "productDetails", key = "#productId")
    public Product getProduct(int productId) {
        Product productDetails = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.PRODUCT_NOT_FOUND));
        return productDetails;
    }



    //DB에서 특정상품과 이미지 조회하여 반환
    public GetProductWithImageDto getProductWithImage(int productId) {
        Product productDetails = getProduct(productId); //productRepository.findById(productId);

        GetProductWithImageDto resultDto = new GetProductWithImageDto(
                productDetails.getProductId(),
                productDetails.getProductName(),
                productDetails.getDetail(),
                productDetails.getPrice(),
                productDetails.getCategory(),
                productDetails.getPoint(),
                productDetails.getStoreLink(),
                imageService.getImageLinks(productDetails),
                false

        );
        return resultDto;
    }


    //DB에서 특정상품과 이미지 조회하여 반환(사용자 cart에 담긴 상품인지 여부도 함께 반환)_현재미사용
//    public GetProductWithImageDto getProductWithImage(int productId, List<Integer> cartProductId) {
//        Product productDetails = getProduct(productId); //productRepository.findById(productId);
//        List<Image> images = imageRepository.findImagesUriByProductProductId(productDetails.getProductId());
//        List<String> imageLinks = images.stream()
//                .map(image -> image.getImageUri())
//                .collect(Collectors.toList());
////        String imageLink = imageRepository.findImageUriByProductProductId(productId).getImageUri();
//
//
//        GetProductWithImageDto resultDto = new GetProductWithImageDto(
//                productDetails.getProductId(),
//                productDetails.getProductName(),
//                productDetails.getDetail(),
//                productDetails.getPrice(),
//                productDetails.getCategory(),
//                productDetails.getPoint(),
//                productDetails.getStoreLink(),
//                imageLinks,
//                existInMyCart(cartProductId, productId)
//
//
//        );
//        return resultDto;
//    }



    //DB에서 상품 이름으로 검색하여 결과 반환
    public List<Product> findProducts(String productName) {

        List<Product> findProducts = productRepository.findByProductName(productName);
        log.info(productName + findProducts);

        //String일 경우 조회결과없음 -> int타입으로 price와비교해서 조회확인(ok)
        //List<Product> findProducts = productRepository.findByPrice(productName);

        return findProducts;
    }



    //상품이 사용자의 cart에 담긴 상품인지 여부 반환
    public boolean existInMyCart(List<Integer> cartProductsId, int productId) {
        log.info(cartProductsId.toString() + " " + productId);

        for(Integer cartProductId : cartProductsId) {
            log.info("cartProductId: " + cartProductId);
            if (cartProductId == productId){
                log.info(productId + "exist in the cart_return true");
                return true;
            }
        }

        return false;
    }


    //DB에서 입력받는 상품에 해당하는 이미지링크 조회하여 반환
//    @Cacheable(value = "productImageLinks", key = "imagelink")
//    public List<String> getImageLinks(Product product){
//        System.out.println("getImage");
//        List<Image> images = imageRepository.findImagesUriByProductProductId(product.getProductId());
//        List<String> imageLinks = images.stream()
//                .map(image -> image.getImageUri())
//                .collect(Collectors.toList());
//
//        return imageLinks;
//    }


    // MebmerService에서 사용자 cart상품 조회시 getCartProducts에서 사용했으나
    // getProductsWithImage(Page<Product> products, List<Integer> cartProductId)로 사용변경
//    public List<GetProductWithImageDto> getProducts(List<Integer> productIds, Pageable pageRequest) {
//        Page<Product> findProducts = productRepository.findByProductIds(productIds, pageRequest);
//        List<GetProductWithImageDto> products = findProducts.getContent().stream()
//                .map(product -> {
//                    GetProductWithImageDto resultDto = new GetProductWithImageDto(
//                            product.getProductId(),
//                            product.getProductName(),
//                            product.getDetail(),
//                            product.getPrice(),
//                            product.getCategory(),
//                            product.getPoint(),
//                            product.getStoreLink(),
////                            imageLinks,
//                            getImageLinks(product),
//                            false
//
//                    );
//
//                    return resultDto;
//
//                })
//                .collect(Collectors.toList());
//
//        return products;
//    }
}
